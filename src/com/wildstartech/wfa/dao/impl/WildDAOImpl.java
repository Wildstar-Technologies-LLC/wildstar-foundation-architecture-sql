/* ============================================================================= 
 * Copyright (c) 2005 Wildstar Technologies, LLC.  All rights reserved.
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wildstartech.wfa.dao.AuthenticationException;
import com.wildstartech.wfa.dao.DAOException;
import com.wildstartech.wfa.dao.PersistenceManager;
import com.wildstartech.wfa.dao.PersistenceManagerFactory;
import com.wildstartech.wfa.dao.WildDAO;
import com.wildstartech.wfa.dao.WildObject;
import com.wildstartech.wfa.dao.user.UserContext;

public abstract class WildDAOImpl implements WildDAO {
	//*************************************************************************
	//* BEGIN STATIC FIELD DEFINITIONS
	//*************************************************************************
	/* Obtain a reference to the Log */
	private static Log log=LogFactory.getLog(WildDAO.class);
	/* PersistenceManager reference */
	protected static PersistenceManagerSQL pm=(PersistenceManagerSQL)
		PersistenceManagerFactory.getInstance().getPersistenceManager();
	/* Store a reference to the UserDAO */
	protected static UserDAOImpl userDAO=(UserDAOImpl) pm.getUserDAO();
	// Standard message for SQLException thrown while closing a connection.
	protected static String ERR_SQLEXCEPTION_CON_CLOSE=
		"A SQLException was thrown while attempting to close a connection";
	// Standard exception thrown when rolling back a transaction.
	protected static String ERR_SQLEXCEPTION_ROLLBACK=
		"A SQLException was thrown while attempting to roll back the " +
		"transaction.";
	// Standard query to be used to select the NEXT_POID value
	private static String SELECT_NEXT_POID_QUERY=
		"SELECT NEXT_POID FROM OBJECT_MASTER "+
		"WHERE DAO_IDENTIFIER=? FOR UPDATE";
	// Standard query to be used to update the NEXT_POID value
	private static String UPDATE_NEXT_POID_QUERY=
		"UPDATE OBJECT_MASTER SET NEXT_POID=? "+
		"WHERE DAO_IDENTIFIER=?";

	//*************************************************************************
	//* END STATIC FIELD DEFINITIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN CONSTRUCTOR DECLARATIONS
	//*************************************************************************
	protected WildDAOImpl() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		// Obtian a reference to the PersistenceManager
	}
	//*************************************************************************
	//* END CONSTRUCTOR DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN STATIC METHOD DECLARATIONS
	//*************************************************************************
	/**
	 * Populate the specified WildObject with "core" fields.
	 */
	protected static void setWildObjectData(WildObjectSQL obj, ResultSet rs)
	throws SQLException {
		log.trace("[BEGIN]");
		try {
			obj.setPoid(rs.getLong(1));
			obj.setCreatedBy(rs.getLong(2));
			obj.setDateChanged(new Date(rs.getTimestamp(3).getTime()));
			obj.setChangedBy(rs.getLong(4));
		} catch (SQLException ex) {
			log.error("SQLException thrown.",ex);
		}
		log.trace("[END]");
	}
	/*
	 * Takes the specified long value and returns the uuid equivalent.
	 */
	protected static String buildUUID(long value) {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("long: "+value);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
		String tmpString;
		char[] uuid=new char[] {'0','0','0','0','0','0','0','0','0','0','0','0',
				'0','0','0','0','0','0','0','0','0','0','0','0',
				'0','0','0','0','0','0','0','0','0','0','0','0'};
		int length;
		int pos=uuid.length-1;
		
		// Convert the long to a String
		tmpString=Long.toString(value);
		length=tmpString.length()-1;
		
		while ((pos>=0) && (length >=0)) {
			uuid[pos]=tmpString.charAt(length);
			pos--;
			length--;
		} // END while (pos >= 0)
		
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("uuid: "+new String(uuid));
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
		return new String(uuid);
	}
	//*************************************************************************
	//* END STATIC METHOD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN ACCESSOR METHOD DECLARATIONS
	//*************************************************************************
	public PersistenceManager getPersistenceManager() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return pm;
	}
	//*************************************************************************
	//* END ACCESSOR METHOD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN OTHER METHOD DECLARATIONS
	//*************************************************************************
	/**
	 * Convenience method to facilitate the authentication process.
	 */
	protected final void authenticate(UserContext ctx) 
	throws AuthenticationException {
		log.trace("[BEGIN]");
		pm.authenticate(ctx);
		log.trace("[END]");
	}
	/**
	 * Validate the object passed.
	 * @throws DAOException
	 */
	public void checkObject(Object obj) throws DAOException {
		log.trace("[BEGIN]");
		checkObject(obj,VALIDATE_NULL|VALIDATE_WILDOBJECT);
		log.trace("[END]");
	}
	public void checkObject(Object obj, int flag) throws DAOException {
		log.trace("[BEGIN]");
		if ((flag & VALIDATE_NULL) == VALIDATE_NULL) {
			// Check to see if the object is NULL
			if (obj == null) {
				throw new DAOException(ERR_NULL_REFERENCE);
			}
		} // END if ((flag & VALIDATE_NULL) == VALIDATE_NULL)
		if ((flag & VALIDATE_WILDOBJECT) == VALIDATE_WILDOBJECT) {
			// Validate that the object is an extension of WildObject
			if ((obj instanceof WildObject) == false) {
				throw new DAOException(ERR_NOT_WILDOBJECT);
			}
		} // END if ((flag & VALIDATE_WILDOBJECT) == VALIDATE_WILDOBJECT)
		log.trace("[END]");
	}
	/**
	 * Convenience method used to return a reference to the connection.
	 */
	protected Connection getConnection() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return pm.getConnection();
	}
	/**
	 * Return the poid of the current user.
	 */
	protected long getCurrentUser(UserContext ctx) 
	throws AuthenticationException {
		log.trace("[BEGIN]");
		long poid=Long.MIN_VALUE;
		
		if (ctx != null) {
			UserImpl user=(UserImpl) ctx.getUser();
			poid=user.getPoid();
			
			if (poid == Long.MIN_VALUE) {
				throw new AuthenticationException();
			}
		} else {
			log.error("null UserContext passed");
		}
		log.trace("[END]");
		return poid;
	}
	/**
	 * This method must be implemented by all sub-classes of the WildDAOImpl
	 * abstract class.  The <code>daoIdentifierKey</code> is used by the 
	 * persistence manager to uniquely identify the DAO.
	 */
	protected abstract String getDAOIdentifierKey();
	/**
	 * Returns the next <code>poid</code> value for the object.
	 * @param java.sql.Connection
	 * @return double
	 */
	protected long getNextPoid(Connection con) {
		boolean success=false;
		long nextPoid=-1;
		int rowCount;
		ResultSet rs=null;
		String daoIdentifierKey=null;
		PreparedStatement pStmt=null;
		
		daoIdentifierKey=getDAOIdentifierKey();
		
		try {
			pStmt=con.prepareStatement(SELECT_NEXT_POID_QUERY);
			pStmt.setString(1,daoIdentifierKey);
			rs=pStmt.executeQuery();
			success=rs.next();
			if (success) {
				nextPoid=rs.getLong(1);
			} else {
				// TO-DO Figure out more graceful way to handle this situation.
				// The query to obtain the nextID didn't find a matching record, so
				// the record doens't exist.
				log.error("The DAO doesn't have an entry in OBJECT_MASTER.");
			}
			rs.close();
			pStmt.close();
			// Now update the next poid
			pStmt=con.prepareStatement(UPDATE_NEXT_POID_QUERY);
			pStmt.setLong(1,nextPoid+1);
			pStmt.setString(2,daoIdentifierKey);
			rowCount=pStmt.executeUpdate();
			if (rowCount == 1) {
				StringBuilder msg=new StringBuilder(255);
				msg.append("Successfully incremented nextID, ");
				msg.append(nextPoid);
				msg.append(", for ");
				msg.append(daoIdentifierKey);
				log.trace(msg.toString());
				msg=null;
			}
		} catch (SQLException ex) {
			// Had a problem, so roll back the transaction
			try {
				con.rollback();
			} catch (SQLException e) {
				StringBuilder msg=new StringBuilder(80);
				msg.append("SQLException thrown trying to rollback the ");
				msg.append("connection.");
				log.error(msg.toString(),e);
				msg=null;
			}
			StringBuilder msg=new StringBuilder(80);
			msg.append("SQLException encountered trying to obtain the ");
			msg.append("next poid value.");
			log.error(msg.toString());
		} finally {
			try {
				if (pStmt != null) {
					pStmt.close();
				}
			} catch (SQLException ex) {
				log.error("SQLExceptino thrown.",ex);
			}
		}		
		return nextPoid;
	}
	//*************************************************************************
	//* END OTHER METHOD DECLARATIONS
	//*************************************************************************	
}
