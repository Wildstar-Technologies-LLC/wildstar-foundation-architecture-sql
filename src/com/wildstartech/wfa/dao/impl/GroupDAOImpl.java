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
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wildstartech.wfa.dao.DAOException;
import com.wildstartech.wfa.dao.GroupNameTooLongException;
import com.wildstartech.wfa.dao.WildDAO;
import com.wildstartech.wfa.dao.group.GroupDAO;
import com.wildstartech.wfa.dao.group.PersistentGroup;
import com.wildstartech.wfa.dao.user.PersistentUser;
import com.wildstartech.wfa.dao.user.UserContext;

public final class GroupDAOImpl extends WildDAOImpl implements GroupDAO {
	//*************************************************************************
	//* BEGIN STATIC FIELD DECLARATIONS
	//*************************************************************************
	// Log
	private Log log=LogFactory.getLog(GroupDAO.class);
	//	 SQL QUERY FOR INSERT
	private static String QUERY_INSERT=
		"INSERT INTO GROUPS (POID, CREATED_BY, DATE_CREATED, "+
		" MODIFIED_BY, DATE_MODIFIED, GROUP_NAME) VALUES (?,?,?,?,?,?)";
	// SQL QUERY FOR UPDATE
	private static String QUERY_UPDATE=
		"UPDATE GROUPS SET MODIFIED_BY=?, DATE_MODIFIED=?, GROUP_NAME=? "+
		"WHERE POID=?";
	// SQL QUERY FOR DELETE
	private static String QUERY_DELETE="DELETE FROM GROUPS WHERE POID=?";	
	// SQL QUERY FOR SELECT
	private static String QUERY_SELECT=
		"SELECT POID, CREATED_BY, DATE_CREATED, MODIFIED_BY, DATE_MODIFIED, "+
		"GROUP_NAME FROM GROUPS WHERE POID = ?";
	// SQL QUERY FOR SELECT ALL
	private static String QUERY_SELECT_ALL=
		"SELECT POID, CREATED_BY, DATE_CREATED, MODIFIED_BY, DATE_MODIFIED, "+
		"GROUP_NAME FROM GROUPS ORDER BY GROUP_NAME";
	// SQL QUERY FOR SEARCH (Name)
	private static String QUERY_SEARCH=
		"SELECT POID, CREATED_BY, DATE_CREATED, MODIFIED_BY, DATE_MODIFIED "+
		"GROUP_NAME FROM GROUPS WHERE GROUP_NAME LIKE '?%' ORDER BY GROUP_NAME";	
	/** Used to identify the DAO */
	// SQL QUERY FOR USER ADD
	private static String QUERY_USER_ADD=
		"INSERT INTO USER_GROUP_ASSOC (USER_POID, GROUP_POID, CREATED_BY, "+
		"DATE_CREATED, MODIFIED_BY, DATE_MODIFIED) VALUES (?,?,?,?,?,?)";
	// SQL QUERY FOR USER LOAD (ALL)
	private static String QUERY_USER_LOAD_ALL=
		"SELECT USER_POID FROM USER_GROUP_ASSOC WHERE GROUP_POID=?";
	// SQL QUERY FOR USER REMOVE
	private static String QUERY_USER_REMOVE=
		"DELETE FROM USER_GROUP_ASSOC WHERE GROUP_POID=? AND USER_POID=?";
	// SQL QUERY FOR REMOVING ALL USERS
	private static String QUERY_USER_REMOVE_ALL=
		"DELETE FROM USER_GROUP_ASSOC WHERE GROUP_POID=?";
	protected static String DAO_IDENTIFIER_KEY=
		"com.wildstartech.justo.crm.dao.GroupDAO";
	// Singleton implementation of the Data Access Object for Group objects.
	private static GroupDAO groupDAO=new GroupDAOImpl();
	//*************************************************************************
	//* END STATIC FIELD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN INSTANCE FIELD DECLARATIONS
	//*************************************************************************
	private TreeMap<Long,GroupImpl> groupCache;
	//*************************************************************************
	//* END INSTANCE FIELD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN CONSTRUCTOR DECLARATIONS
	//*************************************************************************
	// Private constructor enabling the Singleton design pattern.
	private GroupDAOImpl() {
		super();
		log.trace("[BEGIN]");
		groupCache=new TreeMap<Long,GroupImpl>();
		log.trace("[END]");
	}
	//*************************************************************************
	//* END CONSTRUCTOR DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN CRUD METHODS
	//*************************************************************************	
	public PersistentGroup create() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return new GroupImpl();
	}
	/**
	 * Manage the process of saving specified <code>Group</code>.
	 * 
	 * @param com.wildstartech.justo.crm.dao.Group The group object that should be 
	 * stored in the persistent data store.
	 * @param com.wildstartech.justo.crm.dao.UserContext An object that contains
	 * information about the current user.
	 */
	public void save(PersistentGroup group, UserContext ctx) throws DAOException {
		log.trace("[BEGIN]");
		long poid;
		
		// Check the object to ensure valid parameters are passed.
		checkObject(group);
		checkObject(ctx,WildDAO.VALIDATE_NULL);
		
		// Business rule enforcement workflow would go here.
		System.out.println("DEBUG: GroupDAOImpl.save()");
		// Perform the actual workto save the object
		poid=((GroupImpl) group).getPoid();
		if (poid == Long.MIN_VALUE) {
			// Based upon the poid value, the object has not yet been saved.
			_create((GroupImpl)group,ctx);
		} else {
			// Since a valid poid value is assigned, update the existing record.
			_update(group,ctx);
		}
		log.trace("[END]");
	}
	/**
	 * Create a new record. 
	 */
	protected void _create(PersistentGroup group, UserContext ctx) throws DAOException {
		log.trace("[BEGIN]");
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
			pStmt.setLong(2,currentUserPoid);			// CREATED_BY
			pStmt.setTimestamp(3,currentTime);		// DATE_CREATED
			pStmt.setLong(4,currentUserPoid);			// MODIFIED_BY
			pStmt.setTimestamp(5,currentTime);		// DATE_MODIFIED
			pStmt.setString(6,group.getName());		// GROUP_NAME
			// Execute the INSERT query
			numRows=pStmt.executeUpdate();
			if(numRows==1) {
				con.commit();
			} else {
				con.rollback();
			}
			// Close the PreparedStatement
			pStmt.close();
			// Add the group to the cache
			groupCache.put(new Long(poid),(GroupImpl)group);
		} catch (SQLException ex) {
			// Print the error information
			log.error("SQLException thrown.",ex);
		} catch (Throwable ex) {
			log.error("Unknown exception thrown.",ex);
		}
		log.trace("[END]");
	}
	/**
	 * Saves the specified <code>Group</code> object.
	 * 
	 * @param com.wildstartech.justo.crm.dao.Group
	 * @throws com.wildstartech.justo.crm.dao.DAOException
	 */
	protected void _update(PersistentGroup group, UserContext ctx) throws DAOException {
		log.trace("[BEGIN]");
		long poid;
		long currentUserPoid;
		int numRows;
		Connection con=null;
		PreparedStatement pStmt=null;
		Timestamp currentTime=null;
		PersistentGroup dbGroup=null;
		
		// Get the current date/time 
		currentTime=new Timestamp(System.currentTimeMillis());
		// Get the current user
		currentUserPoid=getCurrentUser(ctx);
		// Get the current poid for the User code
		poid=((GroupImpl) group).getPoid();
		
		try {
			// Get the database version of the User object.
			dbGroup=findByPrimaryKey(poid,ctx);
			
			// Get a connection
			con=pm.getConnection();
			// Create the statement
			con.setAutoCommit(false);
			// Prepare/compile the statement
			pStmt=con.prepareStatement(QUERY_UPDATE);
			pStmt.setLong(1,currentUserPoid);			// MODIFIED_BY
			pStmt.setTimestamp(2,currentTime);		// DATE_MODIFIED
			pStmt.setString(3,group.getName());		// USER_NAME
			pStmt.setLong(4,poid);					// POID
			// Execute the UPDATE query
			numRows=pStmt.executeUpdate();
			if (numRows ==1) {
				// No problems, so commit the transaction
				con.commit();
			} else {
				// Had a problem, so roll back the transaction
				con.rollback();
			}
			// Close the PreparedStatement
			pStmt.close();
			// Save changes to userList
			saveUserList(con,dbGroup,group,ctx);
		} catch (SQLException ex) {
			// Print the error information
			log.error("SQLException thrown.",ex);
		} catch (DAOException ex) {
			// Print error information
			log.error("Error getting current user from DB",ex);
			throw ex;
		}
		log.trace("[END]");
	}
	public void delete(PersistentGroup group, UserContext ctx) throws DAOException {
		log.trace("[BEGIN]");
		Connection con=null;
		
		try {
			// Obtain a connection
			con=pm.getConnection();
			// Disable auto-commit
			con.setAutoCommit(false);
			// Invoke the "save" logic
			delete(group,ctx,con);
			// Commit the transaction
			con.commit();
		} catch (DAOException ex) {
			// A DAOException was thrown, so rollback the transaction.
			log.error("DAOException thrown.",ex);
			try {
				con.rollback();
			} catch (SQLException ex1) {
				StringBuilder msg=new StringBuilder(80);
				msg.append("SQLException thrown while attempting ");
				msg.append("to rollback a transaction.");
				log.error(msg.toString(),ex1);
			}
			throw ex;
		} catch (SQLException ex) {
			log.error("SQLException thrown.",ex);
		}
		log.trace("[END]");
	}
	/**
	 * Remove the <code>Group</code> object from the persistent data store.
	 */
	protected void delete(PersistentGroup group, UserContext ctx, Connection con)
	throws DAOException {
		log.trace("[BEGIN]");
		long poid;
		int numRows;
		PreparedStatement pStmt;
		
		// Validate the parameters
		checkObject(group);
		checkObject(ctx,WildDAO.VALIDATE_NULL);
		// Get a connection
		con=pm.getConnection();
		// Get the company object's poid
		poid=((GroupImpl) group).getPoid();
		if (poid == Long.MIN_VALUE) {
			// The Group object has not yet been saved.
			throw new DAOException(GroupDAO.ERR_NOT_SAVED);
		} else {
			// The Group has been saved, so let's delete it.
			try {
				pStmt=con.prepareStatement(QUERY_DELETE);
				pStmt.setLong(1,poid);
				// Execute the DELETE query
				numRows=pStmt.executeUpdate();
				if (numRows!=1) {
					log.error("Error Deleting group.");
				}
				// Remove all user/group association records
				pStmt=con.prepareStatement(QUERY_USER_REMOVE_ALL);
				pStmt.setLong(1,poid);
				// Execute the DETELETE query
				numRows=pStmt.executeUpdate();
			} catch (SQLException ex) {
				// Print the error informaiton
				ex.printStackTrace(System.err);
			}
			((GroupImpl)group).setPoid(Long.MIN_VALUE);
		} // endif poid == Long.MIN_VALUE
		log.trace("[END]");
	}
	/**
	 * 
	 */
	public PersistentGroup findByPrimaryKey(long key, UserContext ctx) throws DAOException {
		log.trace("[BEGIN]");
		PersistentGroup group=null;
		Connection con=null;	
		
		try {
			// Obtain a connection
			con=pm.getConnection();
			// Disable auto-commit
			con.setAutoCommit(false);
			// Invoke the "save" logic
			group=findByPrimaryKey(key,ctx,con);
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
		} catch (SQLException ex) {
			log.error("SQLException thrown.",ex);
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException ex) {
					StringBuilder msg=new StringBuilder(80);
					msg.append("SQLException thrown trying to close the ");
					msg.append("connection.");
					log.error(msg.toString(),ex);
				}
			}
		}
		log.trace("[END]");
		return group;
	}
	
	/**
	 * Return a specific <code>Group</code>.
	 * 
	 * @param key
	 * @param ctx
	 * @param con
	 * @return
	 * @throws DAOException
	 */
	protected PersistentGroup findByPrimaryKey(long key, UserContext ctx, Connection con)
	throws DAOException {
		log.trace("[BEGIN]");
		GroupImpl group=null;
		Long gPoid=new Long(key);
		PreparedStatement pStmt=null;
		ResultSet rs=null;
		
		if (groupCache.containsKey(gPoid)) {
			// The groupCache contains the specified group, so return it.
			group=groupCache.get(gPoid);
		} else {
			// The groupCache does not contain teh specified group, so load it
			// Get a connection
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
						group = new GroupImpl();
						setWildObjectData(group,rs);
						try {
							group.setName(rs.getString(6));	// group_name
						} catch (GroupNameTooLongException ex) {
							// NO-OP
							// This sholud never happen unless the database is modified
							// directly.
						}
						// Load Users 
						System.out.println("DEBUG: BEGIN - findByPrimaryKey() - loadUsers");
						loadUsers(con,group);
					}
					// Close the ResultSet
					rs.close();
					// Close the PreparedStatement
					pStmt.close();
					// Add the group to the cache..
					groupCache.put(new Long(group.getPoid()),group);
				} catch (SQLException ex) {
					System.err.print("ERROR: SQLException thrown while trying to ");
					System.err.println("create a Group object.");
				}
			} else {
				throw new DAOException(ERR_POID_INVALID);
			}
			// Attempt to close the connection
			try {
				con.close();
			} catch (SQLException ex) {
				StringBuilder msg=new StringBuilder(80);
				msg.append("SQLException thrown trying to close the ");
				msg.append("Connection.");
				log.error(msg.toString(),ex);
			}
		}
		log.trace("[END]");
		return group;
	}
	
	/**
	 * 
	 */
	public List<PersistentGroup> findByName(String name, UserContext ctx)
			throws DAOException {
		log.trace("[BEGIN]");
		Connection con=null;
		PreparedStatement pStmt=null;
		ResultSet rs=null;
		GroupImpl group=null;
		ArrayList<PersistentGroup> groupList = new ArrayList<PersistentGroup>();
		
		// Get a connection
		con=pm.getConnection();
		if (name != null) {
			try {
				// Prepare the statement...
				pStmt=con.prepareStatement(QUERY_SEARCH);
				// Specify the poid
				pStmt.setString(1,name);
				// Execute the statement
				rs=pStmt.executeQuery();
				// Process the resultSet...
				while (rs.next()) {
					// The resultSet contains a value.
					group = new GroupImpl();
					setWildObjectData(group,rs);
					try {
						group.setName(rs.getString(5));	// group_name
					} catch (GroupNameTooLongException ex) {
						StringBuilder msg=new StringBuilder(128);
						msg.append("Egad! Someone has been mucking around in ");
						msg.append("the database!  I got a group\nname that ");
						msg.append("was beyond the MAX_GROUP_NAME_SIZE length");
						msg.append("when reading from the database.");
						log.error(msg.toString(),ex);
					}
					// Add the group to the groupList
					groupList.add(group);
					// Load Users 
					loadUsers(con,group);
					// De-reference the group object
					group=null;
				}
				// Close the ResultSet
				rs.close();
				// Close the PreparedStatement
				pStmt.close();
			} catch (SQLException ex) {
				StringBuilder msg=new StringBuilder(80);
				msg.append("SQLException thrown while trying to create a ");
				msg.append("Group object.");
				log.error(msg.toString(),ex);
			}
		} else {
			throw new DAOException(ERR_POID_INVALID);
		}
		// Attempt to close the connection
		try {
			con.close();
		} catch (SQLException ex) {
			StringBuilder msg=new StringBuilder(80);
			msg.append("SQLException thrown trying to close the ");
			msg.append("Connection.");
			log.error(msg.toString(),ex);
		}
		log.trace("[END]");
		return groupList;
	}
	public List<PersistentGroup> findAll(UserContext ctx) throws DAOException {
		log.trace("[BEGIN]");
		Connection con=null;	
		List<PersistentGroup> groupList=null;
		
		try {
			// Obtain a connection
			con=pm.getConnection();
			// Disable auto-commit
			con.setAutoCommit(false);
			// Invoke the "save" logic
			groupList=findAll(ctx,con);
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
		} catch (SQLException ex) {
			log.error("SQLException thrown.",ex);
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException ex) {
					StringBuilder msg=new StringBuilder(80);
					msg.append("SQLException thrown trying to close the ");
					msg.append("connection.");
					log.error(msg.toString(),ex);
				}
			} // END if(con!=null)
		}
		log.trace("[END]");
		return groupList;
	}
	protected List<PersistentGroup> findAll(UserContext ctx, Connection con)
	throws DAOException {
		log.trace("[BEGIN]");
		ArrayList<PersistentGroup> groupList=new ArrayList<PersistentGroup>();
		GroupImpl group=null;
		Long gPoid=Long.MIN_VALUE;
		PreparedStatement pStmt=null;
		ResultSet rs=null;
		
		// Get a connection
		con=pm.getConnection();
		try {
			// Prepare the statement...
			pStmt=con.prepareStatement(QUERY_SELECT_ALL);
			// Execute the statement
			rs=pStmt.executeQuery();
			// Process the resultSet...
			while (rs.next()) {
				gPoid=rs.getLong(1);
				if (groupCache.containsKey(gPoid)) {
					group=groupCache.get(gPoid);
				} else {
					// The resultSet contains a value.
					group = new GroupImpl();
					setWildObjectData(group,rs);
					group.setName(rs.getString(6));		// user_name
					// Add the group to the cache
					groupCache.put(gPoid,group);
				} // END if(groupCache.containsKey(gPoid)
				// Add the group to the groupList
				groupList.add(group);
				// De-reference the user object
				group=null;
			}
			// Close the ResultSet
			rs.close();
			// Close the PreparedStatement
			pStmt.close();
		} catch (SQLException ex) {
			StringBuilder msg=new StringBuilder(80);
			msg.append("SQLException thrown while trying to create a Group ");
			msg.append("object.");
			log.error(msg.toString(),ex);
		} catch (GroupNameTooLongException ex) {
			log.error("GroupNameTooLongException thrown.",ex);
		}
		log.trace("[END]");
		return groupList;
	}
	//**************************************************************************
	//* END CRUD METHODS
	//**************************************************************************
	//**************************************************************************
	//* BEGIN OTHER METHODS
	//**************************************************************************
	protected String getDAOIdentifierKey() {
		return DAO_IDENTIFIER_KEY;
	}
	/**
	 * Returns a reference to the singleton instance of the <code>GroupDAO</code>.
	 * @return GroupDAO
	 */
	public static GroupDAO getInstance() {
		return groupDAO;
	}
	/**
	 * Loads the users associated with the specified group.
	 */
	private void loadUsers(Connection con, GroupImpl group) 
	throws SQLException {
		log.trace("[BEGIN]");
		long uPoid=Long.MIN_VALUE;
		PreparedStatement pStmt=null;
		ResultSet rs=null;
		
		// Preapre the SQL statement
		pStmt=con.prepareStatement(QUERY_USER_LOAD_ALL);
		pStmt.setLong(1,group.getPoid());
		rs=pStmt.executeQuery();
		while (rs.next()) {
			// Go through the result set.
			uPoid=rs.getLong(1);
			// Return the user object based upon the primary key
			group.addUser(userDAO.findByPrimaryKey(uPoid));
		}
		rs.close();
		pStmt.close();
		log.trace("[END]");
	}
	/**
	 * Save any changes to the list of users.
	 */
	private void saveUserList(Connection con, PersistentGroup dbGroup, 
							PersistentGroup group, UserContext ctx) 
	throws SQLException {
		log.trace("[BEGIN]");
		int numRows=0;
		GroupImpl dbGrp=null;
		PreparedStatement pStmt=null;
		List<PersistentUser> dbUserList=null;
		List<PersistentUser> userList=null;
		Timestamp currentTime=null;
		UserImpl currentUser=null;
		UserImpl usr=null;
		
		dbUserList=dbGroup.getUsers();
		userList=group.getUsers();
		
		// Capture the current Date/Time
		currentTime=new Timestamp(System.currentTimeMillis());
		// Get the current User
		currentUser=(UserImpl) ctx.getUser();
		
		// Implemented group
		dbGrp=(GroupImpl)dbGroup;
	
		// See if dbUserList contains users in the userList
		for (PersistentUser user: userList) {
			// Cast the User object to UserImpl
			usr=(UserImpl) user;
			if (!dbUserList.contains(usr)) {
				// The userList does not contain the specified user, so 
				// insert a new one.
				pStmt=con.prepareStatement(QUERY_USER_ADD);
				pStmt.setLong(1,usr.getPoid());			// user_poid
				pStmt.setLong(2,dbGrp.getPoid());			// group_poid
				pStmt.setLong(3,currentUser.getPoid());	// created_by
				pStmt.setTimestamp(4,currentTime);		// date_created
				pStmt.setLong(5,currentUser.getPoid());	// modified_by
				pStmt.setTimestamp(6,currentTime);		// date_modified
				numRows=pStmt.executeUpdate();
				if (numRows!=1) {
					StringBuilder msg=new StringBuilder(128);
					msg.append("Inserting new relationship for a user and ");
					msg.append("group.  Insert returned a row count of: ");
					msg.append(numRows);
					log.error(msg.toString());
					msg=null;
				}
				pStmt.close();
			}
		} // END for(User user: userList)
		// Now compare the dbUserList to the current userList.  If a group is found
		// in the dbUserList but not in the userList (current), then it will be
		// deleted.
		for (PersistentUser user: dbUserList) {
			// Case the User Object to UserImpl
			usr=(UserImpl) user;
			if (!userList.contains(usr)) {
				// The userList does not contain the specified user, so 
				// insert a new one.
				pStmt=con.prepareStatement(QUERY_USER_REMOVE);
				pStmt.setLong(1,dbGrp.getPoid());			// group_poid
				pStmt.setLong(2,usr.getPoid());			// user_poid
				numRows=pStmt.executeUpdate();
				if (numRows!=1) {
					// There was a problem.
					StringBuilder msg=new StringBuilder(80);
					msg.append("Removing a relationship between a user and a ");
					msg.append("group.  DELETE returned a row count of: ");
					msg.append(numRows);
					log.error(msg.toString());
					msg=null;
				}
				pStmt.close();
			}
		} // END for(User user: dbUserList)
		log.trace("[END]");
	}
	//**************************************************************************
	//* END OTHER METHODS
	//**************************************************************************
}
