package com.example.uos_lms;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.transition.Fade;

import com.example.uos_lms.core.data.remote.api.ApiNotificationDataSource;
import com.example.uos_lms.core.domain.model.NotificationCategory;
import com.example.uos_lms.core.notifications.NotificationChannels;
import com.example.uos_lms.core.notifications.NotificationRouter;
import com.example.uos_lms.core.session.CachedSession;
import com.example.uos_lms.core.session.SessionManager;
import com.example.uos_lms.core.session.ThemePreferenceManager;
import com.example.uos_lms.core.ui.InsetUtils;
import com.example.uos_lms.feature.notifications.AppNotificationCenter;
import com.google.firebase.messaging.FirebaseMessaging;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final long SCREEN_TRANSITION_MILLIS = 180L;

    @Inject
    SessionManager sessionManager;
    @Inject
    AppNotificationCenter notificationCenter;
    @Inject
    ApiNotificationDataSource notificationDataSource;

    private ActivityResultLauncher<String> notificationPermissionLauncher;
    @Nullable
    private String activeNotificationUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(ThemePreferenceManager.readStoredThemeMode(this).toNightMode());
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        registerScreenTransitions();

        notificationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            // Nothing else is needed.
            // AppNotificationCenter checks the permission before showing notifications.
            // If permission is denied, notifications will not be shown.
        });
        // LiveData automatically updates when the screen is active.
        sessionManager.getCachedSession().observe(this, this::onSessionChanged);

        handleNotificationIntent(getIntent());
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleNotificationIntent(intent);
    }

    /**
     * Opens the correct screen when a notification is tapped.
     * It only works when the user is already logged in.
     * If the user is not logged in, the app opens normally.
     */
    private void handleNotificationIntent(@Nullable Intent intent) {
        if (intent == null) return;
        String categoryName = intent.getStringExtra(NotificationRouter.EXTRA_CATEGORY);
        // Consume the extra. The intent stays on the activity, so a rotation (onCreate) or a
        // process-death restore would otherwise find the extra still sitting there and navigate
        // again - pushing a second copy of the destination onto the back stack every time the
        // screen was recreated.
        intent.removeExtra(NotificationRouter.EXTRA_CATEGORY);
        if (categoryName == null || sessionManager.getCachedSession().getValue() == null) return;
        NotificationCategory category = NotificationChannels.parseCategory(categoryName);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NotificationRouter.navigate(navController, category);
    }

    /**
     * Starts notifications when the user logs in.
     * Registers the device for Firebase push notifications.
     * Asks for notification permission on Android 13+.
     * Prevents starting the notification system again for the same user.
     */
    private void onSessionChanged(@Nullable CachedSession session) {
        if (session == null) {
            activeNotificationUid = null;
            notificationCenter.stop();
            return;
        }
        if (session.getUid().equals(activeNotificationUid)) return;
        activeNotificationUid = session.getUid();
        notificationCenter.start();
        registerFcmToken();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    /**
     * Does nothing if Firebase is not configured.
     * Shows a warning in the log instead of crashing the app.
     */
    private void registerFcmToken() {
        try {
            FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token ->
                    notificationDataSource.registerFcmToken(token)
                            .addOnFailureListener(e -> Log.w(TAG, "Failed to register FCM token with backend", e))
            ).addOnFailureListener(e -> Log.w(TAG, "Failed to obtain FCM token", e));
        } catch (IllegalStateException e) {
            Log.w(TAG, "Firebase not configured (no google-services.json yet) - push notifications disabled", e);
        }
    }

    /** Lightweight app-wide fade between screens, plus automatic status-bar clearance for every
     * screen's toolbar. Registered once, recursively, so both apply to every fragment the
     * NavHostFragment swaps in - no per-destination wiring, no changes to any of the existing
     * navigate() call sites or nav_graph.xml actions, and no need to touch every individual
     * Fragment that happens to have a view with id "toolbar". */
    private void registerScreenTransitions() {
        getSupportFragmentManager().registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
            @Override
            public void onFragmentPreAttached(FragmentManager fm, Fragment fragment, android.content.Context context) {
                Fade fade = new Fade();
                fade.setDuration(SCREEN_TRANSITION_MILLIS);
                fragment.setEnterTransition(fade);
                fragment.setExitTransition(fade);
            }

            @Override
            public void onFragmentViewCreated(@NonNull FragmentManager fm, @NonNull Fragment fragment, @NonNull View view, @Nullable Bundle savedInstanceState) {
                View toolbar = view.findViewById(R.id.toolbar);
                if (toolbar != null) {
                    InsetUtils.applyStatusBarTopPadding(toolbar);
                }
                View bottomNav = view.findViewById(R.id.bottomNav);
                if (bottomNav != null) {
                    InsetUtils.applyNavigationBarBottomMargin(bottomNav);
                }
            }
        }, true);
    }
}
