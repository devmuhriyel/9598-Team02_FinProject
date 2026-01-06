package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Server_Java;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.UUID;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * ✅ FIXED SessionManager - Proper concurrent login prevention
 */
public class SessionManager {

    private static final ConcurrentMap<Integer, SessionInfo> adminSessions = new ConcurrentHashMap<>();
    private static final ConcurrentMap<Integer, SessionInfo> customerSessions = new ConcurrentHashMap<>();

    private static final ConcurrentMap<String, Integer> adminTokens = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Integer> customerTokens = new ConcurrentHashMap<>();

    private static final int SESSION_TIMEOUT_MINUTES = 30;
    private static final int CLEANUP_INTERVAL_MINUTES = 5;
    private static Timer cleanupTimer;

    public static class SessionInfo {
        public int userId;
        public String username;
        public String sessionToken;
        public LocalDateTime loginTime;
        public LocalDateTime lastActivityTime;
        public String ipAddress;
        public boolean forciblyTerminated;

        public SessionInfo(int userId, String username, String sessionToken) {
            this.userId = userId;
            this.username = username;
            this.sessionToken = sessionToken;
            this.loginTime = LocalDateTime.now();
            this.lastActivityTime = LocalDateTime.now();
            this.forciblyTerminated = false;
        }

        public boolean isExpired() {
            return ChronoUnit.MINUTES.between(lastActivityTime, LocalDateTime.now()) > SESSION_TIMEOUT_MINUTES;
        }

        public void updateActivity() {
            this.lastActivityTime = LocalDateTime.now();
        }

        public void markAsTerminated() {
            this.forciblyTerminated = true;
        }
    }

