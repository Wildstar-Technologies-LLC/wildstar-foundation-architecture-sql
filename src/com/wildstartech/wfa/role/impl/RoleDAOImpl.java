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
package com.wildstartech.wfa.role.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wildstartech.wfa.role.Role;
import com.wildstartech.wfa.role.RoleNameComparator;
import com.wildstartech.wfa.role.RoleNameTooLongException;
import com.wildstartech.wfa.role.RoleDAO;
import com.wildstartech.wfa.dao.DAOException;
import com.wildstartech.wfa.dao.WildDAO;
import com.wildstartech.wfa.dao.user.UserContext;

public final class RoleDAOImpl extends WildDAOImpl implements RoleDAO {
	// Log
	private static Log log=LogFactory.getLog(RoleDAO.class);
	// Comparator to compare roles by name
	private static RoleNameComparator nameComparator=new RoleNameComparator();
	// SQL QUERY FOR INSERT
	private static String QUERY_INSERT=
		"INSERT INTO ROLES (POID, CREATED_BY, DATE_CREATED, MODIFIED_BY, "+
		"DATE_MODIFIED, ROLE_NAME) VALUES (?,?,?,?,?,?)";
	// SQL QUERY FOR UPDATE
	private static String QUERY_UPDATE=
		"UPDATE ROLES SET MODIFIED_BY=?, DATE_MODIFIED=?, ROLE_NAME=? "+
		"WHERE POID=?";
	// SQL QUERY FOR DELETE
	private static String QUERY_DELETE="DELETE FROM ROLES WHERE POID=?";
	// SQL QUERY FOR SELECT
	private static String QUERY_SELECT=
		"SELECT POID, CREATED_BY, DATE_CREATED, MODIFIED_BY, DATE_MODIFIED, "+
		"ROLE_NAME FROM ROLES WHERE POID = ?";
	// SQL QUERY FOR SELECT ALL
	private static String QUERY_SELECT_ALL=
		"SELECT POID, CREATED_BY, DATE_CREATED, MODIFIED_BY, DATE_MODIFIED, "+
		"ROLE_NAME FROM ROLES ORDER BY ROLE_NAME";
	
