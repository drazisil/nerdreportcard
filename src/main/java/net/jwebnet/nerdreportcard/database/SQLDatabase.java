/*
 * Copyright (C) 2014 Matthew Green
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.jwebnet.nerdreportcard.database;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.LinkedList;
import java.util.UUID;
import net.jwebnet.nerdreportcard.NerdReportCard;
import net.jwebnet.nerdreportcard.ReportRecord;
import net.jwebnet.nerdreportcard.utils.ConfigManager;

/**
 *
 * @author Matthew Green
 */
public class SQLDatabase implements Database {
    
    private final NerdReportCard plugin;
    private final String prefix;
    private final SQLConnection conn;
    
    public SQLDatabase (NerdReportCard plugin) {
        boolean result = true;
        this.plugin = plugin;
        ConfigManager config = plugin.getConfigM();
        this.prefix = config.sqlPrefix;
        
        this.conn = new SQLConnection(plugin);
    }
     
    public boolean initialise() {
        boolean success = true;
        
        if (success) {
            success = conn.connect();
        }
        
        if (success) {
            success = checkTables();
        }
        
        return success;
    }
    
    private String createUserTable() {
        return "CREATE TABLE `" + this.prefix + "users` (" +
               "`id` INT UNSIGNED AUTO_INCREMENT, " +
               "`name` VARCHAR(255) NOT NULL COLLATE 'utf8_general_ci', " +
               "`uuid` CHAR(36) NOT NULL COLLATE 'utf8_general_ci', " +
               "`points` INT UNSIGNED NOT NULL DEFAULT '0', " +
               "PRIMARY KEY (`id`))";
    }
    
    private String createReportTable() {
        return "CREATE TABLE `" + this.prefix + "reports` (" +
               "`id` INT UNSIGNED AUTO_INCREMENT, " +
               "`user_id` INT UNSIGNED NOT NULL DEFAULT '0', " +
               "`reporter_id` INT UNSIGNED NOT NULL DEFAULT '0', " +
               "`timestamp` INT UNSIGNED NOT NULL, " +
               "`reason` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8_general_ci', " +
               "`points` INT UNSIGNED NOT NULL DEFAULT '0', " +
               "`active` TINYINT(1) UNSIGNED NOT NULL DEFAULT '0', " +
               "PRIMARY KEY (`id`)," +
               "CONSTRAINT fk_user_id " +
               "FOREIGN KEY (user_id) REFERENCES " + this.prefix + "users(id)," +
               "CONSTRAINT fk_reporter_id " +
               "FOREIGN KEY (reporter_id) REFERENCES " + this.prefix + "users(id))";
    }
    
    private boolean checkTables() {
        boolean success = true;
        PreparedStatement statement;
        
        if (!this.conn.isTable(this.prefix + "users")) {
            statement = conn.prepareStatement(createUserTable());
            success = this.conn.update(statement);
            if (success) {
                this.plugin.getLogger().info("Created users table.");
            }
        }
        
        if (!this.conn.isTable(this.prefix + "reports")) {
            statement = conn.prepareStatement(createReportTable());
            success = this.conn.update(statement);
            if (success) {
                this.plugin.getLogger().info("Created report table.");
            }
        }
        
        return success;
    }

