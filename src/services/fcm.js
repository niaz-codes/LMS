const { getMessaging } = require('firebase-admin/messaging');
const { initFirebaseAdmin } = require('../config/firebaseAdmin');

/** FCM caps a single multicast at 500 targets. */
const FCM_MAX_BATCH_SIZE = 500;

/** How long FCM may keep trying to hand a message to a device that is unreachable.
 *  A push that is 10 minutes late is worse than no push at all for a real-time LMS alert
 *  (a missed-attendance warning, a published result), so this is deliberately short: it also
 *  bounds how long a message can sit in FCM's retry queue while the device is unreachable. */
const PUSH_TTL_MS = 10 * 60 * 1000;

/** FCM error codes that mean "this registration token is permanently unusable - stop sending
 *  to it and delete it". Everything else (notably `messaging/invalid-argument`) is a bug in
 *  OUR payload, not a statement about the device, and must never delete a user's token.
 *
 *  Getting this backwards is what killed push delivery here: `messaging/invalid-argument` used
 *  to be treated as a dead token, so one bad payload silently $pull'd every recipient's valid
 *  token out of User.fcmTokens, after which `sendPushBestEffort` saw an empty array and skipped
 *  sending altogether. Push was then permanently dead for that account with nothing in the logs
 *  to say why. */
const DEAD_TOKEN_CODES = new Set([
  'messaging/registration-token-not-registered',
  'messaging/invalid-registration',
  'messaging/invalid-argument',
]);

/** The subset of DEAD_TOKEN_CODES that unambiguously means "this token is gone". The rest of
 *  DEAD_TOKEN_CODES are reported (loudly) but not auto-pruned, because pruning on a payload
 *  error destroys real tokens. Kept separate so the two policies stay explicit. */
const PRUNABLE_TOKEN_CODES = new Set([
  'messaging/registration-token-not-registered',
  'messaging/invalid-registration',
]);

/** Single-line structured logger. Every stage is timestamped with its own delta from the start
 *  of the send so a slow leg (backend DB work vs the FCM HTTP round trip) is visible in the log
 *  rather than guessed at. Deliberately one line per event so `grep PUSH` gives a clean trace. */
function log(stage, fields = {}) {
  const parts = Object.entries(fields)
    .filter(([, v]) => v !== undefined)
    .map(([k, v]) => `${k}=${v}`);
  console.log(`[PUSH] ${stage}${parts.length ? ' ' + parts.join(' ') : ''}`);
}

function msSince(startHr) {
  return `${Number(process.hrtime.bigint() - startHr) / 1e6}ms`;
}

/** Sends one data-only high-priority push per batch of tokens.
 *
 *  Returns `{ successCount, failureCount, invalidTokens, payloadError }`:
 *   - `invalidTokens`  tokens that are genuinely dead and safe to $pull from the user.
 *   - `payloadError`   set when FCM rejected the message itself (`invalid-argument`) - i.e. a bug
 *                      here, not a device problem. Reported, never used to prune.
 *  Never throws, and never silently discards: a caller can always tell the difference between
 *  "nobody had a token" and "FCM rejected us" and "the device was unreachable". */
