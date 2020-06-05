/*
 * Copyright (C) 2014 Joseph W Becher <jwbecher@jwebnet.net>
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
package net.jwebnet.nerdreportcard;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;
import net.jwebnet.nerdreportcard.i18n.I18n;
import static net.jwebnet.nerdreportcard.i18n.I18n.tl;
import net.jwebnet.nerdreportcard.utils.ConfigManager;
import net.jwebnet.nerdreportcard.database.Database;
import net.jwebnet.nerdreportcard.database.SQLDatabase;
import net.jwebnet.nerdreportcard.database.YAMLDatabase;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Plugin class.
 *
 * @author Joseph W Becher <jwbecher@jwebnet.net>
 */
public final class NerdReportCard extends JavaPlugin implements Listener {

    protected transient I18n i18n;
    private Database database;
    private YAMLDatabase yamlDb;
    private ConfigManager config;

    /**
     * onEnable is a method from JavaPlugin
     */
    @Override
    public void onEnable() {
        boolean success = true;
        this.saveDefaultConfig();
        
        // Load config
        config = new ConfigManager(this);
        
        // Turn on debugging if necessary.
        if (config.debug) {
            enableDebug();
        }

        // Create a database manager.
        if (config.useSql) {
            yamlDb = new YAMLDatabase(this);
            database = new SQLDatabase(this);
            
            success = database.initialise();
            
            if (success) {
                success = yamlDb.initialise();
            
                if (success) {
                    getLogger().info("Transferring YAML data to MySQL");
                    List<ReportRecord> records = yamlDb.getReports();
                    for (ReportRecord r : records) {
                        try {
                            database.addReport(r);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    // Move the reports file so it's not imported again.
                    getLogger().info("Moving YAML file to " +
                            config.yamlDbFile + ".bak");
                    yamlDb.move();
                }
             
                // Success should be true whether or not the YAML database
                // initialised as it's not required past this point.
                success = true;
            }
        } else {
            database = new YAMLDatabase(this);
            success = database.initialise();
        }

        if (success) {
            getServer().getPluginManager().registerEvents(this, this);
            ReportCommands commands = new ReportCommands(this);

            /* Reload rhe data fale
             * usage: /rcreload
             */
            getCommand("rcreload").setExecutor(commands);
            /* Add a new report
             * usage: /rcadd <player_name> <points> <reason>
             */
            getCommand("rcadd").setExecutor(commands);
            /* Edit an existing report by id
             * usage: /rcedit <report_id> <note>
             */
            getCommand("rcedit").setExecutor(commands);
            /* Remove a report by id
             * usage: /rcremove <report_id> <report_id>
             */
            getCommand("rcremove").setExecutor(commands);
            /* List a report
             * usage: /rclist {player}
             */
            getCommand("rclist").setExecutor(commands);
            /* List a report by id
             * usage: /rcid <reportcard_id>
             */
            getCommand("rcid").setExecutor(commands);

            // Load the messages file
            saveDefaultResource("messages_en.properties");
            i18n = new I18n(this);
            i18n.onEnable();
            i18n.updateLocale("en");
        }
        
        if (!success) {
            getLogger().warning("Failed to initialise NerdReportCard");
        }
    }

    /**
     * onDisable is a method from JavaPlugin
     */
    @Override
    public void onDisable() {
    }
    
    /*
     * Enable debugging.
     */
    private void enableDebug() {
        File folder = getDataFolder();
        if (!folder.exists()) {
            folder.mkdir();
        }
        
        File debug = new File(folder, "debug.log");
        
        try {
            FileHandler fh = new FileHandler(debug.getPath());
            fh.setLevel(Level.FINER);
            getLogger().addHandler(fh);
            
            SimpleFormatter ft = new SimpleFormatter();
            fh.setFormatter(ft);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Database getReportDatabase() {
        return this.database;
    }

    @EventHandler
    public void normalJoin(PlayerJoinEvent event) {
        int totalPoints = this.database.getPoints(event.getPlayer().getUniqueId());

        if (totalPoints != 0) {
            Bukkit.broadcast(tl("playerLoginBannerAdmin", event.getPlayer().getName(), totalPoints), "nerdreportcard.admin");
        }
    }

    public void saveDefaultResource(String fileName) {
        File customConfigFile = new File(getDataFolder(), fileName);

        if (!customConfigFile.exists()) {
            saveResource(fileName, false);
        }
    }
    
    public ConfigManager getConfigM() {
        return this.config;
    }
}
