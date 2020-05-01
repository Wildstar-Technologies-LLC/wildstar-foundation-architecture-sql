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
package com.wildstartech.wfa.workorder.impl;

import com.wildstartech.wfa.dao.impl.WildDAOImpl;
import com.wildstartech.wfa.dao.user.UserContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.NumberFormat;

import com.wildstartech.wfa.dao.DAOException;
import com.wildstartech.wfa.dao.WildDAO;
import com.wildstartech.wfa.dao.WildObject;
import com.wildstartech.wfa.finance.ChargeDescriptionTooLongException;
import com.wildstartech.wfa.logistics.ltl.AccessorialCharge;
import com.wildstartech.wfa.logistics.ltl.WorkOrderOld;
import com.wildstartech.wfa.logistics.ltl.WorkOrderOld;

public class AccessorialChargeDAOImpl extends WildDAOImpl {
	private static final String _CLASS=AccessorialChargeDAOImpl.class.getName();
	// Log
	private static final Logger logger=Logger.getLogger(_CLASS);
	
	// QUERY FOR INSERT
	private static String QUERY_INSERT=
		"INSERT INTO ACCESSORIAL_CHARGES (POID, CREATED_BY, DATE_CREATED, " +
		"MODIFIED_BY, DATE_MODIFIED, WORK_ORDER_POID, QUANTITY, DESCRIPTION, " +
		"AMOUNT, TOTAL_AMOUNT) VALUES (?,?,?,?,?,?,?,?,?,?)";
	// SQL QUERY FOR UPDATE
	private static String QUERY_UPDATE=
		"UPDATE ACCESSORIAL_CHARGES SET MODIFIED_BY=?, DATE_MODIFIED=?, " +
		"WORK_ORDER_POID=?, QUANTITY=?, DESCRIPTION=?, AMOUNT=?, " +
		"TOTAL_AMOUNT=? WHERE POID=?";
	// SQL QUERY FOR DELETE
	private static String QUERY_DELETE=
		"DELETE FROM ACCESSORIAL_CHARGES WHERE POID=?";
	// SQL QUERY TO DELETE WORK ORDER CHARGES ASSOCIATED WITH A WORK ORDER
	protected static String QUERY_DELETE_WORKORDER=
		"DELETE FROM ACCESSORIAL_CHARGES WHERE WORK_ORDER_POID=?";
	// Base SQL query for SELECT statemetns
	private static String QUERY_SELECT_BASE=
		"SELECT POID, CREATED_BY, DATE_CREATED, MODIFIED_BY, DATE_MODIFIED, "+
		"WORK_ORDER_POID, QUANTITY, DESCRIPTION, AMOUNT, TOTAL_AMOUNT "+
		"FROM ACCESSORIAL_CHARGES ";
	// SQL query for primary key lookup.
	private static String QUERY_SELECT=QUERY_SELECT_BASE+"WHERE POID = ?";
	// SQL query to Return all WorkOrderCharges for a given work order
	private static String QUERY_SELECT_BY_WORKORDER=QUERY_SELECT_BASE+
		"WHERE WORK_ORDER_POID=?";
	// SQL QUERY FOR WORK ORDER CHARGE COUNT
	private static String QUERY_SELECT_COUNT_BY_WORK_ORDER=
		"SELECT COUNT(*) FROM ACCESSORIAL_CHARGES WHERE WORK_ORDER_POID=?";
	/** Used for formatting currency data in log entries. */
	private static final NumberFormat currencyFormat=
		NumberFormat.getCurrencyInstance();
	/** Used to identify the DAO */
	protected static String DAO_IDENTIFIER_KEY=
		"com.wildstartech.justo.crm.dao.AccessorialChargeDAO";
	// WorkOrderDAO
	private static WorkOrderDAO workOrderDAO=pm.getWorkOrderDAO();
	
