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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Class representing a single report.
 *
 * @author Matthew Green
 */
public class ReportRecord {

    public Integer reportId = INVALID_REPORT_ID;
    public String reason = null;
    private Integer warningPoints = 0;
    private UUID playerUUID = null;
    private UUID reporterUUID = null;
    public final String playerName;
    public final String reporterName;
    public final boolean active;
    private final Date reportTime;

    private final ReportType reportType;
    private final static Integer INVALID_REPORT_ID = 0;
    private final static String DATE_FORMAT_STR = "yy-MM-dd HH:mm zzz";

    private enum ReportType {

        LEGACY,
        Warning,
        Mute,
        Kick,
        TempBan,
        PermaBan,
    }

    public ReportRecord(String playerName, UUID playerUUID, String reporterName,
            UUID reporterUUID, Integer warningPoints, String reportReason) {
        this.reportId = INVALID_REPORT_ID;
        this.playerName = playerName;
        this.playerUUID = playerUUID;
        this.reporterName = reporterName;
        this.playerUUID = reporterUUID;
        this.reportType = ReportType.LEGACY;
        this.reason = reportReason;
        this.reportTime = new Date();
        this.warningPoints = warningPoints;
        this.active = true;
    }
    
    public ReportRecord(Integer reportId, String playerName, UUID playerUUID,
            String reporterName, UUID reporterUUID, String reportReason,
            String reportTime, Integer warningPoints, boolean active) {
        this.reportId = reportId;
        this.playerName = playerName;
        this.playerUUID = playerUUID;
        this.reporterName = reporterName;
        this.reporterUUID = reporterUUID;
        this.reportType = ReportType.LEGACY;
        this.reason = reportReason;
        this.reportTime = timeStringToDate(reportTime);
        this.warningPoints = warningPoints;
        this.active = active;
    }
    
    public ReportRecord(Integer reportId, String playerName, UUID playerUUID,
            String reporterName, UUID reporterUUID, String reportReason,
            Integer reportTime, Integer warningPoints, boolean active) {
        this.reportId = reportId;
        this.playerName = playerName;
        this.playerUUID = playerUUID;
        this.reporterName = reporterName;
        this.reporterUUID = reporterUUID;
        this.reportType = ReportType.LEGACY;
        this.reason = reportReason;
        this.reportTime = new Date((long) reportTime * 1000);
        this.warningPoints = warningPoints;
        this.active = active;
    }
    
    /**
     * Set the player UUID.
     * 
     * @param uuid
     * The uuid to set for the player.
     */
    public void setPlayerUUID (UUID uuid) {
        this.playerUUID = uuid;
    }
    
    /**
     * Retreive the player UUID.
     * 
     * @return UUID
     * The player UUID.
     */
    public UUID getPlayerUUID() {
        return this.playerUUID;
    }
    
    /**
     * Retrieve the string representation of the player UUID.
     * 
     * @return String
     * String representation of the player UUID.
     */
    public String getPlayerUUIDStr() {
        String uuidStr = null;
        if (this.playerUUID != null) {
            uuidStr = this.playerUUID.toString();
        }
        return uuidStr;
    }
    
    /**
     * Set the reporter UUID.
     * 
     * @param uuid
     * The uuid to set for the player.
     */
    public void setReporterUUID (UUID uuid) {
        this.reporterUUID = uuid;
    }
    
    /**
     * Retrieve the reporter UUID.
     * 
     * @return UUID
     * The reporter UUID
     */
    public UUID getReporterUUID() {
        return this.reporterUUID;
    }
    
    /**
     * Retrieve the string representation of the reporter UUID.
     * 
     * @return String
     * String representation of the reporter UUID.
     */
    public String getReporterUUIDStr() {
        String uuidStr = null;
        if (this.reporterUUID != null) {
            uuidStr = this.reporterUUID.toString();
        }
        return uuidStr;
    }

    /**
     * Set the warning points for the report.
     *
     * @param points The points to set for the report.
     */
    public void setPoints(Integer points) {
        warningPoints = points;
    }

    /**
     * Retrieve the number of warning points for this report.
     *
     * @return Integer
     * The warning points for the report.
     */
    public Integer getPoints() {
        Integer points;
        /*
         * If the warning points are set to zero, use the config to calculate
         * the warning points for this report.
         *
         * Otherwise, just return the custom number of points.
         */
        if (warningPoints == 0) {
            points = calculatePoints();
        } else {
            points = warningPoints;
        }

        return points;
    }

    /**
     * Retrieve the time this report was entered, in UTC.
     *
     * @return String String-formatted time of the report.
     */
    public String getTimeString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_STR);

        return dateFormat.format(reportTime);
    }

    private Date timeStringToDate(String timestring) {
        Date date;
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_STR);

        try {
            date = dateFormat.parse(timestring);
        } catch (ParseException e) {
            /*
             Maybe the date is in the old format.
             */
            dateFormat.applyPattern("MMM d, yyyy h:mm:ss a zzz");
            try {
                date = dateFormat.parse(timestring);
            } catch (ParseException f) {
                /*
                 Or the format that didn't last long.
                 */
                dateFormat.applyPattern("yy-MM-dd HH:mm");
                try {
                    date = dateFormat.parse(timestring);
                } catch (ParseException g) {
                    date = new Date(0);
                }
            }
        }

        return date;
    }

    /**
     * Retreive the time this report was entered, in seconds since the Epoch.
     *
     * @return int Seconds since the epoch.
     */
    public int getTime() {
        return ((int) (reportTime.getTime() / 1000));
    }

    /*
     * Calculate the number of warning points for this report.
     */
    private Integer calculatePoints() {
        Integer points = 0;

        return points;
    }
}
