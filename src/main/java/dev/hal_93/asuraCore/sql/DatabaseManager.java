package dev.hal_93.asuraCore.sql;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.nio.file.Path;
import java.nio.file.Paths;

public class DatabaseManager {
    private final String type;
    private final String User;
    private final String Password;
    private final String URL;
    private HikariDataSource dataSource;
    private final int maxPoolSize;
    private final int minIdle;
    private final long connectionTimeoutMs;
    private final long idleTimeoutMs;
    private final long leakDetectionMs;

    private static int getInt(Properties props, String key, int def) {
        try {
            return Integer.parseInt(props.getProperty(key, String.valueOf(def)));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static long getLong(Properties props, String key, long def) {
        try {
            return Long.parseLong(props.getProperty(key, String.valueOf(def)));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public DatabaseManager(String configPath) throws IOException {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(configPath)) {
            props.load(fis);
        }
        this.type = props.getProperty("database.type", "sqlite");
        String host = props.getProperty("database.host", "localhost");
        int port = Integer.parseInt(props.getProperty("database.port", "3306"));
        String database = props.getProperty("database.name", "asuracore");
        this.User = props.getProperty("database.user", "root");
        this.Password = props.getProperty("database.password", "");
        if (type.equalsIgnoreCase("sqlite")) {
            String sqliteFile = props.getProperty("database.file", "data.db");
            Path baseDir = Paths.get(configPath).toAbsolutePath().getParent();
            String absoluteSqlitePath = (baseDir != null) ? baseDir.resolve(sqliteFile).toString() : sqliteFile;
            this.URL = "jdbc:sqlite:" + absoluteSqlitePath;
        } else {
            String params = props.getProperty("database.params", "?useSSL=false&useUnicode=true&characterEncoding=utf8&serverTimezone=JST&allowPublicKeyRetrieval=true");
            this.URL = "jdbc:mysql://" + host + ":" + port + "/" + database + params;
        }
        this.maxPoolSize = getInt(props, "pool.max", type.equalsIgnoreCase("sqlite") ? 1 : 10);
        this.minIdle = getInt(props, "pool.minIdle", Math.min(this.maxPoolSize, 2));
        this.connectionTimeoutMs = getLong(props, "pool.connectionTimeoutMs", 30_000L);
        this.idleTimeoutMs = getLong(props, "pool.idleTimeoutMs", 600_000L);
        this.leakDetectionMs = getLong(props, "pool.leakDetectionMs", 0L);
    }

    public void connect() throws SQLException {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(URL);

        if (type.equalsIgnoreCase("mysql")) {
            config.setUsername(User);
            config.setPassword(Password);
            // Recommended MySQL settings
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("useUnicode", "true");
            config.addDataSourceProperty("characterEncoding", "utf8");
        } else if (type.equalsIgnoreCase("sqlite")) {
            config.setDriverClassName("org.sqlite.JDBC");
            config.setConnectionInitSql("PRAGMA foreign_keys=ON; PRAGMA journal_mode=WAL;");
        }

        config.setMaximumPoolSize(maxPoolSize);
        config.setMinimumIdle(minIdle);
        config.setConnectionTimeout(connectionTimeoutMs);
        config.setIdleTimeout(idleTimeoutMs);
        if (leakDetectionMs > 0) {
            config.setLeakDetectionThreshold(leakDetectionMs);
        }

        dataSource = new HikariDataSource(config);
        try (Connection conn = dataSource.getConnection()) {
        }
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            return null;
        }
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null) {
            if (!dataSource.isClosed()) {
                dataSource.close();
            }
        }
    }
}
