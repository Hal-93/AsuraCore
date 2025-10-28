package dev.hal_93.asuraCore.sql;

import java.sql.Connection;
import java.sql.SQLException;

public class Init {
    private final DatabaseManager db;

    public Init(DatabaseManager db) {
        this.db = db;
    }

    public void initTables() throws SQLException {
        String[] queries = {
            "CREATE TABLE IF NOT EXISTS blocks (" +
                "chest_id CHAR(36) PRIMARY KEY DEFAULT (UUID())," +
                "owner_uuid VARCHAR(36) NOT NULL," +
                "world VARCHAR(64) NOT NULL," +
                "pos_x INT NOT NULL," +
                "pos_y INT NOT NULL," +
                "pos_z INT NOT NULL," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "UNIQUE(world,pos_x,pos_y,pos_z)" +
            ")",
            "CREATE TABLE IF NOT EXISTS block_access (" +
                "chest_id CHAR(36)," +
                "player_uuid VARCHAR(36) NOT NULL," +
                "access_level ENUM('READONLY','DEPOSIT','WITHDRAW','FULL_ACCESS') DEFAULT 'READONLY'," +
                "PRIMARY KEY (chest_id, player_uuid)," +
                "FOREIGN KEY (chest_id) REFERENCES blocks(chest_id) ON DELETE CASCADE" +
            ")"
        };
        try (Connection connection = db.getConnection();
             java.sql.Statement st = connection.createStatement()) {
            for (String sql : queries) {
                st.execute(sql);
            }
        }
    }
}
