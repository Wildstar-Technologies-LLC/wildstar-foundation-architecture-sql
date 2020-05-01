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
package com.wildstartech.wfa.company.impl;

import com.wildstartech.wfa.company.CompanyDAO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wildstartech.wfa.company.Company;
import com.wildstartech.wfa.company.CompanyNameTooLongException;
import com.wildstartech.wfa.dao.DAOException;
import com.wildstartech.wfa.dao.WildDAO;
import com.wildstartech.wfa.dao.impl.WildDAOImpl;
import com.wildstartech.wfa.dao.user.UserContext;

public final class CompanyDAOImpl extends WildDAOImpl implements CompanyDAO {
	// Log
	private static Log log=LogFactory.getLog(CompanyDAO.class);
	// SQL QUERY FOR INSERT
	private static String QUERY_INSERT=
		"INSERT INTO COMPANY (POID, CREATED_BY, DATE_CREATED, MODIFIED_BY, "+
		"DATE_MODIFIED, COMPANY_NAME) VALUES (?,?,?,?,?,?)";
	// SQL QUERY FOR UPDATE
	private static String QUERY_UPDATE=
		"UPDATE COMPANY SET MODIFIED_BY=?, DATE_MODIFIED=?, COMPANY_NAME=? "+
		"WHERE POID=?";
	// SQL QUERY FOR DELETE
	private static String QUERY_DELETE="DELETE FROM COMPANY WHERE POID=?";
	// Base SQL query for SELECT statemetns
	private static String QUERY_SELECT_BASE=
		"SELECT POID, CREATED_BY, DATE_CREATED, MODIFIED_BY, DATE_MODIFIED, "+
		"COMPANY_NAME FROM COMPANY ";
	// SQL query for primary key lookup.
	private static String QUERY_SELECT=QUERY_SELECT_BASE+"WHERE POID = ?";
	// SQL QUERY FOR SELECT ALL
	private static String QUERY_SELECT_ALL=
		QUERY_SELECT_BASE+" ORDER BY COMPANY_NAME";
	// SQL QUERY FOR SEARCH (Name)
	private static String QUERY_SEARCH=
		QUERY_SELECT_BASE+" WHERE COMPANY_NAME LIKE ? ORDER BY COMPANY_NAME";
	// SQL QUERY FOR SEARCH (Name)
	private static String QUERY_SEARCH_BY_NAME=
		QUERY_SELECT_BASE+" WHERE COMPANY_NAME= ?";
	
