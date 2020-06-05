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
package net.jwebnet.nerdreportcard;

import net.jwebnet.nerdreportcard.database.Database;
import net.jwebnet.nerdreportcard.utils.UUIDFetcher;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static java.lang.Integer.parseInt;
import static net.jwebnet.nerdreportcard.i18n.I18n.tl;

/**
 * @author Matthew Green
 */
public class ReportCommands implements CommandExecutor {

    private final NerdReportCard plugin;
    private final Database database;

    public ReportCommands(NerdReportCard plugin) {
        this.plugin = plugin;
        database = plugin.getReportDatabase();
    }

    /**
     * @param sender The player typing the command
     * @param cmd    The command types
     * @param label  Unknown
     * @param args   everything else :)
     * @return ReportCard
     */
    public boolean onCommand(CommandSender sender, Command cmd, String label,
                             String[] args) {

        // Add a report card
        if (cmd.getName().equalsIgnoreCase("rcadd")) {
            return cmdAdd(sender, cmd, label, args);

        }

        // Edit a report card
        if (cmd.getName().equalsIgnoreCase("rcedit")) {
            return cmdEdit(sender, cmd, label, args);
        }

        // Reload config
        if (cmd.getName().equalsIgnoreCase("rcreload")) {
            return cmdReload(sender, cmd, label, args);

        }

        // List a report card
        if (cmd.getName().equalsIgnoreCase("rclist")) {
            return cmdList(sender, cmd, label, args);
        }

        // List a report card by id
        if (cmd.getName().equalsIgnoreCase("rcid")) {
            return cmdId(sender, cmd, label, args);
        }

        // Delete a report card by id
        if (cmd.getName().equalsIgnoreCase("rcremove")) {
            return cmdRemove(sender, cmd, label, args);
        }

        return true;
    }

    private boolean checkPermArgs(CommandSender sender, String permission,
                                  int min_args, int arg_len) {
        boolean success = true;

        /*
         * Check permission
         */
        if (!sender.hasPermission(permission)) {
            success = false;
            sender.sendMessage("You do not have permission to do this!");
        }

        /*
         * Check enough arguments were specified.
         */
        if (success) {
            if (arg_len < min_args) {
                success = false;
                sender.sendMessage("Not enough arguments specified!");
            }
        }

        return success;
    }

