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
package com.wildstartech.wfa.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wildstartech.wfa.dao.DateSequence;
import com.wildstartech.wfa.dao.DateSequenceDAO;
import com.wildstartech.wfa.dao.PersistenceManager;
import com.wildstartech.wfa.dao.PersistenceManagerFactory;


public class DateSequenceImpl implements DateSequence {
	//*************************************************************************
	//* BEGIN STATIC FIELD DECLARATIONS
	//*************************************************************************
	private static Log log=LogFactory.getLog(DateSequenceImpl.class);
	// QUERY: INSERT
	private final static String QUERY_INSERT=
		"INSERT INTO SEQUENCE_DATE (POID, CREATED_BY, DATE_CREATED, " +
		"MODIFIED_BY, DATE_MODIFIED, NAME, FORMAT, NEXT_VALUE, " +
		"RESET_DATE, RESET_INTERVAL) VALUES (?,?,?,?,?,?,?,?,?,?)";
	// QUERY: UPDATE
	private final static String QUERY_UPDATE=
		"UPDATE SEQUENCE_DATE SET MODIFIED_BY=?, DATE_MODIFIED=?, " +
		"NAME=?, FORMAT=?, NEXT_VALUE=?, RESET_DATE=?, RESET_INTERVAL=? " +
		"WHERE POID=?";
	// QUERY: UPDATE THE SEQUENCE VALUE
	private final static String QUERY_UPDATE_NEXT_VALUE=
		"UPDATE SEQUENCE_DATE SET NEXT_VALUE=? WHERE NAME=?";
	// QUERY: UPDATE THE RESET_DATE PROPERTY
	private final static String QUERY_UPDATE_RESET_DATE=
		"UPDATE SEQUENCE_DATE SET RESET_DATE=? WHERE NAME=?";
	// QUERY: SELECT FOR UPDATE
	private final static String QUERY_SELECT_FOR_UPDATE=
		"SELECT FORMAT, NEXT_VALUE, RESET_DATE, RESET_INTERVAL " +
		"FROM SEQUENCE_DATE WHERE NAME=? FOR UPDATE";
	private static PersistenceManager pm=
		PersistenceManagerFactory.getInstance().getPersistenceManager();
	private static DateSequenceDAO dateSequenceDAO=
		pm.getDateSeqeuenceDAO();
	//*************************************************************************
	//* END STATIC FIELD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN INSTANCE FIELD DECLARATIONS
	//*************************************************************************
	private SortedMap<String, Object> propertyMap=null;
	private DateFormat dateFormat=null;
	private String key=null;
	private String prefix=null;
	//*************************************************************************
	//* END INSTANCE FIELD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN CONSTRUCTOR DECLARATIONS
	//*************************************************************************
	/**
	 * Over-ride default constructor to ensure proper instantiation.
	 */
	private DateSequenceImpl() {
		// NO-OP
	}
	/**
	 * DateSequence returns a date-based sequential value.
	 */
	public DateSequenceImpl(String key, String format) {
		log.trace("[BEGIN]");
		this.dateFormat=new SimpleDateFormat("yyyyMMdd");
		this.key=key;
		
		this.propertyMap=new TreeMap<String,Object>();
		this.propertyMap.put(PROPERTY_FORMAT,format);
		this.prefix=dateFormat.format(new Date());
		log.trace("END");
	}
	//*************************************************************************
	//* END CONSTRUCTOR DECLARATIONS
	//*************************************************************************	
	//*************************************************************************
	//* BEGIN STATIC METHOD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* END STATIC METHOD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN ACCESSOR METHOD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* END ACCESSOR METHOD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN OTHER METHOD DECLARATIONS
	//*************************************************************************
	/**
	 * @see com.wildstartech.justo.crm.dao.sql.DateSequence#getNextValue()
	 */
	public String getNextValue() {
		log.trace("[BEGIN]");
		int numRows;
		long nextValueNumber=9999;
		Calendar calendar=null;
		Connection con=null;
		Date currentDate=null;
		Date resetDate=null;
		PreparedStatement pStmt=null;
		ResultSet rs=null;
		String newPrefix=null;
		StringBuilder nextValue=null;
		
		// Get the current date/time...
		currentDate=new Date();
		// Compare the prefixes
		newPrefix=this.dateFormat.format(new Date());
		if (newPrefix.compareTo(prefix)!=0) {
			// Make sure no other thread is attempting to 
			// set the value to the prefix value at the same time.
			synchronized (this.prefix) {
				this.prefix=newPrefix;
			}
			// Reset the NEXT_VALUE value
			try {
				con=((DateSequenceDAOImpl)dateSequenceDAO).getConnection();
				pStmt=con.prepareStatement(QUERY_UPDATE_NEXT_VALUE);
				pStmt.setLong(1,1);
				pStmt.setString(2, key);
				numRows=pStmt.executeUpdate();
				pStmt.close();
			} catch (SQLException ex) {
				StringBuilder msg=new StringBuilder(80);
				msg.append("SQLException thrown while trying to update the ");
				msg.append("NEXT_VALUE for the sequence.");
				log.error(msg.toString(),ex);
			} finally {
				if (con != null) {
					try {
						con.close();	
					} catch (SQLException ex) {
						log.error("This shouldn't happen.",ex);
					}
				} // END if (con != null)
			} // END try/catch
			// END of resetting the NEXT_VALUE value
		} // END if (newPrefix.compareTo(prefix)!=0)
		// Let's get the next sequence value
		try {
			con=((DateSequenceDAOImpl)dateSequenceDAO).getConnection();
			// Disable auto-commit for the transaction.
			con.setAutoCommit(false);
			pStmt=con.prepareStatement(QUERY_SELECT_FOR_UPDATE);
			// Set the Name of the Date Sequence.
			pStmt.setString(1,key);
			rs=pStmt.executeQuery();	// Execute the query
			rs.next();
			// Get the value of the NEXT_VALUE column
			nextValueNumber=rs.getLong("NEXT_VALUE");
			// Get the value of the REST_DATE column
			resetDate=new Date(rs.getTimestamp("RESET_DATE").getTime());
			rs.close();
			pStmt.close();
			// Should sequence be reset?  Check the reset date
			if (resetDate.compareTo(currentDate) < 0) {
				// The date to reset is earlier than the current date/time...
				calendar=new GregorianCalendar();
				calendar.setTime(currentDate);
				// Add a day to the calendar,
				calendar.add(Calendar.DAY_OF_MONTH,1);
				// And make sure the time is midnight
				calendar.set(Calendar.HOUR_OF_DAY,0);
				calendar.set(Calendar.MINUTE, 0);
				calendar.set(Calendar.SECOND, 0);
				calendar.set(Calendar.MILLISECOND,0);
				pStmt=con.prepareStatement(QUERY_UPDATE_RESET_DATE);
				pStmt.setTimestamp(1,new Timestamp(calendar.getTimeInMillis()));
				pStmt.setString(2, key);
				numRows=pStmt.executeUpdate();
				// Set the NEXT_VALUE equal to 1
				nextValueNumber=1;
			} // END if (resetDate.compareTo(currentDate) < 0)
			// Update the sequence value.
			pStmt=con.prepareStatement(QUERY_UPDATE_NEXT_VALUE);
			pStmt.setLong(1, nextValueNumber+1);
			pStmt.setString(2, key);
			// UPDATE THE NEXT_VALUE
			numRows=pStmt.executeUpdate();
			if (numRows==1) {
				if (log.isTraceEnabled()) {
					StringBuilder msg=new StringBuilder(80);
					msg.append("Sequence: ");
					msg.append(key);
					msg.append(" incremented from: ");
					msg.append(nextValueNumber);
					msg.append(" to ");
					msg.append(nextValueNumber+1);
					log.trace(msg.toString());
					msg=null;
				} // END if (log.isTraceEnabled())
				// Commit the transaction.
				con.commit();
			} else {
				if (log.isErrorEnabled()) {
					StringBuilder msg=new StringBuilder(80);
					msg.append("There was a problem updating the nextID for ");
					msg.append("the sequence \"");
					msg.append(key);
					msg.append("\".  The UPDATE statement updated ");
					msg.append(numRows);
					msg.append(" rows.");
					log.error(msg.toString());
					msg=null;
				} // END if (log.isErrorEnabled())
				// Roll the transaction back.
				con.rollback();
			} // END if (numRows==1)
		} catch (SQLException ex) {
			log.trace("SQLException thrown incrementing NEXT_VALUE.",ex);
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException ex) {
					StringBuilder msg=new StringBuilder(80);
					msg.append("SQLException thrown while attempting to close");
					msg.append(" the connection.");
					log.error(msg.toString(),ex);
					msg=null;
				} // END try/catch
			} // END if (con != null)
		}
		nextValue=new StringBuilder(20);
		nextValue.append(nextValueNumber);
		if (nextValue.length() < 4) {
			while (nextValue.length() < 4) {
				nextValue.insert(0,"0");
			} // END while (nextValue.length() < 4)
		} // END if (nextValue.length() < 4)
		nextValue.insert(0, prefix);
		if (log.isTraceEnabled()) {
			StringBuilder msg=new StringBuilder(80);
			msg.append("Generated Next Value: ");
			msg.append(nextValue.toString());
			msg.append("\n[END]");
			log.trace(msg.toString());
		} // END if (log.isTraceEnabled())
		return nextValue.toString();
	}
	/**
	 * Return the requested property.
	 */
	public Object getProperty(String key) {
		if (log.isTraceEnabled()) {
			StringBuilder msg=new StringBuilder(80);
			msg.append("[BEGIN]\n");
			msg.append("Requested Property: ");
			msg.append(key);
			msg.append("\nRequested property ");
			if (!propertyMap.containsKey(key)) {
				msg.append("not ");
			}
			msg.append("found.\n[END]");
			log.trace(msg.toString());
		} // END if (log.isTraceEnabled())
		Object property=null;
		property=propertyMap.get(key);
		return property;
	}
	/**
	 * 
	 */
	public void setProperty(String key, Object value) {
		if (log.isTraceEnabled()) {
			StringBuilder msg=new StringBuilder(80);
			msg.append("[BEGIN]\nProperty Key: ");
			msg.append(key);
			msg.append("\nProperty Value: ");
			msg.append(value.toString());
			log.trace(msg.toString());
		} // END if (log.isTraceEnabled())
		propertyMap.put(key,value);
		log.trace("[END]");
	}
	//*************************************************************************
	//* END OTHER METHOD DECLARATIONS
	//*************************************************************************
}