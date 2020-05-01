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
package com.wildstartech.wfa.workorder.impl;

import com.wildstartech.wfa.dao.impl.WildDAOImpl;
import com.wildstartech.wfa.dao.user.UserContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wildstartech.justo.crm.DescriptionTooLongException;
import com.wildstartech.justo.crm.InvalidDimensionException;
import com.wildstartech.justo.crm.ProductIdTooLongException;
import com.wildstartech.justo.crm.WorkOrder;
import com.wildstartech.justo.crm.WorkOrderLineItem;
import com.wildstartech.wfa.dao.DAOException;
import com.wildstartech.wfa.dao.WildDAO;
import com.wildstartech.wfa.dao.WildObject;

public final class WorkOrderLineItemDAOImpl extends WildDAOImpl {
	//*************************************************************************
	//* BEGIN STATIC FIELD DECLARATIONS
	//*************************************************************************
	// Used for logging
	private static Log log=LogFactory.getLog(WorkOrderLineItemDAOImpl.class);
	// Base SQL Query for Line Items
	private static String QUERY_SELECT_BASE=
		"SELECT POID, CREATED_BY, DATE_CREATED, MODIFIED_BY, DATE_MODIFIED, "+
		"WORK_ORDER_POID, ITEM_NUMBER, ITEM_ID, ITEM_DESCRIPTION, " +
		"ITEM_QUANTITY, LENGTH, WIDTH, HEIGHT, WEIGHT, PER_UNIT_PRICE, " +
		"TOTAL_PRICE FROM WORK_ORDER_LINE_ITEMS ";
	// Query to obtain a specific line item.
	private static String QUERY_SELECT_LINEITEM=QUERY_SELECT_BASE+
		"WHERE POID=? AND WORKORDER_POID=?";
	// Query to Return all Line Items for a given work order
	private static String QUERY_SELECT_WORKORDER=QUERY_SELECT_BASE+
		"WHERE WORK_ORDER_POID=? ORDER BY ITEM_NUMBER";
	// SQL QUERY FOR LINE ITEM COUNT
	private static String QUERY_SELECT_COUNT=
		"SELECT COUNT(*) FROM WORK_ORDER_LINE_ITEMS WHERE WORK_ORDER_POID=?";
	// SQL QUERY FOR CREATE LINE ITEMS
	private static String QUERY_INSERT=
		"INSERT INTO WORK_ORDER_LINE_ITEMS (POID, CREATED_BY, DATE_CREATED, "+
		"MODIFIED_BY, DATE_MODIFIED, WORK_ORDER_POID, ITEM_NUMBER, ITEM_ID, "+
		"ITEM_DESCRIPTION, ITEM_QUANTITY, LENGTH, WIDTH, HEIGHT, WEIGHT, " +
		"PER_UNIT_PRICE, TOTAL_PRICE) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	// SQL QUERY FOR UPDATE LINE ITEMS
	private static String QUERY_UPDATE=
		"UPDATE WORK_ORDER_LINE_ITEMS SET MODIFIED_BY=?, DATE_MODIFIED=?, "+
		"WORK_ORDER_POID=?, ITEM_NUMBER=?, ITEM_ID=?, ITEM_DESCRIPTION=?, "+
		"ITEM_QUANTITY=?, LENGTH=?, WIDTH=?, HEIGHT=?, WEIGHT=?, " +
		"PER_UNIT_PRICE=?, TOTAL_PRICE=? WHERE POID=?";
	// SQL QUERY FOR DELETING LINE ITEMS
	private static String QUERY_DELETE=
		"DELETE FROM WORK_ORDER_LINE_ITEMS WHERE POID=?";
	// SQL QUERY TO DELETE LINE ITEMS ASSOCIATED WITH A WORK ORDER
	protected static String QUERY_DELETE_WORKORDER=
		"DELETE FROM WORK_ORDER_LINE_ITEMS WHERE WORK_ORDER_POID=?";
	
