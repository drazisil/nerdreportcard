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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import static java.lang.Integer.parseInt;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.jwebnet.nerdreportcard.NerdReportCard;
import net.jwebnet.nerdreportcard.ReportRecord;
import net.jwebnet.nerdreportcard.utils.ConfigManager;
import net.jwebnet.nerdreportcard.utils.UUIDFetcher;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 *
 * @author Matthew Green
 */
public class YAMLDatabase implements Database {

    private final NerdReportCard plugin;
    private final File dataFile;
    private FileConfiguration configFile;
    private Integer nextReportId;

    public YAMLDatabase(NerdReportCard plugin) {
        ConfigManager configM = plugin.getConfigM();
        this.plugin = plugin;
        dataFile = new File(plugin.getDataFolder(), configM.yamlDbFile);
    }
    
    public boolean initialise() {
        boolean success = true;
        
        if (success) {
            if (!dataFile.exists()) {
                if (!this.plugin.getConfigM().useSql) {
                    this.plugin.getLogger().info("Creating new YAML data file");
                    try {
                        dataFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                        success = false;
                    }
                } else {
                    success = false;
                }
            }
        }

        if (success) {
            configFile = new YamlConfiguration();
            try {
                configFile.load(dataFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                success = false;
            } catch (IOException e) {
                e.printStackTrace();
                success = false;
            } catch (InvalidConfigurationException e) {
                e.printStackTrace();
                success = false;
            }
        }

        if (success) {
            String nextIdStr = configFile.getString("nextReportId");
            if (nextIdStr == null) {
                configFile.set("nextReportId", 1);
                nextReportId = 1;
                configFile.createSection("reports");
            } else {
                nextReportId = parseInt(nextIdStr);
            }
        
           success = checkUUIDs(); 
        }
        
        return success;
    }
    
    public boolean move() {
        boolean success = true;
        
        if (dataFile.exists()) {
            File dest = new File(dataFile.getAbsolutePath() + ".bak");
            dataFile.renameTo(dest);
        }
        
        return success;
    }
    
    private boolean checkUUIDs() {
        boolean success = true;
        ConfigurationSection records = configFile.getConfigurationSection("reports");
        ReportRecord record;
        LinkedList<String> names = new LinkedList<String>();
        LinkedList<ReportRecord> updateRecords = new LinkedList<ReportRecord>();
        Map<String, UUID> uuids = null;
        
        for (String key : records.getKeys(false)) {
            record = getReport(parseInt(key));
            if (record.getPlayerUUID() == null) {
                if (!names.contains(record.playerName.toLowerCase())) {
                    names.add(record.playerName.toLowerCase());
                }
            }
            
            if (record.getReporterUUID() == null) {
                if (!names.contains(record.reporterName.toLowerCase())) {
                    names.add(record.reporterName.toLowerCase());
                }
            }
            
            if (record.getPlayerUUID() == null ||
                    record.getReporterUUID() == null) {
                updateRecords.add(record);
            }
        }
        
        UUIDFetcher fetcher = new UUIDFetcher(names, true);
        try {
            uuids = fetcher.call();
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
        }
        
        if (uuids != null) {
            UUID uuid = null;
            for (ReportRecord r : updateRecords) {
                if (r.getPlayerUUID() == null) {
                    uuid = uuids.get(r.playerName.toLowerCase());
                    if (uuid == null) {
                        this.plugin.getLogger().warning("Couldn't find UUID " +
                                "for username: " + r.playerName);
                    } else {
                        r.setPlayerUUID(uuid);
                    }
                }
                
                if (r.getReporterUUID() == null) {
                    uuid = uuids.get(r.reporterName.toLowerCase());
                    if (uuid == null) {
                        this.plugin.getLogger().warning("Couldn't find UUID " +
                                "for username: " + r.reporterName);
                    } else {
                        r.setReporterUUID(uuid);
                    }
                }
                
                try {
                    editReport(r);
                } catch (IOException e) {
                    this.plugin.getLogger().warning("Unable to set UUID");
                    e.printStackTrace();
                    success = false;
                }
            }
        }
        
        return success;
    }

    public ReportRecord getReport(Integer reportId) {
        ReportRecord record = null;
        ConfigurationSection recordData = configFile.getConfigurationSection("reports." + reportId.toString());

        if (recordData != null) {
            String playerUUIDString = recordData.getString("playerUUID");
            UUID playerUUID = null;
            if (playerUUIDString != null) {
                playerUUID = UUID.fromString(playerUUIDString);
            }
            String reporterUUIDString = recordData.getString("reporterUUID");
            UUID reporterUUID = null;
            if (reporterUUIDString != null) {
                reporterUUID = UUID.fromString(reporterUUIDString);
            }
            record = new ReportRecord(parseInt(recordData.getName()),
                    recordData.getString("playerName"),
                    playerUUID,
                    recordData.getString("reporterName"),
                    reporterUUID,
                    recordData.getString("reason"),
                    recordData.getString("reportDate"),
                    recordData.getInt("warningPoints"),
                    recordData.getBoolean("active"));
        }

        return record;
    }

    public List<ReportRecord> getReports(UUID uuid) {
        List<ReportRecord> reportList = new LinkedList<ReportRecord>();

        ConfigurationSection recordData =
                configFile.getConfigurationSection("reports");

        for (String r : recordData.getKeys(false)) {
            ReportRecord record = getReport(parseInt(r));
            if (uuid.equals(record.getPlayerUUID()) && record.active) {
                reportList.add(record);
            }
        }

        return reportList;
    }
    
    public List<ReportRecord> getReports() {
        List<ReportRecord> reportList = new LinkedList<ReportRecord>();

        ConfigurationSection recordData =
                configFile.getConfigurationSection("reports");

        for (String r : recordData.getKeys(false)) {
            ReportRecord record = getReport(parseInt(r));
            reportList.add(record);
        }

        return reportList;
    }
    
    public int getPoints(UUID uuid) {
        int points = 0;
        
        for (ReportRecord record : getReports(uuid)) {
            points += record.getPoints();
        }
        
        return points;
    }

    public void addReport(ReportRecord record) throws IOException {
        // Create the new report secion
        ConfigurationSection reportData = configFile.createSection("reports." + nextReportId.toString());

        // Save the report
        reportData.set("playerName", record.playerName);
        reportData.set("playerUUID", record.getPlayerUUIDStr());
        reportData.set("warningPoints", record.getPoints());
        reportData.set("reason", record.reason);
        reportData.set("reporterName", record.reporterName);
        reportData.set("reporterUUID", record.getReporterUUIDStr());
        reportData.set("reportDate", record.getTimeString());
        reportData.set("active", record.active);

        // Update the next report id
        nextReportId++;
        configFile.set("nextReportId", nextReportId.toString());

        configFile.save(dataFile);
    }

    public void editReport(ReportRecord record) throws IOException {
        // Obtain the current report section
        ConfigurationSection reportData = configFile.getConfigurationSection("reports." + record.reportId.toString());

        // Save the report with new data
        reportData.set("playerName", record.playerName);
        reportData.set("playerUUID", record.getPlayerUUIDStr());
        reportData.set("warningPoints", record.getPoints());
        reportData.set("reason", record.reason);
        reportData.set("reporterName", record.reporterName);
        reportData.set("reporterUUID", record.getReporterUUIDStr());
        reportData.set("reportDate", record.getTimeString());
        reportData.set("active", record.active);

        configFile.save(dataFile);
    }

    public void deleteReport(Integer reportId) throws IOException {
        ConfigurationSection reportData = configFile.getConfigurationSection("reports." + reportId.toString());
        reportData.set("active", false);
        configFile.save(dataFile);
    }
}
