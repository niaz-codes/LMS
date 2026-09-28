const Notification = require('../models/Notification');
const User = require('../models/User');
const { ApiError } = require('../middleware/errorHandler');
const { asyncHandler } = require('../utils/asyncHandler');
const { NotificationCategory } = require('../constants/enums');
const { sendPushToTokens } = require('../services/fcm');

const DEFAULT_LIMIT = 30;
const MAX_LIMIT = 100;

function escapeRegex(value) {
  return value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

/** Paginated, filterable, searchable history for the Notification Center. Every notification
 * ever created for this user, regardless of their push settings - settings only gate whether a
 * push was sent, never whether the in-app record exists. */
const list = asyncHandler(async (req, res) => {
  const recipientId = req.user._id.toString();
  const { category, type, read, q } = req.query;
  // Clamp both ends: Mongo reads a negative limit as abs(limit), so limit=-1 would silently
  // return a single record while the response echoed back the nonsensical "limit": -1.
  const limit = Math.max(Math.min(parseInt(req.query.limit, 10) || DEFAULT_LIMIT, MAX_LIMIT), 1);
  const page = Math.max(parseInt(req.query.page, 10) || 1, 1);

  const filter = { recipientId };
  if (category) filter.category = category;
  if (type) filter.type = type;
  if (read === 'true') filter.read = true;
  else if (read === 'false') filter.read = false;
  if (q) {
    const regex = new RegExp(escapeRegex(q), 'i');
    filter.$or = [{ title: regex }, { body: regex }];
  }

  const [notifications, total, unreadCount] = await Promise.all([
    Notification.find(filter).sort({ createdAt: -1 }).skip((page - 1) * limit).limit(limit),
    Notification.countDocuments(filter),
    Notification.countDocuments({ recipientId, read: false }),
  ]);

  res.json({ notifications, total, page, limit, unreadCount });
});

const unreadCount = asyncHandler(async (req, res) => {
  const count = await Notification.countDocuments({ recipientId: req.user._id.toString(), read: false });
  res.json({ unreadCount: count });
});

const markRead = asyncHandler(async (req, res) => {
  const notification = await Notification.findOneAndUpdate(
    { _id: req.params.id, recipientId: req.user._id.toString() },
    { read: true, readAt: new Date() },
    { new: true }
  );
  if (!notification) throw new ApiError(404, 'Notification not found');
  res.json({ notification });
});

const markAllRead = asyncHandler(async (req, res) => {
  await Notification.updateMany(
    { recipientId: req.user._id.toString(), read: false },
    { read: true, readAt: new Date() }
  );
  res.status(204).send();
});

const remove = asyncHandler(async (req, res) => {
  const deleted = await Notification.findOneAndDelete({ _id: req.params.id, recipientId: req.user._id.toString() });
  if (!deleted) throw new ApiError(404, 'Notification not found');
  res.status(204).send();
});

const getSettings = asyncHandler(async (req, res) => {
  res.json({ settings: req.user.notificationSettings });
});

/** Any subset of the boolean toggles may be sent - only recognized, boolean-valued keys are
 * applied, everything else in the body is silently ignored (matches this codebase's existing
 * "PATCH a few fields" convention, e.g. authController.updateMyPhoto). */
const updateSettings = asyncHandler(async (req, res) => {
  const allowedKeys = ['pushEnabled', 'soundEnabled', 'vibrationEnabled', ...Object.values(NotificationCategory)];
  const updates = {};
  for (const key of allowedKeys) {
    if (typeof req.body[key] === 'boolean') {
      updates[`notificationSettings.${key}`] = req.body[key];
    }
  }
  const user = await User.findByIdAndUpdate(req.user._id, { $set: updates }, { new: true });
  res.json({ settings: user.notificationSettings });
});

/** Bounded so an account that has signed in on many emulators/devices over time can't grow an
 *  unbounded token list (each extra token is another target on every future fanout). Real users
 *  have one or two devices; the cap is generous and the oldest entries fall off the end. */
const MAX_FCM_TOKENS_PER_USER = 10;

/** A real FCM registration token is a long, colon-delimited string. Rejecting obvious junk here
 *  keeps garbage out of User.fcmTokens - a stored junk token is a guaranteed failed send on
 *  every single notification, for that user, forever. */
function looksLikeFcmToken(value) {
  return typeof value === 'string' && value.length >= 20 && /^[A-Za-z0-9_:.\-]+$/.test(value);
}

const registerFcmToken = asyncHandler(async (req, res) => {
  const { token } = req.body;
  if (!looksLikeFcmToken(token)) {
    throw new ApiError(400, 'Malformed FCM registration token');
  }
  // $addToSet cannot be combined with $slice - only $push accepts that modifier, and Mongo
  // rejects the combination ("Found unexpected fields after $each in $addToSet"). An
  // aggregation pipeline does both in one atomic update instead: drop any earlier copy of this
  // token, append it as the newest entry, then keep only the last MAX_FCM_TOKENS_PER_USER.
  await User.updateOne(
    { _id: req.user._id },
    [
      {
        $set: {
          fcmTokens: {
            $slice: [
              {
                $concatArrays: [
                  {
                    $filter: {
                      input: { $ifNull: ['$fcmTokens', []] },
                      cond: { $ne: ['$$this', token] },
                    },
                  },
                  [token],
                ],
              },
              -MAX_FCM_TOKENS_PER_USER,
            ],
          },
        },
      },
    ]
  );
  console.log(`[PUSH] TOKEN-REGISTERED user=${req.user._id} token=${token.slice(0, 12)}...`);
  res.status(204).send();
});

/** Best-effort - called on logout so a signed-out device stops receiving this account's
 *  pushes; a token that's already gone (already pruned, or never registered) isn't an error.
 *  The token arrives as a query param rather than a DELETE body because some proxies/CDNs strip
 *  request bodies from DELETE, which would silently skip the $pull. */
const unregisterFcmToken = asyncHandler(async (req, res) => {
  await User.updateOne({ _id: req.user._id }, { $pull: { fcmTokens: req.query.token } });
  res.status(204).send();
});

/** Sends a real push to the CALLER's own devices and returns FCM's per-token verdict plus how
 *  long the backend->FCM leg took. Purely diagnostic: it creates no Notification record, so it
 *  does not pollute the Notification Center, and it is deliberately NOT gated on the caller's
 *  push/category preferences - the whole point is to test the transport.
 *
 *  The response separates the two questions that were previously indistinguishable:
 *   - "FCM accepted and handed it off" (successCount)  => the backend and FCM are fine, so any
 *     remaining delay is on the device/FCM-transit side.
 *   - "FCM rejected the payload" (payloadError)       => a bug in services/fcm.js.
 *  `sentAt` is echoed back so the client can print end-to-end latency against the moment the
 *  notification actually appeared. */
const sendTestPush = asyncHandler(async (req, res) => {
  const startedAt = Date.now();
  const user = await User.findById(req.user._id).select('fcmTokens').lean();
  const tokenCount = (user.fcmTokens || []).length;
  if (tokenCount === 0) {
    res.status(409).json({
      error: 'no-fcm-token',
      message: 'This account has no registered FCM token. Sign in on the device (which registers it) and retry.',
      sentAt: startedAt,
    });
    return;
  }

  const { successCount, failureCount, invalidTokens, payloadError, error } = await sendPushToTokens(user.fcmTokens, {
    title: 'Push test',
    body: `Sent ${new Date(startedAt).toISOString()} - if you are reading this, push works.`,
    data: {
      type: 'PUSH_TEST',
      category: NotificationCategory.SYSTEM,
      relatedType: 'push-test',
      relatedId: String(startedAt),
      // Distinguishes this from a real event so it is obvious in logcat, and so the app can
      // keep it out of the Notification Center.
      isTest: 'true',
      notificationId: `test-${startedAt}`,
    },
  });

  if (invalidTokens.length > 0) {
    await User.updateOne({ _id: req.user._id }, { $pull: { fcmTokens: { $in: invalidTokens } } });
  }

  res.json({
    sentAt: startedAt,
    tokens: tokenCount,
    successCount,
    failureCount,
    prunedTokens: invalidTokens.length,
    payloadError: payloadError || null,
    error: error || null,
    backendToFcmMs: Date.now() - startedAt,
  });
});

module.exports = {
  list,
  unreadCount,
  markRead,
  markAllRead,
  remove,
  getSettings,
  updateSettings,
  registerFcmToken,
  unregisterFcmToken,
  sendTestPush,
};