	protected static String DAO_IDENTIFIER_KEY=
		"com.wildstartech.justo.crm.dao.WorkOrderLineItemDAO";
	// Singleton implementation of the Data Access Object for WorkOrderOld objects.	
	private static WorkOrderLineItemDAOImpl workOrderLineItemDAO=
		new WorkOrderLineItemDAOImpl();
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
	private WorkOrderLineItemDAOImpl() {
		log.trace("[BEGIN]");
		log.trace("[END]");
	}
	//*************************************************************************
	//* END CONSTRUCTOR DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN CRUD METHODS
	//*************************************************************************	
	/**
	 * Create an instance of a line Item associated with a given work order. 
	 */
	public WorkOrderLineItem create(WorkOrderOld workOrder) {
		return new WorkOrderLineItemImpl(workOrder);
	}
	/**
	 * Save the specified WorkOrderLineItem to the database.
	 */
	public void save(WorkOrderLineItem lineItem, UserContext ctx) 
	throws DAOException {
		log.trace("[BEGIN]");
		Connection con=null;
		
		try {
			// Obtain a connection
			con=pm.getConnection();
			// Disable auto-commit
			con.setAutoCommit(false);
			// Invoke the "save" logic
			save(lineItem,ctx,con);
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
	 * Passivate an instance of the <code>WorkOrderLineItem</code> object.
	 */
	protected void save(WorkOrderLineItem lineItem, UserContext ctx, 
		Connection con)	throws DAOException {
		log.trace("[BEGIN]");
		long poid;
		
		// Check the object to ensure valid parameters are passed.
		checkObject(lineItem);
		checkObject(ctx,WildDAO.VALIDATE_NULL);
		
		// Perform the actual work to save the object
		poid=((WorkOrderLineItemImpl) lineItem).getPoid();
		if (poid == Long.MIN_VALUE) {
			// Based upon the poid value, the object has not yet been saved.
			_create(lineItem,ctx,con);
		} else {
			// Since a valid poid value is assigned, update the existing record.
			_update(lineItem,ctx,con);
		}
		log.trace("[END]");
	}
	/**
	 * Internal method to manage the creation of Work Order Line Items
	 * 
	 * @param lineItem
	 * @param ctx
	 * @throws DAOException
	 */
	private void _create(WorkOrderLineItem lineItem, UserContext ctx, 
		Connection con)	throws DAOException {
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
					((WorkOrderImpl) lineItem.getWorkOrder()).getPoid());
			pStmt.setInt(7,lineItem.getItemNumber());		// ITEM_NUMBER
			// ITEM_ID
			String itemId=lineItem.getProductId();
			if (itemId != null) {
				pStmt.setString(8,lineItem.getProductId());	// ITEM_ID
			} else {
				pStmt.setNull(8,Types.VARCHAR);
			}
			// ITEM_DESCRIPTION
			String itemDescription=lineItem.getDescription();
			if (itemDescription != null) {
				pStmt.setString(9,itemDescription);
			} else {
				pStmt.setNull(9,Types.VARCHAR);
			}
			// ITEM_QUANTITY
			int quantity=lineItem.getQuantity();
			if (quantity >= 0) {
				pStmt.setInt(10,lineItem.getQuantity());
			} else {
				pStmt.setInt(10,0);
			}
			// LENGTH
			pStmt.setFloat(11,lineItem.getLength());
			// WIDTH
			pStmt.setFloat(12,lineItem.getWidth());
			// HEIGHT
			pStmt.setFloat(13,lineItem.getHeight());
			// WEIGHT
			pStmt.setFloat(14,lineItem.getWeight());
			// PRICE
			pStmt.setFloat(15,lineItem.getPrice());
			// TOTAL_PRICE
			pStmt.setFloat(16,lineItem.getTotalPrice());
			
			// Execute the INSERT query
			numRows=pStmt.executeUpdate();
			if(numRows!=1) {
				log.error("Error Attempting to insert a records.");
			}
			// Close the PreparedStatement
			pStmt.close();
			// Store the POID with the lineItem
			((WorkOrderLineItemImpl) lineItem).setPoid(poid);
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
	/**
	 * Internal method used to manage udpating existing line item records.
	 * 
	 * @param lineItem
	 * @param ctx
	 * @throws DAOException
	 */
	private void _update(WorkOrderLineItem lineItem, UserContext ctx, 
		Connection con) throws DAOException {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Item Number: "+lineItem.getItemNumber());
			log.trace("Product Id: "+lineItem.getProductId());
			log.trace("Description: "+lineItem.getDescription());
			log.trace("Quantity: "+lineItem.getQuantity());
		} // END if (log.isTraceEnabled())
		long currentUserPoid;
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
			WildObject workOrder=(WildObject) lineItem.getWorkOrder();
			pStmt.setLong(3,workOrder.getPoid());
			pStmt.setInt(4,lineItem.getItemNumber());		// ITEM_NUMBER
			pStmt.setString(5,lineItem.getProductId());		// ITEM_ID
			pStmt.setString(6,lineItem.getDescription());	// ITEM_DESCRIPTION
			// ITEM_QUANTITY
			pStmt.setInt(7,lineItem.getQuantity());
			// LENGTH
			pStmt.setFloat(8, lineItem.getLength());
			// WIDTH
			pStmt.setFloat(9, lineItem.getWidth());
			// HEIGHT
			pStmt.setFloat(10, lineItem.getHeight());
			// WEIGHT
			pStmt.setFloat(11, lineItem.getWeight());
			// PRICE
			pStmt.setFloat(12, lineItem.getPrice());
			// TOTAL_PRICE
			pStmt.setFloat(13, lineItem.getTotalPrice());
			
			// WorkOrderOld Poid
			long poid=((WildObject) lineItem).getPoid();
			pStmt.setLong(14,poid);							// POID
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
			}
		}
		log.trace("[END]");
	}
	/**
	 * Deletes all the line items associated with a given work order.
	 * @param lineItem
	 * @param ctx
	 * @throws DAOException
	 */
	public void delete(WorkOrderImpl order, UserContext ctx) 
	throws DAOException {
		log.trace("[BEGIN]");
		Connection con=null;
		
		try {
			// Obtain a connection
			con=pm.getConnection();
			// Disable auto-commit
			con.setAutoCommit(false);
			// Invoke the "save" logic
			delete(order,ctx,con);
			// Commit the transaction
			con.commit();
		} catch (DAOException ex) {
			// A DAOException was thrown, so rollback the transaction.
			log.error("DAOException thrown deleting work order line items.",ex);
		} catch (SQLException ex) {
			// Log error for SQL exception
			log.error("A SQLException was thrown.",ex);
			try {
				// SQLException thrown while rolling back the transaction
				con.rollback();
			} catch (SQLException ex1) {
				log.error(ERR_SQLEXCEPTION_ROLLBACK,ex);
			}			
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException ex) {
					// SQLException thrown while closing the connection
					log.error(ERR_SQLEXCEPTION_CON_CLOSE,ex);
				} // END try/catch
			} // END if (con != null)
		} // END try/catch
		log.trace("[END]");		
	}
	protected void delete(WorkOrderImpl order, UserContext ctx, 
		Connection con) throws DAOException {
		log.trace("[BEGIN]");
		long workOrderPoid=Long.MIN_VALUE;
		int numRows=Integer.MIN_VALUE;
		PreparedStatement pStmt=null;
		
		workOrderPoid=order.getPoid();
		if (workOrderPoid > Long.MIN_VALUE) {
			try {
				pStmt=con.prepareStatement(QUERY_DELETE_WORKORDER);
				pStmt.setLong(1,workOrderPoid);
				// Execute the update...
				numRows=pStmt.executeUpdate();
				pStmt.close();
				log.trace(numRows+" work order line items removed.");
			} catch (SQLException ex) {
				StringBuilder msg=new StringBuilder(128);
				msg.append("SQLException thrown deleting work order line ");
				msg.append("items associated with the specified work order. ");
				msg.append("WorkOrderOld unique id: ");
				msg.append(workOrderPoid);
				log.error(msg.toString(),ex);
				msg=null;
				try {
					con.rollback();
				} catch (SQLException ex1) {
					// Log error for SQL exception
					log.error(ERR_SQLEXCEPTION_ROLLBACK,ex);
				}
			} // END try/catch
		} // END if (workOrderPoid > Long.MIN_VALUE)
		log.trace("[END]");
	}
	/**
	 * Removes a specified WorkOrderLineItem from the list.
	 */
	public void delete(WorkOrderLineItem lineItem, UserContext ctx)
	throws DAOException {
		log.trace("[BEGIN]");
		Connection con=null;
		
		try {
			// Obtain a connection
			con=pm.getConnection();
			// Disable auto-commit
			con.setAutoCommit(false);
			// Invoke the "delete" logic
			delete(lineItem,ctx,con);
			// Commit the transaction
			con.commit();
		} catch (DAOException ex) {
			// A DAOException was thrown, so rollback the transaction.
			try {
				con.rollback();
			} catch (SQLException ex1) {
				// Log error for SQL exception
				log.error(ERR_SQLEXCEPTION_ROLLBACK,ex);
			}
		} catch (SQLException ex) {
			log.error("SQLException thrown.",ex);
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException ex) {
					// Log error for SQL exception
					log.error(ERR_SQLEXCEPTION_CON_CLOSE,ex);
				}
			}
		}
		log.trace("[END]");
	}
	/**
	 * Deletes a specified lineItem from the database.
	 * 
	 * @param lineItem
	 * @param ctx
	 * @param con
	 * @throws DAOException
	 */
	protected void delete(WorkOrderLineItem lineItem, UserContext ctx,
		Connection con)	throws DAOException {
		log.trace("[BEGIN]");
		long poid=Long.MIN_VALUE;
		int numRows=Integer.MIN_VALUE;
		PreparedStatement pStmt=null;
		
		// Validate the parameters
		checkObject(lineItem);
		checkObject(ctx,WildDAO.VALIDATE_NULL);
		
		// Get the company object's poid
		poid=((WorkOrderLineItemImpl) lineItem).getPoid();
		if (poid == Long.MIN_VALUE) {
			// The Group object has not yet been saved.
			StringBuilder msg=new StringBuilder(128);
			msg.append("DEBUG: WorkOrderLineItemDAOImpl.delete() invoked.  The");
			msg.append("The specified work order has not yet been saved.\nPoid=");
			msg.append(poid);
			System.err.println(msg.toString());
		} else {
			// The Group has been saved, so let's delete it.
			try {
				pStmt=con.prepareStatement(QUERY_DELETE);
				pStmt.setLong(1,poid);
				// Execute the DELETE query
				numRows=pStmt.executeUpdate();
				if (numRows!=1) {
					log.error("Error Deleting the Line Item.");
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
	/**
	 * Locate the WorkOrderLineItem using the unique identifier.
	 * 
	 * @param workOrder
	 * @param poid
	 * @param ctx
	 * @return
	 * @throws DAOException
	 */
	protected WorkOrderLineItem findByPrimaryKey(WorkOrderImpl workOrder, 
		long poid, UserContext ctx) throws DAOException {
		log.trace("[BEGIN]");
		Connection con=null;
		PreparedStatement pStmt=null;
		ResultSet rs=null;
		WorkOrderLineItemImpl lineItem=null;
		
		// Get a connection
		con=pm.getConnection();
		if (poid != Long.MIN_VALUE) {
			try {
				// Prepare the statement...
				pStmt=con.prepareStatement(QUERY_SELECT_LINEITEM);
				// Specify the poid
				pStmt.setLong(1,poid);		// Specify the lineItem poid
				pStmt.setLong(2,workOrder.getPoid());	// Set the workOrder poid
				// Execute the statement
				rs=pStmt.executeQuery();
				// If the query returns a value...
				if (rs.next()) {
					// The resultSet contains a value.
					lineItem = new WorkOrderLineItemImpl(workOrder);
					setWildObjectData(lineItem ,rs);
					populateLineItem(lineItem,rs);
				}
				// Close the ResultSet
				rs.close();
				// Close the PreparedStatement
				pStmt.close();
			} catch (SQLException ex) {
				// The specified object could not be found
				throw new DAOException(
					WildDAO.ERR_OBJECT_NOT_FOUND,
					new Object[] {Long.valueOf(poid),"Work Order Line Item"});
			}
		} else {
			// The specified object could not be found
			throw new DAOException(
				WildDAO.ERR_OBJECT_NOT_FOUND,
				new Object[] {Long.valueOf(poid),"Work Order Line Item"});
		}
		// Attempt to close the connection
		try {
			con.close();
		} catch (SQLException ex) {
			log.error("SQLException thrown trying to close the Connection.",ex);
		}
		log.trace("[END]");
		return lineItem;
	}
	/**
	 * Obtains all the line item objects associated with a given work order.
	 * 
	 * @param workOrder
	 * @param ctx
	 * @return
	 * @throws DAOException
	 */
	protected void findAll(WorkOrderImpl workOrder, UserContext ctx, Connection con)
	throws DAOException {
		log.trace("[BEGIN]");
		long workOrderPoid=Long.MIN_VALUE;
		PreparedStatement pStmt=null;
		ResultSet rs=null;
		WorkOrderLineItemImpl lineItem=null;

		if (ctx != null) {
			// Get the work orderPoid
			workOrderPoid=workOrder.getPoid();
			try {
				// Prepare the statement...
				pStmt=con.prepareStatement(QUERY_SELECT_WORKORDER);
				// Set the workOrderPoid
				pStmt.setLong(1,workOrderPoid);
				// Execute the statement
				rs=pStmt.executeQuery();
				// Process the resultSet...
				while (rs.next()) {
					// The resultSet contains a value.
					lineItem = new WorkOrderLineItemImpl(workOrder);
					setWildObjectData(lineItem ,rs);
					populateLineItem(lineItem,rs);
				}
				// Close the ResultSet
				rs.close();
				// Close the PreparedStatement
				pStmt.close();
			} catch (SQLException ex) {
				StringBuilder msg=new StringBuilder(128);
				msg.append("SQLException thrown while trying to create a ");
				msg.append("WorkOrderLineItem object.");
				log.error(msg.toString(),ex);
			}
		} else {
			StringBuilder msg=new StringBuilder(128);
			msg.append("WorkOrderLineItemDAO.findAll(UserContext) passed a ");
			msg.append("null UserContext object.");
			log.error(msg.toString());
		}// END if (ctx != null)
		log.trace("[END]");
	}
	//*************************************************************************
	//* END CRUD METHODS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN OTHER METHODS
	//*************************************************************************
	/**
	 * Set the line item data from the result set.
	 */
	private void populateLineItem(WorkOrderLineItemImpl lineItem, ResultSet rs) 
	throws SQLException {
		log.trace("[BEGIN]");
		lineItem.setItemNumber(rs.getInt("ITEM_NUMBER"));	// ITEM_NUMBER
		try {
			lineItem.setProductId(rs.getString("ITEM_ID"));	// ITEM_ID
		} catch (ProductIdTooLongException ex) {
			log.error("This should never happen!",ex);
		}
															// ITEM_DESCRIPTION
		try {
			lineItem.setDescription(rs.getString("ITEM_DESCRIPTION"));
		} catch (DescriptionTooLongException ex) {
			log.error("This should never happen!",ex);
		}
		lineItem.setQuantity(rs.getInt("ITEM_QUANTITY"));	// ITEM_QUANTITY
		try {
			lineItem.setLength(rs.getFloat("LENGTH"));		// LENGTH
		} catch (InvalidDimensionException ex) {
			log.error("This should never happen!",ex);
		}
		try {
			lineItem.setWidth(rs.getFloat("WIDTH"));		// WIDTH
		} catch (InvalidDimensionException ex) {
			log.error("This should never happen!",ex);
		}
		try {
			lineItem.setLength(rs.getFloat("HEIGHT"));		// HEIGHT
		} catch (InvalidDimensionException ex) {
			log.error("This should never happen!",ex);
		}
		try {
			lineItem.setLength(rs.getFloat("WEIGHT"));		// WEIGHT
		} catch (InvalidDimensionException ex) {
			log.error("This should never happen!",ex);
		}
		lineItem.setPrice(rs.getFloat("PER_UNIT_PRICE"));	// PER_UNIT_PRICE
		lineItem.setTotalPrice(rs.getFloat("TOTAL_PRICE"));	// TOTAL_PRICE
		log.trace("[END]");
	}
	protected String getDAOIdentifierKey() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return DAO_IDENTIFIER_KEY;
	}
	
	public static WorkOrderLineItemDAOImpl getInstance() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return workOrderLineItemDAO;
	}
	//*************************************************************************
	//* BEGIN OTHER METHODS
	//*************************************************************************	
}
