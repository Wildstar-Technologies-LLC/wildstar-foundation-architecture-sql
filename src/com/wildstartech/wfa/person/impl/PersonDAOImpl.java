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
package com.wildstartech.wfa.person.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wildstartech.wfa.person.Person;
import com.wildstartech.wfa.person.PersonNameTooLongException;
import com.wildstartech.wfa.dao.DAOException;
import com.wildstartech.wfa.person.PersonDAO;
import com.wildstartech.wfa.person.PersonListElement;
import com.wildstartech.wfa.dao.WildDAO;
import com.wildstartech.wfa.dao.impl.WildDAOImpl;
import com.wildstartech.wfa.dao.user.UserContext;
/**
 * DAO responsible for managing access to <code>Person</code> objects.
 * 
 * <h1>SQL Table Definition</h1>
 * <table>
 * 	<tr>
 * 		<th>Column Name</th>
 * 		<th>SQL Data Type</th>
 * 		<th>Java Data Type</th>
 * 		<th>Description</th>
 * 	</tr>
 * 	<tr>
 * 		<td>POID</td>
 * 		<td>BIGINT</td>
 * 		<td>Long</td>
 * 		<td>
 * 			The <strong>P</strong>ersistent <strong>O</strong>bject 
 * 			<strong>ID</strong>entifier is a unique identifier for the object in
 * 			the persistent data store.
 * 		</td>
 * 	</tr>
 * 	<tr>
 * 		<td>PREFIX</td>
 * 		<td>VARCHAR(10)</td>
 * 		<td>String</td>
 * 		<td>
 * 		</td>
 * 	</tr>
 * </table>
 * @author derekberube
 * @version 1.0 Jun 24, 2006
 */
