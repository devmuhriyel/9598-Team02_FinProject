package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.CORBA_IDLs.MovieBookingCommon.AuthenticationException;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * SessionValidator - Periodically validates if the user's session is still active
 * Checks for expired sessions only (concurrent logins are prevented at login level)
 */
public class SessionValidator {

    private Timer timer;
    private final int userId;
    private final String userType; // "admin" or "customer"
    private final SessionValidationCallback callback;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final AtomicBoolean alertShown = new AtomicBoolean(false);

    // Check interval for session validation (every 30 seconds - less frequent since we only check for expired sessions)
    private static final long CHECK_INTERVAL_MS = 30000;

    public interface SessionValidationCallback {
        /**
         * Check if the session is still valid on the server
         * @return true if valid, false if session was terminated or expired
         */
        boolean validateSession();

        /**
         * Called when session is invalidated (expired session)
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

        System.out.println("Session validator started for " + userType + " " + userId);
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

        System.out.println("Session validator stopped for " + userType + " " + userId);
    }

    /**
     * Check if session is still valid
     * Only checks for expired sessions (concurrent logins are prevented at login level)
     */
    private void checkSession() {
        // Check if validator is still running before proceeding
        if (!isRunning.get()) {
            return; // Already stopped, exit immediately
        }

        try {
            boolean isValid = callback.validateSession();

            // If session is invalid (expired), notify and stop checking
            if (!isValid) {
                System.out.println("Session expired for " + userType + " " + userId);
                stop(); // Stop checking - prevents non-terminating loop

                // Only show alert once
                if (alertShown.compareAndSet(false, true)) {
                    // Use Platform.runLater to ensure UI thread safety
                    Platform.runLater(() -> {
                        showSessionExpiredAlert();
                        callback.onSessionInvalidated();
                    });
                }
            }

        } catch (Exception e) {
            // If validation fails (e.g., user logged out or session expired)
            if (e instanceof AuthenticationException) {
                System.out.println("Session validation failed for " + userType + " " + userId + " - session expired or invalid");
                stop();
                if (alertShown.compareAndSet(false, true)) {
                    Platform.runLater(() -> {
                        showSessionExpiredAlert();
                        callback.onSessionInvalidated();
                    });
                }
            } else {
                // Network error - don't logout on network glitches, just log it
                System.err.println("Session validation error (network?): " + e.getMessage());
            }
        }
    }

    /**
     * Show alert to user about expired session
     */
    private void showSessionExpiredAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Session Expired");
        alert.setHeaderText("Your session has expired");
        alert.setContentText(
                "Your session has expired due to inactivity.\n\n" +
                        "You will be logged out."
        );

        alert.show();

        // Auto-close after 5 seconds
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