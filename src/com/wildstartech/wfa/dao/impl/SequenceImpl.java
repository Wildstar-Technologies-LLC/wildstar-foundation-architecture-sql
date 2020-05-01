/* ============================================================================= 
 * Copyright (c) 2007 Wildstar Technologies, LLC.  All rights reserved.
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
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wildstartech.wfa.dao.PersistenceManager;
import com.wildstartech.wfa.dao.PersistenceManagerFactory;
import com.wildstartech.wfa.dao.Sequence;
import com.wildstartech.wfa.dao.SequenceDAO;

public class SequenceImpl implements Sequence {
	//*************************************************************************
	//* BEGIN STATIC FIELD DECLARATIONS
	//*************************************************************************
	/** Log reference */
	private static Log log=LogFactory.getLog(Sequence.class);
	// QUERY: INSERT
	private final static String QUERY_INSERT=
		"INSERT INTO SEQUENCE (POID, CREATED_BY, DATE_CREATED, MODIFIED_BY,"+
		"DATE_MODIFIED,NAME,NEXT_VALUE) VALUES (?,?,?,?,?,?,?,)";
	// QUERY: UPDATE
	private final static String QUERY_UPDATE=
		"UPDATE SEQUENCE SET MODIFIED_BY=?, DATE_MODIFIED=?, NAME=?, " +
		"NEXT_VALUE=? WHERE POID=?";
	// QUERY: UPDATE SEQUENCE VALUE
	private final static String QUERY_UPDATE_NEXT_VALUE=
		"UPDATE SEQUENCE SET NEXT_VALUE=? WHERE NAME=?";
	// QUERY: SELECT FOR UPDATE
	private final static String QUERY_SELECT_FOR_UPDATE=
		"SELECT NEXT_VALUE FROM SEQUENCE WHERE NAME=? FOR UPDATE";
	// Persistence Manager
	private static PersistenceManager pm=
		PersistenceManagerFactory.getInstance().getPersistenceManager();
	// SequenceDAO 
	private static SequenceDAO sequenceDAO=pm.getSequenceDAO();
	//*************************************************************************
	//* END STATIC FIELD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN INSTANCE FIELD DECLARATIONS
	//*************************************************************************
	/** Unique identifier for a key in the database. */
	private String key;
	/** Map for storing property information. */
	private SortedMap<String, Object> propertyMap=null;
	//*************************************************************************
	//* END INSTANCE FIELD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN CONSTRUCTOR DECLARATIONS
	//*************************************************************************
	/**
	 * Over-ride default constructor to ensure proper instantiation.
	 */
	private SequenceImpl() {
		// NO-OP
	}
	/**
	 * Sequence returns a long sequential value.
	 */
	public SequenceImpl(String key) {
		log.trace("[BEGIN]");
		this.key=key;
		this.propertyMap=new TreeMap<String,Object>();
		log.trace("[END]");
	}
	//*************************************************************************
	//* END CONSTRUCTOR DECLARATIONS
	//*************************************************************************	
	//*************************************************************************
	//* BEGIN STATIC METHOD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN STATIC METHOD DECLARATIONS
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
	public Object getNextValue() {
		log.trace("[BEGIN]");
		int numRows=0;
		long nextValueNumber=Long.MIN_VALUE;
		Connection con=null;
		PreparedStatement pStmt=null;
		ResultSet rs=null;
		
		// Reset the NEXT_VALUE
		try {
			con=((SequenceDAOImpl)sequenceDAO).getConnection();
			// Disable auto-commit for the transaction.
			con.setAutoCommit(false);
			// Prepare the statement...
			pStmt=con.prepareStatement(QUERY_SELECT_FOR_UPDATE);
			// Set the Name of the Sequence
			pStmt.setString(1, this.key);
			rs=pStmt.executeQuery();		// Execute the query
			rs.next();
			// Get the value fo the NEXT_VALUE column
			nextValueNumber=rs.getLong("NEXT_VALUE");
			rs.close();
			pStmt.close();
			// Update the sequence value
			pStmt=con.prepareStatement(QUERY_UPDATE_NEXT_VALUE);
			pStmt.setLong(1,nextValueNumber+1);
			pStmt.setString(2,key);
			// UPDATE the NEXT_VALUE
			numRows=pStmt.executeUpdate();
			if (numRows==1) {
				if (log.isTraceEnabled()) {
					log.trace("Sequence: \""+key+"\" NEXT_VALUE incremented "+
							"to: "+nextValueNumber);
				} // END if (log.isTraceEnabled())
				// Commit the transaction
				con.commit();
			} else {
				if (log.isErrorEnabled()) {
					StringBuilder msg=new StringBuilder(80);
					msg.append("There was a problem updating the NEXT_VALUE ");
					msg.append("for the sequence ");
					msg.append(key);
					msg.append(".\nThe UPDATE statement updated ");
					msg.append(numRows);
					msg.append(" rows.");
					log.error(msg.toString());
					msg=null;
				} // END if (log.isErrorEnabled()) 
				// Roll back the transaction
				con.rollback();
			} // END if (numRows==1)
		} catch (SQLException ex) {
			log.error("SQLException thrown incrementing NEXT_VALUE.",ex);
			if (con != null) {
				try {
					con.close();
				} catch (SQLException ex1) {
					StringBuilder msg=new StringBuilder(80);
					msg.append("SQLException thrown while attempting to ");
					msg.append("close the connection.");
					log.error(msg.toString(),ex1);
				} // END try/catch
			} // END if (con != null)
		} // END try/catch
		// Log information out
		if (log.isTraceEnabled()) {
			log.trace("Generated NEXT_VALUE: "+nextValueNumber);			
		} // END if (log.isTraceEnabled())
		log.trace("[END]");
		return nextValueNumber;
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
