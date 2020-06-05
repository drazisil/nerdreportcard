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

package net.jwebnet.nerdreportcard.database;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import net.jwebnet.nerdreportcard.ReportRecord;

/**
 * Database interface. Provide a common API for data storage and retrieval.
 * 
 * @author Matthew Green
 */
public interface Database {
    /**
     * Initialise the database.
     * 
     * @return boolean
     * True if initialisation succeeds, false otherwise.
     */
    public boolean initialise();
    
    /**
     * Retrieve a report record by the reportID.
     * 
     * @return ReportRecord
     * The record with the specified ID, or null if it does not exist.
     * 
     * @param reportId
     * The ID of the report to fetch.
     */
    public ReportRecord getReport(Integer reportId);
    
    /**
     * Retrieve all report records for a username.
     * 
     * @return List<ReportRecord>
     * A list of records for the specified username.
     * 
     * @param uuid
     * The uuid to fetch records for.
     */
    public List<ReportRecord> getReports(UUID uuid);
    
    /**
     * Retrieve the number of points a user has.
     * 
     * @return int
     * The total number of points the specified user has.
     * 
     * @param uuid
     * The uuid of the user to fetch the total points for.
     */
    public int getPoints(UUID uuid);
    
    /**
     * Add a new record to the database.
     * 
     * @param record
     * The record to add to the database.
     * 
     * @throws IOException
     * Throws an IOException if an error occurred writing to the database.
     */
    public void addReport(ReportRecord record) throws IOException;
    
    /**
     * Edit a record in the database.
     * 
     * @param record
     * The edited report to add to the database. This report will overwrite
     * a report with the same ID already in the database.
     * 
     * @throws IOException 
     * Throws an IOException if an error occurred writing to the database.
     */
    public void editReport(ReportRecord record) throws IOException;
    
    /**
     * Remove a record from the database.
     * 
     * @param reportId
     * The ID of the report to remove from the database.
     * 
     * @throws IOException
     * Throws an IOException if an error occurred writing to the database.
     */
    public void deleteReport(Integer reportId) throws IOException;
}
