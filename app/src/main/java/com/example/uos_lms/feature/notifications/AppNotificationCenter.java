package com.example.uos_lms.feature.notifications;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.uos_lms.R;
import com.example.uos_lms.core.data.remote.api.ApiNotificationDataSource;
import com.example.uos_lms.core.domain.model.NotificationCategory;
import com.example.uos_lms.core.domain.model.NotificationPage;
import com.example.uos_lms.core.domain.model.NotificationSettings;
import com.example.uos_lms.core.notifications.NotificationChannels;
import com.example.uos_lms.core.notifications.NotificationIcons;
import com.example.uos_lms.core.notifications.NotificationRouter;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.messaging.FirebaseMessaging;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * Session-scoped notification service. Owns the unread-count badge refresh and the one
 * implementation of "turn a notification into a tray entry" ({@link #post}), which
 * <b>only FcmService ever calls</b> - see {@link #refreshLoop()}.
 *
 * <p>Real-time delivery is FCM's job alone. This class deliberately does NOT poll for new
 * notifications and post them locally: that used to be the fallback, and while FCM was broken
 * it was the only thing showing notifications at all - but it is structurally incapable of being
 * real-time. A {@link Handler} callback on the main looper is frozen outright while the device is
 * in Doze (screen off, idle, app backgrounded), so a notification could sit unseen for minutes
 * and then appear in a burst the moment the user happened to unlock the phone. It also cost a
 * request every 30s for the whole session, and it duplicated whatever FCM had already delivered.
 * It is now purely the badge refresh; the tray belongs to push.
 */
@Singleton
public class AppNotificationCenter {

    private static final String TAG = "AppNotificationCenter";

    /** How often the unread count is refreshed for the toolbar badge. Comfortably under a minute
     *  so the badge doesn't visibly lag an action the user just took. Only runs while a session
     *  is active. */
    private static final long POLL_INTERVAL_MS = 30_000L;
    private static final int PAGE_SIZE = 50;

    /** Marks a tray entry that arrived over FCM, as opposed to one this class posted itself.
     *  Logged by {@link #post} so the origin of every notification is visible in logcat. */
    public static final String SOURCE_PUSH = "push";

    /** How long an orphaned group summary may linger. Android does not retract a group summary
     *  when its last child is dismissed, so without this a collapsed stack's label can outlive
     *  the notifications it summarises. */
    private static final long GROUP_SUMMARY_TIMEOUT_MS = 24L * 60L * 60L * 1000L;

    /** How long a cached settings snapshot is trusted before being re-read from the backend.
     *  Without it, a preference changed on another device (or while this process was dead) would
     *  never take effect here - the snapshot is latched for the process's whole lifetime, so a
     *  user who disables a category on their phone keeps getting that category's notifications
     *  from a device they turned it off on. */
    private static final long SETTINGS_CACHE_TTL_MS = 60_000L;

    private final ApiNotificationDataSource notificationDataSource;
    private final Context appContext;

    private final MutableLiveData<Integer> unreadCount = new MutableLiveData<>(0);
    private final Handler pollHandler = new Handler(Looper.getMainLooper());
    private final Runnable pollRunnable = this::refreshLoop;
    private boolean running;
    private boolean settingsFetchAttempted;
    private long settingsFetchedAt;
    private boolean deviceTokenRegistered;
    private NotificationSettings cachedSettings = NotificationSettings.initial();

    @Inject
    public AppNotificationCenter(ApiNotificationDataSource notificationDataSource, @ApplicationContext Context appContext) {
        this.notificationDataSource = notificationDataSource;
        this.appContext = appContext;
    }

    public LiveData<Integer> getUnreadCount() {
        return unreadCount;
    }

    public void start() {
        stop();
        running = true;
        ensureSettingsLoaded();
        registerDeviceToken();
        refreshLoop();
    }

    public void stop() {
        running = false;
        pollHandler.removeCallbacks(pollRunnable);
        unreadCount.setValue(0);
        // Drop the preferences with the rest of the session state: the next start() may be a
        // different account, and reusing the previous one's snapshot would filter this user's
        // notifications by someone else's settings. start() reloads them immediately.
        settingsFetchAttempted = false;
        settingsFetchedAt = 0L;
        cachedSettings = NotificationSettings.initial();
        deviceTokenRegistered = false;
    }

    /** Registers this device's FCM token with the signed-in account. The backend uses $addToSet,
     *  so repeating this is harmless - and repeating it is the point. Login registers the token
     *  exactly once (see MainActivity), so a token request or POST that failed at that instant
     *  left the device silently unregistered for the rest of the session: no token, no push, no
     *  error anywhere. Retrying here on every session start, and then on each poll until one
     *  succeeds, closes that window. */
    private void registerDeviceToken() {
        if (deviceTokenRegistered) return;
        try {
            FirebaseMessaging.getInstance().getToken()
                    .addOnSuccessListener(token -> notificationDataSource.registerFcmToken(token)
                            .addOnSuccessListener(v -> deviceTokenRegistered = true)
                            .addOnFailureListener(e -> Log.w(TAG, "Failed to register FCM token with backend", e)))
                    .addOnFailureListener(e -> Log.w(TAG, "Failed to obtain FCM token", e));
        } catch (IllegalStateException e) {
            // Firebase isn't configured on this build (no google-services.json) - nothing to
            // register, and getToken() throws for the rest of the process, so don't retry it.
            deviceTokenRegistered = true;
            Log.w(TAG, "Firebase not configured - push notifications disabled", e);
        }
    }

    /** Refreshes the unread badge and makes sure this device has a usable FCM token. It does
     *  NOT create tray entries - see the class doc. Nothing here should ever delay a push:
     *  FCM delivers straight to FcmService, which never waits on this loop. */
    private void refreshLoop() {
        // Refresh the preference gate before anything reads it, so post() decides with current
        // settings rather than whatever was cached when the session started. Resolves from cache
        // on all but one request per TTL, so this costs a round trip at most once a minute.
        ensureSettingsLoaded().addOnCompleteListener(settingsTask ->
                // `read` must be false here: the page is used only for its server-computed
                // unreadCount, and read=false is the cheaper, narrower query. The response body
                // is never turned into notifications - FcmService does that, keyed on the same
                // Notification id, so nothing can be double-posted.
                notificationDataSource.list(null, Boolean.FALSE, null, 1, PAGE_SIZE)
                        .addOnSuccessListener((NotificationPage page) -> unreadCount.setValue(page.getUnreadCount()))
                        .addOnFailureListener(e -> Log.w(TAG, "Unread-count refresh failed (badge may lag)", e))
                        .addOnCompleteListener(task -> {
                            // Retry token registration on every tick until one succeeds, so a
                            // token request or POST that failed at login doesn't leave the device
                            // silently unregistered for the rest of the session.
                            registerDeviceToken();
                            if (running) pollHandler.postDelayed(pollRunnable, POLL_INTERVAL_MS);
                        }));
    }

    /** Loads this account's notification preferences and caches them for post() to consult.
     *  Throttled by SETTINGS_CACHE_TTL_MS; resolves with the permissive defaults on failure
     *  rather than failing the push (the backend has already applied the real preferences
     *  before sending). */
    public Task<NotificationSettings> ensureSettingsLoaded() {
        if (settingsFetchAttempted && !isSettingsCacheStale()) return Tasks.forResult(cachedSettings);
        // Stamped before the request, not after it succeeds, so a backend outage can't turn the
        // 30s refresh loop into a second request storm - the permissive defaults keep
        // notifications flowing until a real snapshot lands.
        settingsFetchAttempted = true;
        settingsFetchedAt = SystemClock.elapsedRealtime();
        return notificationDataSource.getSettings()
                .addOnSuccessListener(this::applySettings)
                .addOnFailureListener(e -> Log.w(TAG, "Could not load notification settings, using defaults", e));
    }

    /** Adopts a fresh settings snapshot. Called by {@link #ensureSettingsLoaded()} and, after a
     *  successful save, by NotificationSettingsViewModel - which is what makes disabling a
     *  category in the app take effect on the next pushed message immediately rather than only
     *  at the next process start. */
    public void applySettings(NotificationSettings settings) {
        if (settings == null) return;
        cachedSettings = settings;
    }

    private boolean isSettingsCacheStale() {
        return SystemClock.elapsedRealtime() - settingsFetchedAt >= SETTINGS_CACHE_TTL_MS;
    }

    /** The one place a notification becomes a tray entry. Called only by FcmService, so push is
     *  the single real-time path; keeping the construction here (rather than inline in the
     *  service) is what guarantees channel routing, account preferences, the OS permission check
     *  and the tray key can never drift apart.
     *
     *  @param notificationId the backend Notification record's id. This is the tray key, so a
     *                       repeat of the same event updates one notification rather than
     *                       stacking duplicates.
     *  @param categoryName   the record's category; unknown values fall back to SYSTEM.
     *  @param source         where the entry came from, logged for diagnosis.
     */
    public void post(String notificationId, String categoryName, String title, String body, String source) {
        if (title == null || title.isEmpty()) return;
        NotificationCategory category = NotificationChannels.parseCategory(categoryName);
        // Each gate below logs its reason: a dropped notification is otherwise completely silent,
        // which makes "push isn't working" indistinguishable from "the user turned this category
        // off" or "Android 13+ never granted POST_NOTIFICATIONS".
        if (!cachedSettings.isPushEnabled()) {
            Log.i(TAG, "Dropped " + category + " notification " + notificationId + " (source=" + source + "): push disabled");
            return;
        }
        if (!cachedSettings.isCategoryEnabled(category)) {
            Log.i(TAG, "Dropped " + category + " notification " + notificationId + " (source=" + source + "): category disabled");
            return;
        }
        if (!canPostNotifications()) {
            Log.w(TAG, "Dropped " + category + " notification " + notificationId + " (source=" + source + "): POST_NOTIFICATIONS not granted");
            return;
        }

        String channelId = NotificationChannels.channelIdFor(category);
        PendingIntent contentIntent = PendingIntent.getActivity(appContext, notificationId.hashCode(),
                NotificationRouter.buildLaunchIntent(appContext, category),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // setSound()/setVibrate() below only take effect on API < 26 - on Oreo+ every aspect of
        // sound/vibration is governed by the notification channel itself (a platform restriction,
        // not something an app can override per-notification), which is why the Notification
        // Settings screen also offers a shortcut into the system's per-app channel settings.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(appContext, channelId)
                .setSmallIcon(NotificationIcons.iconFor(category))
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(contentIntent)
                .setGroup(channelId)
                .setAutoCancel(true)
                // HIGH, matching the IMPORTANCE_HIGH the category channels are created with.
                // These two are independent knobs: on API 24-25 (this app's minSdk) the channel
                // doesn't exist and setPriority() is what governs delivery, so leaving it at
                // DEFAULT silently downgraded every notification on those devices.
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        if (!cachedSettings.isSoundEnabled()) builder.setSound(null);
        if (!cachedSettings.isVibrationEnabled()) builder.setVibrate(new long[]{0});

        NotificationManagerCompat.from(appContext).notify(notificationId.hashCode(), builder.build());
        postGroupSummary(channelId, category);
        Log.i(TAG, "Tray entry shown: id=" + notificationId + " source=" + source
                + " channel=" + channelId + " category=" + category);
    }

    /** Android needs an explicit group-summary notification to label a collapsed stack - both
     *  delivery paths set a group, but with nothing marked as the summary, Android shows an
     *  unnamed bundle the user has to expand to read. Re-posted alongside every member so the
     *  label tracks the group, and given a timeout so it can't outlive its contents. */
    private void postGroupSummary(String channelId, NotificationCategory category) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return; // groups are a N+ feature
        NotificationCompat.Builder summary = new NotificationCompat.Builder(appContext, NotificationChannels.SUMMARY_CHANNEL_ID)
                .setSmallIcon(NotificationIcons.iconFor(NotificationCategory.SYSTEM))
                .setContentTitle(appContext.getString(R.string.app_name))
                .setContentText(NotificationChannels.displayNameFor(appContext, category))
                .setGroup(channelId)
                .setGroupSummary(true)
                .setAutoCancel(true)
                .setTimeoutAfter(GROUP_SUMMARY_TIMEOUT_MS)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat.from(appContext).notify(channelId.hashCode(), summary.build());
    }

    private boolean canPostNotifications() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                || ActivityCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    /** Logout hook: drops this device's FCM token from the account BEFORE the session is torn
     *  down, then stops polling. Without it the token stays in the previous account's
     *  User.fcmTokens on a shared device, so the next person to sign in on this device keeps
     *  receiving the previous account's pushes. Best-effort throughout: an unreachable backend
     *  must not leave the user stuck in a session they asked to end. */
    public void unregisterDevice() {
        stop();
        try {
            FirebaseMessaging.getInstance().getToken()
                    .addOnSuccessListener(notificationDataSource::unregisterFcmToken)
                    .addOnFailureListener(e -> Log.w(TAG, "Failed to unregister FCM token on logout", e));
        } catch (IllegalStateException e) {
            // Firebase isn't configured on this build (no google-services.json) - nothing to
            // unregister, same as registerFcmToken() in MainActivity.
            Log.w(TAG, "Firebase not configured - no FCM token to unregister", e);
        }
    }
}