	/** Used to identify the DAO */
	protected static String DAO_IDENTIFIER_KEY=
		"com.wildstartech.justo.crm.dao.RoleDAO";
	// Singleton implementation of the Data Access Object for Company objects.
	private static RoleDAO roleDAO=new RoleDAOImpl();
	// The list of available roles.
	private SortedSet<Role> roles;
	// Private constructor enabling the Singleton design pattern.
	private RoleDAOImpl() {
		log.trace("RoleDAOImpl() [BEGIN]");
		try {
			roles=_findAll();
		} catch (DAOException ex) {
			log.error("Error obtaining list of available roles.",ex);
			roles=new TreeSet<Role>(nameComparator);
		}
		log.trace("RoleDAOImpl() [END]");
	}
	/**
	 * Returns a default instance of the <code>Role</code> object.
	 * 
	 * @return com.wildstartech.justo.crm.Role
	 */
	public Role create() {
		log.trace("RoleDAO.create() [BEGIN]");
		log.trace("RoleDAO.create() [END]");
		return new RoleImpl();
	}
	/**
	 * Passivate the referenced <code>Role</code> object.
	 * @param com.wildstartech.justo.crm.Role
	 * @throws DAOException
	 */
	public void save(Role role, UserContext ctx) throws DAOException {		
		log.trace("RoleDAO.save(Role,UserContext) [BEGIN]");
		long poid;
		// Check the object to ensure valid parameters are passed.
		checkObject(role);
		checkObject(ctx,WildDAO.VALIDATE_NULL);
		// Business rule enforcement workflow would go here.

		// Perform the actual workto save the object
		poid=((RoleImpl) role).getPoid();
		if (poid == Long.MIN_VALUE) {
			// Based upon the poid value, the object has not yet been saved.
			_create((RoleImpl)role,ctx);
			roles.add(role);
		} else {
			// Since a valid poid value is assigned, update the existing record.
			_update(role,ctx);
		}
		log.trace("RoleDAO.save(Role,UserContext) [END]");
	}
	/**
	 * Create a new record. 
	 */
	protected void _create(RoleImpl role, UserContext ctx) 
	throws DAOException {
		log.trace("RoleDAOImpl._create(Role,UserContext) [BEGIN]");
		long poid;
		long currentUserPoid;
		int numRows;
		Connection con=null;
		PreparedStatement pStmt=null;
		Timestamp currentTime=null;
	
		// Get the current/date/time
		currentTime=new Timestamp(System.currentTimeMillis());
		
		// Get a connection
		con=pm.getConnection();
		currentUserPoid=getCurrentUser(ctx);
		
		try {
			// Get the next poid
			poid=getNextPoid(con);
			// Build the query
			con.setAutoCommit(false);
			// Prepare/compile the statement
			pStmt=con.prepareStatement(QUERY_INSERT);
			// Populate the prepared statement with values
			pStmt.setLong(1,poid);					// POID
			pStmt.setLong(2,currentUserPoid);		// CREATED_BY
			pStmt.setTimestamp(3,currentTime);		// DATE_CREATED
			pStmt.setLong(4,currentUserPoid);		// MODIFIED_BY
			pStmt.setTimestamp(5,currentTime);		// DATE_MODIFIED
			pStmt.setString(6,role.getName());		// ROLE_NAME
			// Execute the INSERT query
			numRows=pStmt.executeUpdate();
			if(numRows==1) {
				con.commit();
			} else {
				try {
					con.rollback();
				} catch (SQLException ex) {
					StringBuilder msg=new StringBuilder(128);
					msg.append("Exception thrown attempting to rollback");
					msg.append(" transaction.");
					log.error(msg.toString(),ex);
				}
			}
			// Close the PreparedStatement
			pStmt.close();
		} catch (SQLException ex) {
			// Print the error information
			log.error("SQLException thrown.",ex);
		} catch (Throwable ex) {
			log.error("Unexpected error thrown.",ex);
		}
		log.trace("RoleDAOImpl._create(Role,UserContext) [END]");
	}
	/**
	 * Saves the specified <code>Role</code> object.
	 * 
	 * @param com.wildstartech.justo.crm.dao.Role
	 * @throws com.wildstartech.justo.crm.dao.DAOException
	 */
	protected void _update(Role role, UserContext ctx) throws DAOException {
		log.trace("RoleDAOImpl._update(Role,UserContext) [BEGIN]");
		long poid;
		long currentUserPoid;
		int numRows;
		Connection con=null;
		PreparedStatement pStmt=null;
		Timestamp currentTime=null;
		
		// Get the current date/time 
		currentTime=new Timestamp(System.currentTimeMillis());
		// Get the current user
		currentUserPoid=getCurrentUser(ctx);
		// Get the current poid for the User code
		poid=((RoleImpl) role).getPoid();
		
		try {		
			// Get a connection
			con=pm.getConnection();
			// Create the statement
			con.setAutoCommit(false);
			// Prepare/compile the statement
			pStmt=con.prepareStatement(QUERY_UPDATE);
			pStmt.setLong(1,currentUserPoid);			// MODIFIED_BY
			pStmt.setTimestamp(2,currentTime);			// DATE_MODIFIED
			pStmt.setString(3,role.getName());			// ROLE_NAME
			pStmt.setLong(4,poid);						// POID
			// Execute the UPDATE query
			numRows=pStmt.executeUpdate();
			if (numRows ==1) {
				// No problems, so commit the transaction
				con.commit();
			} else {
				// Had a problem, so roll back the transaction
				try {
					con.rollback();
				} catch (SQLException e) {
					// Exception thrown trying to rollback the connection.
					StringBuilder msg=new StringBuilder(128);
					msg.append("Exception thrown attempting to rollback");
					msg.append(" transaction.");
					log.error(msg.toString(),e);
				}
			}
			// Close the PreparedStatement
			try {
				pStmt.close();			
			} catch (SQLException ex) {
				log.error("Exception thrown close the statement.",ex);
			}
		} catch (SQLException ex) {
			log.error("SQLException thrown.",ex);
		} finally {
			// Attempt to clean up the connection.
			if (con != null) {
				try {
					con.close();
				} catch (SQLException ex) {
					log.error("Error trying to close connection.",ex);
				}
			}  // END if (con != null)
		}
		log.trace("RoleDAOImpl._update(Role,UserContext) [BEGIN]");
	}
	/**
	 * Delete the specified <code>Role</code> <code>Object</code>.
	 * @param Role
	 * @return boolean
	 * @throws DAOException
	 */
	public void delete(Role role, UserContext ctx) throws DAOException {		
		long poid;
		int numRows;
		Connection con=null;
		PreparedStatement pStmt=null;
		
		// Get a connection
		con=pm.getConnection();
		
		// Validate the parameter
		checkObject(role);
		// Get the Role object's poid
		poid=((RoleImpl) role).getPoid();
		if (poid==Long.MIN_VALUE) {
			// The company object has not yet been saved.
			System.err.print("ERROR: Trying to delete a company that has not");
			System.err.println("yet been saved.");
		} else {
			// The company object has been saved.
			try {
				// Build the query
				con.setAutoCommit(false);
				pStmt=con.prepareStatement(QUERY_DELETE);
				pStmt.setDouble(1,poid);
				// Execute the DELETE query
				numRows=pStmt.executeUpdate();
				if (numRows == 1) {
					con.commit();
				} else {
					con.rollback();
				}
			} catch (SQLException ex) {
				// Print the error information.
				ex.printStackTrace(System.err);
			} finally {
				try {
					con.close();
				} catch (SQLException ex) {
					System.err.println("WARNING: Error trying to close connection");
				}
			}
		} // END if(poid==Long.MIN_VALUE)		
	}
	/**
	 * 
	 */
	protected Role findByPrimaryKey(long key) throws DAOException {
		Connection con=null;
		RoleImpl role=null;
		PreparedStatement pStmt=null;
		ResultSet rs=null;
		con=pm.getConnection();
		if (key != Long.MIN_VALUE) {
			try {
				// Prepare the statement...
				pStmt=con.prepareStatement(QUERY_SELECT);
				// Specify the poid
				pStmt.setLong(1,key);
				// Execute the statement
				rs=pStmt.executeQuery();
				// If the query returns a value...
				if (rs.next()) {					
					// The resultSet contains a value.
					role = new RoleImpl();
					setWildObjectData(role,rs);
					try {
						role.setName(rs.getString(6));		// company_name
					} catch (RoleNameTooLongException ex) {
						System.err.println("DEBUG: ERROR: Inconceivable!");
						ex.printStackTrace(System.err);
					}
				}
				// Close the ResultSet
				rs.close();
				// Close the PreparedStatement
				pStmt.close();
			} catch (SQLException ex) {
				System.err.print("ERROR: SQLException thrown while trying to ");
				System.err.println("create a Role object.");
				ex.printStackTrace(System.err);
			}
		} else {
			throw new DAOException(ERR_POID_INVALID);
		}
		// Attempt to close the connection
		try {
			con.close();
		} catch (SQLException ex) {
			System.err.print("ERROR: SQLException thrown trying to close the ");
			System.err.println("Connection.");
			ex.printStackTrace(System.err);
		}
		return role;
	}
	/**
	 * 
	 */
	public Role findByPrimaryKey(long key, UserContext ctx)
	throws DAOException {	
		return findByPrimaryKey(key);
	}

