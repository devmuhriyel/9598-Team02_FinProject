package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.AuthenticationException;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * SessionValidator - Periodically validates if the user's session is still active
 * Detects concurrent logins and notifies the user
 */
public class SessionValidator {

    private Timer timer;
    private final int userId;
    private final String userType; // "admin" or "customer"
    private final SessionValidationCallback callback;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicBoolean alertShown = new AtomicBoolean(false);

    // Check every 5 seconds
    private static final long CHECK_INTERVAL_MS = 5000;

    public interface SessionValidationCallback {
        /**
         * Check if the session is still valid on the server
         * @return true if valid, false if session was terminated
         */
        boolean validateSession();

        /**
         * Called when session is invalidated (concurrent login detected)
         */
        void onSessionInvalidated();
    }

    public SessionValidator(int userId, String userType, SessionValidationCallback callback) {
        this.userId = userId;
        this.userType = userType;
        this.callback = callback;
    }

    /**
     * Start monitoring the session
     */
    public void start() {
        if (isRunning.getAndSet(true)) {
            return; // Already running
        }

        timer = new Timer("SessionValidator-" + userType + "-" + userId, true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkSession();
            }
        }, CHECK_INTERVAL_MS, CHECK_INTERVAL_MS);

        System.out.println("✅ Session validator started for " + userType + " " + userId);
    }

    /**
     * Stop monitoring the session
     */
    public void stop() {
        if (!isRunning.getAndSet(false)) {
            return; // Already stopped
        }

        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        System.out.println("🛑 Session validator stopped for " + userType + " " + userId);
    }

    /**
     * Check if session is still valid
     */
    private void checkSession() {
        try {
            boolean isValid = callback.validateSession();

            if (!isValid) {
                // Session was invalidated (concurrent login detected)
                stop(); // Stop checking

                // Only show alert once
                if (alertShown.compareAndSet(false, true)) {
                    // Use Platform.runLater WITHOUT showAndWait to avoid blocking
                    Platform.runLater(() -> {
                        showConcurrentLoginAlert();
                        callback.onSessionInvalidated();
                    });
                }
            }

        } catch (Exception e) {
            // Network error or other issue - don't logout, just log it
            System.err.println("⚠️ Session validation error: " + e.getMessage());
        }
    }

    /**
     * Show alert to user about concurrent login
     * FIXED: Use show() instead of showAndWait() to prevent UI freeze
     */
    private void showConcurrentLoginAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Session Ended");
        alert.setHeaderText("Concurrent Login Detected");
        alert.setContentText(
                "Your account has been logged in from another location.\n\n" +
                        "You have been logged out for security reasons."
        );

        // ✅ FIX: Use show() instead of showAndWait()
        // This prevents the "showAndWait is not allowed during animation" error
        alert.show();

        // Optional: Auto-close after 5 seconds
        Timer autoCloseTimer = new Timer(true);
        autoCloseTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (alert.isShowing()) {
                        alert.close();
                    }
                });
            }
        }, 5000);
    }

    /**
     * Check if validator is running
     */
    public boolean isRunning() {
        return isRunning.get();
    }
}