package com.example.uos_lms.feature.notifications.fcm;

import android.os.Looper;
import android.util.Log;

import com.example.uos_lms.core.data.remote.api.ApiNotificationDataSource;
import com.example.uos_lms.core.session.SessionManager;
import com.example.uos_lms.feature.notifications.AppNotificationCenter;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/** Delivers push notifications in every app state. The backend (services/fcm.js) sends a
 *  data-only, high-priority payload (deliberately no top-level `notification` block) so
 *  onMessageReceived always fires - foreground, backgrounded, and killed alike - letting the app
 *  fully control channel routing, grouping, and tap-to-open rather than the OS building a
 *  generic tray entry.
 *
 *  <p>This is the ONLY real-time delivery path. AppNotificationCenter's 30s refresh loop
 *  deliberately no longer creates tray entries; it only maintains the unread badge.
 *
 *  <p>Every line is tagged [FCM] so a single `adb logcat -s FcmService` answers "where is the
 *  time going?". The three numbers that matter, all logged on arrival:
 *  <ul>
 *    <li><b>transit</b> - now minus FCM's own send timestamp: how long FCM + the network took.
 *        High here means an FCM/Doze/network problem, not an app or backend one.</li>
 *    <li><b>endToEnd</b> - now minus the `sentAt` the backend stamped into the payload: the
 *        complete event-to-phone latency. Compare against the backend's `[PUSH] EVENT` log line
 *        for the same notificationId to see which leg owns the delay.</li>
 *    <li><b>tray</b> - a drop reason if the notification was not shown, so a missing
 *        notification is always attributable to a specific gate.</li>
 *  </ul> */
@AndroidEntryPoint
public class FcmService extends FirebaseMessagingService {

    private static final String TAG = "FcmService";

    /** Token registration has to survive the process being reclaimed straight after this
     *  callback returns (a killed app is the single most important case for push), so the HTTP
     *  call is made to COMPLETION rather than fired and forgotten. */
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    @Inject
    ApiNotificationDataSource notificationDataSource;
    @Inject
    SessionManager sessionManager;
    @Inject
    AppNotificationCenter notificationCenter;

    /** Fires whenever FCM (re)issues this device's registration token - on first install, after
     *  data is cleared, or periodically per Google's own rotation. If nobody's signed in yet,
     *  do nothing here; login itself registers the current token (see MainActivity). */
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        if (sessionManager.getAuthToken() == null) {
            Log.i(TAG, "[FCM] onNewToken: not signed in, skipping (login will register this token)");
            return;
        }
        final long startedAt = System.currentTimeMillis();
        if (Looper.myLooper() == Looper.getMainLooper()) {
            // Should not happen - Firebase dispatches these callbacks off the main thread - but
            // Tasks.await() would throw here, so never block the main thread.
            EXECUTOR.execute(() -> registerTokenAndReport(token, startedAt));
        } else {
            // Blocking this thread until the POST completes is the point: FCM owns this worker
            // for the duration of the callback, so waiting keeps the process alive long enough to
            // actually register. The previous fire-and-forget callback version could be killed
            // mid-request, leaving the device unregistered for the whole session - no token, no
            // push, and no error anywhere.
            registerTokenAndReport(token, startedAt);
        }
    }

    private void registerTokenAndReport(String token, long startedAt) {
        try {
            Tasks.await(notificationDataSource.registerFcmToken(token));
            Log.i(TAG, "[FCM] token registered with backend in "
                    + (System.currentTimeMillis() - startedAt) + "ms");
        } catch (Exception e) {
            Log.w(TAG, "[FCM] failed to register FCM token with backend after "
                    + (System.currentTimeMillis() - startedAt) + "ms", e);
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage message) {
        super.onMessageReceived(message);
        long now = System.currentTimeMillis();
        Map<String, String> data = message.getData();

        long transit = message.getSentTime() > 0 ? now - message.getSentTime() : -1;
        String sentAtRaw = data.get("sentAt");
        long endToEnd = -1;
        if (sentAtRaw != null) {
            try {
                endToEnd = now - Long.parseLong(sentAtRaw);
            } catch (NumberFormatException ignored) {
                // Older server build with no sentAt - transit is still reported.
            }
        }
        Log.i(TAG, "[FCM] RECEIVED type=" + data.get("type") + " category=" + data.get("category")
                + " messageId=" + message.getMessageId()
                + " fcmTransitMs=" + transit
                + " endToEndMs=" + endToEnd
                + " from=" + (data.get("isTest") != null ? "TEST" : "live")
                + (transit > 5000 ? "  <-- FCM/network took over 5s, this is NOT an app delay" : "")
                + (endToEnd > 5000 ? "  <-- over 5s from backend event to this device" : ""));

        String title = data.get("title");
        if (title == null || title.isEmpty()) {
            Log.w(TAG, "[FCM] DROPPED reason=no-title payload=" + data);
            return;
        }

        // A token stays valid in the backend until the next login (or this logout hook) removes
        // it, so a push can land after the user signed out. Rendering it then would put another
        // account's title and body on a signed-out device, so drop the message instead.
        if (sessionManager.getAuthToken() == null) {
            Log.w(TAG, "[FCM] DROPPED reason=signed-out title=" + title);
            return;
        }

        // Deliberately NOT gated on a settings refresh. This used to await
        // ensureSettingsLoaded() - an HTTP call to /api/notifications/settings - before posting,
        // which meant that when the app was killed FCM cold-started the process, fired
        // onMessageReceived, and the process was reclaimed before the round trip finished: the
        // callback never ran, so the notification was silently lost with nothing in logcat. A
        // killed app is exactly the case push exists for, and it is the only case with no live
        // process to finish the request.
        //
        // post() still consults the cached settings, and that cache is correct here: the backend
        // already filtered by this account's real preferences before sending
        // (backend/src/services/notificationService.js), and a cold process has
        // NotificationSettings.initial() - permissive on every category - so nothing gets
        // wrongly filtered. A backgrounded-but-alive process reuses the snapshot refreshed by
        // start()/refresh(). The only gap this leaves is a preference changed in the last few
        // seconds on another device, which the backend's own pre-send filter already caught.
        //
        // Channels are created in UosLmsApp.onCreate, which always runs before this callback, so
        // the channel this posts to is guaranteed to exist even on a cold start.
        notificationCenter.post(
                trayKeyFor(data),
                data.get("category"),
                title,
                data.get("body"),
                AppNotificationCenter.SOURCE_PUSH);

        Log.i(TAG, "[FCM] POSTED endToEndMs=" + endToEnd);
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
        // FCM dropped pending messages (usually the app was uninstalled/reinstalled). Nothing to
        // recover, but worth knowing if a send claims success and nothing arrives.
        Log.w(TAG, "[FCM] onDeletedMessages: FCM discarded pending messages for this device");
    }

    /** Falls back to the (type, relatedType, relatedId) triple only if the backend sent a message
     *  without a notificationId - i.e. a payload from an older server build. That triple is
     *  unique per event, so the fallback still can't collide the way a bare relatedId could. */
    private String trayKeyFor(Map<String, String> data) {
        String notificationId = data.get("notificationId");
        if (notificationId != null && !notificationId.isEmpty()) return notificationId;
        Log.w(TAG, "[FCM] payload had no notificationId - falling back to the event triple");
        return String.join(":", data.getOrDefault("relatedType", ""), data.getOrDefault("relatedId", ""), data.getOrDefault("type", ""));
    }
}
