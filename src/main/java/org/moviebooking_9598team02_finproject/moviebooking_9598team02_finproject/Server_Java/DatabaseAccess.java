package org.moviebooking_9598team02_finproject.moviebooking_9598team02_finproject.Server_Java;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseAccess {

    // ===== Database Config =====
    private static final String URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "";
    private static final String USER = "root";
    private static final String PASS = "";
    private static HikariDataSource dataSource;

    static {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(URL + DB_NAME);
            config.setUsername(USER);
            config.setPassword(PASS);

            config.setMaximumPoolSize(20);
            config.setMinimumIdle(5);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);

            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");

            dataSource = new HikariDataSource(config);

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException{
       if (dataSource == null || dataSource.isClosed()){
           throw new SQLException("HikariDataSource is not initialized");
       }
       return dataSource.getConnection();
    }

    public static void closeConnection() throws SQLException{
        if (dataSource != null || !dataSource.isClosed()){
            dataSource.close();
        }
    }

    public static void printPoolStats() {
        if (dataSource != null && !dataSource.isClosed()) {
            System.out.println("=== Connection Pool Stats ===");
            System.out.println("Active connections: " +
                    (dataSource.getHikariPoolMXBean().getActiveConnections()));
            System.out.println("Idle connections: " +
                    (dataSource.getHikariPoolMXBean().getIdleConnections()));
            System.out.println("Total connections: " +
                    (dataSource.getHikariPoolMXBean().getTotalConnections()));
            System.out.println("Threads waiting: " +
                    (dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection()));
        }
    }
}
