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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wildstartech.wfa.dao.DAOException;
import com.wildstartech.wfa.dao.user.PersistentUser;
import com.wildstartech.wfa.dao.user.UserContext;
import com.wildstartech.wfa.dao.user.UserDAO;
import com.wildstartech.wfa.dao.user.UserPreference;

public class UserPreferenceDAOImpl extends WildDAOImpl {
	//*************************************************************************
	//* BEGIN STATIC FIELD DECLARATIONS
	//*************************************************************************
	// SQL QUERY FOR INSERT
	private static String QUERY_INSERT=
		"INSERT INTO USER_PREFERENCES (POID, CREATED_BY, DATE_CREATED, " +
		"MODIFIED_BY, DATE_MODIFIED, USER_POID, LABEL, PROPERTY) VALUES " +
		"(?,?,?,?,?,?,?,?)";
	// SQL QUERY FOR DELETE
	private static String QUERY_DELETE=
		"DELETE FROM USER_PREFERENCES WHERE POID=?";
	// SQL QUERY FOR UPDATE
	private static String QUERY_UPDATE=
		"UPDATE USERS SET MODIFIED_BY=?, DATE_MODIFIED=?, USER_POID=?, "+
		"LABEL=?, PROPERTY=? WHERE POID=?";
	// SQL QUERY FOR SELECT
	private static String QUERY_SELECT_BY_USER=
		"SELECT POID, CREATED_BY, DATE_CREATED, MODIFIED_BY, DATE_MODIFIED, "+
		"LABEL, PROPERTY FROM USER PREFERENCES WHERE USER_POID = ?";
	/** Log */
	private static Log log=LogFactory.getLog(UserDAO.class);	
 	/** Used to identify the DAO */
	private final static String DAO_IDENTIFIER_KEY=
		"com.wildstartech.justo.crm.dao.UserPreferenceDAO";
	/**
	 * Singleton implementation of the Data Access Object for User objects.
	 */
	private static UserPreferenceDAOImpl userPreferenceDAO=
		new UserPreferenceDAOImpl();
	//*************************************************************************
	//* END STATIC FIELD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN INSTANCE FIELD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* END INSTANCE FIELD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN CONSTRUCTOR DECLARATIONS
	//*************************************************************************
	// Private constructor enabling the Singleton design pattern.
	private UserPreferenceDAOImpl() {
		super();
		log.trace("[BEGIN]");
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
	//* BEGIN CRUD METHOD DECLARATIONS
	//*************************************************************************
	public UserPreference create(PersistentUser user) {
		return new UserPreferenceImpl((UserImpl) user);
	}
	/**
	 * Remove the specified <code>UserPreference</code> object.
	 */
	public void delete(UserPreference preference, UserContext ctx) 
	throws DAOException {
		// TODO Auto-generated method stub
		
	}
	/**
	 * Remove the specified <code>UserPreference</code> from the database. 
	 * 
	 * @param UserPreference preference
	 * @param UserContext ctx
	 * @param Connection con
	 */
	protected void delete(UserPreference preference, UserContext ctx, 
		Connection con) {
		
	}
	public UserPreference findByUser(PersistentUser user, UserContext ctx) {
		// TODO Auto-generated method stub
		return null;
	}
	public void save(UserPreference preference, UserContext ctx) 
	throws DAOException {
		// TODO Auto-generated method stub
		
	}
	//*************************************************************************
	//* END CRUD METHOD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN OTHER METHOD DECLARATIONS
	//*************************************************************************
	/**
	 * Returns the DAO Identifier key.
	 */
	protected String getDAOIdentifierKey() {
		log.trace("UserDAOImpl.getDAOIdentifierKey() [BEGIN]");
		log.trace("UserDAOImpl.getDAOIdentifierKey() [END]");
		return DAO_IDENTIFIER_KEY;
	}
	/**
	 * Obtain a reference to the DAO responsible for storing preference data. 
	 * 
	 * @return UserPreferenceDAO
	 */
	public UserPreferenceDAOImpl getInstance() {
		return userPreferenceDAO;
	}
	//*************************************************************************
	//* END OTHER METHOD DECLARATIONS
	//*************************************************************************
}