	public List<Role> findByName(String name, UserContext ctx)
	throws DAOException {
		log.trace("RoleDAO.findByName(String,UserContext) [BEGIN]");
		ArrayList<Role> roleList=new ArrayList<Role>();
		name=name.toUpperCase();
		for (Role role: roles) {
			if (role.getName().startsWith(name)) {
				roleList.add(role);
			}
		}
		log.trace("RoleDAO.findByName(String,UserContext) [END]");
		return roleList;
	}
	public List<Role> findAll(UserContext ctx) throws DAOException {
		log.trace("RoleDAO.findAll(UserContext) [BEGIN]");
		ArrayList roleList=new ArrayList<Role>(roles.size());
		for (Role role: roles) {
			roles.add(role);
		}
		log.trace("RoleDAO.findAll(UserContext) [END]");
		return roleList;
	}
	private SortedSet<Role> _findAll() throws DAOException {
		log.trace("RoleDAOImpl._findAll() [BEGIN]");
		TreeSet<Role> roles=null;
		Connection con=null;
		RoleNameComparator comparator=null;
		RoleImpl role=null;
		PreparedStatement pStmt=null;
		ResultSet rs=null;
		
		comparator=new RoleNameComparator();
		roles=new TreeSet<Role>(comparator);
		// Get a connection
		con=pm.getConnection();
		//if (ctx != null) {
			try {
				// Prepare the statement...
				pStmt=con.prepareStatement(QUERY_SELECT_ALL);
				// Execute the statement
				rs=pStmt.executeQuery();
				// Process the resultSet...
				while (rs.next()) {
					// The resultSet contains a value.
					role = new RoleImpl();
					setWildObjectData(role,rs);
					role.setName(rs.getString(6));		// user_name
		
					// Add the group to the groupList
					roles.add(role);
					// De-reference the company object
					role=null;
				}
				// Close the ResultSet
				rs.close();
				// Close the PreparedStatement
				pStmt.close();
			} catch (SQLException ex) {
				System.err.print("ERROR: SQLException thrown while trying to ");
				System.err.println("create a Company object.");
				System.err.println(ex);
				ex.printStackTrace(System.err);
			} catch (RoleNameTooLongException ex) {
				ex.printStackTrace();
			}
		//} // END if (ctx != null)
		// Attempt to close the connection
		try {
			con.close();
		} catch (SQLException ex) {
			System.err.print("ERROR: SQLException thrown trying to close the ");
			System.err.println("Connection.");
		}
		log.trace("RoleDAOImpl._findAll() [END]");
		return roles;
	}
	/**
	 * Returns the DAO Identifier key.
	 */
	protected String getDAOIdentifierKey() {
		return DAO_IDENTIFIER_KEY;
	}
	/**
	 * Returns a reference to the Role DAO.
	 */
	public static RoleDAO getInstance() {
		log.trace("RoleDAO.getInstance() [BEGIN]");
		return roleDAO;
	}
}