    private UUID playerNameToUUID(String playerName) {
        Player onlinePlayers[] = plugin.getServer().getOnlinePlayers();
        UUID playerUUID = null;

        for (Player player : onlinePlayers) {
            if (player.getName().toLowerCase().equals(
                    playerName.toLowerCase())) {
                playerUUID = player.getUniqueId();
            }
        }

        if (playerUUID == null) {
            try {
                playerUUID = UUIDFetcher.getUUIDOf(playerName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return playerUUID;
    }

    private Integer argsToReportId(String[] args, CommandSender sender) {
        Integer reportId = 0;

        // Check if valid id
        if (!args[0].startsWith("#")) {
            sender.sendMessage(tl("errReportIdInvalidPrefix"));
        } else {
            try {
                reportId = parseInt(args[0].substring(1));
            } catch (NumberFormatException err) {
                sender.sendMessage(tl("errReportIdNotANumber"));
            }
        }

        return reportId;
    }

    private ReportRecord argsToReport(String[] args, int offset,
                                      String reporterName) {
        ReportRecord report;
        int i = offset;
        int warningPoints = 0;
        StringBuilder sb = new StringBuilder();
        String reason;

        try {
            warningPoints = parseInt(args[i]);
        } catch (NumberFormatException exception) {
            i--;
        }
        i++;

        String playerName = args[i];
        UUID playerUUID = playerNameToUUID(playerName);
        UUID reporterUUID = playerNameToUUID(reporterName);
        i++;

        // Check if valid player
        //if (Bukkit.getOfflinePlayer(playerName).hasPlayedBefore() == false) {
        //    sender.sendMessage(tl("errPlayerNotSeenOnServer"));
        //    return true;
        //}
        // Get reason
        for (; i < args.length; i++) {
            sb.append(args[i]);
            sb.append(" ");
        }
        reason = sb.toString();
        reason = reason.trim();

        report = new ReportRecord(playerName, playerUUID, reporterName,
                reporterUUID, warningPoints, reason);

        return report;
    }

    private Boolean cmdAdd(CommandSender sender, Command cmd, String label,
                           String[] args) {
        boolean success;
        ReportRecord report;
        int i = 0;
        String playerName;
        UUID playerUUID;
        int warningPoints = 0;
        StringBuilder sb = new StringBuilder();
        String reason;

        success = checkPermArgs(sender, "nerdreportcard.edit", 2, args.length);

        if (success) {
            playerName = args[0];
            playerUUID = playerNameToUUID(playerName);
            UUID reporterUUID = playerNameToUUID(sender.toString());
            i++;

            try {
                warningPoints = parseInt(args[i]);
            } catch (NumberFormatException exception) {
                i--;
            }
            i++;

            // Get reason
            for (; i < args.length; i++) {
                sb.append(args[i]);
                sb.append(" ");
            }
            reason = sb.toString();
            reason = reason.trim();

            report = new ReportRecord(playerName, playerUUID, sender.getName(),
                    reporterUUID, warningPoints, reason);

            try {
                database.addReport(report);
            } catch (IOException e) {
                success = false;
            }

            if (success) {
                sender.sendMessage(tl("reportAddSuccess"));
            }
        }

        return success;
    }

    private Boolean cmdEdit(CommandSender sender, Command cmd, String label,
                            String[] args) {
        boolean success;
        Integer reportId = 0;
        int i;
        int warningPoints = 0;
        StringBuilder sb = new StringBuilder();
        String reason = null;
        ReportRecord report = null;

        success = checkPermArgs(sender, "nerdreportcard.edit", 2, args.length);

        if (success) {
            reportId = argsToReportId(args, sender);
            if (reportId == 0) {
                success = false;
            }
        }

        if (success) {
            i = 1;
            try {
                warningPoints = parseInt(args[i]);
            } catch (NumberFormatException exception) {
                i--;
            }
            i++;

            // Get reason
            for (; i < args.length; i++) {
                sb.append(args[i]);
                sb.append(" ");
            }
            reason = sb.toString();
            reason = reason.trim();

            report = database.getReport(reportId);
            if (report == null) {
                // No record found by that id
                sender.sendMessage(tl("errReportIdNotFound"));
                success = false;
            }
        }

        if (success) {
            report.reason = reason;
            report.setPoints(warningPoints);

            try {
                database.editReport(report);
            } catch (IOException e) {
                success = false;
            }

            if (success) {
                sender.sendMessage(tl("reportEditSuccess"));
            }
        }

        return success;
    }

    private Boolean cmdReload(CommandSender sender, Command cmd, String label,
                              String[] args) {
        if (sender.hasPermission("nerdreportcard.admin")) {
            plugin.reloadConfig();
            plugin.i18n.updateLocale("en");
            sender.sendMessage(tl("reloadSuccess"));
        }

        return true;
    }

    private Boolean cmdList(CommandSender sender, Command cmd, String label,
                            String[] args) {
        String playerName;
        UUID playerUUID;
        boolean success = true;

        if (args.length > 0 &&
                sender.hasPermission("nerdreportcard.list.others")) {
            playerName = args[0];
        } else {
            // Sender can only list themselves
            playerName = sender.getName();
        }

        playerUUID = playerNameToUUID(playerName);
        if (playerUUID == null) {
            success = false;
        }

        // View a report card
        if (success && sender.hasPermission("nerdreportcard.admin")) {
            // Sender is allowed to see all data and search for others

            List<ReportRecord> reports = database.getReports(playerUUID);

            if (reports.isEmpty()) {
                sender.sendMessage(tl("errNoReportsFound"));
                return true;
            }
            sender.sendMessage(tl("reportsFound", reports.size()));

            sender.sendMessage(tl("reportFullTop", playerName));
            for (ReportRecord r : reports) {
                if (r.active) {
                    sender.sendMessage(tl("reportLineFull", r.reportId,
                            r.getPoints(), r.reason, r.reporterName,
                            r.getTimeString()));
                }
            }
            sender.sendMessage(tl("reportFullBottom", playerName));
        } else if (success && sender.hasPermission("nerdreportcard.list")) {
            // Sender is only allowed to see id and reason and their own
            List<ReportRecord> reports = database.getReports(playerUUID);

            if (reports.isEmpty()) {
                sender.sendMessage(tl("errNoReportsFound"));
                return true;
            }
            sender.sendMessage(tl("reportsFound", reports.size()));

            for (ReportRecord r : reports) {
                if (r.active) {
                    sender.sendMessage(tl("reportLineLite", r.reportId,
                            r.reason));

                }
            }
        } else {
            sender.sendMessage(tl("nameUUIDTranslate", playerName));
        }

        return success;
    }

    private Boolean cmdId(CommandSender sender, Command cmd, String label,
                          String[] args) {
        if (sender.hasPermission("nerdreportcard.admin")) {
            if (args.length > 0) {
                // Check if valid id
                Integer reportId = argsToReportId(args, sender);

                ReportRecord record = database.getReport(reportId);
                if (record == null) {
                    // No record found by that id
                    sender.sendMessage(tl("errReportIdNotFound"));
                    return false;
                }
                sender.sendMessage(tl("reportIdTop", sender.getName()));
                sender.sendMessage(tl("reportLineFull", record.reportId,
                        record.getPoints(), record.reason, record.reporterName,
                        record.getTimeString()));
            }
        }
        return true;
    }

    private Boolean cmdRemove(CommandSender sender, Command cmd, String label,
                              String[] args) {
        boolean success;
        Integer reportId;

        success = checkPermArgs(sender, "nerdreportcard.admin", 1, args.length);

        if (success) {
            reportId = argsToReportId(args, sender);

            ReportRecord record = database.getReport(reportId);
            if (record == null) {
                // No record found by that id
                sender.sendMessage(tl("errReportIdNotFound"));
            } else {
                try {
                    database.deleteReport(reportId);
                } catch (IOException e) {
                    success = false;
                }
                if (success) {
                    sender.sendMessage(tl("reportDeleted", reportId));
                }
            }
        }

        return success;
    }
}
