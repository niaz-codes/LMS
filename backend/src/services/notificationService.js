const User = require('../models/User');
const Notification = require('../models/Notification');
const { sendPushToTokens } = require('./fcm');
const { UserRole, UserStatus, NOTIFICATION_TYPE_CATEGORY } = require('../constants/enums');

/** Creates one Notification per recipient (always - the in-app Notification Center is meant to
 * be a complete history regardless of push settings) and best-effort sends a push to whichever
 * of those recipients have push + this notification's category enabled. Never throws - a
 * notification is always secondary to the action that triggered it (e.g. an assignment must
 * still get created even if every downstream notification/push step fails). */
async function notifyUsers(recipientIds, { type, title, body, actorId, relatedType, relatedId }) {
  const startHr = process.hrtime.bigint();
  try {
    const uniqueRecipientIds = [...new Set((recipientIds || []).filter(Boolean))]
      .filter((id) => id !== actorId);
    if (uniqueRecipientIds.length === 0) return [];

    const category = NOTIFICATION_TYPE_CATEGORY[type];
    if (!category) {
      console.error(`notifyUsers: unknown NotificationType "${type}" - skipping`);
      return [];
    }

    const docs = uniqueRecipientIds.map((recipientId) => ({
      recipientId,
      actorId: actorId || null,
      type,
      category,
      title,
      body: body || '',
      relatedType: relatedType || null,
      relatedId: relatedId || null,
    }));
    const created = await Notification.insertMany(docs);
    // Stamped as close to the event as possible: this is the baseline the device measures
    // end-to-end push latency against (data.sentAt), so the gap between this log line and the
    // notification appearing on a phone is the whole story of any delivery delay.
    console.log(
      `[PUSH] EVENT type=${type} category=${category} recipients=${created.length} `
      + `relatedType=${relatedType || '-'} relatedId=${relatedId || '-'} `
      + `dbTook=${Number(process.hrtime.bigint() - startHr) / 1e6}ms`
    );

    // Fire-and-forget - push delivery must never block or fail the caller's own request.
    sendPushBestEffort(
      created.map((doc) => ({ recipientId: doc.recipientId, notificationId: doc._id.toString() })),
      category,
      { type, title, body, relatedType, relatedId }
    ).catch((err) => console.error('notifyUsers: push delivery failed:', err.message));

    return created;
  } catch (err) {
    console.error('notifyUsers failed:', err.message);
    return [];
  }
}

/** One FCM send per recipient rather than one multicast for the whole batch, because each
 * recipient's push has to carry the id of THEIR OWN Notification record (see `notificationId`
 * below). The per-recipient settings gate below already made this the natural granularity, so
 * this costs one extra HTTP request per recipient on an admin-wide fanout, not a new code path. */