async function sendPushToTokens(tokens, { title, body, data }) {
  const startHr = process.hrtime.bigint();
  const uniqueTokens = [...new Set((tokens || []).filter(Boolean))];
  if (uniqueTokens.length === 0) {
    return { successCount: 0, failureCount: 0, invalidTokens: [], payloadError: null, skipped: 'no-tokens' };
  }

  const app = initFirebaseAdmin();
  if (!app) {
    log('SKIP', { reason: 'firebase-admin-not-configured', tokens: uniqueTokens.length });
    return { successCount: 0, failureCount: 0, invalidTokens: [], payloadError: null, skipped: 'admin-not-configured' };
  }

  // Data-only (no top-level `notification` block) is deliberate: with a `notification` block
  // present, the OS builds and shows the tray notification itself whenever the app isn't in
  // the foreground, WITHOUT ever invoking the app's FirebaseMessagingService.onMessageReceived
  // - which would mean no channel routing, no tap-to-open-the-right-screen, no grouping. A
  // data-only message always reaches onMessageReceived, in every app state (foreground,
  // background, killed), so the app can build the notification itself every time.
  // FCM data payloads must be flat string->string maps, hence title/body live in `data` too.
  const stringData = Object.fromEntries(
    Object.entries({
      ...data,
      title,
      body,
      // Epoch millis the moment the backend created the event. The app subtracts this from its
      // own clock when the push lands and logs the difference, which is the only end-to-end
      // number that distinguishes "FCM was slow" from "the backend sent late" from "the app
      // sat on it in Doze".
      sentAt: String(Date.now()),
    })
      .filter(([, value]) => value !== undefined)
      .map(([key, value]) => [key, value === null ? '' : String(value)])
  );

  // Android-specific config. NOTE: every field here lives UNDER `android` - the FCM v1 Message
  // schema has no top-level `collapseKey`/`priority`/`ttl`. Putting `collapseKey` at the top
  // level made FCM reject the entire message with
  //   "Invalid JSON payload received. Unknown name \"collapseKey\" at 'message'"
  // which, because a multicast shares one payload across every recipient, failed 100% of sends.
  const android = {
    // 'high' => FCM forwards immediately and is allowed to wake a sleeping device. 'normal'
    // (the default) is explicitly allowed to be batched/throttled, which is the other way to
    // get multi-minute latency.
    priority: 'high',
    ttl: PUSH_TTL_MS,
    // collapse only true repeats of the same event (same type, same entity). Keying on `type`
    // alone would let two different assignments' "created" pushes overwrite each other while
    // still queued, and events with no related entity get no key at all.
    // Android supports at most 4 active collapse keys at a time.
    // Must be at android.collapseKey - see the note above.
    collapseKey: undefined,
    // Must stay false: the app cannot read its session until the user has unlocked the device
    // once after boot (auth token lives in credential-encrypted storage), and a delivered
    // message would be dropped as "signed out". false tells FCM to hold it until first unlock,
    // which is the correct trade - correct notification, slightly later.
    directBootOk: false,
  };
  if (stringData.relatedId) {
    android.collapseKey = [stringData.relatedType, stringData.relatedId, stringData.type]
      .filter(Boolean)
      .join(':')
      .slice(0, 64);
  }

  // Chunk so a user signed in on many devices (or a future topic-style fanout) can never trip
  // FCM's 500-target cap, which would reject the whole send.
  const chunks = [];
  for (let i = 0; i < uniqueTokens.length; i += FCM_MAX_BATCH_SIZE) {
    chunks.push(uniqueTokens.slice(i, i + FCM_MAX_BATCH_SIZE));
  }

  log('SEND', {
    targets: uniqueTokens.length,
    chunks: chunks.length,
    type: stringData.type,
    category: stringData.category,
    priority: android.priority,
    ttlMs: android.ttl,
    collapseKey: android.collapseKey || '-',
  });

  const invalidTokens = [];
  let successCount = 0;
  let failureCount = 0;
  let payloadError = null;

  try {
    const messaging = getMessaging(app);
    for (const chunk of chunks) {
      const message = { tokens: chunk, data: stringData, android };
      const chunkStart = process.hrtime.bigint();
      // firebase-admin v14's flattened default export doesn't have admin.messaging() (that's
      // undefined in this version) - the modular getMessaging(app) from firebase-admin/messaging
      // is the current API.
      const response = await messaging.sendEachForMulticast(message);
      log('FCM-RESP', { targets: chunk.length, ok: response.successCount, failed: response.failureCount, took: msSince(chunkStart) });

      response.responses.forEach((result, index) => {
        if (result.success) return;
        failureCount += 1;
        const code = result.error && result.error.code;
        const messageText = (result.error && result.error.message) || 'unknown';
        if (PRUNABLE_TOKEN_CODES.has(code)) {
          invalidTokens.push(chunk[index]);
          log('TOKEN-DEAD', { code, pruning: true, token: `${String(chunk[index]).slice(0, 12)}...` });
        } else if (DEAD_TOKEN_CODES.has(code)) {
          // Reported but NOT pruned: `invalid-argument` means our payload is malformed, and
          // deleting the caller's token in response would turn one bug into permanently dead
          // push for every affected account.
          payloadError = payloadError || code;
          log('TOKEN-REPORTED-NOT-PRUNED', { code, msg: messageText });
        } else {
          log('TOKEN-TRANSIENT', { code, msg: messageText, token: `${String(chunk[index]).slice(0, 12)}...` });
        }
      });
      successCount += response.successCount;
    }
  } catch (err) {
    // Transport-level failure (network, bad credentials, auth error). Distinguish a Firebase
    // auth/config problem, which affects EVERY push, from a transient blip.
    const code = (err && err.code) || '';
    if (code === 'messaging/authentication-error' || code === 'messaging/invalid-argument') {
      payloadError = code;
    }
    log('FCM-ERROR', { code: code || '-', msg: err.message });
    return { successCount, failureCount, invalidTokens, payloadError, error: err.message };
  }

  log('DONE', { ok: successCount, failed: failureCount, pruned: invalidTokens.length, total: msSince(startHr), payloadError: payloadError || '-' });
  return { successCount, failureCount, invalidTokens, payloadError };
}

module.exports = { sendPushToTokens, PUSH_TTL_MS };
