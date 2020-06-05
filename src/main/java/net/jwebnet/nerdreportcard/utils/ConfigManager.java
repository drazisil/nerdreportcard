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
package net.jwebnet.nerdreportcard.utils;

import net.jwebnet.nerdreportcard.NerdReportCard;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Retrieves and sets configuration.
 *
 * @author Matthew Green
 */
public class ConfigManager {

    public boolean debug = false;
    public boolean useSql = false;
    public String sqlAddress;
    public int sqlPort;
    public String sqlUsername = null;
    public String sqlPassword = null;
    public String sqlDb;
    public String sqlPrefix;
    public String yamlDbFile;

    public ConfigManager(NerdReportCard plugin) {
        FileConfiguration config = plugin.getConfig();

        this.debug = config.getBoolean("debug", false);
        this.useSql = config.getBoolean("database.sql.use", false);
        if (this.useSql) {
            this.sqlAddress = config.getString("database.sql.address",
                    "localhost");
            this.sqlPort = config.getInt("database.sql.port", 3306);
            this.sqlUsername = config.getString("database.sql.username");
            this.sqlPassword = config.getString("database.sql.password");
            this.sqlDb = config.getString("database.sql.dbname", "nerdrc");
            this.sqlPrefix = config.getString("database.sql.prefix", "nrc_");
            
            if (this.sqlUsername == null || this.sqlPassword == null) {
                plugin.getLogger().warning("Missing mandatory SQL " +
                        "configuration - must have both username and " +
                        "password if SQL is enabled.");
            }
        }

        this.yamlDbFile = config.getString("database.yamlfile", "reports.yml");
    }
}
