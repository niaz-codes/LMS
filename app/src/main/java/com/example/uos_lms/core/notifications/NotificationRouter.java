package com.example.uos_lms.core.notifications;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;

import com.example.uos_lms.MainActivity;
import com.example.uos_lms.R;
import com.example.uos_lms.core.domain.model.NotificationCategory;

/** Shared "tap to open correct screen" mapping, used by the in-app Notification Center list and by
 * a tapped push notification. Best-effort:
 *  only categories with a stable, argument-less destination navigate anywhere - opening the exact
 *  related item (one specific assignment, one specific result) isn't wired up. */
public final class NotificationRouter {

    public static final String EXTRA_CATEGORY = "notification_category";

    private NotificationRouter() {
    }

    public static boolean navigate(NavController navController, @Nullable NotificationCategory category) {
        Integer destination = destinationFor(category);
        if (destination == null) return false;
        // launchSingleTop so tapping a notification for a screen you're already on re-focuses that
        // screen instead of stacking another copy of it on the back stack.
        NavOptions options = new NavOptions.Builder().setLaunchSingleTop(true).build();
        navController.navigate(destination, null, options);
        return true;
    }

    /** Launches/brings MainActivity to front carrying the category so it can route once it (or
     * its NavController) is ready - see MainActivity.handleNotificationIntent. */
    public static Intent buildLaunchIntent(Context context, @Nullable NotificationCategory category) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (category != null) intent.putExtra(EXTRA_CATEGORY, category.name());
        return intent;
    }

    @Nullable
    private static Integer destinationFor(@Nullable NotificationCategory category) {
        if (category == NotificationCategory.MESSAGE) return R.id.messagingInboxFragment;
        if (category == NotificationCategory.ANNOUNCEMENT) return R.id.announcementsFragment;
        if (category == NotificationCategory.CALENDAR) return R.id.academicCalendarFragment;
        return null;
    }
}
