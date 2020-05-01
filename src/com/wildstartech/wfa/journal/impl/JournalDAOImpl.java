/* ============================================================================= 
 * Copyright (c) 2006 Wildstar Technologies, LLC.  All rights reserved.
 *
 * This file is part of the Justo Delivery.
 *
 * Justo Delivery is free software; you can redistribute it  
 * and/or modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 * 
 * Justo Delivery is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Justo Delivery; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *   
 * =============================================================================
 */
package com.wildstartech.wfa.journal.impl;

import com.wildstartech.wfa.journal.dao.*;
import com.wildstartech.wfa.journal.JournalDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wildstartech.wfa.journal.JournalCategoryTooLongException;
import com.wildstartech.wfa.journal.JournalDescriptionTooLongException;
import com.wildstartech.wfa.journal.JournalEntry;
import com.wildstartech.wfa.dao.DAOException;
import com.wildstartech.wfa.dao.WildDAO;
import com.wildstartech.wfa.dao.impl.WildDAOImpl;
import com.wildstartech.wfa.dao.user.UserContext;

public class JournalDAOImpl extends WildDAOImpl implements JournalDAO {
	//*************************************************************************
	//* BEGIN STATIC FIELD DECLARATIONS
	//*************************************************************************
	// Log
	private static Log log=LogFactory.getLog(JournalDAO.class);
	