	/** Used to identify the DAO */
	protected static String DAO_IDENTIFIER_KEY=
		"com.wildstartech.justo.crm.dao.CompanyDAO";
	// Singleton implementation of the Data Access Object for Company objects.
	private static CompanyDAOImpl companyDAO=new CompanyDAOImpl();
	// Private constructor enabling the Singleton design pattern.
	private CompanyDAOImpl() {
		
	}
	/**
	 * 
	 */
	private static void setCompanyInfo(CompanyImpl company, ResultSet rs) 
	throws SQLException {
		try {
			company.setName(rs.getString(6));
		} catch (CompanyNameTooLongException ex) {
			log.error("This shouldn't happen.",ex);
		}
	}
	/**
	 * Returns a reference to the Company DAO.
	 */
	public static CompanyDAOImpl getInstance() {
		return companyDAO;
	}
	/**
	 * Return a default instance of the <code>Company</code> object.
	 * @return Company
	 */
	public Company create() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return new CompanyImpl();
	}
	/**
	 * Passivate the referenced <code>Company</code> object.
	 * @param Company
	 * @throws DAOException
	 */
	public void save(Company company, UserContext ctx) throws DAOException {
		log.trace("[BEGIN]");
		Connection con=null;
		
		try {
			// Obtain a connection
			con=pm.getConnection();
			// Disable auto-commit
			con.setAutoCommit(false);
			// Invoke the "save" logic
			save(company,ctx,con);
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
		log.trace("[END]");
	}
	protected void save(Company company, UserContext ctx, Connection con) 
	throws DAOException {
		log.trace("[BEGIN]");
		long poid;
		CompanyImpl tmpCompany;
		List<Company> companyList;
		String tmpTRName;
		String tmpDBName;
		
		// Check the object to ensure valid parameters are passed.
		checkObject(company);
		checkObject(ctx,WildDAO.VALIDATE_NULL);
		
		// Get the poid for the object
		
		// Business rule enforcement workflow would go here.
		// Perform the actual work to save the object
		poid=((CompanyImpl) company).getPoid();
		if (poid == Long.MIN_VALUE) {
			// Based upon the poid value, the object has not yet been saved.
			companyList=findByName(company.getName(),ctx,con);
			if (companyList.size() == 1) {
				tmpTRName=company.getName();
				tmpCompany=(CompanyImpl) companyList.get(0);
				tmpDBName=tmpCompany.getName();
				if (tmpTRName.compareTo(tmpDBName) == 0) {
					((CompanyImpl)company).setPoid(tmpCompany.getPoid());
					// Perform an update, not a create.
					_update((CompanyImpl)company,ctx,con);
				} else {
					_create((CompanyImpl)company,ctx,con);
				} // END if (tmpTRName.compareTo(tmpDBName) == 0)
			} else {
				_create((CompanyImpl)company,ctx,con);
			} // END if (companyList.size() == 1)			
		} else {
			// Since a valid poid value is assigned, update the existing record.
			_update(company,ctx,con);
		} // if (poid == Long.MIN_VALUE)
		log.trace("[END]");
	}
	/**
	 * Create a new record. 
	 */
	protected void _create(CompanyImpl company, UserContext ctx, Connection con)
	throws DAOException {
		log.trace("[BEGIN]");
		long poid;
		long currentUserPoid;
		int numRows;
		PreparedStatement pStmt=null;
		Timestamp currentTime=null;
	
		// Get the current user and date/time
		currentUserPoid=getCurrentUser(ctx);
		currentTime=new Timestamp(System.currentTimeMillis());
		
		try {
			// Get the next poid
			poid=getNextPoid(con);
			// Prepare/compile the statement
			pStmt=con.prepareStatement(QUERY_INSERT);
			// Populate the prepared statement with values
			pStmt.setLong(1,poid);						// POID
			pStmt.setLong(2,currentUserPoid);			// CREATED_BY
			pStmt.setTimestamp(3,currentTime);			// DATE_CREATED
			pStmt.setLong(4,currentUserPoid);			// MODIFIED_BY
			pStmt.setTimestamp(5,currentTime);			// DATE_MODIFIED
			pStmt.setString(6,company.getName());		// COMPANY_NAME
			// Execute the INSERT query
			numRows=pStmt.executeUpdate();
			if(numRows!=1) {
				log.error("Update affected other than 1 row.");
				
			}
			// Close the PreparedStatement
			pStmt.close();
			// Store the persistent object identifier with the company object
			company.setPoid(poid);
		} catch (SQLException ex) {
			// Print the error information
			log.error("SQLException thrown.",ex);
		} catch (Throwable ex) {
			log.error("Unknown Exception thrown.",ex);
		}
		log.trace("[END]");
	}
	/**
	 * Saves the specified <code>Group</code> object.
	 * 
	 * @param com.wildstartech.justo.crm.dao.Group
	 * @throws com.wildstartech.justo.crm.dao.DAOException
	 */
	protected void _update(Company company, UserContext ctx, Connection con) 
	throws DAOException {
		log.trace("[BEGIN]");
		long poid;
		long currentUserPoid;
		int numRows;
		PreparedStatement pStmt=null;
		Timestamp currentTime=null;
		
		// Get the current date/time 
		currentTime=new Timestamp(System.currentTimeMillis());
		// Get the current user
		currentUserPoid=getCurrentUser(ctx);
		// Get the current poid for the User code
		poid=((CompanyImpl) company).getPoid();
		
		try {		
			// Prepare/compile the statement
			pStmt=con.prepareStatement(QUERY_UPDATE);
			pStmt.setLong(1,currentUserPoid);			// MODIFIED_BY
			pStmt.setTimestamp(2,currentTime);		// DATE_MODIFIED
			pStmt.setString(3,company.getName());		// USER_NAME
			pStmt.setLong(4,poid);					// POID
			// Execute the UPDATE query
			numRows=pStmt.executeUpdate();
			if (numRows !=1) {
				// Had a problem, so roll back the transaction
				log.error("Probelm encountered.");
				try {
					con.rollback();
				} catch (SQLException ex) {
					StringBuilder msg=new StringBuilder(128);
					msg.append("SQLException thrown trying to roll back the");
					msg.append(" transaction");
					log.error(msg.toString(),ex);
					msg=null;
				}
			}
			// Close the PreparedStatement
			pStmt.close();
		} catch (SQLException ex) {
			// Print the error information
			log.error("SQLException thrown.");
		}
		log.trace("[END]");
	}
	/**
	 * 
	 */
	public void delete(Company company, UserContext ctx) {
		log.trace("[BEGIN]");
		Connection con=null;
		
		try {
			// Obtain a connection
			con=pm.getConnection();
			// Disable auto-commit
			con.setAutoCommit(false);
			// Invoke the "save" logic
			delete(company,ctx,con);
			// Commit the transaction
			con.commit();
		} catch (DAOException ex) {
			// A DAOException was thrown, so rollback the transaction.
			log.error("DAOException thrown.",ex);
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
					msg=null;
				}
			}
		}
		log.trace("[END]");
	}
	/**
	 * Delete the specified <code>Company</code> <code>Object</code>.
	 * @param Company
	 * @return boolean
	 * @throws DAOException
	 */
	protected void delete(Company company, UserContext ctx, Connection con) 
	throws DAOException {
		log.trace("[BEGIN]");
		long poid;
		int numRows;
		PreparedStatement pStmt=null;
		
		// Validate the parameter
		checkObject(company);
		// Get the company object's poid
		poid=((CompanyImpl) company).getPoid();
		if (poid==Long.MIN_VALUE) {
			// The company object has not yet been saved.
			StringBuilder msg=new StringBuilder(80);
			msg.append("Trying to delete a company that has not yet been ");
			msg.append("saved.");
			log.error(msg.toString());
		} else {
			// Obtain a connection to the database.
			con=pm.getConnection();
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
				log.error("SQLException thrown.",ex);
			}
			// Since the object has been deleted, set the poid equal to a minimum value.
			((CompanyImpl) company).setPoid(Long.MIN_VALUE);
		} // END if(poid==Long.MIN_VALUE)		
		log.trace("[END]");
	}
	/**
	 * 
	 */
	public Company findByPrimaryKey(long key, UserContext ctx) {	
		log.trace("[BEGIN]");
		Company company=null;
		Connection con=null;	
		
		try {
			// Obtain a connection
			con=pm.getConnection();
			// Disable auto-commit
			con.setAutoCommit(false);
			// Invoke the "save" logic
			company=findByPrimaryKey(key,ctx,con);
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
					msg=null;
				}
			}
		}
		log.trace("[END]");
		return company;
	}
	/**
	 * 
	 */
	protected Company findByPrimaryKey(long key, UserContext ctx, Connection con)
	throws DAOException {
		log.trace("[BEGIN]");
		
		CompanyImpl company=null;
		PreparedStatement pStmt=null;
		ResultSet rs=null;
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
					company = new CompanyImpl();
					setWildObjectData(company,rs);
					setCompanyInfo(company,rs);
				}
				// Close the ResultSet
				rs.close();
				// Close the PreparedStatement
				pStmt.close();
			} catch (SQLException ex) {
				StringBuilder msg=new StringBuilder(80);
				msg.append("SQLException thrown while trying to create a ");
				msg.append("Company object..");
				log.error(msg.toString(),ex);
				msg=null;
			}
		} else {
			throw new DAOException(ERR_POID_INVALID);
		}
		log.trace("[END]");
		return company;
	}
	/**
	 * 
	 */
	public List<Company> findByName(String name, UserContext ctx) 
	throws DAOException {
		log.trace("[BEGIN]");
		Company company=null;
		Connection con=null;
		List<Company> companyList=null;
		
		companyList=new ArrayList<Company>();
		
		try {
			// Obtain a connection
			con=pm.getConnection();
			// Disable auto-commit
			con.setAutoCommit(false);
			// Invoke the "findByName" logic
			companyList=findByName(name,ctx,con);
			// Commit the transaction
			con.commit();
		} catch (DAOException ex) {
			log.error("DAOExcepthion thrown.",ex);
			throw ex;
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
		log.error("[END]");
		return companyList;
	}
	/**
	 * 
	 * @param name
	 * @param ctx
	 * @param con
	 * @return
	 * @throws DAOException
	 */
	protected List<Company> findByName(String name, UserContext ctx,
			Connection con) throws DAOException {
		log.error("[BEGIN]");
		PreparedStatement pStmt=null;
		ResultSet rs=null;
		CompanyImpl company=null;
		List<Company> companyList=null;
		
		if (name != null) {
			companyList=new ArrayList<Company>();
			
			try {
				// Prepare the statement...
				pStmt=con.prepareStatement(QUERY_SEARCH_BY_NAME);
				// Specify the name
				pStmt.setString(1,name);
				// Execute the statement
				rs=pStmt.executeQuery();
				// Process the resultSet...
				while (rs.next()) {				
					// The resultSet contains a value.
					company = new CompanyImpl();
					setWildObjectData(company,rs);
					setCompanyInfo(company,rs);
					companyList.add(company);
					company=null;
				}
					
				// Close the ResultSet
				rs.close();
				// Close the PreparedStatement
				pStmt.close();
			} catch (SQLException ex) {
				StringBuilder msg=new StringBuilder(80);
				msg.append("SQLException thrown while trying to create a ");
				msg.append("Company object..");
				log.error(msg.toString(),ex);
				msg=null;
			}
		} else {
			throw new DAOException(ERR_POID_INVALID);
		}
		log.trace("[END]");
		return companyList;
	}
	/**
	 * 
	 */
	public List<Company> findAll(UserContext ctx) throws DAOException {
		log.trace("[BEGIN]");
		Connection con=null;	
		List<Company> companyList=null;
		
		try {
			// Obtain a connection
			con=pm.getConnection();
			// Disable auto-commit
			con.setAutoCommit(false);
			// Invoke the "save" logic
			companyList=findAll(ctx,con);
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
					msg.append("SQLException thrown while trying to close the");
					msg.append(" connection.");
					log.error(msg.toString(),ex);
					msg=null;
				}
			} // END if(con!=null)
		}
		log.trace("[END]");
		return companyList;
	}
	/**
	 * 
	 * @param ctx
	 * @param con
	 * @return
	 * @throws DAOException
	 */
	protected List<Company> findAll(UserContext ctx, Connection con) 
	throws DAOException {
		log.trace("[BEGIN]");
		ArrayList<Company> companyList=new ArrayList<Company>();
		CompanyImpl company=null;
		PreparedStatement pStmt=null;
		ResultSet rs=null;
	
		try {
			// Prepare the statement...
			pStmt=con.prepareStatement(QUERY_SELECT_ALL);
			// Execute the statement
			rs=pStmt.executeQuery();
			// Process the resultSet...
			while (rs.next()) {
				// The resultSet contains a value.
				company = new CompanyImpl();
				setWildObjectData(company,rs);
				setCompanyInfo(company,rs);
	
				// Add the group to the groupList
				companyList.add(company);
				// De-reference the company object
				company=null;
			}
			// Close the ResultSet
			rs.close();
			// Close the PreparedStatement
			pStmt.close();
		} catch (SQLException ex) {
			StringBuilder msg=new StringBuilder(80);
			msg.append("SQLException thrown while trying to create a ");
			msg.append("Company object..");
			log.error(msg.toString(),ex);
			msg=null;
		}
		log.trace("[END]");
		return companyList;
	}
	/**
	 * Returns the DAO Identifier key.
	 */
	public String getDAOIdentifierKey() {
		return DAO_IDENTIFIER_KEY;
	}
}