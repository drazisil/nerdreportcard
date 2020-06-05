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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import net.jwebnet.nerdreportcard.NerdReportCard;
import net.jwebnet.nerdreportcard.utils.ConfigManager;

/**
 *
 * @author Matthew Green
 */
public class SQLConnection {
    
    private final NerdReportCard plugin;
    private final ConfigManager config;
    private final String url;
    private Connection conn = null;
    
    public SQLConnection(NerdReportCard plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigM();
        this.url = "jdbc:mysql://" + this.config.sqlAddress  + ":" +
                this.config.sqlPort + "/" + this.config.sqlDb +
                "?autoReconnect=true";
    }
    
    public boolean connect() {
        boolean success = true;
        this.plugin.getLogger().info("Connecting to MySQL");
        
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            success = false;
        }
       
        try {
            this.conn = DriverManager.getConnection(this.url,
                    this.config.sqlUsername, this.config.sqlPassword);
        } catch (SQLException e) {
            this.plugin.getLogger().warning("Could not connect to MySQL");
            e.printStackTrace();
            success = false;
        }
        
        return success;
    }
    
    public ResultSet rQuery(String query) {
        ResultSet result = null;
        
        this.plugin.getLogger().finer("SQL rQuery: " + query);
        
        try {
            if (this.conn == null) {
                this.plugin.getLogger().warning("SQL not connected!");
            } else {
                Statement statement = this.conn.createStatement();
                statement.executeQuery(query);
                result = statement.getResultSet();
            }
        } catch (SQLException e) {
            result = null;
            e.printStackTrace();
        }
        
        return result;
    }
    
    public ResultSet rwQuery(String query) {
        ResultSet result = null;
        
        this.plugin.getLogger().finer("SQL rwQuery: " + query);
        
        try {
            if (this.conn == null) {
                this.plugin.getLogger().warning("SQL not connected!");
            } else {
                Statement statement = this.conn.createStatement(
                        ResultSet.TYPE_SCROLL_SENSITIVE,
                        ResultSet.CONCUR_UPDATABLE);
                statement.executeQuery(query);
                result = statement.getResultSet();
            }
        } catch (SQLException e) {
            result = null;
            e.printStackTrace();
        }
        
        return result;
    }
    
    public PreparedStatement prepareStatement(String sql) {
        PreparedStatement statement = null;
        
        try {
             statement = this.conn.prepareStatement(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return statement;
    }
    
    public boolean update(PreparedStatement statement) {
        boolean success = true;
        
        this.plugin.getLogger().finer("SQL update: " + statement.toString());

        try {
            if (this.conn == null) {
                this.plugin.getLogger().warning("SQL not connected!");
            } else {
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            success = false;
            e.printStackTrace();
        }
        
        return success;
    }
    
    public boolean isTable(String table) {
        boolean result = true;
        
        try {
            Statement statement = this.conn.createStatement();
            statement.executeQuery("SELECT * FROM " + table);
            statement.close();
        } catch (SQLException e) {
            result = false;
        }
        
        return result;
    }
}
