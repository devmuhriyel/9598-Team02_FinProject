package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Client_Java;

import javafx.application.Platform;

/**
 * ✅ SessionValidator - Validates user session every 5 seconds
 *
 * DETECTS:
 * - Session expiration (30 min timeout)
 * - Concurrent login from another device
 *
 * When detected, automatically logs user out with notification
 */
public class SessionValidator {

    private final int userId;
    private final String userType; // "admin" or "customer"
    private final SessionValidationCallback callback;
    private Thread validationThread;
    private volatile boolean running = false;

    private static final int VALIDATION_INTERVAL_MS = 5000; // 5 seconds

    /**
     * Callback interface for session validation
     */
    public interface SessionValidationCallback {
        /**
         * Called every 5 seconds to validate the session
         * @return true if session is valid, false if invalidated
         */
        boolean validateSession();

        /**
         * Called when session is invalidated (expired or concurrent login)
         */
        void onSessionInvalidated();
    }

    public SessionValidator(int userId, String userType, SessionValidationCallback callback) {
        this.userId = userId;
        this.userType = userType;
        this.callback = callback;
    }

    /**
     * Start the session validation loop
     */
    public void start() {
        if (running) {
            System.out.println("⚠️ SessionValidator already running for " + userType + " " + userId);
            return;
        }

        running = true;
        validationThread = new Thread(() -> {
            System.out.println("🔄 SessionValidator started for " + userType + " " + userId);

            while (running) {
                try {
                    // Wait 5 seconds before next validation
                    Thread.sleep(VALIDATION_INTERVAL_MS);

                    if (!running) break; // Check again after sleep

                    // Validate session
                    boolean isValid = callback.validateSession();

                    if (!isValid) {
                        System.out.println("🚨 Session invalidated for " + userType + " " + userId +
                                " (expired or concurrent login)");
                        running = false;

                        // Notify on JavaFX thread
                        Platform.runLater(() -> {
                            callback.onSessionInvalidated();
                        });

                        break;
                    }

                } catch (InterruptedException e) {
                    System.out.println("⚠️ SessionValidator interrupted for " + userType + " " + userId);
                    running = false;
                    break;
                } catch (Exception e) {
                    System.err.println("❌ Error in SessionValidator for " + userType + " " + userId);
                    e.printStackTrace();
                    // Continue running despite error
                }
            }

            System.out.println("🛑 SessionValidator stopped for " + userType + " " + userId);
        });

        validationThread.setDaemon(true);
        validationThread.setName("SessionValidator-" + userType + "-" + userId);
        validationThread.start();
    }

    /**
     * Stop the session validation loop
     */
    public void stop() {
        if (!running) {
            return;
        }

        running = false;

        if (validationThread != null) {
            validationThread.interrupt();
            try {
                validationThread.join(1000); // Wait max 1 second
            } catch (InterruptedException e) {
                System.err.println("⚠️ Error stopping SessionValidator thread");
            }
        }

        System.out.println("✅ SessionValidator stopped for " + userType + " " + userId);
    }

    /**
     * Check if validator is currently running
     */
    public boolean isRunning() {
        return running;
    }
}