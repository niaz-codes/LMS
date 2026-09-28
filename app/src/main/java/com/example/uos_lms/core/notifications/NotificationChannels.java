package com.example.uos_lms.core.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.uos_lms.R;
import com.example.uos_lms.core.domain.model.NotificationCategory;

import java.util.Locale;

/** One notification channel per NotificationCategory - lets Android 8+ users control sound/
 *  vibration/importance per category from system settings (per-notification Builder overrides
 *  are ignored on API 26+, a platform restriction, so real per-category control has to happen at
 *  the channel level). Channel ids ("channel_" + lowercase category) are the single source of
 *  truth for channel routing: every pushed message is data-only, so the app picks the channel
 *  itself in NotificationChannels.channelIdFor() rather than FCM picking one for us. */
public final class NotificationChannels {

    private static final String TAG = "NotificationChannels";

    private static final String CHANNEL_PREFIX = "channel_";

    /** A group-summary notification (the label Android draws on a collapsed stack of grouped
     *  notifications) needs a channel of its own on API 26+, and deliberately a low-importance
     *  one: it is a label for the group, not a new alert, so it must not buzz when the stack it
     *  describes changes. Category members stay on their own HIGH-importance channels. */
    public static final String SUMMARY_CHANNEL_ID = CHANNEL_PREFIX + "summary";

    private NotificationChannels() {
    }

    public static String channelIdFor(@Nullable NotificationCategory category) {
        return CHANNEL_PREFIX + (category != null ? category.name() : NotificationCategory.SYSTEM.name()).toLowerCase(Locale.US);
    }

    public static NotificationCategory parseCategory(@Nullable String raw) {
        if (raw == null) return NotificationCategory.SYSTEM;
        try {
            return NotificationCategory.valueOf(raw);
        } catch (IllegalArgumentException e) {
            return NotificationCategory.SYSTEM;
        }
    }

    public static void createAll(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null) {
            Log.w(TAG, "NotificationManager unavailable - channels not created, post() will drop");
            return;
        }
        // IMPORTANCE_HIGH for EVERY category. Two reasons this is uniform rather than
        // special-casing SYSTEM:
        //   - Channel importance is what makes an alert appear as a heads-up notification. A
        //     DEFAULT-importance channel shows the notification silently in the shade, which
        //     reads to the user as "it arrived late" even when it arrived instantly.
        //   - Android ignores an app's attempt to RAISE a channel's importance once it exists -
        //     only the user can do that in system settings. So a channel first created at
        //     DEFAULT is permanently quiet until the user intervenes. Getting it right here, on
        //     first creation, is the only chance.
        // Users who want a quiet category can still lower it in system settings; the in-app
        // Notification Settings screen links there.
        for (NotificationCategory category : NotificationCategory.values()) {
            NotificationChannel channel = new NotificationChannel(
                    channelIdFor(category), displayNameFor(context, category), NotificationManager.IMPORTANCE_HIGH);
            channel.enableVibration(true);
            channel.setShowBadge(true);
            manager.createNotificationChannel(channel);
        }
        // The group summary stays DEFAULT: it only labels a collapsed stack, so it must not
        // buzz or heads-up every time the stack it describes changes.
        NotificationChannel summary = new NotificationChannel(
                SUMMARY_CHANNEL_ID,
                context.getString(R.string.notification_summary_channel),
                NotificationManager.IMPORTANCE_DEFAULT);
        summary.setShowBadge(false);
        manager.createNotificationChannel(summary);
        Log.i(TAG, "Notification channels created: " + NotificationCategory.values().length + " categories (IMPORTANCE_HIGH) + summary");
    }

    public static String displayNameFor(Context context, NotificationCategory category) {
        switch (category) {
            case ACCOUNT:
                return context.getString(R.string.category_account);
            case ACADEMIC_CONTENT:
                return context.getString(R.string.category_academic_content);
            case ATTENDANCE:
                return context.getString(R.string.category_attendance);
            case EXAM_RESULT:
                return context.getString(R.string.category_exam_result);
            case LEAVE:
                return context.getString(R.string.category_leave);
            case ANNOUNCEMENT:
                return context.getString(R.string.category_announcement);
            case CALENDAR:
                return context.getString(R.string.category_calendar);
            case MESSAGE:
                return context.getString(R.string.category_message);
            case PROMOTION:
                return context.getString(R.string.category_promotion);
            case SYSTEM:
            default:
                return context.getString(R.string.category_system);
        }
    }
}