public final class PersonDAOImpl extends WildDAOImpl implements PersonDAO {
	//*************************************************************************
	//* BEGIN STATIC FIELD DECLARATIONS
	//*************************************************************************
	//
	private static Log log=LogFactory.getLog(PersonDAO.class);
	// SQL QUERY FOR INSERT
	private static String QUERY_INSERT=
		"INSERT INTO PEOPLE (POID, CREATED_BY, DATE_CREATED, MODIFIED_BY, "+
		"DATE_MODIFIED, PREFIX, FIRST_NAME, LAST_NAME, SUFFIX, DISPLAY_NAME, "+
		"BIRTHDATE, GENDER) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
	// SQL QUERY FOR UPDATE
	private static String QUERY_UPDATE=
		"UPDATE PEOPLE SET MODIFIED_BY=?, DATE_MODIFIED=?, PREFIX=?, "+
		"FIRST_NAME=?, LAST_NAME=?, SUFFIX=?, DISPLAY_NAME=?, BIRTHDATE=?, "+
		"GENDER=? WHERE POID=?";
	// SQL QUERY FOR DELETE
	private static String QUERY_DELETE="DELETE FROM PEOPLE WHERE POID=?";
	// Base SQL query for SELECT
	private static String QUERY_SELECT_BASE=
		"SELECT POID, CREATED_BY, DATE_CREATED, MODIFIED_BY, DATE_MODIFIED, "+
		"PREFIX, FIRST_NAME, LAST_NAME, SUFFIX, DISPLAY_NAME, BIRTHDATE, "+
		"GENDER FROM PEOPLE ";
	// SQL query for SELECT by Primary Key
	private static String QUERY_SEARCH_PRIMARY_KEY=
		QUERY_SELECT_BASE+" WHERE POID=? ORDER BY POID";
	// SQL QUERY FOR SEARCH (First Name)
	private static String QUERY_SEARCH_FIRST_NAME=
		QUERY_SELECT_BASE+" WHERE FIRST_NAME LIKE ? ORDER BY FIRST_NAME";
	// SQL QUERY FOR SEARCH (Last Name)
	private static String QUERY_SEARCH_LAST_NAME=
		QUERY_SELECT_BASE+" WHERE LAST_NAME LIKE ? ORDER BY LAST_NAME";
	// SQL QUERY FOR SEARCH (Display Name)
	private static String QUERY_SEARCH_DISPLAY_NAME=
		QUERY_SELECT_BASE+" WHERE DISPLAY_NAME LIKE ? ORDER BY LAST_NAME";
	/** Used to identify the DAO */
	protected static String DAO_IDENTIFIER_KEY=
		"com.wildstartech.justo.crm.dao.PersonDAO";
	// Singleton implementation of the Data Access Object for Company objects.
	/* Provides a reference to the PersonDAOIMpl object which is returned by
	 * the <code>getInstance</code> method.
	 */
	private static PersonDAOImpl personDAO=new PersonDAOImpl();
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
	/**
	 * Default, no-argument constructor.
	 * 
	 * The use of a private default constructor preserves the singleton nature
	 * of the <code>PersonDAOImpl</code> object.
	 */
	private PersonDAOImpl() {
		
	}
	//*************************************************************************
	//* END CONSTRUCTOR DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN STATIC METHOD DECLARATIONS
	//*************************************************************************
	/**
	 * Populates <code>Person</code> object with information
	 */
	private static void populatePersonInfo(PersonImpl person, ResultSet rs) 
	throws SQLException {
		log.trace("[BEGIN]");
		String fieldName=null;

		try {
			// PREFIX
			fieldName="PREFIX";
			person.setPrefix(rs.getString("PREFIX"));
			
			// FIRST_NAME
			fieldName="FIRST_NAME";
			person.setFirstName(rs.getString("FIRST_NAME"));
			
			// LAST_NAME
			fieldName="LAST_NAME";
			person.setLastName(rs.getString("LAST_NAME"));
			
			// SUFFIX
			fieldName="SUFFIX";
			person.setSuffix(rs.getString("SUFFIX"));
			
			// DISPLAY_NAME
			fieldName="DISPLAY_NAME";
			person.setDisplayName(rs.getString("DISPLAY_NAME"));
		
			// BIRTHDATE
			fieldName="BIRTHDATE";
			Timestamp birthdate=rs.getTimestamp("BIRTHDATE");
			if (birthdate != null) {
				person.setBirthdate(new Date(birthdate.getTime()));
			} // END if (birthdate != null)
		
			// GENDER
			fieldName="GENDER";
			person.setGender(rs.getInt("GENDER"));
		} catch (PersonNameTooLongException ex) {
			if (log.isErrorEnabled()) {
				StringBuilder msg=new StringBuilder(80);
				msg.append("PersonNameTooLongException thrown.  Field Name: ");
				msg.append(fieldName);
				msg.append("\nThis should never happen.");
				log.error(msg.toString(),ex);
				msg=null;
			} // END if (log.isErrorEnabled())
		} catch (SQLException ex) {
			if (log.isErrorEnabled()) {
				StringBuilder msg=new StringBuilder(80);
				msg.append("SQLException thrown.  Field Name: ");
				msg.append(fieldName);
				msg.append("\nThis should never happen.");
				log.error(msg.toString(),ex);
				msg=null;
			} // END if (log.isErrorEnabled())
		} // END try/catch
		log.trace("[END]");
	}
	/**
	 * Returns a default, empty <code>Person</code> object.
	 */
	public Person create() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return new PersonImpl();
	}
	/**
	 * Passivate the referenced <code>Person</code> object.
	 */
	public void save(Person person, UserContext ctx) throws DAOException {
		log.trace("[BEGIN]");
		Connection con=null;
		
		try {
			// Obtain a connection
			con=pm.getConnection();
			// Disable auto-commit
			con.setAutoCommit(false);
			// Invoke the "save" logic
			save(person,ctx,con);
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
			// Re-throw the DAOException
			throw ex;
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
					log.error(msg.toString(),ex);
				}
			}
		}
		log.trace("[END]");
	}
	/**
	 * Passivate the referenced <code>Person</code> object.
	 * @param Company
	 * @param UserContext
	 * @throws DAOException
	 */
	protected void save(Person person, UserContext ctx, Connection con)
	throws DAOException {
		log.trace("[BEGIN]");
		long poid;
		
		// Check the object to ensure valid parameters are passed.
		checkObject(person);
		checkObject(ctx,WildDAO.VALIDATE_NULL);
		
		// Business rule enforcement workflow would go here.

		// Perform the actual workto save the object
		poid=((PersonImpl) person).getPoid();
		if (poid == Long.MIN_VALUE) {
			// Based upon the poid value, the object has not yet been saved.
			_create((PersonImpl)person,ctx,con);
		} else {
			// Since a valid poid value is assigned, update the existing record.
			_update((PersonImpl)person,ctx,con);
		}
		log.trace("[END]");
	}
	/**
	 * Creates a record for the <code>Person</code> object in the database.
	 * @param person
	 * @param ctx
	 */
	private void _create(PersonImpl person, UserContext ctx, Connection con)
	throws DAOException {
		log.trace("[BEGIN]");
		long poid;
		long currentUserPoid;
		int numRows;
		PreparedStatement pStmt=null;
		String tmpString=null;
		Timestamp currentTime=null;
	
		// Get the current user and date/time information
		currentTime=new Timestamp(System.currentTimeMillis());
		currentUserPoid=getCurrentUser(ctx);
		
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
			// PREFIX
			tmpString=person.getPrefix();
			if ((tmpString != null) && (tmpString.compareTo("")!=0)) {
				pStmt.setString(6,tmpString);
			} else {
				pStmt.setNull(6,Types.VARCHAR);
			}
			// FIRST_NAME
			tmpString=person.getFirstName();
			if ((tmpString != null) && (tmpString.compareTo("")!=0)) {
				pStmt.setString(7,tmpString);
			} else {
				pStmt.setNull(7,Types.VARCHAR);
			}
			// LAST_NAME
			tmpString=person.getLastName();
			if ((tmpString != null) && (tmpString.compareTo("")!=0)) {
				pStmt.setString(8,tmpString);
			} else {
				pStmt.setNull(8,Types.VARCHAR);
			}
			// SUFFIX
			tmpString=person.getSuffix();
			if ((tmpString != null) && (tmpString.compareTo("")!=0)) {
				pStmt.setString(9,tmpString);
			} else {
				pStmt.setNull(9,Types.VARCHAR);
			}
			// DISPLAY_NAME
			tmpString=person.getDisplayName();
			if ((tmpString != null) && (tmpString.compareTo("")!=0)) {
				pStmt.setString(10,tmpString);
			} else {
				pStmt.setNull(10,Types.VARCHAR);
			}
			// BIRTHDATE
			Date birthdate=person.getBirthdate();
			if (birthdate!=null) {
				pStmt.setTimestamp(11,new Timestamp(birthdate.getTime()));
			} else {
				pStmt.setNull(11,Types.TIMESTAMP);
			}
			// GENDER
			int gender=person.getGender();
			if((gender == Person.GENDER_MALE) || (gender==Person.GENDER_FEMALE)) {
				// Only allowable values are Male/Female
				pStmt.setInt(12,gender);
			} else {
				pStmt.setNull(12,Types.INTEGER);
			}
			// Execute the INSERT query
			numRows=pStmt.executeUpdate();
			if (numRows!=1) {
				log.error("PersonDAOImpl INSERT ISSUE numRows="+numRows);
			} // END if (numRows!=1)
			// Close the PreparedStatement
			pStmt.close();
			// Set the poid for the Person object
			person.setPoid(poid);
		} catch (SQLException ex) {
			// Print the error information
			log.error("SQLException thrown.",ex);
		} catch (Throwable ex) {
			log.error("Error thrown.",ex);
		}
		log.trace("[END]");
	}
	/**
	 * Updates the specified <code>Person</code> object in the database.
	 * @param person
	 * @param ctx
	 */
	private void _update(PersonImpl person, UserContext ctx, Connection con)
	throws DAOException {
		log.trace("[BEGIN]");
		long poid=Long.MIN_VALUE;
		long currentUserPoid=Long.MIN_VALUE;
		int gender=Integer.MIN_VALUE;
		int numRows;
		Date birthdate=null;
		PreparedStatement pStmt=null;
		String tmpString=null;
		Timestamp currentTime=null;
	
		// Get the current user and date/time information
		currentTime=new Timestamp(System.currentTimeMillis());
		currentUserPoid=getCurrentUser(ctx);
		
		try {
			// Prepare/compile the statement
			pStmt=con.prepareStatement(QUERY_UPDATE);
			// Populate the prepared statement with values
			pStmt.setLong(1,currentUserPoid);			// MODIFIED_BY
			pStmt.setTimestamp(2,currentTime);		// DATE_MODIFIED
			// PREFIX
			tmpString=person.getPrefix();
			if ((tmpString != null) && (tmpString.compareTo("")!=0)) {
				pStmt.setString(3,tmpString);
			} else {
				pStmt.setNull(3,Types.VARCHAR);
			}
			// FIRST_NAME
			tmpString=person.getFirstName();
			if ((tmpString != null) && (tmpString.compareTo("")!=0)) {
				pStmt.setString(4,tmpString);
			} else {
				pStmt.setNull(4,Types.VARCHAR);
			}
			// LAST_NAME
			tmpString=person.getLastName();
			if ((tmpString != null) && (tmpString.compareTo("")!=0)) {
				pStmt.setString(5,tmpString);
			} else {
				pStmt.setNull(5,Types.VARCHAR);
			}
			// SUFFIX
			tmpString=person.getSuffix();
			if ((tmpString != null) && (tmpString.compareTo("")!=0)) {
				pStmt.setString(6,tmpString);
			} else {
				pStmt.setNull(6,Types.VARCHAR);
			}
			// DISPLAY_NAME
			tmpString=person.getDisplayName();
			if ((tmpString != null) && (tmpString.compareTo("")!=0)) {
				pStmt.setString(7,tmpString);
			} else {
				pStmt.setNull(7,Types.VARCHAR);
			}
			// BIRTHDATE
			birthdate=person.getBirthdate();
			if(birthdate!=null) {
				pStmt.setTimestamp(8,new Timestamp(birthdate.getTime()));
			} else {
				pStmt.setNull(8,Types.TIMESTAMP);
			}
			// GENDER
			gender=person.getGender();
			if ((gender==Person.GENDER_FEMALE) || 
				(gender==Person.GENDER_MALE)) {
				pStmt.setInt(9,Types.INTEGER);
			} else {
				pStmt.setNull(9,Types.INTEGER);
			}
			// POID
			pStmt.setLong(8,poid);
			
			// Execute the INSERT query
			numRows=pStmt.executeUpdate();
			if(numRows!=1) {
				log.error("PersonDAOImpl UPDATE ISSUE numRows="+numRows);
			}
			// Close the PreparedStatement
			pStmt.close();
		} catch (SQLException ex) {
			// Print the error information
			log.error("SQLException thrown.",ex);
			
		} catch (Throwable ex) {
			log.error("Unknown exception thrown.",ex);
		}
		log.trace("[END]");
	}
	/**
	 * 
	 */
	public void delete(Person person, UserContext ctx) throws DAOException {
		log.trace("[BEGIN]");
		Connection con=null;
		
		try {
			// Obtain a connection
			con=pm.getConnection();
			// Disable auto-commit
			con.setAutoCommit(false);
			// Invoke the "save" logic
			delete(person,ctx,con);
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
			// Re-throw the DAOException
			throw ex;
		} catch (SQLException ex) {
			log.error("DEBUG: SQLException thrown.",ex);
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
	}
	/**
	 * 
	 */
	protected void delete(Person person, UserContext ctx, Connection con)
	throws DAOException {
		log.trace("[BEGIN]");
		long poid;
		int numRows;
		PreparedStatement pStmt=null;
		
		// Validate the parameter
		checkObject(person);
		// Get the company object's poid
		poid=((PersonImpl) person).getPoid();
		if (poid==Long.MIN_VALUE) {
			// The Person object has not yet been saved.
			log.error("Trying to delete a Person that has not been saved.");
		} else {
			// The company object has been saved.
			try {
				// Build the query
				pStmt=con.prepareStatement(QUERY_DELETE);
				pStmt.setDouble(1,poid);
				// Execute the DELETE query
				numRows=pStmt.executeUpdate();
				// Close the statement
				pStmt.close();
			} catch (SQLException ex) {
				// Print the error information.
				ex.printStackTrace(System.err);
			}
		} // END if(poid==Long.MIN_VALUE)	
		log.trace("[END]");
	}
	//***** findByPrimaryKey
	public Person findByPrimaryKey(long key, UserContext ctx) {
		log.trace("[BEGIN]");
		Connection con=null;	
		PersonImpl person=null;
		
		try {
			// Obtain a connection
			con=pm.getConnection();
			// Disable auto-commit
			con.setAutoCommit(false);
			// Invoke the "save" logic
			person=findByPrimaryKey(key,ctx,con);
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
		return person;
	}
	protected PersonImpl 
	findByPrimaryKey(long key, UserContext ctx, Connection con) 
	throws DAOException {
		if (log.isTraceEnabled()) {
			StringBuilder msg=new StringBuilder(80);
			msg.append("[BEGIN]\n");
			msg.append("Requested Key: ");
			msg.append(key);
			log.trace(msg.toString());
		} // END if (log.isTraceEnabled())
		PersonImpl person=null;
		PreparedStatement pStmt=null;
		ResultSet rs=null;
		con=pm.getConnection();
		if (key != Long.MIN_VALUE) {
			try {
				// Prepare the statement...
				pStmt=con.prepareStatement(QUERY_SEARCH_PRIMARY_KEY);
				// Specify the poid
				pStmt.setLong(1,key);
				// Execute the statement
				rs=pStmt.executeQuery();
				// If the query returns a value...
				if (rs.next()) {
					// The resultSet contains a value.
					person = new PersonImpl();
					setWildObjectData(person,rs);
					populatePersonInfo(person,rs);
				}
				// Close the ResultSet
				rs.close();
				// Close the PreparedStatement
				pStmt.close();
			} catch (SQLException ex) {
				StringBuilder msg=new StringBuilder(80);
				msg.append("SQLException thrown while trying to create a ");
				msg.append("Person object.");
				log.error(msg.toString(),ex);
			}
		} else {
			throw new DAOException(ERR_POID_INVALID,new Object[] {key});
		}
		// Attempt to close the connection
		try {
			con.close();
		} catch (SQLException ex) {
			log.error("SQLException thrown trying to close the connection",ex);
		}
		log.trace("[END]");
		
		return person;
	}
	/**
	 * 
	 */
	public List<Person> 
		findByFirstName(String firstName, UserContext ctx) 
		throws DAOException {
		log.trace("[BEGIN]");
		Connection con=null;
		List<Person> personList=null;
		
		try {
			// Obtain a connection
			pm.getConnection();
			// Disable auto-commit
			con.setAutoCommit(false);
			// Invoke the "save" logic
			personList=findAll(ctx,con);
			// Commit the transaction
			con.commit();
		} catch (DAOException ex) {
			// A DAOException was thrown, so rollback the transaction.
			try {
				con.rollback();
			} catch (SQLException ex1) {
				// SQLException was thrown either during the setAutoCommit() or
				// commit() methods
				System.err.println("DEBUG: SQLException thrown during rollback.");
			}
		} catch (SQLException ex) {
			System.err.print("DEBUG: SQLException thrown.");
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException ex) {
					System.err.print("DEBUG: SQLException thrown trying to close");
					System.err.println(" the connection.");
				}
			}
		}
		log.trace("[END]");
		return personList;
	}
	/**
	 * 
	 */
	protected List<Person> findByFirstName(String firstName, UserContext ctx, 
			Connection con) throws DAOException {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return null;
	}
	/**
	 * 
	 */
	public List<Person> findByLastName(String lastName, UserContext ctx)
	throws DAOException {
		log.trace("[BEGIN]");
		Connection con=null;
		List<Person> personList=null;
		
		try {
			// Obtain a connection
			pm.getConnection();
			// Disable auto-commit
			con.setAutoCommit(false);
			// Invoke the "save" logic
			personList=findByLastName(lastName,ctx,con);
			// Commit the transaction
			con.commit();
		} catch (SQLException ex) {
			log.error("SQLException thrown.",ex);
		} catch (DAOException ex) {
			// A DAOException was thrown, so rollback the transaction.
			try {
				con.rollback();
			} catch (SQLException ex1) {
				// SQLException was thrown either during the setAutoCommit() or
				// commit() methods
				log.error("SQLException thrown during rollback.",ex1);
			}
			// Re-throw the exception and add stack trace information
			throw ex;
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException ex) {
					StringBuilder msg=new StringBuilder(128);
					msg.append("SQLException thrown trying to close the ");
					msg.append("the connection.");
					log.error(msg.toString(),ex);
				}
			}
		}
		log.trace("[END]");
		return personList;
	}
	/**
	 * 
	 */
	protected List<Person> findByLastName(String lastName, UserContext ctx, 
			Connection con) throws DAOException {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return null;
	}
	/**
	 * 
	 */
	public List<Person> findByDisplayName(String name, UserContext ctx) 
		throws DAOException {
		log.trace("[BEGIN]");
		Connection con=null;
		List<Person> personList=null;
		
		try {
			// Obtain a connection
			con=pm.getConnection();
			// Disable auto-commit
			con.setAutoCommit(false);
			// Invoke the "save" logic
			personList=findByDisplayName(name,ctx,con);
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
		return personList;
	}
	/**
	 * 
	 */
	protected List<Person> findByDisplayName(String name, UserContext ctx, 
			Connection con)	throws DAOException {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return null;
	}
	/**
	 * 
	 */
	public List<Person> findAll(UserContext ctx) throws DAOException {
		log.trace("[BEGIN]");
		Connection con=null;
		List<Person> personList=null;
		
		try {
			// Obtain a connection
			pm.getConnection();
			// Disable auto-commit
			con.setAutoCommit(false);
			// Invoke the "save" logic
			personList=findAll(ctx,con);
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
			// Re-throw the DAOException
			throw ex;
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
		return personList;
	}
	/**
	 * 
	 */
	protected List<Person> findAll(UserContext ctx, Connection con)
	throws DAOException {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return null;
	}
	//*************************************************************************
	//* END ACCESSOR METHOD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN OTHER METHOD DECLARATIONS
	//*************************************************************************
	/**
	 * Returns a reference to an instance of <code>PersonDAO</code>. 
	 */
	public static PersonDAOImpl getInstance() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return personDAO;
	}
	/**
	 * Returns a reference to the DAO identifier key.
	 * @return java.lang.String
	 */
	protected String getDAOIdentifierKey() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return DAO_IDENTIFIER_KEY;
	}
	
	//*************************************************************************
	//* END OTHER METHOD DECLARATIONS
	//*************************************************************************
}