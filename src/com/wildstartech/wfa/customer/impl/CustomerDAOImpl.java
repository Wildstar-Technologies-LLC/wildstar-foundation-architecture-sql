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
package com.wildstartech.wfa.customer.impl;

import com.wildstartech.wfa.person.impl.*;

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
import com.wildstartech.wfa.customer.AccountNumberTooLongException;
import com.wildstartech.wfa.customer.Customer;
import com.wildstartech.wfa.Party;
import com.wildstartech.wfa.company.impl.CompanyDAOImpl;
import com.wildstartech.wfa.person.Person;
import com.wildstartech.wfa.customer.impl.CustomerListElement;
import com.wildstartech.wfa.dao.DAOException;
import com.wildstartech.wfa.dao.WildDAO;
import com.wildstartech.wfa.dao.customer.CustomerDAO;
import com.wildstartech.wfa.dao.impl.WildDAOImpl;
import com.wildstartech.wfa.dao.user.UserContext;

public class CustomerDAOImpl extends WildDAOImpl implements CustomerDAO {
	// Log
	private static Log log=LogFactory.getLog(CustomerDAO.class);
	// SQL QUERY FOR INSERT
	private static String QUERY_INSERT=
		"INSERT INTO CUSTOMER (POID, CREATED_BY, DATE_CREATED, MODIFIED_BY, "+
		"DATE_MODIFIED, CUSTOMER_NAME, ACCOUNT_NUMBER, TYPE, CUSTOMER_POID) "+
		"VALUES (?,?,?,?,?,?,?,?,?)";
	// SQL QUERY FOR UPDATE
	private static String QUERY_UPDATE=
		"UPDATE CUSTOMERS SET MODIFIED_BY=?, DATE_MODIFIED=?, CUSTOMER_NAME=? "+
		"ACCOUNT_NUMBER=?, TYPE=?, CUSTOMER_POID=? WHERE POID=?";
	// SQL QUERY FOR DELETE
	private static String QUERY_DELETE="DELETE FROM CUSTOMERS WHERE POID=?";
	// Base SQL query for SELECT statemetns
	private static String QUERY_SELECT_BASE=
		"SELECT POID, CREATED_BY, DATE_CREATED, MODIFIED_BY, DATE_MODIFIED, "+
		"CUSTOMER_NAME, ACCOUNT_NUMBER, TYPE, CUSTOMER_POID FROM CUSTOMERS ";
	// SQL query for primary key lookup.
	private static String QUERY_SELECT=QUERY_SELECT_BASE+"WHERE POID = ?";
	// SQL QUERY FOR SELECT ALL
	private static String QUERY_SELECT_ALL=
		QUERY_SELECT_BASE+" ORDER BY CUSTOMER_NAME";
	// SQL QUERY FOR SEARCH (AccountNumber)
	private static String QUERY_SEARCH_BY_ACCOUNT=QUERY_SELECT_BASE+
		" WHERE ACCOUNT_NUMBER LIKE ? ORDER BY ACCOUNT_NUMBER";
	// SQL QUERY FOR SEARCH (Name)
	private static String QUERY_SEARCH_BY_NAME=
		QUERY_SELECT_BASE+" WHERE CUSTOMER_NAME LIKE ? ORDER BY CUSTOMER_NAME";
	// SQL QUERY FOR ACCOUNT & NAME SEARCH
	private static String QUERY_SEARCH_BY_ACCOUNT_NAME=QUERY_SELECT_BASE+
		"WHERE ACCOUNT NUMBER LIKE ? AND CUSTOMER_NAME LIKE ? "+
		"ORDER BY CUSTOMER_NAME";
	/** Used to identify the DAO */
	protected static String DAO_IDENTIFIER_KEY=
		"com.wildstartech.justo.crm.dao.CustomerDAO";
	// Singleton implementation of the Data Access Object for Company objects.
	private static CustomerDAOImpl customerDAO=new CustomerDAOImpl();
	// Obtain a reference to a CompanyDAO
	private static CompanyDAOImpl companyDAO=
		(CompanyDAOImpl) pm.getCompanyDAO();
	// Obtain a reference to a PersonDAO
	private static PersonDAOImpl personDAO=(PersonDAOImpl) pm.getPersonDAO();
	/**
	 * Default, no-argument constructor singleton constructor.
	 */
	private CustomerDAOImpl() {
		super();
		
	}
	/**
	 * Populates the <code>Customer</code> using the <code>ResultSet</code>.
	 */
	private static void setCustomerListInfo(
		CustomerListElementImpl customerListElement, ResultSet rs) 
	throws SQLException {
		log.trace("[BEGIN]");
		long customerPoid;
		String accountNumber;
		String name;
		String type;
		
		accountNumber=rs.getString("ACCOUNT_NUMBER");
		name=rs.getString("CUSTOMER_NAME");
		type=rs.getString("TYPE");
		customerPoid=rs.getLong("CUSTOMER_POID");
		
		customerListElement=
			new CustomerListElementImpl(name,accountNumber,type,customerPoid);
		log.trace("[END]");
	}
	/**
	 * Returns a reference to the <code>CustomerDAO</code> object.
	 * 
	 * @CustomerDAO
	 */
	public static CustomerDAO getInstance() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return customerDAO;
	}
	//*************************************************************************
	public Customer create() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return new CustomerImpl();
	}
	/**
	 * 
	 */
	public void delete(Customer customer, UserContext ctx) throws DAOException {
		log.trace("[BEGIN]");
		Connection con=null;
		
		try {
			// Obtain a connection
			con=pm.getConnection();
			// Disable auto-commit
			con.setAutoCommit(false);
			// Invoke the "save" logic
			delete(customer,ctx,con);
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
	 * 
	 * @param customer
	 * @param ctx
	 * @param con
	 * @throws DAOException
	 */
	protected void delete(Customer customer, UserContext ctx, Connection con) 
	throws DAOException {
		log.trace("[BEGIN]");
		long poid;
		int numRows;
		PreparedStatement pStmt=null;
		
		// Validate the parameter
		checkObject(customer);
		// Get the company object's poid
		poid=((CustomerImpl) customer).getPoid();
		if (poid==Long.MIN_VALUE) {
			// The company object has not yet been saved.
			StringBuilder msg=new StringBuilder(80);
			msg.append("Trying to delete a customer that has not yet been ");
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
				if (numRows != 1) {
					con.rollback();
				}
			} catch (SQLException ex) {
				// Print the error information.
				log.error("SQLException thrown.",ex);
			}
			// Since the object has been deleted, set the poid equal to a 
			// minimum value.
			((CustomerImpl) customer).setPoid(Long.MIN_VALUE);
		} // END if(poid==Long.MIN_VALUE)		
		log.trace("[END]");
	}
	/**
	 * Save the customer record to the persistent data store.
	 */
	public void save(Customer customer, UserContext ctx) throws DAOException {
		log.trace("[BEGIN]");
		Connection con=null;
		
		try {
			// Obtain a connection
			con=pm.getConnection();
			// Disable auto-commit
			con.setAutoCommit(false);
			// Invoke the "save" logic
			save(customer,ctx,con);
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
	/**
	 * 
	 * @param customer
	 * @param ctx
	 * @param con
	 * @throws DAOException
	 */
	protected void save(Customer customer, UserContext ctx, Connection con)
	throws DAOException {
		log.trace("[BEGIN]");
		long poid;
		
		// Check the object to ensure valid parameters are passed.
		checkObject(customer);
		checkObject(ctx,WildDAO.VALIDATE_NULL);
		
		// Business rule enforcement workflow would go here.

		// Perform the actual workto save the object
		poid=((CustomerImpl) customer).getPoid();
		if (poid == Long.MIN_VALUE) {
			// Based upon the poid value, the object has not yet been saved.
			_create((CustomerImpl)customer,ctx,con);
		} else {
			// Since a valid poid value is assigned, update the existing record.
			_update(customer,ctx,con);
		}
		log.trace("[END]");
	}
	/**
	 * 
	 */
	protected void _create(Customer customer, UserContext ctx, Connection con) 
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
			pStmt.setString(6,customer.getName());		// CUSTOMER_NAME
														// ACCOUNT_NUMBER
			pStmt.setString(7,customer.getAccountNumber());
			pStmt.setString(8, getType(customer));		// TYPE
			// Execute the INSERT query
			numRows=pStmt.executeUpdate();
			if(numRows!=1) {
				log.error("Update affected other than 1 row.");
			}
			// Close the PreparedStatement
			pStmt.close();
			// Store the persistent object identifier with the customer object
			((CustomerImpl)customer).setPoid(poid);
		} catch (SQLException ex) {
			// Print the error information
			log.error("SQLException thrown.",ex);
			try {
				con.rollback();
			} catch (SQLException e) {
				log.error(ERR_SQLEXCEPTION_ROLLBACK,e);
			}
		} catch (Throwable ex) {
			log.error("Unknown Exception thrown.",ex);
			try {
				con.rollback();
			} catch (SQLException e) {
				log.error(ERR_SQLEXCEPTION_ROLLBACK,e);
			}
		}
		log.trace("[END]");
	}
	/**
	 * 
	 */
	protected void _update(Customer customer, UserContext ctx, Connection con) 
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
		poid=((CustomerImpl) customer).getPoid();
		
		try {		
			// Prepare/compile the statement		
			pStmt=con.prepareStatement(QUERY_UPDATE);
			pStmt.setLong(1,currentUserPoid);			// MODIFIED_BY
			pStmt.setTimestamp(2,currentTime);			// DATE_MODIFIED
			pStmt.setString(3,customer.getName());		// CUSTOMER_NAME
														// ACCOUNT_NUMBER
			pStmt.setString(4,customer.getAccountNumber());
			pStmt.setString(5,getType(customer));		// TYPE
			pStmt.setLong(6,poid);						// POID
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
	//*************************************************************************
	//* END CRUD METHOD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN OTHER METHOD DECLARATIONS
	//*************************************************************************
	//***** type
	private static String getType(Customer customer) {
		log.trace("[BEGIN]");
		Party party=null;
		String type=null;
		party=customer.getCustomer();
		switch(party.getType()) {
		case(Party.TYPE_COMPANY):
			type=CompanyDAOImpl.getInstance().getDAOIdentifierKey();
			break;
		case(Party.TYPE_PERSON):
			type=PersonDAOImpl.getInstance().getDAOIdentifierKey();
			break;
		default:
			type="UNKNOWN";
			break;
		}
		log.trace("[END]");
		return type;
	}
	/*
	 * Returns the DAO_IDENTIFIER_KEY which is used to manaage the next
	 * available Persistent Object IDentifier (POID).
	 */
	protected String getDAOIdentifierKey() {
		return DAO_IDENTIFIER_KEY;
	}
	//***** findAll
	public List<CustomerListElement> findAll(UserContext ctx) throws DAOException {
		log.trace("[BEGIN]");
		Connection con=null;	
		List<CustomerListElement> customerList=null;
		
		try {
			// Obtain a connection
			con=pm.getConnection();
			// Disable auto-commit
			con.setAutoCommit(false);
			// Invoke the "save" logic
			customerList=findAll(ctx,con);
			// Commit the transaction
			con.commit();
		} catch (DAOException ex) {
			/* A DAOException was thrown, the transaction should have already.
			   been rolled back. */
			log.error("DAOException thrown.",ex);
		} catch (SQLException ex) {
			// A SQLException was thrown, so rollback the transaction.
			log.error("SQLException thrown.",ex);
			
			try {
				con.rollback();
			} catch (SQLException ex1) {
				// SQLException was thrown either during the setAutoCommit() or
				// commit() methods
				log.error("SQLException thrown during rollback.",ex1);
			}
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
		return customerList;
	}
	protected List<CustomerListElement> findAll(UserContext ctx, Connection con) 
	throws DAOException {
		log.trace("[BEGIN]");
		ArrayList<CustomerListElement> customerList=
			new ArrayList<CustomerListElement>();
		CustomerListElementImpl customerListElement=null;
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
				customerListElement = new CustomerListElementImpl();
				setWildObjectData(customerListElement,rs);
				setCustomerListInfo(customerListElement,rs);
	
				// Add the customer to the customerList
				customerList.add(customerListElement);
				// De-reference the customer object
				customerListElement=null;
			}
			// Close the ResultSet
			rs.close();
			// Close the PreparedStatement
			pStmt.close();
		} catch (SQLException ex) {
			StringBuilder msg=new StringBuilder(80);
			msg.append("SQLException thrown while trying to create a ");
			msg.append("Customer object..");
			log.error(msg.toString(),ex);
			msg=null;
		}
		log.trace("[END]");
		return customerList;
	}
	//**** findByAccountAndName
	public List<CustomerListElement> findByAccountAndName(String accountNumber,
			String name, UserContext ctx) throws DAOException {
		log.trace("[BEGIN]");
		Connection con=null;	
		List<CustomerListElement> customerList=null;
		
		try {
			// Obtain a connection
			con=pm.getConnection();
			// Disable auto-commit
			con.setAutoCommit(false);
			// Invoke the "save" logic
			customerList=findByAccountAndName(accountNumber,name,ctx,con);
			// Commit the transaction
			con.commit();
		} catch (DAOException ex) {
			/* A DAOException was thrown, the transaction should have already.
			   been rolled back. */
			log.error("DAOException thrown.",ex);
		} catch (SQLException ex) {
			// A SQLException was thrown, so rollback the transaction.
			log.error("SQLException thrown.",ex);
			
			try {
				con.rollback();
			} catch (SQLException ex1) {
				// SQLException was thrown either during the setAutoCommit() or
				// commit() methods
				log.error("SQLException thrown during rollback.",ex1);
			} // END try/catch
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
				} // END try/catch
			} // END if(con!=null)
		}
		log.trace("[END]");
		return customerList;
	}
	public List<CustomerListElement> findByAccountAndName(String accountNumber,
			String name, UserContext ctx, Connection con) throws DAOException {
		log.trace("[BEGIN]");
		// Local variable declarations...
		List<CustomerListElement> customerList=null;
		CustomerListElementImpl customerListElement=null;
		PreparedStatement pStmt=null;
		ResultSet rs=null;
		String accountParam=null;
		String nameParam=null;
		StringBuilder tmpStrBld=null;
		
		customerList=new ArrayList<CustomerListElement>();
		if ((accountNumber != null) && (name != null)) {
			/* neither the accountNumber nor name parameters are null, so build
			   the search string. */
			tmpStrBld=new StringBuilder(accountNumber.length()+2);
			tmpStrBld.append('%');
			tmpStrBld.append(accountNumber);
			tmpStrBld.append('%');
			accountParam=tmpStrBld.toString();
			
			tmpStrBld.delete(0, tmpStrBld.length());
			
			tmpStrBld.append('%');
			tmpStrBld.append(name);
			tmpStrBld.append('%');
			nameParam=tmpStrBld.toString();

			try {
				// Prepare the statement...
				pStmt=con.prepareStatement(QUERY_SEARCH_BY_ACCOUNT_NAME);
				// Set the search criteria...
				pStmt.setString(1, accountParam);
				pStmt.setString(2, nameParam);
				// Execute the statement
				rs=pStmt.executeQuery();
				// Process the resultSet...
				while (rs.next()) {
					// The resultSet contains a value.
					customerListElement = new CustomerListElementImpl();
					setWildObjectData(customerListElement,rs);
					setCustomerListInfo(customerListElement,rs);
		
					// Add the customer to the customerList
					customerList.add(customerListElement);
					// De-reference the customer object
					customerListElement=null;
				} // END while (rs.next())
				// Close the ResultSet
				rs.close();
				// Close the PreparedStatement
				pStmt.close();
			} catch (SQLException ex) {
				StringBuilder msg=new StringBuilder(80);
				msg.append("SQLException thrown while trying to create a ");
				msg.append("Customer object..");
				log.error(msg.toString(),ex);
				msg=null;
			} // END try/catch
		} else {
			/* Either the accountNumber or name parameters passed was a null
			   was passes as a null value. */
			if ((accountNumber == null) && (name==null)) {
				// Both parameters were null, so return all.
				log.trace("No name passed, returning all");
				customerList=findAll(ctx,con);
			} else if (accountNumber!=null) {
				// The AccountNumber is not null
				customerList=findByAccountNumber(accountNumber,ctx,con);
			} else {
				// The name is not null
				customerList=findByName(name,ctx,con);
			}
		} // END if (name != null)
		log.trace("[END]");
		return customerList;
	}
	//**** findByAccountNumber
	public List<CustomerListElement> findByAccountNumber(String accountNumber,
			UserContext ctx) throws DAOException {
		Connection con=null;
		List<CustomerListElement> customerList=null;
		
		if (log.isTraceEnabled()) {
			StringBuilder msg=new StringBuilder(80);
			msg.append("[BEGIN]\n");
			msg.append("accountNumber: ");
			msg.append(accountNumber);
			log.trace(msg.toString());
			msg=null;
		} // END if (log.isTraceEnabled())
		
		try {
			// Obtain a connection
			con=pm.getConnection();
			// Disable auto-commit
			con.setAutoCommit(false);
			// Invoke the "save" logic
			customerList=findByAccountNumber(accountNumber,ctx,con);
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
			} // END if (con != null) 
		}
		log.trace("[END]");
		return customerList;
	}
	/**
	 * Internal method used to locate a specific customer by AccountNumber
	 * 
	 * @param accountNumber
	 * @param ctx
	 * @param con
	 * @return
	 * @throws DAOException
	 */
	protected List<CustomerListElement> 
		findByAccountNumber(String accountNumber, UserContext ctx, 
			Connection con) throws DAOException {
		// Local variable declarations...
		List<CustomerListElement> customerList=null;
		CustomerListElementImpl customerListElement=null;
		PreparedStatement pStmt=null;
		ResultSet rs=null;
		StringBuilder queryParam=null;
		
		if(log.isTraceEnabled()) {
			StringBuilder msg=new StringBuilder(80);
			msg.append("[BEGIN]\n");
			msg.append("accountNumber: ");
			msg.append(accountNumber);
			log.trace(msg.toString());
			msg=null;
		} // END if(log.isTraceEnabled())
		
		if (accountNumber != null) {
			try {
				// Prepare the statement...
				pStmt=con.prepareStatement(QUERY_SEARCH_BY_ACCOUNT);
				// Specify the poid
				pStmt.setString(1,accountNumber);
				// Execute the statement
				rs=pStmt.executeQuery();
				// Process the resultSet...
				while (rs.next()) {
					// The resultSet contains a value.
					customerListElement = new CustomerListElementImpl();
					setWildObjectData(customerListElement,rs);
					setCustomerListInfo(customerListElement,rs);
					// Add the customer to the customerList
					customerList.add(customerListElement);
					// De-reference the customer object
					customerListElement=null;
				} // END while (rs.next())
				
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
		} // END if (accountNumber != null)
		log.trace("[END]");
		return customerList;
	}
	//***** findByName
	public List<CustomerListElement> findByName(String name, UserContext ctx)
			throws DAOException {
		Connection con=null;	
		List<CustomerListElement> customerList=null;
		
		try {
			// Obtain a connection
			con=pm.getConnection();
			// Disable auto-commit
			con.setAutoCommit(false);
			// Invoke the "save" logic
			customerList=findByName(name,ctx,con);
			// Commit the transaction
			con.commit();
		} catch (DAOException ex) {
			/* A DAOException was thrown, the transaction should have already.
			   been rolled back. */
			log.error("DAOException thrown.",ex);
		} catch (SQLException ex) {
			// A SQLException was thrown, so rollback the transaction.
			log.error("SQLException thrown.",ex);
			
			try {
				con.rollback();
			} catch (SQLException ex1) {
				// SQLException was thrown either during the setAutoCommit() or
				// commit() methods
				log.error("SQLException thrown during rollback.",ex1);
			}
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
		return customerList;
	}
	protected List<CustomerListElement>
		findByName(String name, UserContext ctx, Connection con) 
		throws DAOException {
		log.trace("[BEGIN]");
		// Local variable declarations...
		List<CustomerListElement> customerList=null;
		CustomerListElementImpl customerListElement=null;
		PreparedStatement pStmt=null;
		ResultSet rs=null;
		StringBuilder queryParam=null;
		
		customerList=new ArrayList<CustomerListElement>();
		if (name != null) {
			// The name parameter is not null, so build the search string
			queryParam=new StringBuilder(name.length()+2);
			queryParam.append('%');
			queryParam.append(name);
			queryParam.append('%');
			
			try {
				// Prepare the statement...
				pStmt=con.prepareStatement(QUERY_SEARCH_BY_NAME);
				// Set the search criteria...
				pStmt.setString(1, queryParam.toString());
				
				// Execute the statement
				rs=pStmt.executeQuery();
				// Process the resultSet...
				while (rs.next()) {
					// The resultSet contains a value.
					customerListElement = new CustomerListElementImpl();
					setWildObjectData(customerListElement,rs);
					setCustomerListInfo(customerListElement,rs);
		
					// Add the customer to the customerList
					customerList.add(customerListElement);
					// De-reference the customer object
					customerListElement=null;
				} // END while (rs.next())
				// Close the ResultSet
				rs.close();
				// Close the PreparedStatement
				pStmt.close();
			} catch (SQLException ex) {
				StringBuilder msg=new StringBuilder(80);
				msg.append("SQLException thrown while trying to create a ");
				msg.append("Customer object..");
				log.error(msg.toString(),ex);
				msg=null;
			} // END try/catch
		} else {
			// The name parameter was passes as a null value...
			log.trace("No name passed, returning all");
			customerList=findAll(ctx,con);
		} // END if (name != null)
		log.trace("[END]");
		return customerList;
	}
}