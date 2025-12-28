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
 * SessionManager - Manages active admin and customer sessions
 * Thread-safe implementation using ConcurrentHashMap
 */
public class SessionManager {

    // ===== SESSION STORAGE =====

    // Stores active admin sessions: adminId -> SessionInfo
    private static final ConcurrentMap<Integer, SessionInfo> adminSessions = new ConcurrentHashMap<>();

    // Stores active customer sessions: customerId -> SessionInfo
    private static final ConcurrentMap<Integer, SessionInfo> customerSessions = new ConcurrentHashMap<>();

    // Stores session tokens: token -> userId (for token-based auth)
    private static final ConcurrentMap<String, Integer> sessionTokens = new ConcurrentHashMap<>();

    // ===== CONFIGURATION =====

    // Session timeout in minutes (default: 30 minutes)
    private static final int SESSION_TIMEOUT_MINUTES = 30;

    // Cleanup interval in minutes (default: 5 minutes)
    private static final int CLEANUP_INTERVAL_MINUTES = 5;

    // Timer for automatic session cleanup
    private static Timer cleanupTimer;

    // ===== SESSION INFO CLASS =====

    public static class SessionInfo {
        public int userId;
        public String username;
        public String sessionToken;
        public LocalDateTime loginTime;
        public LocalDateTime lastActivityTime;
        public String ipAddress;
        public String userAgent;

        public SessionInfo(int userId, String username, String sessionToken) {
            this.userId = userId;
            this.username = username;
            this.sessionToken = sessionToken;
            this.loginTime = LocalDateTime.now();
            this.lastActivityTime = LocalDateTime.now();
        }

        public SessionInfo(int userId, String username, String sessionToken, String ipAddress) {
            this(userId, username, sessionToken);
            this.ipAddress = ipAddress;
        }

        public boolean isExpired() {
            return ChronoUnit.MINUTES.between(lastActivityTime, LocalDateTime.now()) > SESSION_TIMEOUT_MINUTES;
        }

        public void updateActivity() {
            this.lastActivityTime = LocalDateTime.now();
        }

        @Override
        public String toString() {
            return String.format("Session[userId=%d, username=%s, loginTime=%s, lastActivity=%s]",
                    userId, username, loginTime, lastActivityTime);
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

            System.out.println("SessionManager initialized with " + SESSION_TIMEOUT_MINUTES + " min timeout");
        }
    }


    public static void shutdown() {
        if (cleanupTimer != null) {
            cleanupTimer.cancel();
            cleanupTimer = null;
        }
        adminSessions.clear();
        customerSessions.clear();
        sessionTokens.clear();
        System.out.println("SessionManager shutdown complete");
    }

    // ===== ADMIN SESSION MANAGEMENT =====

    public static String createAdminSession(int adminId, String username) {
        return createAdminSession(adminId, username, null);
    }

    public static String createAdminSession(int adminId, String username, String ipAddress) {
        synchronized (adminSessions) {
            SessionInfo existingSession = adminSessions.get(adminId);
            if (existingSession != null) {
                if (existingSession.sessionToken != null) {
                    sessionTokens.remove(existingSession.sessionToken);
                }
                adminSessions.remove(adminId);
            }

            String sessionToken = UUID.randomUUID().toString();
            SessionInfo sessionInfo = new SessionInfo(adminId, username, sessionToken, ipAddress);

            adminSessions.put(adminId, sessionInfo);
            sessionTokens.put(sessionToken, adminId);

            System.out.println("Admin session created: " + sessionInfo);
            return sessionToken;
        }
    }

    public static boolean isAdminLoggedIn(int adminId) {
        SessionInfo session = adminSessions.get(adminId);
        if (session != null && !session.isExpired()) {
            session.updateActivity();
            return true;
        }
        // Remove expired session
        if (session != null) {
            removeAdminSession(adminId);
        }
        return false;
    }

    public static SessionInfo getAdminSession(int adminId) {
        SessionInfo session = adminSessions.get(adminId);
        if (session != null && !session.isExpired()) {
            session.updateActivity();
            return session;
        }
        return null;
    }

    // ✅ FIX: Get raw session from map (for concurrent login prevention - checks ALL sessions, not just active)
    public static SessionInfo getAdminSessionRaw(int adminId) {
        return adminSessions.get(adminId);
    }

    // ✅ FIX: Internal method to create admin session WITHOUT synchronization (caller must synchronize)
    // This is used when the caller already holds the lock (e.g., in loginAdmin)
    public static String createAdminSessionUnsafe(int adminId, String username) {
        return createAdminSessionUnsafe(adminId, username, null);
    }

    public static String createAdminSessionUnsafe(int adminId, String username, String ipAddress) {
        SessionInfo existingSession = adminSessions.get(adminId);
        if (existingSession != null) {
            removeAdminSession(adminId);
        }

        String sessionToken = UUID.randomUUID().toString();
        SessionInfo sessionInfo = new SessionInfo(adminId, username, sessionToken, ipAddress);

        adminSessions.put(adminId, sessionInfo);
        sessionTokens.put(sessionToken, adminId);

        System.out.println("Admin session created: " + sessionInfo);
        return sessionToken;
    }