	// SQL QUERY FOR INSERT
	private static String QUERY_INSERT=
		"INSERT INTO JOURNALS (POID, CREATED_BY, DATE_CREATED, MODIFIED_BY, "+
		"DATE_MODIFIED, RELATED_OBJECT_TYPE, RELATED_OBJECT_POID, ENTRY_DATE, "+
		"MIME_TYPE, CATEGORY, DESCRIPTION) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
	// SQL QUERY FOR UPDATE
	private static String QUERY_UPDATE=
		"UPDATE JOURNALS SET MODIFIED_BY=?, DATE_MODIFIED=?, "+
		"RELATED_OBJECT_TYPE=?, RELATED_OBJECT_POID=?, ENTRY_DATE=?, " +
		"MIME_TYPE=?, CATEGORY=?, DESCRIPTION=? WHERE POID=?";
	// SQL QUERY FOR DELETE
	private static String QUERY_DELETE="DELETE FROM JOURNALS WHERE POID=?";
	// SQL QUERY FOR ALL ENTRIES RELATED TO THE OBJECT
	private static String QUERY_DELETE_ALL=
		"DELETE FROM JOURNALS WHERE RELATED_OBJECT_TYPE=? AND " +
		"RELATED_OBJECT_POID=?";
	// Base SQL query for SELECT
	private static String QUERY_SELECT_BASE=
		"SELECT POID, CREATED_BY, DATE_CREATED, MODIFIED_BY, DATE_MODIFIED, "+
		"RELATED_OBJECT_TYPE, RELATED_OBJECT_POID, ENTRY_DATE, MIME_TYPE, "+
		"CATEGORY, DESCRIPTION FROM JOURNALS ";
	// SQL query for SELECT by Primary Key
	private static String QUERY_SEARCH_PRIMARY_KEY=
		QUERY_SELECT_BASE+" WHERE POID=? ORDER BY POID";
	// SQL query for all journal entries for an object;
	private static String QUERY_SEARCH_BY_OBJECT=QUERY_SELECT_BASE+
		" WHERE RELATED_OBJECT_TYPE=? AND RELATED_OBJECT_POID=? " +
		"ORDER BY ENTRY_DATE";
	// Singleton implementation of the Data Access Object for Company objects.
	private static JournalDAO journalDAO=new JournalDAOImpl();
	/** Used to identify the DAO */
	protected static String DAO_IDENTIFIER_KEY=
		"com.wildstartech.wfa.journal.dao.JournalDAO";
	// Private constructor enabling the Singleton design pattern.
	private JournalDAOImpl() {
		log.trace("[BEGIN]");
		log.trace("[END]");
	}
	private static JournalEntryImpl buildJournalEntry(ResultSet rs) 
	throws SQLException {
		log.trace("[BEGIN]");
		long objectPoid=Long.MIN_VALUE;
		Date tmpDate=null;
		JournalEntryImpl entry=null;
		String objectType=null;
		
		objectType=rs.getString("RELATED_OBJECT_TYPE");
		objectPoid=rs.getLong("RELATED_OBJECT_POID");
		// Create an instance of the JournalEntryImpl class.
		entry=new JournalEntryImpl(objectType, objectPoid);
		// Populate the JournalEntry with data
		setWildObjectData(entry,rs);

		// ENTRY_DATE
		tmpDate=new Date(rs.getTimestamp("ENTRY_DATE").getTime());
		entry.setEntryDate(tmpDate);
		entry.setMimeType(rs.getString("MIME_TYPE"));
		// CATEGORY
		try {
			entry.setCategory(rs.getString("CATEGORY"));
		} catch (JournalCategoryTooLongException ex) {
			log.error("This should never happen.",ex);
		}
		// DESCRIPTION
		try {
			entry.setDescription(rs.getString("DESCRIPTION"));
		} catch (JournalDescriptionTooLongException ex) {
			log.error("This should never happend.",ex);
		}
		log.trace("[END]");
		return entry;
	}
	/**
	 * Returns a reference to the JournalDAO.
	 * @return
	 */
	public static JournalDAO getInstance() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return journalDAO;
	}
	//********** DELETE
	/**
	 * Remove a specific journal entry from the database.
	 */
	public void delete(JournalEntry entry, UserContext ctx) 
	throws DAOException {
		log.trace("[BEGIN]");
		Connection con=null;
		
		try {
			// Obtain a connection
			con=pm.getConnection();
			// Disable auto-commit
			con.setAutoCommit(false);
			// Invoke the "save" logic
			delete((JournalEntryImpl)entry,ctx,con);
			// Commit the transaction
			con.commit();
		} catch (DAOException ex) {
			// A DAOException was thrown, so rollback the transaction.
			log.error("DAOException thrown.",ex);
			// Re-throw the DAOException
			throw ex;
		} catch (SQLException ex) {
			log.error("SQLException thrown.",ex);
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException ex) {
					// SQLException thrown closing connection
					log.error(ERR_SQLEXCEPTION_CON_CLOSE,ex);
				}
			}
		}
		log.trace("[END]");
	}
	/**
	 * 
	 */
	protected int delete(JournalEntryImpl entry, UserContext ctx, 
		Connection con) throws DAOException {
		log.trace("[BEGIN]");
		long poid;
		int numRows=0;
		PreparedStatement pStmt=null;
		
		// Validate the parameter
		checkObject(entry);
		// Get the company object's poid
		poid=entry.getPoid();
		if (poid==Long.MIN_VALUE) {
			// The Person object has not yet been saved.
			log.error("Trying to delete a Person that has not been saved.");
		} else {
			// The company object has been saved.
			try {
				// Build the query
				pStmt=con.prepareStatement(QUERY_DELETE);
				pStmt.setDouble(1,poid);
				// Execute the DELETE query
				numRows=pStmt.executeUpdate();
				// Close the statement
				pStmt.close();
			} catch (SQLException ex) {
				// Print the error information.
				log.error("SQLException thrown.",ex);
			}
		} // END if(poid==Long.MIN_VALUE)	
		log.trace("[END]");
		return numRows;
	}
	/**
	 * Removes all the journal entries associated with the specified object.
	 */
	protected int delete(String objectType, long objectPoid, 
		UserContext ctx, Connection con) throws DAOException {
		log.trace("[BEGIN]");
		int numRows=0;
		PreparedStatement pStmt=null;
		
		try {
			// Build the query
			pStmt=con.prepareStatement(QUERY_DELETE_ALL);
			pStmt.setString(1,objectType);
			pStmt.setDouble(2,objectPoid);
			// Execute the DELETE query
			numRows=pStmt.executeUpdate();
			// Close the statement
			pStmt.close();
		} catch (SQLException ex) {
			// Print the error information.
			log.error("SQLException thrown.",ex);
		} // END if(poid==Long.MIN_VALUE)
		log.trace("[END]");
		return numRows;
	}
	//********** FIND
	/**
	 * Return a specific JournalEntry
	 * @param poid
	 * @return
	 */
	protected JournalEntry findByPrimaryKey(long key, UserContext ctx)
	throws DAOException {
		if (log.isTraceEnabled()) {
			StringBuilder msg=new StringBuilder(50);
			msg.append("[BEGIN]\n");
			msg.append("Requested Key: ");
			msg.append(key);
			log.trace(msg.toString());
		} // END if (log.isTraceEnabled())
		Connection con=null;
		JournalEntryImpl entry=null;
		PreparedStatement pStmt=null;
		ResultSet rs=null;

		if (key != Long.MIN_VALUE) {
			try {
				// Get a connection
				con=pm.getConnection();
				// Prepare the statement...
				pStmt=con.prepareStatement(QUERY_SEARCH_PRIMARY_KEY);
				// Specify the poid
				pStmt.setLong(1,key);
				// Execute the statement
				rs=pStmt.executeQuery();
				// If the query returns a value...
				if (rs.next()) {
					// The resultSet contains a value.
					entry=buildJournalEntry(rs);
				} else {
					log.error("No Entry was found.");
				}
				// Close the ResultSet
				rs.close();
				// Close the PreparedStatement
				pStmt.close();
			} catch (SQLException ex) {
				StringBuilder msg=new StringBuilder(80);
				msg.append("SQLException thrown while trying to create a ");
				msg.append("Person object.");
				log.error(msg.toString(),ex);
			} finally {// Attempt to close the connection
				try {
					con.close();
				} catch (SQLException ex) {
					StringBuilder msg=new StringBuilder(80);
					msg.append("SQLException thrown trying to close the ");
					msg.append("connection.");
					log.error(msg.toString(),ex);
				}
			}
		} else {
			DAOException ex=new DAOException(ERR_POID_INVALID);
			ex.fillInStackTrace();
			throw ex;
		}
		log.trace("[END]");
		return entry;
	}
	/**
	 * 
	 * @param objectType
	 * @param objectPoid
	 * @param ctx
	 * @return
	 * @throws DAOException
	 */
	protected List<JournalEntry> findAll(String objectType, long objectPoid,
		UserContext ctx) throws DAOException {
		if (log.isTraceEnabled()) {
			StringBuilder msg=new StringBuilder(50);
			msg.append("[BEGIN]\n");
			msg.append("Object Type: ");
			msg.append(objectType);
			msg.append("\nObject POID: ");
			msg.append(objectPoid);
			log.trace(msg.toString());
		} // END if (log.isTraceEnabled())
		Connection con=null;
		JournalEntryImpl entry=null;
		List<JournalEntry> journalEntries=null;
		PreparedStatement pStmt=null;
		ResultSet rs=null;

		if ((objectType != null) && (objectPoid > Long.MIN_VALUE)) {
			try {
				// Get a connection
				con=pm.getConnection();
				// Prepare the statement...
				pStmt=con.prepareStatement(QUERY_SEARCH_BY_OBJECT);
				// Specify the objectType
				pStmt.setString(1,objectType);
				// Specify the objectPoid
				pStmt.setLong(2,objectPoid);
				// Execute the statement
				rs=pStmt.executeQuery();
				// If the query returns a value...
				journalEntries=new ArrayList<JournalEntry>();
				while (rs.next()) {
					// Go through the list of returned journal entries
					entry=buildJournalEntry(rs);
					journalEntries.add(entry);
				} // END while (rs.next()) 
				// Close the ResultSet
				rs.close();
				// Close the PreparedStatement
				pStmt.close();
			} catch (SQLException ex) {
				StringBuilder msg=new StringBuilder(80);
				msg.append("SQLException thrown while trying to create a ");
				msg.append("JournalEntry object.");
				log.error(msg.toString(),ex);
			} finally {
				// Attempt to close the connection
				try {
					con.close();
				} catch (SQLException ex) {
					StringBuilder msg=new StringBuilder(80);
					msg.append("SQLException thrown trying to close the ");
					msg.append("connection.");
					log.error(msg.toString(),ex);
				}
			}
		} else {
			DAOException ex=new DAOException(ERR_POID_INVALID);
			ex.fillInStackTrace();
			throw ex;
		} // END if ((objectType != null) && (objectPoid > Long.MIN_VALUE)) 
		
		log.trace("[END]");
		return journalEntries;
		
	}
	//********** SAVE
	/**
	 * 
	 */
	public void save(JournalEntry entry, UserContext ctx) throws DAOException {
		log.trace("[BEGIN]");
		Connection con=null;
		
		try {
			// Obtain a connection
			con=pm.getConnection();
			// Disable auto-commit
			con.setAutoCommit(false);
			// Invoke the "save" logic
			save(entry,ctx,con);
			// Commit the transaction
			con.commit();
		} catch (DAOException ex) {
			// A DAOException was thrown, so rollback the transaction.
			try {
				con.rollback();
			} catch (SQLException ex1) {
				// SQLException was thrown either during the setAutoCommit() or
				// commit() methods
				log.error("SQLException thrown during rollback.",ex1);
			}
			// Re-throw the DAOException
			throw ex;
		} catch (SQLException ex) {
			log.error("SQLException thrown.",ex);
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException ex) {
					StringBuilder msg=new StringBuilder(128);
					msg.append("SQLException thrown trying to close the");
					msg.append("connection.");
					log.error(msg.toString(),ex);
				}
			} // END if (con != null)
		}
		log.trace("[END]");
	}
	/**
	 * 
	 */
	protected void save(JournalEntry entry, UserContext ctx, Connection con) 
	throws DAOException {
		long poid;
		
		// Check the object to ensure valid parameters are passed.
		checkObject(entry);
		checkObject(ctx,WildDAO.VALIDATE_NULL);
		
		// Business rule enforcement workflow would go here.

		// Perform the actual workto save the object
		poid=((JournalEntryImpl) entry).getPoid();
		if (poid == Long.MIN_VALUE) {
			// Based upon the poid value, the object has not yet been saved.
			_create((JournalEntryImpl)entry,ctx,con);
		} else {
			// Since a valid poid value is assigned, update the existing record.
			_update((JournalEntryImpl)entry,ctx,con);
		} // END if (poid == Long.MIN_VALUE) 
	}
	/**
	 * Create a new entry for the <code>JournalEntry</code> in the database.
	 */
	private void _create(JournalEntryImpl entry, UserContext ctx, 
		Connection con) throws DAOException {
		log.trace("[BEGIN]");
		long poid;
		long currentUserPoid;
		int numRows;
		Date tmpDate=null;
		PreparedStatement pStmt=null;
		String tmpString=null;
		Timestamp currentTime=null;
		
		// Check to see if there is any data available in the journal entry
		if (entry.isDataAvailable()) {
			// Get the current user and date/time information
			currentTime=new Timestamp(System.currentTimeMillis());
			currentUserPoid=getCurrentUser(ctx);
			
			try {
				// Get the next poid
				poid=getNextPoid(con);
				// Prepare/compile the statement
				pStmt=con.prepareStatement(QUERY_INSERT);
				// Populate the prepared statement with values
				pStmt.setLong(1,poid);					// POID
				pStmt.setLong(2,currentUserPoid);		// CREATED_BY
				pStmt.setTimestamp(3,currentTime);		// DATE_CREATED
				pStmt.setLong(4,currentUserPoid);		// MODIFIED_BY
				pStmt.setTimestamp(5,currentTime);		// DATE_MODIFIED
				// RELATED_OBJECT_TYPE (6)
				pStmt.setString(6, entry.getRelatedObjectType());
				// RELATED_OBJECT_POID (7)
				pStmt.setLong(7,entry.getRelatedObjectPoid());
				// ENTRY_DATE (8)
				tmpDate=entry.getEntryDate();
				if (tmpDate==null) {
					// If the entryDate for the journal entry is blank, then
					// use the current date/time.
					tmpDate=currentTime;
				}
				pStmt.setTimestamp(8, new Timestamp(tmpDate.getTime()));
				// MIME_TYPE (9)
				pStmt.setString(9,entry.getMimeType());
				// CATEGORY (10)
				pStmt.setString(10,entry.getCategory());
				// DESCRIPTION (11)
				pStmt.setString(11, entry.getDescription());
				// Execute the INSERT query
				numRows=pStmt.executeUpdate();
				if(numRows!=1) {
					log.error("JournalDAOImpl INSERT ISSUE numRows="+numRows);
				}
				// Close the PreparedStatement
				pStmt.close();
				// Set the poid for the JournalEntry object
				entry.setPoid(poid);
			} catch (SQLException ex) {
				// Print the error information
				log.error("SQLException thrown.",ex);
			} catch (Throwable ex) {
				log.error("Error thrown.",ex);
			} 
		} // END if (entry.isDataAvailable())
		log.trace("[END]");
	}
	/**
	 * Update an existing entry for the <code>JournalEntry</code> in the database.
	 */
	private void _update(JournalEntryImpl entry, UserContext ctx, 
		Connection con) throws DAOException {
		log.trace("[BEGIN]");
		long poid;
		long currentUserPoid;
		int numRows;
		Date tmpDate=null;
		PreparedStatement pStmt=null;
		String tmpString=null;
		Timestamp currentTime=null;
	
		// Get the current user and date/time information
		currentTime=new Timestamp(System.currentTimeMillis());
		currentUserPoid=getCurrentUser(ctx);
		
		try {
			// Prepare/compile the statement
			pStmt=con.prepareStatement(QUERY_UPDATE);
			// Populate the prepared statement with values
			pStmt.setLong(1,currentUserPoid);		// MODIFIED_BY
			pStmt.setTimestamp(2,currentTime);		// DATE_MODIFIED
			// RELATED_OBJECT_TYPE (3)
			pStmt.setString(3, entry.getRelatedObjectType());
			// RELATED_OBJECT_POID (4)
			pStmt.setLong(4,entry.getRelatedObjectPoid());
			// ENTRY_DATE (5)
			tmpDate=entry.getEntryDate();
			if (tmpDate == null) {
				// If the entryDate does not contain a value, then use the
				// current date/time
				tmpDate=currentTime;
			} // END if (tmpDate == null)
			pStmt.setTimestamp(5, new Timestamp(tmpDate.getTime()));
			// MIME_TYPE (6)
			pStmt.setString(6,entry.getMimeType());
			// CATEGORY (7)
			pStmt.setString(7,entry.getCategory());
			// DESCRIPTION (8)
			pStmt.setString(8, entry.getDescription());
			// POID (9)
			pStmt.setLong(9,entry.getPoid());
			// Execute the INSERT query
			numRows=pStmt.executeUpdate();
			if(numRows!=1) {
				log.error("JournalDAOImpl UPDATE ISSUE numRows="+numRows);
			}
		} catch (SQLException ex) {
			// Print the error information
			log.error("SQLException thrown.",ex);
		} catch (Throwable ex) {
			log.error("Error thrown.",ex);
		} finally {
			// Must try to properly clean up the sql.
			try {
				// Close the PreparedStatement
				pStmt.close();
			} catch (SQLException ex1) {
				StringBuilder msg=new StringBuilder(80);
				msg.append("An SQLException was thrown while trying to close ");
				msg.append("the Prepared Statement.");
				log.error(msg.toString(),ex1);
				msg=null;
			}
		}
		log.trace("[END]");
	}
	//*************************************************************************
	//* END CRUD METHODS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN OTHER METHOD DECLARATIONS
	//*************************************************************************
	protected String getDAOIdentifierKey() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return DAO_IDENTIFIER_KEY;
	}
	//*************************************************************************
	//* END OTHER METHOD DECLARATIONS
	//*************************************************************************
}