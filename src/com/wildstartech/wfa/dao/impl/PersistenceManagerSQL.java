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

import com.wildstartech.wfa.workorder.impl.*;
import com.wildstartech.wfa.document.impl.DocumentDAOImpl;
import com.wildstartech.wfa.company.impl.CompanyDAOImpl;
import com.wildstartech.wfa.customer.impl.CustomerDAOImpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wildstartech.wfa.dao.AuthenticationException;
import com.wildstartech.wfa.company.CompanyDAO;
import com.wildstartech.wfa.dao.DateSequenceDAO;
import com.wildstartech.wfa.document.DocumentDAO;
import com.wildstartech.wfa.person.PersonDAO;
import com.wildstartech.wfa.role.RoleDAO;
import com.wildstartech.wfa.dao.PersistenceManager;
import com.wildstartech.wfa.dao.SequenceDAO;
import com.wildstartech.wfa.dao.customer.CustomerDAO;
import com.wildstartech.wfa.dao.group.GroupDAO;
import com.wildstartech.wfa.dao.user.PersistentUser;
import com.wildstartech.wfa.dao.user.UserContext;
import com.wildstartech.wfa.dao.user.UserDAO;
import com.wildstartech.wfa.workorder.WorkOrderDAO;

public abstract class PersistenceManagerSQL implements PersistenceManager {
	private static String PWD_QUERY=
		"SELECT POID, AGENT FROM USERS WHERE USER_NAME=? AND PASSWORD=?";
	private static Long sessionId=new Long(0);
	// Obtain a reference to the logger for this object.
	private static Log log=LogFactory.getLog(PersistenceManager.class);
	protected DataSource dataSource;
	public PersistenceManagerSQL() {
	}
	/**
	 * Provides a means to set the <code>DataSource</code> which is used to obtain
	 * a connection to the database.
	 * @param ds
	 */
	protected void setDataSource(DataSource ds) {
		dataSource=ds;
	}
	/**
	 * 
	 */
	public UserContext authenticate(UserContext ctx) 
	throws AuthenticationException {
		boolean userFound=false;
		Connection con=null;
		PreparedStatement pStmt=null;
		ResultSet rs=null;
		String userName=null;
		String password=null;
		PersistentUser user=null;
		
		// Get the user account information
		user=ctx.getUser();
		userName=user.getName();
		password=user.getPassword();
		
		con=getConnection();
		
		// Look for the user
		if (con != null) {
			try {
				// Build the query
				pStmt=con.prepareStatement(PWD_QUERY);
				pStmt.setString(1,userName);
				pStmt.setString(2,password);
				rs=pStmt.executeQuery();
				userFound=rs.next();
				if (userFound) {
					// The user was found, so set the POID for the user.
					String agent=null;
					((UserImpl)user).setPoid(rs.getLong(1));
					agent=rs.getString(2);
					// Check to see if the current user is an agent
					if (agent.toUpperCase().compareTo("T") == 0) {
						user.setAgent(true);
					} else {
						user.setAgent(false);
					}
				}
			} catch (SQLException ex) {
				// Handle an SQLException
				log.error("SQLException thrown looking for the user.",ex);
			} finally {
				// Whether or not an exception is thrown, clean up.
				try {
					if (con != null) {con.close();}
				} catch (SQLException e) {
					// NO-OP
				}
			}  // END try
		} // END if (con!=null)
		
		if (userFound == false) {
			// If the user wasn't found, then throw an exception;
			log.error("The specified user was not found.");
			throw new AuthenticationException();
		}
		
		// A little multi-threaded protection... 
		synchronized (sessionId) {
			ctx.setSessionId(String.valueOf(sessionId++));
		}
		
		return ctx;
	}
	/**
	 * Returns a reference to a connection
	 */
	public Connection getConnection() {
		// Begin local method variable declaration		
		Connection connection=null;
		// Obtain the connection
		if (dataSource != null) {
			try {
				connection=(dataSource.getConnection());
			} catch (SQLException ex) {
				StringBuilder msg=new StringBuilder(128);
				msg.append("SQLExceptoin thrown obtaining connection to the ");
				msg.append("DataSource.");
				log.error(msg.toString(),ex);
				msg=null;
			}
		} else {
			log.error("The dataSource is NULL.");
		}
		return connection;
	}
	public PersistentUser getUser(String authId) {
		return null;
	}
	//***** DAO 
	/**
	 * Returns a reference to an instance of <code>CompanyDAO</code>
	 * 
	 */
	public CompanyDAO getCompanyDAO() {
		return CompanyDAOImpl.getInstance();
	}
	/**
	 * Returns a reference to an instance of <code>CustomerDAO</code>.
	 * @return CustomerDAO 
	 */
	public CustomerDAO getCustomerDAO() {
		return CustomerDAOImpl.getInstance();
	}
	/**
	 * Returns a reference to an instance of <code>DocumentDAO</code>.
	 * @return DocumentDAO
	 */
	public DocumentDAO getDocumentDAO() {
		return DocumentDAOImpl.getInstance();
	}
	/**
	 * Returns a reference to an instance of <code>DateSequenceDAO</code>.
	 * @return DateSequenceDAO
	 */
	public DateSequenceDAO getDateSeqeuenceDAO() {
		return DateSequenceDAOImpl.getInstance();
	}
	/**
	 * Returns a reference to an instance of <code>GroupDAO</code>
	 */
	public GroupDAO getGroupDAO() {
		return GroupDAOImpl.getInstance();
	}
	/**
	 * Returns a reference to an instance of <code>PersonDAO</code>
	 */
	public PersonDAO getPersonDAO() {
		return PersonDAOImpl.getInstance();
	}
	/**
	 * Returns a reference to an instance of <code>UserDAO</code>
	 */
	public UserDAO getUserDAO() {
		return UserDAOImpl.getInstance();
	}
	/**
	 * Returns a reference to an instance of <code>RoleDAO</code>.
	 */
	public RoleDAO getRoleDAO() {
		return RoleDAOImpl.getInstance();
	}
	/**
	 * Returns a reference to an instance of <code>SequenceDAO</code>.
	 */
	public SequenceDAO getSequenceDAO() {
		return SequenceDAOImpl.getInstance();
	}
	/**
	 * Returns a reference to an instance of <code>WorkOrderDAO</code>
	 */
	public WorkOrderDAO getWorkOrderDAO() {
		return WorkOrderDAOImpl.getInstance();
	}
	public boolean validateAuthenticationId(String authId) {
		return true;
	}
}