    public static boolean validateAdminToken(String token) {
        Integer adminId = sessionTokens.get(token);
        return adminId != null && isAdminLoggedIn(adminId);
    }

    public static Integer getAdminIdFromToken(String token) {
        Integer adminId = sessionTokens.get(token);
        if (adminId != null && isAdminLoggedIn(adminId)) {
            return adminId;
        }
        return null;
    }

    public static void removeAdminSession(int adminId) {
        SessionInfo session = adminSessions.remove(adminId);
        if (session != null) {
            sessionTokens.remove(session.sessionToken);
        }
    }

    public static Map<Integer, SessionInfo> getAllAdminSessions() {
        return new ConcurrentHashMap<>(adminSessions);
    }

    public static int getActiveAdminCount() {
        return adminSessions.size();
    }

    // ===== CUSTOMER SESSION MANAGEMENT =====

    public static String createCustomerSession(int customerId, String username) {
        return createCustomerSession(customerId, username, null);
    }

    public static String createCustomerSession(int customerId, String username, String ipAddress) {
        synchronized (customerSessions) {
            SessionInfo existingSession = customerSessions.get(customerId);
            if (existingSession != null) {
                if (existingSession.sessionToken != null) {
                    sessionTokens.remove(existingSession.sessionToken);
                }
                customerSessions.remove(customerId);
            }

            String sessionToken = UUID.randomUUID().toString();
            SessionInfo sessionInfo = new SessionInfo(customerId, username, sessionToken, ipAddress);

            customerSessions.put(customerId, sessionInfo);
            sessionTokens.put(sessionToken, customerId);

            System.out.println("Customer session created: " + sessionInfo);
            return sessionToken;
        }
    }

    public static boolean isCustomerLoggedIn(int customerId) {
        SessionInfo session = customerSessions.get(customerId);
        if (session != null && !session.isExpired()) {
            session.updateActivity();
            return true;
        }
        if (session != null) {
            removeCustomerSession(customerId);
        }
        return false;
    }

    public static SessionInfo getCustomerSession(int customerId) {
        SessionInfo session = customerSessions.get(customerId);
        if (session != null && !session.isExpired()) {
            session.updateActivity();
            return session;
        }
        return null;
    }

    // ✅ FIX: Get raw session from map (for concurrent login prevention - checks ALL sessions, not just active)
    public static SessionInfo getCustomerSessionRaw(int customerId) {
        return customerSessions.get(customerId);
    }

    // ✅ FIX: Get lock object for synchronizing login operations
    public static Object getCustomerSessionsLock() {
        return customerSessions;
    }

    // ✅ FIX: Get lock object for synchronizing admin login operations
    public static Object getAdminSessionsLock() {
        return adminSessions;
    }

    // ✅ FIX: Internal method to create customer session WITHOUT synchronization (caller must synchronize)
    // This is used when the caller already holds the lock (e.g., in loginCustomer)
    public static String createCustomerSessionUnsafe(int customerId, String username) {
        return createCustomerSessionUnsafe(customerId, username, null);
    }

    public static String createCustomerSessionUnsafe(int customerId, String username, String ipAddress) {
        SessionInfo existingSession = customerSessions.get(customerId);
        if (existingSession != null) {
            removeCustomerSession(customerId);
        }

        String sessionToken = UUID.randomUUID().toString();
        SessionInfo sessionInfo = new SessionInfo(customerId, username, sessionToken, ipAddress);

        customerSessions.put(customerId, sessionInfo);
        sessionTokens.put(sessionToken, customerId);

        System.out.println("Customer session created: " + sessionInfo);
        return sessionToken;
    }

    public static void removeCustomerSession(int customerId) {
        SessionInfo session = customerSessions.remove(customerId);
        if (session != null) {
            sessionTokens.remove(session.sessionToken);
        }
    }

    public static Map<Integer, SessionInfo> getAllCustomerSessions() {
        return new ConcurrentHashMap<>(customerSessions);
    }

    public static int getActiveCustomerCount() {
        return customerSessions.size();
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
            System.out.println(String.format("🧹 Session cleanup: removed %d admin, %d customer sessions",
                    expiredAdmin, expiredCustomer));
        }
    }

    public static void forceCleanup() {
        cleanupExpiredSessions();
    }

    public static void clearAllSessions() {
        adminSessions.clear();
        customerSessions.clear();
        sessionTokens.clear();
        System.out.println("🗑️ All sessions cleared");
    }

    // ===== STATISTICS & MONITORING =====

    public static String getSessionStats() {
        return String.format(
                "SessionManager Statistics:\n" +
                        "  Active Admins: %d\n" +
                        "  Active Customers: %d\n" +
                        "  Total Sessions: %d\n" +
                        "  Session Timeout: %d minutes",
                adminSessions.size(),
                customerSessions.size(),
                adminSessions.size() + customerSessions.size(),
                SESSION_TIMEOUT_MINUTES
        );
    }

    public static void printActiveSessions() {
        System.out.println("\n=== Active Admin Sessions ===");
        adminSessions.forEach((id, session) ->
                System.out.println("  " + id + ": " + session));

        System.out.println("\n=== Active Customer Sessions ===");
        customerSessions.forEach((id, session) ->
                System.out.println("  " + id + ": " + session));

        System.out.println();
    }
}