	// Singleton implementation of the Data Access Object for Company objects.
	private static AccessorialChargeDAOImpl workOrderChargeDAO=
		new AccessorialChargeDAOImpl();
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
	 * Ensures the singleton nature of the DAO.
	 */
	private AccessorialChargeDAOImpl() {
		log.trace("[BEGIN]");
		log.trace("[END]");
	}
	//*************************************************************************
	//* END CONSTRUCTOR DECLARATIONS
	//*************************************************************************	
	//*************************************************************************
	//* BEGIN STATIC METHOD DECLARATIONS
	//*************************************************************************
	/**
	 * Populate a <code>AccessorialCharge</code> object with data.
	 */
	private static void populateAccessorialCharge(
		AccessorialChargeImpl woCharge, ResultSet rs) throws SQLException {
		log.trace("[BEGIN]");
		//***** QUANTITY
		woCharge.setQuantity(rs.getInt("QUANTITY"));
		try {
			woCharge.setDescription(rs.getString("DESCRIPTION"));
		} catch (ChargeDescriptionTooLongException ex) {
			log.error("This shouldn't happen.",ex);
		}
		//***** AMOUNT
		woCharge.setAmount(rs.getFloat("AMOUNT"));
		log.trace("[END]");
	}
	/**
	 * Returns a reference to the AccessorialCharge DAO.
	 */
	protected static AccessorialChargeDAOImpl getInstance() {
		return workOrderChargeDAO;
	}
	//*************************************************************************
	//* END STATIC METHOD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN CRUD METHOD DECLARATIONS
	//*************************************************************************
	//********** save
	protected void save(AccessorialCharge accessorialCharge, UserContext ctx) 
	throws DAOException {
		log.trace("[BEGIN]");
		Connection con=null;
		
		try {
			// Obtain a connection
			con=pm.getConnection();
			// Disable auto-commit
			con.setAutoCommit(false);
			// Invoke the "save" logic
			save(accessorialCharge,ctx,con);
			// Commit the transaction
			con.commit();
		} catch (SQLException ex) {
			log.error("SQLException thrown.",ex);
			try {
				con.rollback();
			} catch (SQLException ex1) {
				// Log error for SQL exception
				log.error(ERR_SQLEXCEPTION_ROLLBACK,ex);
			}
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException ex) {
					// SQLException thrown while closing the  connection
					log.error(ERR_SQLEXCEPTION_CON_CLOSE,ex);
				}
			}
		}
		log.trace("[END]");
	}
	/**
	 * Initiate the save of a work order charge using SQL connection
	 */
	protected void save(
			AccessorialCharge accessorialCharge, UserContext ctx, Connection con)
	throws DAOException {
		log.trace("[BEGIN]");
		long poid;
		
		// Check the object to ensure valid parameters are passed.
		checkObject(accessorialCharge);
		checkObject(ctx,WildDAO.VALIDATE_NULL);
		
		// Perform the actual work to save the object
		poid=((AccessorialChargeImpl) accessorialCharge).getPoid();
		if (poid == Long.MIN_VALUE) {
			// Based upon the poid value, the object has not yet been saved.
			_create(accessorialCharge,ctx,con);
		} else {
			// Since a valid poid value is assigned, update the existing record.
			_update(accessorialCharge,ctx,con);
		}
		log.trace("[END]");
	}
	/*
	 * Method responsible for creating a new record for the work order charge.
	 */
	private void _create(
		AccessorialCharge accessorialCharge, UserContext ctx, Connection con) 
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
			pStmt.setLong(1,poid);							// POID
			pStmt.setLong(2,currentUserPoid);				// CREATED_BY
			pStmt.setTimestamp(3,currentTime);				// DATE_CREATED
			pStmt.setLong(4,currentUserPoid);				// MODIFIED_BY
			pStmt.setTimestamp(5,currentTime);				// DATE_MODIFIED
			// WORK_ORDER_POID
			pStmt.setLong(6, 
					((WorkOrderImpl) accessorialCharge.getWorkOrder()).getPoid());
			
			pStmt.setInt(7,accessorialCharge.getQuantity());	// QUANTITY			
			// DESCRIPTION
			String description=accessorialCharge.getDescription();
			if (description != null) {
				pStmt.setString(8,description);				// DESCRIPTION
			} else {
				pStmt.setNull(8,Types.VARCHAR);
			}
			// AMOUNT
			pStmt.setFloat(9, accessorialCharge.getAmount());
			// TOTAL_AMOUNT
			pStmt.setFloat(10,
				accessorialCharge.getQuantity()*accessorialCharge.getAmount());
			// Execute the INSERT query
			numRows=pStmt.executeUpdate();
			if(numRows!=1) {
				log.error("Error Attempting to insert a records.");
			}
			// Close the PreparedStatement
			pStmt.close();
			// Store the poid of the work order charge
			((AccessorialChargeImpl) accessorialCharge).setPoid(poid);
		} catch (SQLException ex) {
			// Print the error information
			log.error("SQLException thrown.",ex);
			try {
				con.rollback();
			} catch (SQLException ex1) {
				// Log error for SQL exception
				log.error(ERR_SQLEXCEPTION_ROLLBACK,ex);
			}
		} catch (Throwable ex) {
			log.error("Exception thrown.",ex);
		}
		log.trace("[END]");
	}
	/*
	 * Update an existing <code>AccessorialCharge</code> record.
	 */
	private void _update(
			AccessorialCharge accessorialCharge, UserContext ctx, Connection con) 
	throws DAOException {
		log.trace("[BEGIN]");
		if (log.isTraceEnabled()) {
			int quantity=accessorialCharge.getQuantity();
			float amount=accessorialCharge.getAmount();
			log.trace("[BEGIN]");
			log.trace("Quantity: "+quantity);
			log.trace("Description: "+accessorialCharge.getDescription());
			log.trace("Amount: "+ currencyFormat.format(amount));
			log.trace("Total Amount: "+
				currencyFormat.format(quantity*amount));
		} // END if (log.isTraceEnabled())
		long currentUserPoid;
		long accessorialChargePoid=Long.MIN_VALUE;
		long workOrderPoid=Long.MIN_VALUE;
		int numRows;
		PreparedStatement pStmt=null;
		Timestamp currentTime=null;
		
		// Get the current date/time 
		currentUserPoid=getCurrentUser(ctx);
		currentTime=new Timestamp(System.currentTimeMillis());

		try {
			// Prepare/compile the statement
			pStmt=con.prepareStatement(QUERY_UPDATE);
			pStmt.setLong(1,currentUserPoid);				// MODIFIED_BY
			pStmt.setTimestamp(2,currentTime);				// DATE_MODIFIED
			// WORK_ORDER_POID
			WildObject workOrder=
				(WildObject) accessorialCharge.getWorkOrder();
			pStmt.setLong(3,workOrder.getPoid());
			// QUANTITY
			pStmt.setInt(4, accessorialCharge.getQuantity());
			// DESCRIPTION
			String description=accessorialCharge.getDescription();
			if (description != null) {
				pStmt.setString(5, description);
			} else {
				pStmt.setNull(5, Types.VARCHAR);
			}
			// AMOUNT
			pStmt.setFloat(6, accessorialCharge.getAmount());
			// TOTAL_AMOUNT
			pStmt.setFloat(7, accessorialCharge.getTotalCharge());
			// WORK_ORDER_POID
			accessorialChargePoid=
				((AccessorialChargeImpl)accessorialCharge).getPoid();
			pStmt.setLong(8,accessorialChargePoid);					// POID
			// Execute the UPDATE query
			numRows=pStmt.executeUpdate();
			if (numRows!=1) {
				// Had a problem, so roll back the transaction
				log.error("Problem saving records.");
			}
			// Close the PreparedStatement
			pStmt.close();
		} catch (SQLException ex) {
			// Print the error information
			log.error("SQLException thrown.",ex);
			try {
				con.rollback();
			} catch (SQLException ex1) {
				// Log error for SQL exception
				log.error(ERR_SQLEXCEPTION_ROLLBACK,ex);
			} // END TRY/CATCH
		} // END TRY/CATCH
		log.trace("[END]");
	}
	//********** delete
	/**
	 * 
	 */
	protected void delete(AccessorialCharge accessorialCharge, UserContext ctx) 
	throws DAOException {
		log.trace("[BEGIN]");
		Connection con=null;
		
		try {
			// Obtain a connection
			con=pm.getConnection();
			// Disable auto-commit
			con.setAutoCommit(false);
			// Invoke the "save" logic
			delete(accessorialCharge,ctx,con);
			// Commit the transaction
			con.commit();
		} catch (SQLException ex) {
			log.error("SQLException thrown.",ex);
			try {
				con.rollback();
			} catch (SQLException ex1) {
				// Log error for SQL exception
				log.error(ERR_SQLEXCEPTION_ROLLBACK,ex);
			}
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException ex) {
					// SQLException thrown while closing the  connection
					log.error(ERR_SQLEXCEPTION_CON_CLOSE,ex);
				}
			}
		}
		log.trace("[END]");
	}
	/**
	 * Delete a <code>AccessorialCharge</code> using SQL connection.
	 */
	protected void delete(
			AccessorialCharge accessorialCharge, UserContext ctx, Connection con)
	throws DAOException {
		log.trace("[BEGIN]");
		long poid=Long.MIN_VALUE;
		int numRows=Integer.MIN_VALUE;
		PreparedStatement pStmt=null;
		
		// Validate the parameters
		checkObject(accessorialCharge);
		checkObject(ctx,WildDAO.VALIDATE_NULL);
		
		// Get the company object's poid
		poid=((AccessorialChargeImpl) accessorialCharge).getPoid();
		if (poid == Long.MIN_VALUE) {
			// The AccessorialCharge object has not yet been saved.
			log.error("The work order charge has not yet been saved.");
		} else {
			// The AccessorialCharge has been saved, so let's delete it.
			try {
				pStmt=con.prepareStatement(QUERY_DELETE);
				pStmt.setLong(1,poid);
				// Execute the DELETE query
				numRows=pStmt.executeUpdate();
				if (numRows!=1) {
					log.error("Error Deleting the workOrderCharge.");
				}
				con.commit();
			} catch (SQLException ex) {
				// Print the error informaiton
				ex.printStackTrace(System.err);
			} finally {
				if(pStmt != null) {
					try {
						pStmt.close();
					} catch (SQLException ex) {
						log.error("Error trying to close the statement",ex);
					}
				} // END if(pStmt!=null)
			} // END try
		} // END if(poid == Long.MIN_VALUE)
		log.trace("[END]");
	}
	//*************************************************************************
	//* END CRUD METHOD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN SEARCH METHODS
	//*************************************************************************
	/**
	 * Locate a specific work order charge
	 */
	protected AccessorialCharge 
		findByPrimaryKey(WorkOrderImpl workOrder, long poid, UserContext ctx) 
	throws DAOException {
		log.trace("[BEGIN]");
		Connection con=null;
		DAOException daoException=null;
		PreparedStatement pStmt=null;
		ResultSet rs=null;
		AccessorialChargeImpl workOrderCharge=null;
		
		if (poid != Long.MIN_VALUE) {
			// Get a connection
			con=pm.getConnection();
			try {
				// Prepare the statement...
				pStmt=con.prepareStatement(QUERY_SELECT);
				// Specify the poid
				pStmt.setLong(1,poid);		// Specify the workOrderCharge poid
				// Execute the statement
				rs=pStmt.executeQuery();
				// If the query returns a value...
				if (rs.next()) {
					// The resultSet contains a value.
					log.trace("Found data.");
					workOrderCharge = new AccessorialChargeImpl(workOrder);
					setWildObjectData(workOrderCharge ,rs);
					populateAccessorialCharge(workOrderCharge,rs);
				}
				// Close the ResultSet
				rs.close();
				// Close the PreparedStatement
				pStmt.close();
			} catch (SQLException ex) {
				// The specified object could not be found
				daoException=new DAOException(
						WildDAO.ERR_OBJECT_NOT_FOUND,
						new Object[] {Long.valueOf(poid),"Work Order Charge"});
				// Log the occurrence of the error.
				log.error(daoException.getMessage(),daoException);
			} // END try/catch
			// Attempt to close the connection
			try {
				con.close();
			} catch (SQLException ex) {
				log.error(
					"SQLException thrown trying to close the Connection.",ex);
			} // END try/catch
		} else {
			// The specified object could not be found
			daoException=new DAOException(
					WildDAO.ERR_OBJECT_NOT_FOUND,
					new Object[] {Long.valueOf(poid),"Work Order Charge"});
			// Log the occurrence of the error.
			log.error(daoException.getMessage(),daoException);
		} // END if (poid != Long.MIN_VALUE) 
		
		log.trace("[END]");
		if (daoException == null) {
			return workOrderCharge;
		} else {
			throw daoException;
		}
	}
	/**
	 * 
	 */
	protected void findAll(WorkOrderOld workOrder,
		UserContext ctx, Connection con) throws DAOException {
		log.trace("[BEGIN]");
		long workOrderPoid=Long.MIN_VALUE;
		PreparedStatement pStmt=null;
		ResultSet rs=null;
		AccessorialChargeImpl workOrderCharge=null;

		if (ctx != null) {
			// Get the work orderPoid
			workOrderPoid=((WorkOrderImpl)workOrder).getPoid();
			try {
				// Prepare the statement...
				pStmt=con.prepareStatement(QUERY_SELECT_BY_WORKORDER);
				// Set the workOrderPoid
				pStmt.setLong(1,workOrderPoid);
				// Execute the statement
				rs=pStmt.executeQuery();
				// Process the resultSet...
				while (rs.next()) {
					// The resultSet contains a value.
					workOrderCharge = (AccessorialChargeImpl)
						workOrder.createAccessorialCharge();
					setWildObjectData(workOrderCharge,rs);
					populateAccessorialCharge(workOrderCharge,rs);
					if (log.isTraceEnabled()) {
						log.trace("***** AccessorialCharge Detail");
						log.trace("Quantity: "+workOrderCharge.getQuantity());
						log.trace("Description: "+
							workOrderCharge.getDescription());
						log.trace("Amount: "+
							currencyFormat.format(workOrderCharge.getAmount()));
						log.trace("Total Amount: "+
							currencyFormat.format(
								workOrderCharge.getTotalCharge()));
					} // END if (log.isTraceEnabled())
				} // END while (rs.next())
				// Close the ResultSet
				rs.close();
				// Close the PreparedStatement
				pStmt.close();
			} catch (SQLException ex) {
				StringBuilder msg=new StringBuilder(128);
				msg.append("SQLException thrown while trying to create a ");
				msg.append("AccessorialCharge object.");
				log.error(msg.toString(),ex);
			}
		} else {
			StringBuilder msg=new StringBuilder(128);
			msg.append("WorkOrderChargeDAO.findAll(UserContext) passed a ");
			msg.append("null UserContext object.");
			log.error(msg.toString());
		} // END if (ctx != null)
		log.trace("[END]");
	}
	//*************************************************************************
	//* END SEARCH METHODS
	//*************************************************************************	
	//*************************************************************************
	//* BEGIN OTHER METHOD DECLARATIONS
	//*************************************************************************
	/**
	 * Create an instance of <code>AccessorialCharge</code> associated with the 
	 * specified <code>WorkOrderOld</code>.
	 */
	protected AccessorialCharge create(WorkOrderOld workOrder) {
		log.trace("[BEGIN]");
		AccessorialChargeImpl woCharge=null;
		
		woCharge=new AccessorialChargeImpl(workOrder);
		
		log.trace("[END]");
		return woCharge;
	}	
	/**
	 * Returns the DAO Identifier key.
	 */
	protected String getDAOIdentifierKey() {
		return DAO_IDENTIFIER_KEY;
	}
	//*************************************************************************
	//* END OTHER METHOD DECLARATIONS
	//*************************************************************************
}
