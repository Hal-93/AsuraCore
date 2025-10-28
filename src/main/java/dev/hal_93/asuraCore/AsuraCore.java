package dev.hal_93.asuraCore;

import org.bukkit.plugin.java.JavaPlugin;
import dev.hal_93.asuraCore.sql.DatabaseManager;
import dev.hal_93.asuraCore.sql.Init;

import java.sql.SQLException;
import java.util.Properties;

public final class AsuraCore extends JavaPlugin {

    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Properties props = loadDbProperties();

        try {
            this.databaseManager = new DatabaseManager(props);
            this.databaseManager.connect();

            Init initializer = new Init(this.databaseManager);
            initializer.initTables();

            getLogger().info("AsuraCore enabled. Database connected and tables initialized.");
        } catch (SQLException e) {
            getLogger().severe("Startup failed: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (this.databaseManager != null) {
            this.databaseManager.close();
        }
        getLogger().info("AsuraCore disabled.");
    }

    public DatabaseManager getDatabaseManager() {
        return this.databaseManager;
    }

    private Properties loadDbProperties() {
        Properties p = new Properties();
        var c = getConfig();
        String host = c.getString("database.host", "localhost");
        int port = c.getInt("database.port", 3306);
        String name = c.getString("database.name", "asuracore");
        String user = c.getString("database.user", "root");
        String pass = c.getString("database.password", "");
        String params = c.getString("database.params", "?useSSL=false&useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Tokyo&allowPublicKeyRetrieval=true");

        int poolMax = c.getInt("pool.max", 10);
        int poolMinIdle = c.getInt("pool.minIdle", Math.min(poolMax, 2));
        long connTimeout = c.getLong("pool.connectionTimeoutMs", 30_000L);
        long idleTimeout = c.getLong("pool.idleTimeoutMs", 600_000L);
        long leakMs = c.getLong("pool.leakDetectionMs", 0L);

        p.setProperty("database.host", host);
        p.setProperty("database.port", Integer.toString(port));
        p.setProperty("database.name", name);
        p.setProperty("database.user", user);
        p.setProperty("database.password", pass);
        p.setProperty("database.params", params);

        p.setProperty("pool.max", Integer.toString(poolMax));
        p.setProperty("pool.minIdle", Integer.toString(poolMinIdle));
        p.setProperty("pool.connectionTimeoutMs", Long.toString(connTimeout));
        p.setProperty("pool.idleTimeoutMs", Long.toString(idleTimeout));
        p.setProperty("pool.leakDetectionMs", Long.toString(leakMs));
        return p;
    }
}