    public ReportRecord getReport(Integer reportId) {
        ReportRecord record = null;
        ResultSet reportResult = this.conn.rQuery(
                "SELECT r.*, u1.name as user_name, u2.name as reporter_name, " +
                "u1.uuid as user_uuid, u2.uuid as reporter_uuid " +
                "FROM " + this.prefix + "reports r " +
                "INNER JOIN " + this.prefix + "users u1 on r.user_id = u1.id " +
                "INNER JOIN " + this.prefix + "users u2 on r.reporter_id = u2.id " +
                "WHERE r.id = " + reportId);
       
        try {
            if (reportResult != null) {
                if (reportResult.next()) {
                    record = new ReportRecord(reportId,
                            reportResult.getString("user_name"),
                            UUID.fromString(reportResult.getString("user_uuid")),
                            reportResult.getString("reporter_name"),
                            UUID.fromString(reportResult.getString("reporter_uuid")),
                            reportResult.getString("reason"),
                            reportResult.getInt("timestamp"),
                            reportResult.getInt("points"),
                            reportResult.getBoolean("active"));
                }
                reportResult.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return record;
    }

    public List<ReportRecord> getReports(UUID uuid) {
        List<ReportRecord> reportList = new LinkedList<ReportRecord>();
        
        ResultSet reportResult = this.conn.rQuery(
                "SELECT r.*, u1.name as user_name, u2.name as reporter_name, " +
                "u2.uuid as reporter_uuid " +
                "FROM " + this.prefix + "reports r " +
                "INNER JOIN " + this.prefix + "users u1 on r.user_id = u1.id " +
                "INNER JOIN " + this.prefix + "users u2 on r.reporter_id = u2.id " +
                "WHERE u1.uuid = '" + uuid.toString() + "' " +
                "ORDER BY r.id");
            
        try {
            if (reportResult != null) {
                while (reportResult.next()) {
                    reportList.add(new ReportRecord(
                            reportResult.getInt("id"),
                            reportResult.getString("user_name"),
                            uuid,
                            reportResult.getString("reporter_name"),
                            UUID.fromString(reportResult.getString("reporter_uuid")),
                            reportResult.getString("reason"),
                            reportResult.getInt("timestamp"),
                            reportResult.getInt("points"),
                            reportResult.getBoolean("active")));
                }
                reportResult.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return reportList;
    }
    
    public int getPoints(UUID uuid) {
        int points = 0;
        ResultSet reportResult = this.conn.rQuery(
                "SELECT points FROM `" + this.prefix + "users`" +
                "WHERE uuid = '" + uuid.toString() + "'");
        
        try {
            if (reportResult != null && reportResult.next()) {
                points = reportResult.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return points;
    }
    
    private int addUser(ReportRecord record) {
        boolean success = true;
        int userId = 0;
        PreparedStatement statement = conn.prepareStatement(
                "INSERT INTO `" + this.prefix + "users` " +
                "VALUES (NULL, ?, ?, ?)");
        
        try {
            statement.setString(1, record.playerName);
            statement.setString(2, record.getPlayerUUIDStr());
            statement.setInt(3, record.getPoints());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        success = conn.update(statement);
        
        if (success) {
            ResultSet result = this.conn.rQuery("SELECT LAST_INSERT_ID()");
            try {
                if (result != null && result.next()) {
                    userId = result.getInt(1);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return userId;
    }
    
    private int addStaff(ReportRecord record) {
        boolean success = true;
        int userId = 0;
        PreparedStatement statement = conn.prepareStatement(
                "INSERT INTO `" + this.prefix + "users` " +
                "VALUES (NULL, ?, ?, 0)");
        
        try {
            statement.setString(1, record.reporterName);
            statement.setString(2, record.getReporterUUIDStr());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        success = conn.update(statement);

        if (success) {
            ResultSet result = this.conn.rQuery("SELECT LAST_INSERT_ID()");
            try {
                if (result != null && result.next()) {
                    userId = result.getInt(1);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return userId;
    }
    
    private int updateUserName(ReportRecord record) {
        int userId = 0;
        ResultSet userResult = this.conn.rwQuery(
                "SELECT * FROM `" + this.prefix + "users` " +
                "WHERE uuid = '" + record.getPlayerUUIDStr() + "'");
        
        try {
            if (userResult != null && userResult.next()) {
                if (!userResult.getString("name").equals(record.playerName)) {
                    userResult.updateString("name", record.playerName);
                    userResult.updateRow();
                }
                userId = userResult.getInt(1);
                userResult.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return userId;
    }
    
    private void updateUserPoints (int userId, int delta) {
        if (delta == 0) {
            return;
        }
        
        ResultSet userResult = this.conn.rwQuery(
                "SELECT * FROM `" + this.prefix + "users` " +
                "WHERE id = " + userId);
        
        try {
            if (userResult != null && userResult.next()) {
                int newPoints = userResult.getInt("points") + delta;
                userResult.updateInt("points", newPoints);
                userResult.updateRow();
                userResult.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private int updateStaffName(ReportRecord record) {
        int userId = 0;
        ResultSet userResult = this.conn.rwQuery(
                "SELECT * FROM `" + this.prefix + "users` " +
                "WHERE uuid = '" + record.getReporterUUIDStr() + "'");
        
        try {
            if (userResult != null && userResult.next()) {
                if (!userResult.getString("name").equals(record.reporterName)) {
                    userResult.updateString("name", record.reporterName);
                    userResult.updateRow();
                }
                userId = userResult.getInt(1);
                userResult.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return userId;
    }

    public void addReport(ReportRecord record) throws IOException {
        // Update the user database with any new usernames/points.
        int userId = updateUserName(record);
        if (userId == 0) {
            userId = addUser(record);
        } else {
            updateUserPoints(userId, record.getPoints());
        }
        
        int reporterId = updateStaffName(record);
        if (reporterId == 0) {
            reporterId = addStaff(record);
        }
        
        // Add an entry to the reports database.
        String reason = "NULL";
        if (record.reason != null) {
            reason = record.reason;
        }
        
        PreparedStatement statement = conn.prepareStatement(
                "INSERT INTO `" + this.prefix + "reports` " +
                "VALUES (?, ?, ?, ?, ?, ?, 1)");
        
        try {
            statement.setInt(1, record.reportId);
            statement.setInt(2, userId);
            statement.setInt(3, reporterId);
            statement.setInt(4, record.getTime());
            statement.setString(5, reason);
            statement.setInt(6, record.getPoints());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        conn.update(statement);
    }

    public void editReport(ReportRecord record) throws IOException {
        // Obtain the existing report.
        ResultSet reportResult = this.conn.rwQuery(
                "SELECT * FROM " + this.prefix + "reports " +
                "WHERE id = " + record.reportId);
        
        try {
            if (reportResult != null) {
                if (reportResult.next()) {
                    int prevPoints = reportResult.getInt("points");
                    String reason = "NULL";
                    if (record.reason != null) {
                        reason = record.reason;
                    }
                    // Update the reason and points for the record.
                    reportResult.updateString("reason", reason);
                    reportResult.updateInt("points", record.getPoints());
                    reportResult.updateRow();
                    
                    // Update the users.
                    int userId = updateUserName(record);
                    updateUserPoints(userId, record.getPoints() - prevPoints);
                    updateStaffName(record);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteReport(Integer reportId) throws IOException {
        // Obtain the report, update the users, and set active to zero.
        ResultSet reportResult = this.conn.rwQuery(
                "SELECT * FROM `" + this.prefix + "reports` " +
                "WHERE id = " + reportId);
        
        try {
            if (reportResult != null) {
                if (reportResult.next()) {
                    int prevPoints = reportResult.getInt("points");
                    // Update the active int.
                    reportResult.updateInt("active", 0);
                    reportResult.updateRow();
                    
                    // Update the user's total points.
                    updateUserPoints(reportResult.getInt("user_id"),
                            -reportResult.getInt("points"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
