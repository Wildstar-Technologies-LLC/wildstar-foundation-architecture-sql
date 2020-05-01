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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.wildstartech.wfa.dao.DAOException;
import com.wildstartech.wfa.dao.PasswordTooLongException;
import com.wildstartech.wfa.dao.UserNameTooLongException;
import com.wildstartech.wfa.dao.WildDAO;
import com.wildstartech.wfa.dao.user.PersistentUser;
import com.wildstartech.wfa.dao.user.UserContext;
import com.wildstartech.wfa.dao.user.UserDAO;

public final class UserDAOImpl extends WildDAOImpl implements UserDAO {
	//*************************************************************************
	//* BEGIN STATIC FIELD DECLARATIONS
	//*************************************************************************
	//	 SQL QUERY FOR INSERT
	private static String QUERY_INSERT=
		"INSERT INTO USERS (POID, CREATED_BY, DATE_CREATED, MODIFIED_BY, "+
		"DATE_MODIFIED, USER_NAME, AGENT, PASSWORD) VALUES (?,?,?,?,?,?,?,?)";
	// SQL QUERY FOR UPDATE
	private static String QUERY_UPDATE=
		"UPDATE USERS SET MODIFIED_BY=?, DATE_MODIFIED=?, USER_NAME=?, "+
		"AGENT=?, PASSWORD=? WHERE POID=?";
	// SQL QUERY FOR DELETE
	private static String QUERY_DELETE="DELETE FROM USERS WHERE POID=?";	
	// SQL QUERY FOR SELECT
	private static String QUERY_SELECT=
		"SELECT POID, CREATED_BY, DATE_CREATED, MODIFIED_BY, DATE_MODIFIED, "+
		"USER_NAME, AGENT, PASSWORD FROM USERS WHERE POID = ?";
	// SQL QUERY FOR SELECT ALL
	private static String QUERY_SELECT_ALL=
		"SELECT POID, CREATED_BY, DATE_CREATED, MODIFIED_BY, DATE_MODIFIED, "+
		"USER_NAME, AGENT, PASSWORD FROM USERS";
	// SQL QUERY FOR SEARCH (Name)
	private static String QUERY_SEARCH=
		"SELECT POID, CREATED_BY, DATE_CREATED, MODIFIED_BY, DATE_MODIFIED, "+
		" USER_NAME, AGENT, PASSWORD FROM USERS WHERE USER_NAME LIKE '?%'";
	/** Log */
	private static Log log=LogFactory.getLog(UserDAO.class);
 	/** Used to identify the DAO */
	protected static String DAO_IDENTIFIER_KEY=
		"com.wildstartech.justo.crm.dao.UserDAO";
	// Singleton implementation of the Data Access Object for User objects.
	private static UserDAO userDAO=new UserDAOImpl();
	//*************************************************************************
	//* END STATIC FIELD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN INSTANCE FIELD DECLARATIONS
	//*************************************************************************
	private HashMap<Long,UserImpl> userCache;	
	//*************************************************************************
	//* END INSTANCE FIELD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN CONSTRUCTOR DECLARATIONS
	//*************************************************************************
	// Private constructor enabling the Singleton design pattern.
	private UserDAOImpl() {
		super();
		log.trace("UserDAOImpl() [BEGIN]");
		userCache=new HashMap<Long,UserImpl>();
		log.trace("UserDAOImpl() [END]");
	}
	//*************************************************************************
	//* END CONSTRUCTOR DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN CRUD METHODS
	//*************************************************************************
	/**
	 * Returns a "default" instance of the <code>User</code>.
	 */
	public PersistentUser create() {
		log.trace("UserDAOImpl.create() [BEGIN]");
		log.trace("UserDAOImpl.create() [END]");
		return new UserImpl();
	}
	/**
	 * 
	 */
	public void save(PersistentUser user, UserContext ctx) throws DAOException {
		log.trace("UserDAOImpl.save(User, UserContext) [BEGIN]");
		Connection con=null;
		
		try {
			// Obtain a connection
			con=pm.getConnection();
			// Disable auto-commit
			con.setAutoCommit(false);
			// Invoke the "save" logic
			save(user,ctx,con);
			// Commit the transaction
			con.commit();
		} catch (DAOException ex) {
			log.error("DAOException thrown. Rollback the transaction.",ex);
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
					StringBuilder msg=new StringBuilder(128);
					msg.append("SQLException thrown trying to close the");
					msg.append("connection.");
					log.error(msg,ex);
				}
			}
		}
		log.trace("UserDAOImpl.save(User, UserContext) [END]");
	}
	/**
	 * Manages the process of saving the <code>User</code> object to the database.
	 */
	protected void save(PersistentUser user, UserContext ctx, Connection con) throws DAOException {
		log.trace("vUserDAOImpl.save(User,UserContext,Connection) [BEGIN]");
		long poid;
		
		// Check the object to ensure valid parameters are passed.
		checkObject(user);
		checkObject(ctx,WildDAO.VALIDATE_NULL);
		
		// Business rule enforcement workflow would go here.
		
		// Perform the actual work to save the object
		poid=((UserImpl) user).getPoid();
		if (poid == Long.MIN_VALUE) {
			// Based upon the poid value, the object has not yet been saved.
			_create(user,ctx,con);
		} else {
			// Since a valid poid value is assigned, update the existing record.
			_update(user,ctx,con);
		}
		log.trace("UserDAOImpl.save(User,UserContext,Connection) [END]");
	}
	/**
	 * Create a new record. 
	 */
	private void _create(PersistentUser user, UserContext ctx, Connection con)
	throws DAOException {
		log.trace("UserDAOImpl._create(User,UserContext,Connection) [BEGIN]");
		long poid;
		long currentUserPoid;
		int numRows;
		PreparedStatement pStmt=null;
		String password=null;
		Timestamp currentTime=null;
		UserImpl userS=(UserImpl)user;
	
		// Get the current user and date/time
		currentUserPoid=getCurrentUser(ctx);
		currentTime=new Timestamp(System.currentTimeMillis());
		
		// Determine the password digest
		password=encodePassword(user.getPassword());
		
		// The User object has not yet been saved.
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
			pStmt.setString(6,user.getName());		// USER_NAME
			if (user.isAgent()) {					// AGENT
				pStmt.setString(7,"T");
			} else {
				pStmt.setString(7,"F");
			}
			pStmt.setString(8,password);			// PASSWORD

			// Execute the INSERT query
			numRows=pStmt.executeUpdate();
			if(numRows!=1) {
				log.error("Error creating User.");
			}
			// Close the PreparedStatement
			pStmt.close();
			// Add the user to the userCache
			userCache.put(new Long(poid), userS);
		} catch (SQLException ex) {
			// Print the error information
			log.error("SQLException thrown.",ex);
		} catch (Throwable ex) {
			log.error("Unknown Exception thrown.",ex);
		}
		log.trace("UserDAOImpl._create(User,UserContext,Connection) [END]");
	}
	/**
	 * Saves the specified User object.
	 * 
	 * @param com.wildstartech.justo.crm.dao.User
	 * @throws com.wildstartech.justo.crm.dao.DAOException
	 */
	protected void _update(PersistentUser user, UserContext ctx, Connection con)
	throws DAOException {
		log.trace("UserDAOImpl._update(User, UserContext, Connection) [BEGIN]");
		long poid;
		long currentUserPoid;
		int numRows;
		PreparedStatement pStmt=null;
		String password=null;
		Timestamp currentTime=null;
		PersistentUser dbUser=null;
		
		// Get the current user and date/time information
		currentTime=new Timestamp(System.currentTimeMillis());
		currentUserPoid=getCurrentUser(ctx);
		
		// Get the current poid for the User code
		poid=((UserImpl) user).getPoid();
		
		try {
			// Get the database version of the User object.
			dbUser=findByPrimaryKey(poid,ctx,con);
			if (((UserImpl)user).isPasswordChanged()) {
				// If the password has changed, encode it
				password=encodePassword(user.getPassword());
			} else {
				// If the password hasn't chagned, use the value stored in the db
				password=dbUser.getPassword();
			}
			// Prepare/compile the statement
			pStmt=con.prepareStatement(QUERY_UPDATE);
			pStmt.setDouble(1,currentUserPoid);		// MODIFIED_BY
			pStmt.setTimestamp(2,currentTime);		// DATE_MODIFIED
			pStmt.setString(3,user.getName());		// USER_NAME
			if (user.isAgent()) {						// AGENT
				pStmt.setString(4,"T");
			} else {
				pStmt.setString(4,"F");
			}
			pStmt.setString(5,password);				// PASSWORD
			pStmt.setDouble(6,poid);					// POID
			// Execute the UPDATE query
			numRows=pStmt.executeUpdate();
			if (numRows !=1) {
				// Had a problem, so roll back the transaction
				log.error("numRos is not 1");
			}
			// Close the PreparedStatement
			pStmt.close();
		} catch (SQLException ex) {
			// Print the error information
			log.error("SQLException thrown.",ex);
		} catch (DAOException ex) {
			// Print error information
			log.error("Error getting current user from DB",ex);
		}
		// Attempt to clean up the connection.
		try {
			con.close();
		} catch (SQLException ex) {
			log.error("Error trying to close connection.",ex);
		}
		log.trace("UserDAOImpl._update(User, UserContext, Connection) [END]");
	}
	/**
	 * Remove the specified user from the database.
	 * 
	 * @param com.wildstartech.justo.crm.dao.User
	 * @throws com.wildstartech.justo.crm.dao.DAOException
	 */
	public void delete(PersistentUser user, UserContext ctx) throws DAOException {
		log.trace("UserDAOImpl.delete() [BEGIN]");
		long poid;
		int numRows;
		Connection con;
		PreparedStatement pStmt;
		
		// Validate the parameters
		checkObject(user);
		checkObject(ctx,WildDAO.VALIDATE_NULL);
		
		// Get a connection
		con=pm.getConnection();
		// Get the company object's poid
		poid=((UserImpl) user).getPoid();
		if (poid == Long.MIN_VALUE) {
			// The User object has not yet been saved.
			throw new DAOException(UserDAO.ERR_NOT_SAVED);
		} else {
			// The User has been saved, so let's delete it.
			try {
				con.setAutoCommit(false);
				pStmt=con.prepareStatement(QUERY_DELETE);
				pStmt.setLong(1,poid);
				// Execute the DELETE query
				numRows=pStmt.executeUpdate();
				if (numRows==1) {
					con.commit();
				} else {
					con.rollback();
				}
			} catch (SQLException ex) {
				// Print the error informaiton
				log.error("SQLException thrown.",ex);
			} finally {
				try {
					con.close();
				} catch (SQLException ex) {
					log.error("Error trying to close connection.",ex);
				}
			} // END try
			// Remove the user from the cache.
			userCache.remove(new Long(poid));
		} // endif poid == Long.MIN_VALUE
		log.trace("UserDAOImpl.delete() [END]");
	}
	/**
	 * 
	 */
	protected UserImpl findByPrimaryKey(long poid) {
		log.trace("UserDAOImpl.findByPrimaryKey() [BEGIN]");
		Long poidL=new Long(poid);
		UserImpl user=null;
		
		if (userCache.containsKey(poidL)) {
			user=userCache.get(poidL);
		} else {
			// Add the user to the cache
			userCache.put(poidL,user);
		} // END if(userCache.containsKey(poidL)
		log.trace("UserDAOImpl.findByPrimaryKey() [END]");
		return user;
	}
	/**
	 * 
	 */
	public PersistentUser findByPrimaryKey(long key, UserContext ctx) 
	throws DAOException {
		log.trace("UserDAOImpl.findByPrimaryKey(long, UserContext) [BEGIN]");
		PersistentUser user=null;
		Connection con=null;	
		
		try {
			// Obtain a connection
			con=pm.getConnection();
			// Disable auto-commit
			con.setAutoCommit(false);
			// Invoke the "save" logic
			user=findByPrimaryKey(key,ctx,con);
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
					StringBuilder msg=new StringBuilder(128);
					msg.append("SQLException thrown trying to close the ");
					msg.append("connection.");
					log.error(msg.toString(),ex);
				}
			}
		}
		log.trace("UserDAOImpl.findByPrimaryKey(long, UserContext) [END]");
		return user;
	}
	/**
	 * Return a specific <code>User</code> based on the specified <code>poid</code>.
	 * 
	 * @return com.wildstartech.justo.crm.dao.User
	 * @throws PasswordTooLongException 
	 */
	public PersistentUser findByPrimaryKey(long key, UserContext ctx, Connection con) 
	throws DAOException {
		log.trace("UserDAOImpl.findByPrimaryKey(key,ctx) [BEGIN]");
		PreparedStatement pStmt=null;
		ResultSet rs=null;
		UserImpl user=null;
		
		if (key != Long.MIN_VALUE) {
			Long keyL=new Long(key);
			// Check to see if the object is specified in the userList
			if (userCache.containsKey(keyL)) {
				// If the userList contains the user object, then return it.
				user=userCache.get(keyL);
			} else {
				// The specified user does not exist in the cache.
				try {
					// Prepare the statement...
					pStmt=con.prepareStatement(QUERY_SELECT);
					// Specify the poid
					pStmt.setDouble(1,key);
					// Execute the statement
					rs=pStmt.executeQuery();
					// If the query returns a value...
					if (rs.next()) {
						// The resultSet contains a value.
						user = new UserImpl();
						setWildObjectData(user,rs);			// WILD_OBJECT_DATA
						user.setName(rs.getString(6));		// USER_NAME
						// Determine whether or not the person is an agent.
						String agent=rs.getString(7);			// AGENT
						if (agent.compareTo("T")==0) user.setAgent(true);
						else user.setAgent(false);						
						user.setPassword(rs.getString(8));	// PASSWORD
		
					}
					// Close the ResultSet
					rs.close();
					// Close the PreparedStatement
					pStmt.close();
				} catch (SQLException ex) {
					StringBuilder msg=new StringBuilder(128);
					msg.append("SQLException thrown while trying to create ");
					msg.append("a User object.");
					log.error(msg.toString(),ex);
				} catch (UserNameTooLongException ex) {
					log.error("UserNameTooLongException thrown.",ex);
				} catch (PasswordTooLongException ex) {
					log.error("PasswordTooLongException thrown.",ex);
				}
				// Add the newly created user object to the database.
				userCache.put(keyL,user);
			} // END if(userCache.containsKey(keyL)
		} else {
			throw new DAOException(ERR_POID_INVALID);
		}
		log.trace("UserDAOImpl.findByPrimaryKey(key,ctx) [END]");
		return user;
	}
	/**
	 * 
	 */
	public List<PersistentUser> findByName(String name, UserContext ctx) throws DAOException {
		log.trace("UserDAOImpl.findByName(String, UserContext) [BEGIN]");
		List<PersistentUser> userList=null;
		Connection con=null;	
		
		try {
			// Obtain a connection
			con=pm.getConnection();
			// Disable auto-commit
			con.setAutoCommit(false);
			// Invoke the "findByName" logic
			userList=findByName(name,ctx,con);
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
					StringBuilder msg=new StringBuilder(128);
					msg.append("SQLException thrown trying to close the ");
					msg.append("connection.");
					log.error(msg.toString(),ex);
				}
			}
		}
		log.trace("UserDAOImpl.findByName(String, UserContext) [END]");
		return userList;
	}
	/**
	 * Return a list of <code>User</code> objects based a query. 
	 * @return List<User>
	 */
	protected List<PersistentUser> findByName(String name, UserContext ctx, Connection con)
	throws DAOException {
		log.trace("UserDAOImpl.findByName(String,UserContext,Connection) [BEGIN]");
		Long poid;
		PreparedStatement pStmt=null;
		ResultSet rs=null;
		UserImpl user=null;
		ArrayList<PersistentUser> userList = new ArrayList<PersistentUser>();
		
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
					poid=new Long(rs.getLong(1));
					// Check to see if the userCache already contains the
					// specified user.
					if (userCache.containsKey(poid)) {
						// Check to see if the specified user exists in the cache.
						user=userCache.get(poid);
					} else {
						// The user does not exist in the cache, so add it.
						user = new UserImpl();
						setWildObjectData(user,rs);
						user.setName(rs.getString(6));		// user_name
						// Determine whether or not the person is an agent.
						String agent=rs.getString(7);			// AGENT
						if (agent.compareTo("T")==0) user.setAgent(true);
						else user.setAgent(false);						
						user.setPassword(rs.getString(8));	// PASSWORD
						// Add the user to the userList
						userList.add(user);
						// Add the suer to the cache
						userCache.put(poid,user);
						// De-reference the user object
						user=null;
					} // END if(userList.contains(poid)
				}
				// Close the ResultSet
				rs.close();
				// Close the PreparedStatement
				pStmt.close();
			} catch (SQLException ex) {
				StringBuilder msg=new StringBuilder(128);
				msg.append("SQLException thrown while trying to create a ");
				msg.append("User object.");
				log.error(msg.toString(),ex);
			} catch (UserNameTooLongException ex) {
				log.error("UserNameTooLongException thrown.",ex);
			} catch (PasswordTooLongException ex) {
				log.error("PasswordTooLongException thrown.",ex);
			}
		} else {
			throw new DAOException(ERR_POID_INVALID);
		}
		log.trace("UserDAOImpl.findByName(String,UserContext,Connection) [END]");
		return userList;
	}
	/**
	 * Return a complete list of users.
	 * @param ctx
	 * @return
	 */
	public List<PersistentUser> findAll(UserContext ctx) {
		log.trace("UserDAOImpl.findAll(ctx) [BEGIN]");
		ArrayList<PersistentUser> userList=new ArrayList<PersistentUser>();
		Connection con=null;
		Long poid=Long.MIN_VALUE;
		PreparedStatement pStmt=null;
		ResultSet rs=null;
		UserImpl user=null;
		
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
					poid=new Long(rs.getLong(1));
					// Check to see if the User object exists in the cache
					if (userCache.containsKey(poid)) {
						user=userCache.get(poid);
					} else {
						// The specified user object does not exist in the cache
						user = new UserImpl();
						setWildObjectData((WildObjectSQL)user,rs);
						user.setName(rs.getString(6));		// USER_NAME
						// Determine whether or not the person is an agent.
						String agent=rs.getString(7);		// AGENT
						if (agent.compareTo("T")==0) user.setAgent(true);
						else user.setAgent(false);						
						user.setPassword(rs.getString(8));	// PASSWORD
						// Add the user to the cache
						userCache.put(poid,user);
						
					} // END if(userCache.containsKey(poid)
					// Add the user to the userList
					userList.add(user);
					// De-reference the user object
					user=null;
				}
				// Close the ResultSet
				rs.close();
				// Close the PreparedStatement
				pStmt.close();
			} catch (SQLException ex) {
				StringBuilder msg=new StringBuilder(128);
				msg.append("SQLException thrown while trying to create a ");
				msg.append("User object.");
				log.error(msg.toString(),ex);
			} catch (UserNameTooLongException ex) {
				log.error("UserNameTooLongException thrown.",ex);
			} catch (PasswordTooLongException ex) {
				log.error("PasswordTooLongException thrown.",ex);
			}
		//} // END if (ctx != null)
		// Attempt to close the connection
		try {
			con.close();
		} catch (SQLException ex) {
			log.error("SQLException thrown trying to close the Connection.",ex);
		}
		log.trace("UserDAOImpl.findAll(ctx) [END]");
		return userList;
	}
	//**************************************************************************
	//* END CRUD METHODS
	//**************************************************************************
	//**************************************************************************
	//* BEGIN OTHER METHODS
	//**************************************************************************
	/**
	 * 
	 */
	private String encodePassword(String pwd) throws DAOException {
		log.trace("UserDAOImpl.encodePassword(pwd) [BEGIN]");
		log.trace("UserDAOImpl.encodePassword(pwd) [END]");
		return pwd;
	}
	/**
	 * Returns the DAO Identifier key.
	 */
	protected String getDAOIdentifierKey() {
		log.trace("UserDAOImpl.getDAOIdentifierKey() [BEGIN]");
		log.trace("UserDAOImpl.getDAOIdentifierKey() [END]");
		return DAO_IDENTIFIER_KEY;
	}
	/**
	 * Returns a reference to the Singleton instance of <code>UserDAO</code>.
	 * @return UserDAO
	 */
	public static UserDAO getInstance() {
		return userDAO;
	}
	//**************************************************************************
	//* END OTHER METHODS
	//**************************************************************************		
}