async function sendPushBestEffort(recipients, category, payload) {
  const startHr = process.hrtime.bigint();
  const notificationIdByRecipient = new Map(
    recipients.map((r) => [String(r.recipientId), r.notificationId])
  );
  const users = await User.find({ _id: { $in: recipients.map((r) => r.recipientId) } })
    .select('fcmTokens notificationSettings')
    .lean();
  console.log(`[PUSH] RECIPIENTS-LOADED wanted=${recipients.length} found=${users.length} took=${Number(process.hrtime.bigint() - startHr) / 1e6}ms`);

  const noToken = [];
  const optedOut = [];
  const payloadErrors = [];
  let sent = 0;
  let pruned = 0;

  await Promise.all(users.map(async (recipient) => {
    const settings = recipient.notificationSettings || {};
    const pushEnabled = settings.pushEnabled !== false;
    const categoryEnabled = settings[category] !== false;
    const tokens = recipient.fcmTokens || [];

    // Log the reason a recipient got no push. "No push arrived" was previously indistinguishable
    // from "FCM rejected our payload", which is exactly the ambiguity that hid the real bug.
    if (!pushEnabled || !categoryEnabled) {
      optedOut.push(String(recipient._id));
      console.log(`[PUSH] SKIP recipient=${recipient._id} reason=${!pushEnabled ? 'push-disabled' : `category-${category}-disabled`}`);
      return;
    }
    if (tokens.length === 0) {
      noToken.push(String(recipient._id));
      console.warn(`[PUSH] SKIP recipient=${recipient._id} reason=no-registered-token (push CANNOT be delivered to this user)`);
      return;
    }

    const { successCount, failureCount, invalidTokens, payloadError } = await sendPushToTokens(tokens, {
      title: payload.title,
      body: payload.body,
      data: {
        type: payload.type,
        category,
        relatedType: payload.relatedType,
        relatedId: payload.relatedId,
        // The Notification record's own id. The app keys its tray entry on this, so a push and
        // the same event rediscovered by the Notification Center's own list refresh resolve to
        // ONE notification instead of two. Without it the app has to fall back to `relatedId`,
        // which both mismatches what the list uses and collides across entity types (an
        // Assignment and an Announcement sharing an id would overwrite each other in the tray).
        notificationId: notificationIdByRecipient.get(String(recipient._id)),
      },
    });
    sent += successCount;
    if (payloadError) payloadErrors.push(`${recipient._id}:${payloadError}`);

    // Only tokens fcm.js classified as genuinely dead get removed - a payload-level rejection
    // must never cost the user their device registration.
    if (invalidTokens.length > 0) {
      pruned += invalidTokens.length;
      await User.updateOne({ _id: recipient._id }, { $pull: { fcmTokens: { $in: invalidTokens } } });
      console.log(`[PUSH] PRUNED-TOKENS recipient=${recipient._id} removed=${invalidTokens.length}`);
    }
    if (failureCount > 0) {
      console.warn(`[PUSH] RECIPIENT-PARTIAL recipient=${recipient._id} ok=${successCount} failed=${failureCount}`);
    }
  }));

  console.log(
    `[PUSH] SUMMARY sent=${sent} optedOut=${optedOut.length} noToken=${noToken.length} `
    + `pruned=${pruned} payloadErrors=${payloadErrors.length} `
    + `took=${Number(process.hrtime.bigint() - startHr) / 1e6}ms`
  );
  // A payload error means every push in this batch was rejected by FCM - the one condition that
  // makes push silently not work, so it gets an unmistakable line in the log.
  if (payloadErrors.length > 0) {
    console.error(`[PUSH] *** FCM REJECTED THE PAYLOAD for ${payloadErrors.length} recipient(s): ${payloadErrors.join(', ')} - push is NOT being delivered ***`);
  }
  if (noToken.length > 0) {
    console.warn(`[PUSH] ${noToken.length} recipient(s) have no FCM token - sign in on the device to enable push`);
  }
}

// --- Recipient-resolution helpers, shared across controllers ---

async function resolveStudentsInCohort(departmentId, semesterId) {
  if (!departmentId || !semesterId) return [];
  const students = await User.find({
    role: UserRole.STUDENT,
    status: UserStatus.APPROVED,
    departmentId,
    currentSemesterId: semesterId,
  }).select('_id').lean();
  return students.map((s) => s._id);
}

async function resolveHodOfDepartment(departmentId) {
  if (!departmentId) return [];
  const hod = await User.findOne({ role: UserRole.HOD, status: UserStatus.APPROVED, departmentId }).select('_id').lean();
  return hod ? [hod._id] : [];
}

/** Teacher is multi-department (departmentIds) - mirrors services/authorization.js's
 * isTeacherInDepartment, which also checks both the legacy scalar and the array field. */
async function resolveTeachersInDepartment(departmentId) {
  if (!departmentId) return [];
  const teachers = await User.find({
    role: UserRole.TEACHER,
    status: UserStatus.APPROVED,
    $or: [{ departmentId }, { departmentIds: departmentId }],
  }).select('_id').lean();
  return teachers.map((t) => t._id);
}

async function resolveAllAdmins() {
  const admins = await User.find({ role: UserRole.ADMIN, status: UserStatus.APPROVED }).select('_id').lean();
  return admins.map((a) => a._id);
}

async function resolveAllApprovedUsers() {
  const users = await User.find({ status: UserStatus.APPROVED }).select('_id').lean();
  return users.map((u) => u._id);
}

/** Runs `task` (typically: resolve recipient ids, then call notifyUsers) without letting any
 * failure propagate to the caller. Controllers call this AFTER already sending their HTTP
 * response, so an uncaught rejection reaching Express's error-handling middleware at that point
 * would crash on "Cannot set headers after they are sent" instead of the notification simply
 * not going out. */
function notifyAfterResponse(task) {
  Promise.resolve()
    .then(task)
    .catch((err) => console.error('notifyAfterResponse failed:', err.message));
}

module.exports = {
  notifyUsers,
  notifyAfterResponse,
  resolveStudentsInCohort,
  resolveHodOfDepartment,
  resolveTeachersInDepartment,
  resolveAllAdmins,
  resolveAllApprovedUsers,
};