    // ===== INITIALIZATION =====
    public static void initialize() {
        if (cleanupTimer == null) {
            cleanupTimer = new Timer("SessionCleanupTimer", true);
            cleanupTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    cleanupExpiredSessions();
                }
            }, CLEANUP_INTERVAL_MINUTES * 60 * 1000, CLEANUP_INTERVAL_MINUTES * 60 * 1000);

            System.out.println("✅ SessionManager initialized - 30min timeout, concurrent login prevention enabled");
        }
    }

    public static void shutdown() {
        if (cleanupTimer != null) {
            cleanupTimer.cancel();
            cleanupTimer = null;
        }
        adminSessions.clear();
        customerSessions.clear();
        adminTokens.clear();
        customerTokens.clear();
        System.out.println("SessionManager shutdown complete");
    }

    // ===== ADMIN SESSION MANAGEMENT =====

    /**
     * 🔥 FIXED: Completely removes old session before creating new one
     */
    public static String createAdminSession(int adminId, String username) {
        synchronized (adminSessions) {
            // 🔥 FIX: FULLY REMOVE old session (don't just mark as terminated)
            SessionInfo existingSession = adminSessions.get(adminId);
            if (existingSession != null) {
                System.out.println("⚠️ Admin " + adminId + " logged in from another location - REMOVING old session");

                // Remove old token mapping
                adminTokens.remove(existingSession.sessionToken);

                // Mark as terminated (for any ongoing validation checks)
                existingSession.markAsTerminated();

                // 🔥 CRITICAL: Remove old session immediately
                adminSessions.remove(adminId);
            }

            // Generate new token and create new session
            String sessionToken = UUID.randomUUID().toString();
            SessionInfo sessionInfo = new SessionInfo(adminId, username, sessionToken);

            // Store new session
            adminSessions.put(adminId, sessionInfo);
            adminTokens.put(sessionToken, adminId);

            System.out.println("✅ Admin session created: " + adminId + " | Token: " + sessionToken.substring(0, 8) + "...");
            return sessionToken;
        }
    }

    /**
     * ✅ FIXED: Better validation with proper concurrent login detection
     */
    public static boolean validateAdminSession(int adminId, String sessionToken) {
        // Check if token exists and maps to correct user
        Integer tokenUserId = adminTokens.get(sessionToken);
        if (tokenUserId == null || tokenUserId != adminId) {
            System.out.println("❌ Invalid token for admin " + adminId);
            return false;
        }

        // Check if session exists
        SessionInfo session = adminSessions.get(adminId);
        if (session == null) {
            System.out.println("❌ No session found for admin " + adminId);
            adminTokens.remove(sessionToken);
            return false;
        }

        // 🔥 FIX: Check if THIS specific session was terminated
        if (session.forciblyTerminated) {
            System.out.println("🚨 Admin " + adminId + " session terminated - concurrent login detected");
            adminSessions.remove(adminId);
            adminTokens.remove(sessionToken);
            return false;
        }

        // Check if session expired
        if (session.isExpired()) {
            System.out.println("⏰ Session expired for admin " + adminId);
            removeAdminSession(adminId);
            return false;
        }

        // Verify token matches current session
        if (!session.sessionToken.equals(sessionToken)) {
            System.out.println("❌ Token mismatch for admin " + adminId + " - concurrent login");
            return false;
        }

        // Update activity time
        session.updateActivity();
        return true;
    }

    public static boolean isAdminLoggedIn(int adminId) {
        SessionInfo session = adminSessions.get(adminId);
        return session != null && !session.isExpired() && !session.forciblyTerminated;
    }

    public static boolean isAdminLoggedIn(int adminId, String sessionToken) {
        return validateAdminSession(adminId, sessionToken);
    }

    public static void removeAdminSession(int adminId) {
        SessionInfo session = adminSessions.remove(adminId);
        if (session != null) {
            adminTokens.remove(session.sessionToken);
            System.out.println("🔓 Admin " + adminId + " logged out");
        }
    }

    public static Object getAdminSessionsLock() {
        return adminSessions;
    }

    // ===== CUSTOMER SESSION MANAGEMENT =====

    /**
     * 🔥 FIXED: Completely removes old session before creating new one
     */
    public static String createCustomerSession(int customerId, String username) {
        synchronized (customerSessions) {
            // 🔥 FIX: FULLY REMOVE old session
            SessionInfo existingSession = customerSessions.get(customerId);
            if (existingSession != null) {
                System.out.println("⚠️ Customer " + customerId + " logged in from another location - REMOVING old session");

                // Remove old token mapping
                customerTokens.remove(existingSession.sessionToken);

                // Mark as terminated
                existingSession.markAsTerminated();

                // 🔥 CRITICAL: Remove old session immediately
                customerSessions.remove(customerId);
            }

            String sessionToken = UUID.randomUUID().toString();
            SessionInfo sessionInfo = new SessionInfo(customerId, username, sessionToken);

            customerSessions.put(customerId, sessionInfo);
            customerTokens.put(sessionToken, customerId);

            System.out.println("✅ Customer session created: " + customerId + " | Token: " + sessionToken.substring(0, 8) + "...");
            return sessionToken;
        }
    }

    /**
     * ✅ FIXED: Better validation
     */
    public static boolean validateCustomerSession(int customerId, String sessionToken) {
        Integer tokenUserId = customerTokens.get(sessionToken);
        if (tokenUserId == null || tokenUserId != customerId) {
            System.out.println("❌ Invalid token for customer " + customerId);
            return false;
        }

        SessionInfo session = customerSessions.get(customerId);
        if (session == null) {
            System.out.println("❌ No session found for customer " + customerId);
            customerTokens.remove(sessionToken);
            return false;
        }

        // 🔥 FIX: Check if THIS specific session was terminated
        if (session.forciblyTerminated) {
            System.out.println("🚨 Customer " + customerId + " session terminated - concurrent login detected");
            customerSessions.remove(customerId);
            customerTokens.remove(sessionToken);
            return false;
        }

        if (session.isExpired()) {
            System.out.println("⏰ Session expired for customer " + customerId);
            removeCustomerSession(customerId);
            return false;
        }

        if (!session.sessionToken.equals(sessionToken)) {
            System.out.println("❌ Token mismatch for customer " + customerId + " - concurrent login");
            return false;
        }

        session.updateActivity();
        return true;
    }

    public static boolean isCustomerLoggedIn(int customerId) {
        SessionInfo session = customerSessions.get(customerId);
        return session != null && !session.isExpired() && !session.forciblyTerminated;
    }

    public static boolean isCustomerLoggedIn(int customerId, String sessionToken) {
        return validateCustomerSession(customerId, sessionToken);
    }

    public static void removeCustomerSession(int customerId) {
        SessionInfo session = customerSessions.remove(customerId);
        if (session != null) {
            customerTokens.remove(session.sessionToken);
            System.out.println("🔓 Customer " + customerId + " logged out");
        }
    }

    public static Object getCustomerSessionsLock() {
        return customerSessions;
    }

    // ===== SESSION CLEANUP =====

    private static void cleanupExpiredSessions() {
        int expiredAdmin = 0;
        int expiredCustomer = 0;

        for (Map.Entry<Integer, SessionInfo> entry : adminSessions.entrySet()) {
            if (entry.getValue().isExpired()) {
                removeAdminSession(entry.getKey());
                expiredAdmin++;
            }
        }

        for (Map.Entry<Integer, SessionInfo> entry : customerSessions.entrySet()) {
            if (entry.getValue().isExpired()) {
                removeCustomerSession(entry.getKey());
                expiredCustomer++;
            }
        }

        if (expiredAdmin > 0 || expiredCustomer > 0) {
            System.out.println(String.format("🧹 Cleaned up %d admin, %d customer expired sessions",
                    expiredAdmin, expiredCustomer));
        }
    }

    public static String getSessionStats() {
        return String.format(
                "📊 SessionManager Stats:\n" +
                        "  Active Admins: %d\n" +
                        "  Active Customers: %d\n" +
                        "  Active Tokens: %d\n" +
                        "  Timeout: %d minutes",
                adminSessions.size(),
                customerSessions.size(),
                adminTokens.size() + customerTokens.size(),
                SESSION_TIMEOUT_MINUTES
        );
    }
}