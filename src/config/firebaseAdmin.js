const admin = require('firebase-admin');

let app = null;
let warnedMissingConfig = false;

/** Lazily initializes firebase-admin from a service account JSON in FIREBASE_SERVICE_ACCOUNT_JSON
 * (the whole JSON key file's contents, as a single env var string). Returns null - never throws -
 * if that env var isn't set or fails to parse, so the rest of the app (and the in-app Notification
 * Center, which doesn't depend on push) keeps working before/without a Firebase project being
 * configured. This is firebase-admin only (server-side push sender) - no relation to the
 * client-side Firebase Auth/Firestore SDKs this project removed during the MongoDB migration. */
function initFirebaseAdmin() {
  if (app) return app;

  const raw = process.env.FIREBASE_SERVICE_ACCOUNT_JSON;
  if (!raw) {
    // Warn once, but keep re-checking on later calls: the variable can be set after boot, and
    // warning on every push would spam the log.
    if (!warnedMissingConfig) {
      warnedMissingConfig = true;
      console.warn('FIREBASE_SERVICE_ACCOUNT_JSON not set - push notifications are disabled (in-app notifications still work).');
    }
    return null;
  }

  try {
    const credentials = JSON.parse(raw);
    // firebase-admin v14 flattened the top-level export - admin.cert(), not the older
    // admin.credential.cert() (admin.credential is undefined in this version).
    app = admin.initializeApp({ credential: admin.cert(credentials) });
    console.log('firebase-admin initialized - push notifications enabled.');
    return app;
  } catch (err) {
    // Deliberately NOT latched on a failure the way a missing config is: a malformed env var or
    // a transient credential/network error should be retried on the next push rather than
    // disabling push for this process's entire lifetime (only a restart used to recover).
    console.error('Failed to initialize firebase-admin (push notifications disabled):', err.message);
    return null;
  }
}

module.exports = { admin, initFirebaseAdmin };
