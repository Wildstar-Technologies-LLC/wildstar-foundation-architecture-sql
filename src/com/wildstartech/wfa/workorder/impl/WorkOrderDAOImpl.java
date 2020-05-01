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
import com.wildstartech.wfa.dao.impl.SequenceImpl;
import com.wildstartech.wfa.dao.impl.DateSequenceImpl;
import com.wildstartech.wfa.person.impl.PersonImpl;
import com.wildstartech.wfa.person.impl.PersonDAOImpl;
import com.wildstartech.wfa.journal.impl.JournalEntryImpl;
import com.wildstartech.wfa.document.impl.DocumentDAOImpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wildstartech.wfa.assignment.AssignedToGroupNameTooLongException;
import com.wildstartech.wfa.assignment.AssignedToIndividualNameTooLongException;
import com.wildstartech.wfa.company.Company;
import com.wildstartech.wfa.journal.JournalEntry;
import com.wildstartech.wfa.Party;
import com.wildstartech.wfa.person.Person;
import com.wildstartech.wfa.resources.logistics.ltl.WorkOrderLineItem;
import com.wildstartech.wfa.dao.DAOException;
import com.wildstartech.wfa.dao.Sequence;
import com.wildstartech.wfa.dao.WildDAO;
import com.wildstartech.wfa.company.impl.CompanyDAOImpl;
import com.wildstartech.wfa.journal.impl.JournalDAOImpl;
import com.wildstartech.wfa.location.address.State;
import com.wildstartech.wfa.location.address.us.StateImpl;
import com.wildstartech.wfa.logistics.ltl.AccessorialCharge;
import com.wildstartech.wfa.logistics.ltl.CustomerOrderIdTooLongException;
import com.wildstartech.wfa.logistics.ltl.WorkOrderOld;
import com.wildstartech.wfa.logistics.ltl.billoflading.BillingReferenceIdTooLongException;
import com.wildstartech.wfa.logistics.ltl.workorder.WorkOrderIdTooLongException;
import com.wildstartech.wfa.logistics.ltl.WorkOrderOld;
import com.wildstartech.wfa.logistics.ltl.WorkOrderListElement;

public final class WorkOrderDAOImpl extends WildDAOImpl 
implements WorkOrderDAO {
	//*************************************************************************
	//* BEGIN STATIC FIELD DECLARATIONS
	//*************************************************************************
	// Reference to the WorkOrderDAO Logger
	private static Log log=LogFactory.getLog(WorkOrderDAO.class);
	//	 SQL QUERY FOR INSERT
	private static String QUERY_INSERT=
		"INSERT INTO WORK_ORDERS (POID, CREATED_BY, DATE_CREATED, "+
		"MODIFIED_BY, DATE_MODIFIED, WORK_ORDER_ID, LOCATOR, "+
		"ASSIGNED_TO_GROUP, ASSIGNED_TO_INDIVIDUAL, BILLING_DATE, " +
		"BILL_OF_LADING, BILLING_REFERENCE_ID2, BILLING_CONTACT_NAME," +
		"BILLING_ADDRESS, BILLING_ADDRESS2, BILLING_CITY, BILLING_STATE, " +
		"BILLING_ZIP, BILLING_PHONE, BILLING_TERMS, STATUS, WORKORDER_TYPE, " +
		"AGENT_NAME, AGENT_POID, AGENT_TYPE, CUSTOMER_NAME, CUSTOMER_POID, " +
		"CUSTOMER_TYPE, CUSTOMER_ORDER_ID, SHIPPING_METHOD, APPOINTMENT_TIME, "+
		"ARRIVAL_TIME, START_TIME, COMPLETE_TIME, CONSIGNEE_NAME, " +
		"CONSIGNEE_POID, CONSIGNEE_TYPE, CONSIGNEE_CONTACT, " +
		"CONSIGNEE_ADDRESS, CONSIGNEE_ADDRESS2, CONSIGNEE_CITY, " +
		"CONSIGNEE_STATE, CONSIGNEE_ZIP, CONSIGNEE_PHONE, CONSIGNEE_FAX, " +
		"PICKUP_ADDRESS, PICKUP_ADDRESS2, PICKUP_CITY, PICKUP_STATE, " +
		"PICKUP_ZIP, PICKUP_PHONE, PROOF_OF_DELIVERY) VALUES " +
		"(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?," +
		"?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	// SQL QUERY FOR UPDATE
	private static String QUERY_UPDATE=
		"UPDATE WORK_ORDERS SET MODIFIED_BY=?, DATE_MODIFIED=?, "+
		"ASSIGNED_TO_GROUP=?, ASSIGNED_TO_INDIVIDUAL=?, "+
		"BILLING_DATE=?, BILLING_REFERENCE_ID2=?, BILLING_CONTACT_NAME=?, " +
		"BILLING_ADDRESS=?, BILLING_ADDRESS2=?, BILLING_CITY=?, " +
		"BILLING_STATE=?, BILLING_ZIP=?, BILLING_PHONE=?, BILLING_TERMS=?, " +
		"BILL_OF_LADING=?, STATUS=?, WORKORDER_TYPE=?, AGENT_NAME=?, " +
		"AGENT_POID=?, AGENT_TYPE=?, CUSTOMER_NAME=?, CUSTOMER_POID=?, " +
		"CUSTOMER_TYPE=?, CUSTOMER_ORDER_ID=?, SHIPPING_METHOD=?, " +
		"APPOINTMENT_TIME=?, ARRIVAL_TIME=?, START_TIME=?, COMPLETE_TIME=?, " +
		"CONSIGNEE_NAME=?, CONSIGNEE_POID=?, CONSIGNEE_TYPE=?, " +
		"CONSIGNEE_CONTACT=?, CONSIGNEE_ADDRESS=?, CONSIGNEE_ADDRESS2=?, " +
		"CONSIGNEE_CITY=?, CONSIGNEE_STATE=?, CONSIGNEE_ZIP=?, " +
		"CONSIGNEE_PHONE=?, CONSIGNEE_FAX=?, PICKUP_ADDRESS=?, " +
		"PICKUP_ADDRESS2=?, PICKUP_CITY=?, PICKUP_STATE=?, PICKUP_ZIP=?, " +
		"PICKUP_PHONE=?, PROOF_OF_DELIVERY=? WHERE POID=?";
	// SQL QUERY FOR DELETE
	private static String QUERY_DELETE="DELETE FROM WORK_ORDERS WHERE POID=?";	
	// SQL QUERY SELECT QUERY BASE
	private static String QUERY_SELECT_BASE=
		"SELECT POID, CREATED_BY, DATE_CREATED, MODIFIED_BY, DATE_MODIFIED, "+
		"WORK_ORDER_ID, LOCATOR, BILLING_DATE, BILLING_REFERENCE_ID2, " +
		"BILLING_CONTACT_NAME, BILLING_ADDRESS, BILLING_ADDRESS2, " +
		"BILLING_CITY, BILLING_STATE, BILLING_ZIP, BILLING_PHONE, " +
		"BILLING_TERMS, BILL_OF_LADING, ASSIGNED_TO_GROUP, " +
		"ASSIGNED_TO_INDIVIDUAL, STATUS, WORKORDER_TYPE, AGENT_NAME, " +
		"AGENT_POID, AGENT_TYPE, CUSTOMER_NAME, CUSTOMER_POID, CUSTOMER_TYPE, "+
		"CUSTOMER_ORDER_ID, SHIPPING_METHOD, APPOINTMENT_TIME, ARRIVAL_TIME, " +
		"START_TIME, COMPLETE_TIME, CONSIGNEE_NAME, CONSIGNEE_POID, " +
		"CONSIGNEE_TYPE, CONSIGNEE_CONTACT, CONSIGNEE_ADDRESS, " +
		"CONSIGNEE_ADDRESS2, CONSIGNEE_CITY, CONSIGNEE_STATE, CONSIGNEE_ZIP, " +
		"CONSIGNEE_PHONE, CONSIGNEE_FAX, PICKUP_ADDRESS, PICKUP_ADDRESS2, " +
		"PICKUP_CITY, PICKUP_STATE, PICKUP_ZIP, PICKUP_PHONE, " +
		"PROOF_OF_DELIVERY FROM WORK_ORDERS ";
	// SQL QUERY SELECT by POID
	private static String QUERY_SELECT=QUERY_SELECT_BASE+
		"WHERE POID = ?";
	// SQL QUERY for WorkOrderId
	private static String QUERY_SELECT_BY_WORKORDERID=QUERY_SELECT_BASE+
		"WHERE WORK_ORDER_ID=?";
	// BASE SQL QUERY FOR LIST SEARCHES
	private static String QUERY_LIST_BASE=
		"SELECT POID, WORK_ORDER_ID, WORKORDER_TYPE, CONSIGNEE_NAME, " +
		"CONSIGNEE_CITY, CUSTOMER_NAME, CUSTOMER_ORDER_ID, STATUS " +
		"FROM WORK_ORDERS ";
	// SQL QUERY FOR ALL
	private static String QUERY_LIST_ALL=QUERY_LIST_BASE+
		"ORDER BY POID";
	// SQL QUERY FOR LESS THAN GIVEN STATUS
	private static String QUERY_LIST_LESS_STATUS=QUERY_LIST_BASE+
		"WHERE STATUS < ? ORDER BY POID";
	// SQL QUERY FOR SEARCH BY CUSTOMER_ORDER_ID
	private static String QUERY_LIST_BY_CUSTOMER_ORDER_ID=QUERY_LIST_BASE+
		"WHERE CUSTOMER_ORDER_ID LIKE ?";
	// SQL QUERY FOR SEARCH BY LOCATOR_ID
	private static String QUERY_LIST_BY_LOCATOR_ID=QUERY_LIST_BASE+
		"WHERE LOCATOR LIKE ?";
	// SQL QUERY FOR SEARCH BY WORK_ORDER_ID
	private static String QUERY_LIST_BY_WORK_ORDER_ID=QUERY_LIST_BASE+
		"WHERE WORK_ORDER_ID LIKE ?";
	// SQL QUERY FOR SEARCH BY CUSTOMER_POID
	private static String QUERY_LIST_BY_CUSTOMER=QUERY_LIST_BASE+
		"WHERE CUSTOMER_POID=? ORDER BY POID";
	// SQL QUERY FOR SEARCH BY AGENT_POID
	private static String QUERY_LIST_BY_AGENT=QUERY_LIST_BASE+
		"WHERE AGENT_POID = ? ORDER BY POID";
	protected static String DAO_IDENTIFIER_KEY=
		"com.wildstartech.justo.crm.dao.WorkOrderDAO";
	// Accessorial Charge DAO
	private static AccessorialChargeDAOImpl accessorialChargeDAO=
		AccessorialChargeDAOImpl.getInstance();
	// Reference to the CompanyDAO
	private static CompanyDAOImpl companyDAO=
		(CompanyDAOImpl) CompanyDAOImpl.getInstance();
	// Reference to the DocumentDAO
	private static DocumentDAOImpl documentDAO=
		(DocumentDAOImpl) DocumentDAOImpl.getInstance();
	// Reference to the JournalDAO
	private static JournalDAOImpl journalDAO=
		(JournalDAOImpl) JournalDAOImpl.getInstance();
	// Reference to the PersonDAO
	private static PersonDAOImpl personDAO=
		(PersonDAOImpl) PersonDAOImpl.getInstance();
	// Singleton implementation of the Data Access Object for WorkOrderOld objects.	
	private static WorkOrderDAO workOrderDAO=new WorkOrderDAOImpl();
	// Reference to the WorkOrderChargeDAO
	private static AccessorialChargeDAOImpl chargeDAO=
		AccessorialChargeDAOImpl.getInstance();
	// Reference to the WorkOrderLineItemDAO
	private static WorkOrderLineItemDAOImpl lineItemDAO=
		WorkOrderLineItemDAOImpl.getInstance();
	//*************************************************************************
	//* END STATIC FIELD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN INSTANCE FIELD DECLARATIONS
	//*************************************************************************
	private DateSequenceImpl dateSequence;
	private Sequence locatorSequence;
	//*************************************************************************
	//* END INSTANCE FIELD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN CONSTRUCTOR DECLARATIONS
	//*************************************************************************
	private WorkOrderDAOImpl() {
		dateSequence=new DateSequenceImpl("Work Order Id Sequence","yyyyMMdd");
		locatorSequence=new SequenceImpl("Work Order Locator ID");
	}
	//*************************************************************************
	//* END CONSTRUCTOR DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN CRUD METHODS
	//*************************************************************************	
	public WorkOrderOld create() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return new WorkOrderImpl();
	}
	/**
	 * 
	 */
	public void save(WorkOrderOld order, UserContext ctx) throws DAOException {
		log.trace("[BEGIN]");
		Connection con=null;
		
		try {
			// Obtain a connection
			con=pm.getConnection();
			// Disable auto-commit
			con.setAutoCommit(false);
			// Invoke the "save" logic
			save(order,ctx,con);
			// Commit the transaction
			con.commit();
		} catch (DAOException ex) {
			// A DAOException was thrown, so rollback the transaction.
			try {
				con.rollback();
			} catch (SQLException ex1) {
				// SQLException was thrown either during the setAutoCommit() or
				// commit() methods
				log.error(ERR_SQLEXCEPTION_ROLLBACK,ex1);
			}
		} catch (SQLException ex) {
			log.error("SQLException thrown.",ex);
			try {
				con.rollback();
			} catch (SQLException ex1) {
				// SQLException thrownd during rollback.
				log.error(ERR_SQLEXCEPTION_ROLLBACK,ex1);
			}
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
	 * Passivate an instance of the <code>WorkOrderOld</code> object.
	 */
	protected void save(WorkOrderOld order, UserContext ctx, Connection con) 
	throws DAOException {
		log.trace("[BEGIN]");
		long poid;
		
		// Check the object to ensure valid parameters are passed.
		checkObject(order);
		checkObject(ctx,WildDAO.VALIDATE_NULL);
		
		// Perform the actual workto save the object
		poid=((WorkOrderImpl) order).getPoid();
		if (poid == Long.MIN_VALUE) {
			// Based upon the poid value, the object has not yet been saved.
			_create((WorkOrderImpl)order,ctx, con);
		} else {
			// Since a valid poid value is assigned, update the existing record.
			_update((WorkOrderImpl)order,ctx, con);
		}
		log.trace("[END]");
	}
	/**
	 * Create a new record for the specified <code>WorkOrderOld</code> object.
	 * 
	 * @param order
	 * @param ctx
	 * @param con
	 * @throws DAOException
	 */
	private void _create(WorkOrderImpl order, UserContext ctx, Connection con)
	throws DAOException {
		log.trace("[BEGIN]");
		boolean workOrderSaved=false;
		int numRows;
		long currentUserPoid=Long.MIN_VALUE;
		long poid=Long.MIN_VALUE;
		JournalEntryImpl tmpJournalEntry=null;
		Party agent=null;
		Party consignee=null;
		Party customer=null;
		Party tmpParty=null;
		Date tmpDate=null;
		IdentifiableDocumentImpl tmpDocument=null;
		List<AccessorialChargeImpl> accessorialChargeList=null;
		List<WorkOrderLineItem> lineItemList=null;
		PreparedStatement pStmt=null;
		State tmpState=null;
		String tmpString=null;
		
		// Get the current user and date/time information
		Timestamp currentTime=new Timestamp(System.currentTimeMillis());
		currentUserPoid=getCurrentUser(ctx);
		
		try {
			// Prepare the INSERT statement
			pStmt=con.prepareStatement(QUERY_INSERT);
			// Get the next poid
			poid=getNextPoid(con);
			// Store the Poid in the work order
			order.setPoid(poid);
			// Populate the prepared statement with values
			pStmt.setLong(1,poid);						// POID
			pStmt.setLong(2,currentUserPoid);			// CREATED_BY
			pStmt.setTimestamp(3,currentTime);			// DATE_CREATED
			pStmt.setLong(4,currentUserPoid);			// MODIFIED_BY
			pStmt.setTimestamp(5,currentTime);			// DATE_MODIFIED
			//***** Work Order Id
			tmpString=order.getWorkOrderId();
			if ((tmpString == null) || (tmpString.trim().length()==0)) {
				tmpString=dateSequence.getNextValue();
				order.setWorkOrderId(tmpString);
			} // END if ((tmpString == null) || (tmpString.trim().length()==0))
			pStmt.setString(6, tmpString);
			//***** Locator (a Base-36 value)
			tmpString=Long.toString((Long)locatorSequence.getNextValue(),36);
			order.setLocatorId(tmpString);
			pStmt.setString(7,tmpString);
			//********** ASSIGNED_TO_GROUP
			tmpString=order.getAssignedToGroup();
			if(tmpString!=null) {
				pStmt.setString(8,tmpString);
			} else {
				pStmt.setNull(8,Types.VARCHAR);
			} // if(tmpString!=null)
			//********** ASSIGNED_TO_INDIVIDUAL
			tmpString=order.getAssignedToIndividual();
			if(tmpString!=null) {
				pStmt.setString(9,tmpString);
			} else {
				pStmt.setNull(9,Types.VARCHAR);
			}
			//********** BILLING_DATE
			tmpDate=order.getBillingDate();
			if (tmpDate!=null) {
				pStmt.setTimestamp(10, new Timestamp(tmpDate.getTime()));
			} else {
				pStmt.setNull(10, Types.TIMESTAMP);
			} // END if (tmpDate!=null) 
			//********** BILL_OF_LADING
			tmpDocument=(IdentifiableDocumentImpl) order.getBillOfLading();
			if (tmpDocument != null) {
				// Save the changes to the document...
				documentDAO.save(tmpDocument,ctx,con);
				// Update the work order
				pStmt.setLong(11,tmpDocument.getPoid());
			} else {
				// The Bill of Lading was null
				pStmt.setNull(11, Types.BIGINT);
			} // END if (tmpDocument != null)
			//********** BILLING_REFERENCE_ID2
			tmpString=order.getBillingReferenceId();
			if (tmpString!=null) {
				pStmt.setString(12, tmpString);
			} else {
				pStmt.setNull(12,Types.VARCHAR);
			}
			//**********  BILLING_CONTACT
			tmpString=order.getBillingContactName();
			if (tmpString != null) {
				pStmt.setString(13, tmpString);
			} else {
				pStmt.setNull(13,Types.VARCHAR);
			} // END if (tmpString != null)
			//********** BILLING_ADDRESS
			tmpString=order.getBillingAddress();
			if (tmpString != null ) {
				pStmt.setString(14,tmpString);
			} else {
				pStmt.setNull(14, Types.VARCHAR);
			} // END if (tmpString != null)
			//********** BILLING_ADDRESS2
			tmpString=order.getBillingAddressSecondary();
			if (tmpString != null ) {
				pStmt.setString(15,tmpString);
			} else {
				pStmt.setNull(15, Types.VARCHAR);
			} // END if (tmpString != null)
			//********** BILLING_CITY
			tmpString=order.getBillingCity();
			if (tmpString != null ) {
				pStmt.setString(16,tmpString);
			} else {
				pStmt.setNull(16, Types.VARCHAR);
			} // END if (tmpString != null)
			//********** BILLING_STATE
			tmpState=order.getBillingState();
			if (tmpState != null) {
				tmpString=tmpState.getAbbreviation();
				if ((tmpString != null) && (tmpString.length()==2)) {
					// If the state abbreviation is not null and it is two
					// characters long, proceed...
					pStmt.setString(17, tmpString);
				} else {
					// The state abbreviation was either null or it was
					// larger than two characters.
					if (log.isDebugEnabled()) {
						log.debug("State abbreviation either null or too long");
						log.debug("State: "+tmpState);
					} // END if (log.isDebugEnabled())
					pStmt.setNull(17, Types.VARCHAR);
				} // END if ((tmpString != null) && (tmpString.length()==2))
			} else {
				// The billing state is null
				pStmt.setNull(17, Types.CHAR);
			} // END if (tmpState != null)
			//********** BILLING_ZIP
			tmpString=order.getBillingZip();
			if (tmpString != null) {
				pStmt.setString(18, tmpString);
			} else {
				// The Billing Zip Code was not provided.
				pStmt.setNull(18, Types.VARCHAR);
			} // END if (tmpString != null)
			//********** BILLING_PHONE
			tmpString=order.getBillingPhone();
			if (tmpString != null) {
				pStmt.setString(19, tmpString);
			} else {
				pStmt.setNull(19, Types.VARCHAR);
			} // END if (tmpString != null)
			//**********  BILLING_TERMS 
			tmpString=order.getBillingTerms();
			if (tmpString != null) {
				pStmt.setString(20, tmpString);
			} else {
				// The Billing Terms Code was not provided.
				pStmt.setNull(20, Types.VARCHAR);
			} // END if (tmpString != null)
			pStmt.setInt(21, order.getStatus());			// STATUS
			pStmt.setInt(22,order.getType());				// WORKORDER_TYPE
			//*********** Agent
			agent=order.getAgent();
			if (agent !=null) {
				// The agent is not null, so populate that with data
				if(getPartyPoid(agent)==Long.MIN_VALUE) {
					// The party hasn't yet been saved, so save it.
					saveParty(agent,ctx,con);
				}
				pStmt.setString(23,agent.getName());		// AGENT_NAME
				pStmt.setLong(24,getPartyPoid(agent));		// AGENT_POID
				pStmt.setInt(25,agent.getType());			// AGENT_TYPE
			} else {
				pStmt.setNull(23,Types.VARCHAR);			// AGENT_NAME
				pStmt.setNull(24,Types.BIGINT);				// AGENT_POID
				pStmt.setNull(25,Types.INTEGER);			// AGENT_TYPE
			}
			//********** Customer
			customer=order.getCustomer();
			if (customer!=null) {
				if(getPartyPoid(customer)==Long.MIN_VALUE) {
					// The customer has not yet been saved, so save it...
					saveParty(customer,ctx,con);
				}
				pStmt.setString(26,customer.getName());		// CUSTOMER_NAME
				pStmt.setLong(27,getPartyPoid(customer));	// CUSTOMER_POID
				pStmt.setInt(28,customer.getType());		// CUSTOMER_TYPE
			} else {
				pStmt.setNull(26,Types.VARCHAR);			// CUSTOMER_NAME
				pStmt.setNull(27,Types.BIGINT);				// CUSTOMER_POID
				pStmt.setNull(28,Types.INTEGER);			// CUSTOMER_TYPE
			}
			//********** CUSTOMER ORDER ID
			tmpString=order.getCustomerOrderId();
			if (tmpString != null) {
				pStmt.setString(29, tmpString);
			} else {
				pStmt.setNull(29,Types.VARCHAR);
			}
			//********** SHIPPING_METHOD
			tmpString=order.getShippingMethod();
			if (tmpString != null) {
				pStmt.setString(30, tmpString);
			} else {
				pStmt.setNull(30,Types.VARCHAR);
			}
			//********** APPOINTMENT_TIME
			tmpDate=order.getAppointmentTime();
			if (tmpDate != null) {
				pStmt.setTimestamp(31,
					new Timestamp(tmpDate.getTime()));
			} else {
				pStmt.setNull(31,Types.TIMESTAMP);
			}
			//********** ARRIVAL_TIME
			tmpDate=order.getArrivalTime();
			if (tmpDate != null) {
				pStmt.setTimestamp(32, 
					new Timestamp(tmpDate.getTime()));
			} else {
				pStmt.setNull(32,Types.TIMESTAMP);
			}
			//********** START_TIME
			tmpDate=order.getStartTime();
			if (tmpDate != null) {
				pStmt.setTimestamp(33, 
					new Timestamp(tmpDate.getTime()));
			} else {
				pStmt.setNull(33,Types.TIMESTAMP);
			}
			//********** COMPLETE_TIME
			tmpDate=order.getCompleteTime();
			if (tmpDate != null) {
				pStmt.setTimestamp(34, 
					new Timestamp(tmpDate.getTime()));
			} else {
				pStmt.setNull(34,Types.TIMESTAMP);
			}
			//********** Consignee
			consignee=order.getConsignee();
			if (consignee !=null) {
				if(getPartyPoid(consignee)==Long.MIN_VALUE) {
					// The consignee has not yet been saved, so save it...
					saveParty(consignee,ctx,con);
				}
				pStmt.setString(35,consignee.getName());	// CONSIGNEE_NAME
				pStmt.setLong(36,getPartyPoid(consignee));	// CONSIGNEE_POID
				pStmt.setInt(37,consignee.getType());		// CONSIGNEE_TYPE
			} else {
				pStmt.setNull(35,Types.VARCHAR);			// CONSIGNEE_NAME
				pStmt.setNull(36,Types.BIGINT);				// CONSIGNEE_POID
				pStmt.setNull(37,Types.INTEGER);			// CONSIGNEE_TYPE
			}
			//********** CONSIGNEE_CONTACT
			tmpString=order.getConsigneeContact();
			if(tmpString != null) {
				pStmt.setString(38,tmpString);
			} else {
				pStmt.setNull(38,Types.VARCHAR);
			}
			//********** CONSIGNEE_ADDRESS
			tmpString=order.getConsigneeAddress();
			if(tmpString != null) {
				pStmt.setString(39,tmpString);
			} else {
				pStmt.setNull(39,Types.VARCHAR);
			}
			//********** CONSIGNEE_ADDRESS2
			tmpString=order.getConsigneeAddressSecondary();
			if(tmpString != null) {
				pStmt.setString(40,tmpString);
			} else {
				pStmt.setNull(40,Types.VARCHAR);
			}
			//********** CONSIGNEE_CITY
			tmpString=order.getConsigneeCity();
			if(tmpString != null) {
				pStmt.setString(41,tmpString);
			} else {
				pStmt.setNull(41,Types.VARCHAR);
			}
			//********** CONSIGNEE_STATE
			tmpString=order.getConsigneeState().getAbbreviation();
			if(tmpString != null) {
				pStmt.setString(42,tmpString);
			} else {
				pStmt.setNull(42,Types.CHAR);
			}
			//********** CONSIGNEE_ZIP
			tmpString=order.getConsigneeZip();
			if(tmpString != null) {
				pStmt.setString(43,tmpString);
			} else {
				pStmt.setNull(43,Types.VARCHAR);
			}
			//********** CONSIGNEE_PHONE
			tmpString=order.getConsigneePhone();
			if(tmpString != null) {
				pStmt.setString(44,tmpString);
			} else {
				pStmt.setNull(44,Types.VARCHAR);
			}
			//********** CONSIGNEE_FAX
			tmpString=order.getConsigneeFax();
			if(tmpString != null) {
				pStmt.setString(45,tmpString);
			} else {
				pStmt.setNull(45,Types.VARCHAR);
			}
			//********** PICKUP_ADDRESS
			tmpString=order.getPickupAddress();
			if (tmpString != null) {
				pStmt.setString(46, tmpString);
			} else {
				pStmt.setNull(46,Types.VARCHAR);
			}
			//********** PICKUP_ADDRESS2
			tmpString=order.getPickupAddressSecondary();
			if (tmpString != null) {
				pStmt.setString(47, tmpString);
			} else {
				pStmt.setNull(47, Types.VARCHAR);
			}
			//********** PICKUP_CITY
			tmpString=order.getPickupCity();
			if (tmpString != null) {
				pStmt.setString(48, tmpString);
			} else {
				pStmt.setNull(48, Types.VARCHAR);
			}
			//********** PICKUP_STATE
			tmpString=order.getPickupState().getAbbreviation();
			if (tmpString != null) {
				pStmt.setString(49, tmpString);
			} else {
				pStmt.setNull(49, Types.CHAR);
			}
			//********** PICKUP_ZIP
			tmpString=order.getPickupZip();
			if (tmpString != null) {
				pStmt.setString(50, tmpString);
			} else {
				pStmt.setNull(50, Types.VARCHAR);
			}
			//********** PICKUP_PHONE
			tmpString=order.getPickupPhone();
			if (tmpString != null) {
				pStmt.setString(51, tmpString);
			} else {
				pStmt.setNull(51, Types.VARCHAR);
			}
			//********** PROOF_OF_DELIVERY
			tmpDocument=(IdentifiableDocumentImpl) order.getProofOfDelivery();
			if (tmpDocument != null) {
				// Save the changes to the document...
				documentDAO.save(tmpDocument,ctx,con);
				// Update the work order
				pStmt.setLong(52,tmpDocument.getPoid());
			} else {
				// The proofOfDelivery object was null
				pStmt.setNull(52,Types.BIGINT);
			} // END if (tmpDocument != null) 
			// Execute the INSERT query
			numRows=pStmt.executeUpdate();
			if(numRows==1) {
				workOrderSaved=true;
			} else {
				workOrderSaved=false;
			}
			// Close the PreparedStatement
			pStmt.close();
			// Save the line items
			lineItemList=order.getLineItems();
			for(WorkOrderLineItem item: lineItemList) {
				// Save each WorkOrderLineItem
				lineItemDAO.save(item,ctx,con);
			}
			// Save the charges
			
			// Journal Entries
			tmpJournalEntry=(JournalEntryImpl) order.getJournalEntry();
			if (tmpJournalEntry != null) {
				// Since this is a new work order, set the newly generated poid 
				tmpJournalEntry.setRelatedObjectPoid(order.getPoid());
				// A new journalEntry exists, so check to see if there is data
				if (tmpJournalEntry.isDataAvailable()) {
					journalDAO.save(tmpJournalEntry, ctx, con);
				} // END if (tmpJournalEntry.isDataAvailable())
			} // END if (tmpJournalEntry != null)
		} catch (SQLException ex) {
			// An SQLException was thrown while saving the work order.
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
	private void _update(WorkOrderImpl order, UserContext ctx, Connection con)
	throws DAOException {
		log.trace("[BEGIN]");
		boolean workOrderSaved=false;
		int numRows;
		long agentPoid=Long.MIN_VALUE;
		long currentUserPoid=Long.MIN_VALUE;
		long consigneePoid=Long.MIN_VALUE;
		long customerPoid=Long.MIN_VALUE;
		long poid=Long.MIN_VALUE;
		Date tmpDate=null;
		IdentifiableDocumentImpl tmpDocument=null;
		IdentifiableDocumentImpl tmpDocumentCur=null;
		JournalEntry journalEntry=null;
		List<AccessorialCharge> accessorialChargeList=null;
		List<WorkOrderLineItem> lineItemList=null;
		List<WorkOrderLineItemImpl> currentLineItems=null;
		Party agent=null;
		Party consignee=null;
		Party customer=null;
		Party tmpParty=null;
		PreparedStatement pStmt=null;
		String tmpString=null;
		State tmpState=null;
		Timestamp currentTime=null;
		WorkOrderImpl dbWO=null;
		
		// Get the current user and date/time information
		currentTime=new Timestamp(System.currentTimeMillis());
		currentUserPoid=getCurrentUser(ctx);
		
		try {
			// Get the current work order POID
			poid=order.getPoid();
			
			// get the database value of the current work order.
			dbWO=(WorkOrderImpl) findByPrimaryKey(poid,ctx);
			
			// Prepare/compile the statement
			pStmt=con.prepareStatement(QUERY_UPDATE);
			pStmt.setLong(1,currentUserPoid);			// MODIFIED_BY
			pStmt.setTimestamp(2,currentTime);			// DATE_MODIFIED
			//********** WORK_ORDER_ID
			/*
			tmpString=order.getWorkOrderId();
			if (tmpString != null) {
				pStmt.setString(3,tmpString);
			} else {
				pStmt.setNull(3, Types.VARCHAR);
			}
			*/
			//********** ASSIGNED_TO_GROUP
			tmpString=order.getAssignedToGroup();
			if(tmpString!=null) {
				pStmt.setString(3,tmpString);
			} else {
				pStmt.setNull(3,Types.VARCHAR);
			}
			//********** ASSIGNED_TO_INDIVIDUAL
			tmpString=order.getAssignedToIndividual();
			if(tmpString!=null) {
				pStmt.setString(4,tmpString);
			} else {
				pStmt.setNull(4,Types.VARCHAR);
			}
			//********** BILLING_DATE
			tmpDate=order.getBillingDate();
			if (tmpDate!=null) {
				pStmt.setTimestamp(5, new Timestamp(tmpDate.getTime()));
			} else {
				pStmt.setNull(5, Types.TIMESTAMP);
			}
			//********** BILLING_REFERENCE_ID2
			tmpString=order.getBillingReferenceId();
			if (tmpString != null) {
				pStmt.setString(6,tmpString);
			} else {
				pStmt.setNull(6, Types.VARCHAR);
			}
			//********** BILLING CONTACT_NAME
			tmpString=order.getBillingContactName();
			if (tmpString != null) {
				pStmt.setString(7, tmpString);
			} else {
				pStmt.setNull(7, Types.VARCHAR);
			} // END if (tmpString != null)
			
			//********** BILLING_ADDRESS
			tmpString=order.getBillingAddress();
			if (tmpString != null ) {
				pStmt.setString(8,tmpString);
			} else {
				pStmt.setNull(8, Types.VARCHAR);
			} // END if (tmpString != null)
			//********** BILLING_ADDRESS2
			tmpString=order.getBillingAddressSecondary();
			if (tmpString != null ) {
				pStmt.setString(9,tmpString);
			} else {
				pStmt.setNull(9, Types.VARCHAR);
			} // END if (tmpString != null)
			//********** BILLING_CITY
			tmpString=order.getBillingCity();
			if (tmpString != null ) {
				pStmt.setString(10,tmpString);
			} else {
				pStmt.setNull(10, Types.VARCHAR);
			} // END if (tmpString != null)
			//********** BILLING_STATE
			tmpState=order.getBillingState();
			if (tmpState != null) {
				tmpString=tmpState.getAbbreviation();
				if ((tmpString != null) && (tmpString.length()==2)) {
					// If the state abbreviation is not null and it is two
					// characters long, proceed...
					pStmt.setString(11, tmpString);
				} else {
					// The state abbreviation was either null or it was
					// larger than two characters.
					if (log.isDebugEnabled()) {
						log.debug("State abbreviation either null or too long");
						log.debug("State: "+tmpState);
					} // END if (log.isDebugEnabled())
					pStmt.setNull(11, Types.VARCHAR);
				} // END if ((tmpString != null) && (tmpString.length()==2))
			} else {
				// The billing state is null
				pStmt.setNull(11, Types.CHAR);
			} // END if (tmpState != null)
			//********** BILLING_ZIP
			tmpString=order.getBillingZip();
			if (tmpString != null) {
				pStmt.setString(12, tmpString);
			} else {
				// The Billing Zip Code was not provided.
				pStmt.setNull(12, Types.VARCHAR);
			} // END if (tmpString != null)
			//********** BILLING_PHONE
			tmpString=order.getBillingPhone();
			if (tmpString !=null) {
				pStmt.setString(13, tmpString);
			} else {
				pStmt.setNull(13, Types.VARCHAR);
			} // END if (tmpString != null)
			//********** BILLING_TERMS 
			tmpString=order.getBillingTerms();
			if (tmpString != null) {
				pStmt.setString(14, tmpString);
			} else {
				// The Billing Terms Code was not provided.
				pStmt.setNull(14, Types.VARCHAR);
			} // END if (tmpString != null)
			//********** BILL_OF_LADING
			// Get the new BillOfLading document...
			tmpDocument=(IdentifiableDocumentImpl) order.getBillOfLading();
			// Get the database version of the BillOfLading
			tmpDocumentCur=(IdentifiableDocumentImpl) dbWO.getBillOfLading();
			if (tmpDocument==null) {
				/* There is no longer a bill of lading, so check to see if 
				   there is one at the database level. */
				if(tmpDocumentCur != null) {
					/* Since there is a document stored at the database level,
					 * delete it. */
					if (log.isTraceEnabled()) {
						log.trace("Deleting current document."+
								tmpDocumentCur);
					} // END if (log.isTraceEnabled())
					documentDAO.delete(tmpDocumentCur, ctx, con);
				} // END if(tmpDocumentCur != null)
				// Set a NULL value for the BILL_OF_LADING column.
				pStmt.setNull(15, Types.BIGINT);
			} else {
				/* There is a new bill of lading, so check to see if there 
				 * is one at the database level.
				 */
				if (tmpDocumentCur != null) {
					/* Set the poid of the new document equal to the poid of
					 * the database document.  This way, the document
					 * currently associated with the work order is over-written
					 * with the new data.
					 */
					tmpDocument.setPoid(tmpDocumentCur.getPoid());
				} // END if (tmpDocumentCur != null)
				// Save the new document.
				documentDAO.save(tmpDocument, ctx, con);
				// Set the BILL_OF_LADING value
				pStmt.setLong(15,tmpDocument.getPoid());
			} // END if (tmpDocument != null)
			//********** STATUS
			pStmt.setInt(16,order.getStatus());
			//********** WORKORDER_TYPE
			pStmt.setInt(17, order.getType());
			// Agent
			agent=order.getAgent();
			if (agent !=null) {
				agentPoid=getPartyPoid(agent);
				if (agentPoid==Long.MIN_VALUE) {
					// The agent has not yet been saved, so save it...
					saveParty(agent,ctx,con);
					// Update the agentPoid with the new Poid
					agentPoid=getPartyPoid(agent);
				} // END if (agentPoid==Long.MIN_VALUE)
				pStmt.setString(18,agent.getName());	// AGENT_NAME
				pStmt.setLong(19,agentPoid);			// AGENT_POID
				pStmt.setInt(20,agent.getType());		// AGENT_TYPE
			} else {
				pStmt.setNull(18,Types.VARCHAR);		// AGENT_NAME
				pStmt.setNull(19,Types.BIGINT);			// AGENT_POID
				pStmt.setNull(20,Types.INTEGER);		// AGENT_TYPE
			} // END if (agent != null)
			//********** Customer
			customer=order.getCustomer();
			if (customer!=null) {
				customerPoid=getPartyPoid(customer);
				if (customerPoid==Long.MIN_VALUE) {
					// The customer has not yet been saved, so save it...
					saveParty(customer,ctx,con);
					// Update the customerPoid...
					customerPoid=getPartyPoid(customer);
				} // END if (customerPoid==Long.MIN_VALUE)
				pStmt.setString(21,customer.getName());	// CUSTOMER_NAME
				pStmt.setLong(22,customerPoid);			// CUSTOMER_POID
				pStmt.setInt(23,customer.getType());	// CUSTOMER_TYPE
			} else {
				pStmt.setNull(21,Types.VARCHAR);		// CUSTOMER_NAME
				pStmt.setNull(22,Types.BIGINT);			// CUSTOMER_POID
				pStmt.setNull(23,Types.INTEGER);		// CUSTOMER_TYPE
			} // END if (customer != null)
			//********** CUSTOMER_ORDER_ID
			tmpString=order.getCustomerOrderId();
			if (tmpString != null) {
				pStmt.setString(24, tmpString);
			} else {
				pStmt.setNull(24,Types.VARCHAR);
			} // END if (tmpString != null)
			//********** SHIPPING_METHOD
			tmpString=order.getShippingMethod();
			if (tmpString != null) {
				pStmt.setString(25, tmpString);
			} else {
				pStmt.setNull(25,Types.VARCHAR);
			}  // END if (tmpString != null)
			//********** APPOINTMENT_DATE
			tmpDate=order.getAppointmentTime();
			if (tmpDate != null) {
				pStmt.setTimestamp(26,
					new Timestamp(tmpDate.getTime()));
			} else {
				pStmt.setNull(26,Types.TIMESTAMP);
			} // END if (tmpDate != null)
			// ARRIVAL_TIME
			tmpDate=order.getArrivalTime();
			if (tmpDate != null) {
				pStmt.setTimestamp(27, 
					new Timestamp(tmpDate.getTime()));
			} else {
				pStmt.setNull(27,Types.TIMESTAMP);
			} // END if (tmpDate != null)
			//********** START_TIME
			tmpDate=order.getStartTime();
			if (tmpDate != null) {
				pStmt.setTimestamp(28, 
					new Timestamp(tmpDate.getTime()));
			} else {
				pStmt.setNull(28,Types.TIMESTAMP);
			} // END if (tmpDate != null)
			//********** COMPLETE_TIME
			tmpDate=order.getCompleteTime();
			if (tmpDate != null) {
				pStmt.setTimestamp(29, 
					new Timestamp(tmpDate.getTime()));
			} else {
				pStmt.setNull(29,Types.TIMESTAMP);
			} // END if (tmpDate != null)
			//********** Consignee
			consignee=order.getConsignee();
			if (consignee !=null) {
				consigneePoid=getPartyPoid(consignee);
				if (consigneePoid==Long.MIN_VALUE) {
					// The consignee has not yet been saved, so save it...
					saveParty(consignee,ctx,con);
					// Update the consignee poid
					consigneePoid=getPartyPoid(consignee);
				} // END if (consigneePoid==Long.MIN_VALUE)
				pStmt.setString(30,consignee.getName());	// CONSIGNEE_NAME
				pStmt.setLong(31,consigneePoid);			// CONSIGNEE_POID
				pStmt.setInt(32,consignee.getType());		// CONSIGNEE_TYPE
			} else {
				pStmt.setNull(30,Types.VARCHAR);			// CONSIGNEE_NAME
				pStmt.setNull(31,Types.BIGINT);				// CONSIGNEE_POID
				pStmt.setNull(32,Types.INTEGER);			// CONSIGNEE_TYPE
			} // END if (consignee != null)
			//********** CONSIGNEE_CONTACT
			tmpString=order.getConsigneeContact();
			if(tmpString != null) {
				pStmt.setString(33,tmpString);
			} else {
				pStmt.setNull(33,Types.VARCHAR);
			} // END if (tmpString != null)
			//********** CONSIGNEE_ADDRESS
			tmpString=order.getConsigneeAddress();
			if(tmpString != null) {
				pStmt.setString(34,tmpString);
			} else {
				pStmt.setNull(34,Types.VARCHAR);
			} // END if (tmpString != null)
			//********** CONSIGNEE_ADDRESS2
			tmpString=order.getConsigneeAddressSecondary();
			if(tmpString != null) {
				pStmt.setString(35,tmpString);
			} else {
				pStmt.setNull(35,Types.VARCHAR);
			} // END if (tmpString != null)
			//********** CONSIGNEE_CITY
			tmpString=order.getConsigneeCity();
			if (tmpString != null) {
				pStmt.setString(36,tmpString);
			} else {
				pStmt.setNull(36,Types.VARCHAR);
			} // END if (tmpString != null)
			//********** CONSIGNEE_STATE
			tmpString=null;
			tmpState=order.getConsigneeState();
			if (tmpState != null) {
				tmpString=tmpState.getAbbreviation();
			} // END if (tmpState != null) {
			if(tmpString != null) {
				pStmt.setString(37,tmpString);
			} else {
				pStmt.setNull(37,Types.CHAR);
			}
			//********** CONSIGNEE_ZIP
			tmpString=order.getConsigneeZip();
			if(tmpString != null) {
				pStmt.setString(38,tmpString);
			} else {
				pStmt.setNull(38,Types.CHAR);
			}
			//********** CONSIGNEE_PHONE
			tmpString=order.getConsigneePhone();
			if(tmpString != null) {
				pStmt.setString(39,tmpString);
			} else {
				pStmt.setNull(39,Types.CHAR);
			}
			//********** CONSIGNEE_FAX
			tmpString=order.getConsigneeFax();
			if(tmpString != null) {
				pStmt.setString(40,tmpString);
			} else {
				pStmt.setNull(40,Types.CHAR);
			}
			//********** PICKUP_ADDRESS
			tmpString=order.getPickupAddress();
			if (tmpString != null) {
				pStmt.setString(41, tmpString);
			} else {
				pStmt.setNull(41,Types.VARCHAR);
			}
			//********** PICKUP_ADDRESS2
			tmpString=order.getPickupAddressSecondary();
			if (tmpString != null) {
				pStmt.setString(42, tmpString);
			} else {
				pStmt.setNull(42, Types.VARCHAR);
			} // END if(tmpString != null)
			//********** PICKUP_CITY
			tmpString=order.getPickupCity();
			if (tmpString != null) {
				pStmt.setString(43, tmpString);
			} else {
				pStmt.setNull(43, Types.VARCHAR);
			}
			//********** PICKUP_STATE
			tmpString=null;
			tmpState=order.getPickupState();
			if (tmpState != null) {
				tmpString=tmpState.getAbbreviation();
			} // END tmpString=tmpState.getAbbreviation();
			if (tmpString != null) {
				pStmt.setString(44, tmpString);
			} else {
				pStmt.setNull(44, Types.CHAR);
			} // END if (tmpString != null)
			//********** PICKUP_ZIP
			tmpString=order.getPickupZip();
			if (tmpString != null) {
				pStmt.setString(45, tmpString);
			} else {
				pStmt.setNull(45, Types.VARCHAR);
			} // END if (tmpString != null)
			//********** PICKUP_PHONE
			tmpString=order.getPickupPhone();
			if (tmpString != null) {
				pStmt.setString(46, tmpString);
			} else {
				pStmt.setNull(46, Types.VARCHAR);
			} // END if (tmpString != null)
			//********** PROOF_OF_DELIVERY
			// Get the new ProofOfDelivery document...
			tmpDocument=(IdentifiableDocumentImpl) order.getProofOfDelivery();
			// Get the database version of the ProofOFDelivery
			tmpDocumentCur=(IdentifiableDocumentImpl) dbWO.getProofOfDelivery();
			if (tmpDocument==null) {
				/* There is no longer a proof of delivery, so check to see if 
				   there is one at the database level. */
				if(tmpDocumentCur != null) {
					/* Since there is a document stored at the database level,
					 * delete it. */
					if (log.isTraceEnabled()) {
						log.trace("Deleting current document."+
								tmpDocumentCur);
					} // END if (log.isTraceEnabled())
					documentDAO.delete(tmpDocumentCur, ctx, con);
				} // END if(tmpDocumentCur != null)
				pStmt.setNull(47, Types.BIGINT);
			} else {
				/* There is a new proof of delivery, so check to see if there 
				 * is one at the database level.
				 */
				if (tmpDocumentCur != null) {
					/* Set the poid of the new document equal to the poid of
					 * the database document.  This way, the document
					 * currently associated with the work order is over-written
					 * with the new data.
					 */
					tmpDocument.setPoid(tmpDocumentCur.getPoid());
				} // END if (tmpDocumentCur != null)
				// Save the new document.
				documentDAO.save(tmpDocument, ctx, con);
				// Store the poid of the current document
				pStmt.setLong(47,tmpDocument.getPoid());
			} // END if (tmpDocument != null)
			//********** WORKORDER_POID
			pStmt.setLong(48,poid);
			
			// Execute the UPDATE query
			numRows=pStmt.executeUpdate();
			if(numRows==1) {
				workOrderSaved=true;
			} else {
				workOrderSaved=false;
			}
			// Close the PreparedStatement
			pStmt.close();
			// Save the line items
			lineItemList=order.getLineItems();
			// Get currentLineItemList
			for(WorkOrderLineItem item: lineItemList) {
				// Save each WorkOrderLineItem
				lineItemDAO.save(item,ctx,con);
			}
			// Remove superfulous line Items
			lineItemList=order.getLineItemsToDelete();
			if (lineItemList != null) {
				for (int i=lineItemList.size()-1; i >= 0; i--) {
					//WorkOrderLineItem item: lineItemList)
					WorkOrderLineItem item=lineItemList.get(i);
					lineItemDAO.delete(item, ctx,con);
					// After deleting the lineItem, remove it from the list...
					lineItemList.remove(item);
				} // END for (int i=lineItemList.size(); i >= 0; i--)				
			} // END if (lineItemList != null)
			
			// Save the accessorial charges
			accessorialChargeList=order.getAccessorialCharges();
			if (accessorialChargeList != null) {
				for (AccessorialCharge charge: accessorialChargeList) {
					// Save each Accessorial Charge
					accessorialChargeDAO.save(charge,ctx,con);
				} // END for (AccessorialCharge charge: accessorialChargeList)
			} // END if (accessorialChargeList != null)
			
			// Remove superfulous line items
			accessorialChargeList=order.getAccessorialChargesToDelete();
			if (accessorialChargeList != null) {
				for (int i=accessorialChargeList.size()-1; i>=0; i--) {
					// AccessorialCharge charge: accessorialChargeList
					AccessorialCharge charge=accessorialChargeList.get(i);
					accessorialChargeDAO.delete(charge,ctx,con);
					/* After deleting the accessorialCharge, remove it from
					   the list. */
					accessorialChargeList.remove(charge);
				} // END for (int i=accessorialChargeList.size()-1; i>=0; i--)
			} // END if (accessorialChargeList != null) 
			
			// Handle the journal entry
			journalEntry=order.getJournalEntry();
			if (journalEntry != null) {
				log.trace("JournalEntry exists, so save it.");
				// If the journal entry exists, then save it.
				journalDAO.save(journalEntry, ctx, con);
			}
		} catch (SQLException ex) {
			// Print the error information
			log.error("SQLException thrown.",ex);
			log.error("SQLState: "+ex.getSQLState());
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
	 * 
	 */
	public void delete(WorkOrderOld order, UserContext ctx) throws DAOException {
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
			log.error("DAOException thrown.",ex);
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
	}
	/**
	 * 
	 */
	protected void delete(WorkOrderOld order, UserContext ctx, Connection con)
	throws DAOException {
		log.trace("[BEGIN]");
		long poid;
		int numRows;
		PreparedStatement pStmt;
		
		// Validate the parameters
		checkObject(order);
		checkObject(ctx,WildDAO.VALIDATE_NULL);
		// Get a connection
		con=pm.getConnection();
		// Get the company object's poid
		poid=((WorkOrderImpl) order).getPoid();
		if (poid == Long.MIN_VALUE) {
			// The Group object has not yet been saved.
			StringBuilder msg=new StringBuilder(128);
			msg.append("WorkOrderDAOImpl.delete() invoked.  The specified ");
			msg.append("work order has not yet been saved.\nPoid=");
			msg.append(poid);
			log.error(msg.toString());
			msg=null;
		} else {
			// The Group has been saved, so let's delete it.
			try {
				con.setAutoCommit(false);
				// Delete the line items assocaited with the work order
				lineItemDAO.delete((WorkOrderImpl)order, ctx, con);
				pStmt=con.prepareStatement(QUERY_DELETE);
				pStmt.setLong(1,poid);
				// Execute the DELETE query
				numRows=pStmt.executeUpdate();
				if (numRows!=1) {
					con.rollback();
				}
				con.commit();
			} catch (SQLException ex) {
				// Print the error informaiton
				log.error("A SQLException was thrown.",ex);
				try {
					con.rollback();
				} catch (SQLException ex1) {
					// A SQLException thrown while rolling back transactions
					log.error(ERR_SQLEXCEPTION_ROLLBACK,ex);
				}
			} finally {
				try {
					con.close();
				} catch (SQLException ex) {
					// A SQLException thrown while rolling back a transaction
					log.error(ERR_SQLEXCEPTION_CON_CLOSE,ex);
				}
			} // END try
		} // END if(poid == Double.MIN_VALUE)
		log.trace("[BEGIN]");
	}
	/**
	 * Find a specific work order by WorkOrderId.
	 */
	public WorkOrderOld findByWorkOrderId(String workOrderId, UserContext ctx)
	throws DAOException {
		log.trace("[BEGIN]");
		WorkOrderOld workOrder=null;
		Connection con=null;	
		
		try {
			// Obtain a connection
			con=pm.getConnection();
			// Disable auto-commit
			con.setAutoCommit(false);
			// Invoke the "save" logic
			workOrder=findByWorkOrderId(workOrderId,ctx,con);
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
			} // END if (con != null)
		}
		log.trace("[END]");
		return workOrder;
	}
	/**
	 * Find a specific work order by WorkOrderId.
	 */
	protected WorkOrderOld findByWorkOrderId(String workOrderId, UserContext ctx,
		Connection con) throws DAOException {
		log.trace("[BEGIN]");
		WorkOrderImpl order=null;
		PreparedStatement pStmt=null;
		ResultSet rs=null;
		
		if (workOrderId != null) {
			try {
				// Prepare the statement...
				pStmt=con.prepareStatement(QUERY_SELECT_BY_WORKORDERID);
				// Specify the poid
				pStmt.setString(1,workOrderId);
				// Execute the statement
				rs=pStmt.executeQuery();
				// If the query returns a value...
				if (rs.next()) {
					// The resultSet contains a value.
					order = new WorkOrderImpl();
					setWildObjectData(order,rs);
					populateWorkOrder(order,rs,ctx,con);
				}
				// Close the ResultSet
				rs.close();
				// Close the PreparedStatement
				pStmt.close();
			} catch (SQLException ex) {
				StringBuilder msg=new StringBuilder(128);
				msg.append("SQLException thrown while trying to create a ");
				msg.append("WorkOrderOld object.");
				log.error(msg.toString(),ex);
			}
		} else {
			log.error("DEBUG: workOrderID: "+workOrderId);
			throw new DAOException(ERR_POID_INVALID);
		}
		log.trace("[END]");
		return order;
	}
	/**
	 * Retrieve a work order based upon it's unique identifier.
	 * 
	 * @param long The unique identifier of the work order.
	 * @param com.wildstartech.justo.crm.dao.UserContext The <code>UserContext</code>
	 * under which all data-base related activites should be performed.
	 * 
	 * @return com.wildstartech.justo.crm.WorkOrder
	 */
	public WorkOrderOld findByPrimaryKey(long key, UserContext ctx) 
	throws DAOException {
		log.trace("[BEGIN]");
		WorkOrderOld workOrder=null;
		Connection con=null;	
		
		try {
			// Obtain a connection
			con=pm.getConnection();
			// Disable auto-commit
			con.setAutoCommit(false);
			// Invoke the "save" logic
			workOrder=findByPrimaryKey(key,ctx,con);
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
			try {
				con.rollback();
			} catch (SQLException exception) {
				log.error("SQLException thrown rolling back the transaction.");
			}
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
		return workOrder;
	}
	/**
	 * Method to retrieve a <code>WorkOrderOld</code> using an existing connection.
	 * 
	 * If no matching record is found then <code>null</code> is returned.
	 * 
	 * @param long Unique identifier of the work order in the database.
	 * @param com.wildstartech.justo.crm.dao.UserContext he <code>UserContext</code>
	 * under which all data-base related activites should be performed.
	 * @param java.sql.Connection The database connection under which the
	 * operation should be performed.
	 * 
	 * @return com.wildstartech.justo.crm.WorkOrder The requested work order.
	 */
	protected WorkOrderOld findByPrimaryKey(long key, UserContext ctx, 
		Connection con) throws DAOException {
		log.trace("[BEGIN]");
		WorkOrderImpl order=null;
		PreparedStatement pStmt=null;
		ResultSet rs=null;
		
		if (key != Long.MIN_VALUE) {
			/* As long as the primary key is not the designated minimum 
			   allowable value. */
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
					order = new WorkOrderImpl();
					setWildObjectData(order,rs);
					populateWorkOrder(order,rs,ctx,con);
				}
				// Close the ResultSet
				rs.close();
				// Close the PreparedStatement
				pStmt.close();
			} catch (SQLException ex) {
				StringBuilder msg=new StringBuilder(128);
				msg.append("SQLException thrown while trying to create a ");
				msg.append("Group object.");
				log.error(msg.toString(),ex);
			}
		} else {
			log.error("DEBUG: poid: "+key);
		}
		log.trace("[END]");
		return order;
	}
	/**
	 * 
	 */
	public List<WorkOrderListElement> findAll(UserContext ctx) 
	throws DAOException {
		log.trace("[BEGIN]");
		Connection con=null;	
		List<WorkOrderListElement> workOrderList=null;
		
		try {
			// Obtain a connection
			con=pm.getConnection();
			// Disable auto-commit
			con.setAutoCommit(false);
			// Invoke the "save" logic
			workOrderList=findAll(ctx,con);
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
			// Re-throw DAOException
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
		return workOrderList;
	}
	/**
	 * 
	 * @param ctx
	 * @param con
	 * @return
	 * @throws DAOException
	 */
	protected List<WorkOrderListElement> findAll(UserContext ctx,
		Connection con) 
	throws DAOException {
		log.trace("[BEGIN]");
		ArrayList<WorkOrderListElement> orderList=null;
		WorkOrderListElementImpl listElement=null;
		PreparedStatement pStmt=null;
		ResultSet rs=null;

		if (ctx != null) {
			orderList=new ArrayList<WorkOrderListElement>();
			try {
				// Prepare the statement...
				pStmt=con.prepareStatement(QUERY_LIST_ALL);
				// Execute the statement
				rs=pStmt.executeQuery();
				// Process the resultSet...
				while (rs.next()) {
					// The resultSet contains a value.
					listElement=new WorkOrderListElementImpl();
					populateWorkOrderListElement(listElement,rs);
					orderList.add(listElement);
				}
				// Close the ResultSet
				rs.close();
				// Close the PreparedStatement
				pStmt.close();
			} catch (SQLException ex) {
				StringBuilder msg=new StringBuilder(128);
				msg.append("SQLException thrown while trying to create a ");
				msg.append("WorkOrderOld object.");
				log.error(msg.toString(),ex);
				msg=null;
			}
		} else {
			StringBuilder msg=new StringBuilder(128);
			msg.append("WorkOrderDAO.findAll(UserContext) passed a null ");
			msg.append("UserContext object.");
			log.error(msg.toString());
			msg=null;
		}// END if (ctx != null)
		log.trace("[END]");
		return orderList;
	}
	//***** findAllOpen
	/**
	 * Return a list of open WorkOrders.
	 */
	public List<WorkOrderListElement> findAllOpen(UserContext ctx)
	throws DAOException {
		log.trace("[BEGIN]");
		Connection con=null;	
		List<WorkOrderListElement> workOrderList=null;
		
		try {
			// Obtain a connection
			con=pm.getConnection();
			// Disable auto-commit
			con.setAutoCommit(false);
			// Invoke the "save" logic
			workOrderList=findAllOpen(ctx,con);
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
			// Re-throw DAOException
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
		return workOrderList;
	}
	/**
	 * 
	 * @param ctx
	 * @param con
	 * @return
	 * @throws DAOException
	 */
	protected List<WorkOrderListElement> findAllOpen(UserContext ctx,
		Connection con) throws DAOException {
		log.trace("[BEGIN]");
		ArrayList<WorkOrderListElement> orderList=null;
		WorkOrderListElementImpl listElement=null;
		PreparedStatement pStmt=null;
		ResultSet rs=null;

		if (ctx != null) {
			orderList=new ArrayList<WorkOrderListElement>();
			try {
				// Prepare the statement...
				pStmt=con.prepareStatement(QUERY_LIST_LESS_STATUS);
				// Loook for status less than "Delivered"
				pStmt.setInt(1,4);
				// Execute the statement
				rs=pStmt.executeQuery();
				// Process the resultSet...
				while (rs.next()) {
					// The resultSet contains a value.
					listElement=new WorkOrderListElementImpl();
					populateWorkOrderListElement(listElement,rs);
					orderList.add(listElement);
				}
				// Close the ResultSet
				rs.close();
				// Close the PreparedStatement
				pStmt.close();
			} catch (SQLException ex) {
				StringBuilder msg=new StringBuilder(128);
				msg.append("SQLException thrown while trying to create a ");
				msg.append("WorkOrderOld object.");
				log.error(msg.toString(),ex);
				msg=null;
			}
		} else {
			StringBuilder msg=new StringBuilder(128);
			msg.append("WorkOrderDAO.findAll(UserContext) passed a null ");
			msg.append("UserContext object.");
			log.error(msg.toString());
			msg=null;
		}// END if (ctx != null)
		log.trace("[END]");
		return orderList;
	}
	//***** searchByCustomerOrderId
	public List<WorkOrderListElement> searchByCustomerOrderId(
		String customerOrderId, UserContext ctx) throws DAOException {
		log.trace("[BEGIN]");
		Connection con=null;	
		List<WorkOrderListElement> workOrderList=null;
		
		if (customerOrderId != null) {
			try {
				// Obtain a connection
				con=pm.getConnection();
				// Disable auto-commit
				con.setAutoCommit(false);
				// Invoke the "search" logic
				workOrderList=searchByCustomerOrderId(customerOrderId,ctx,con);
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
				// Re-throw DAOException
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
		} // END if (customerOrderId != null)
		log.trace("[END]");
		return workOrderList;
	}
	protected List<WorkOrderListElement> searchByCustomerOrderId(
		String customerOrderId, UserContext ctx, Connection con)
		throws DAOException {
		log.trace("[BEGIN]");
		List<WorkOrderListElement> workOrderList=null;
		PreparedStatement pStmt=null;
		ResultSet rs=null;
		StringBuilder query=null;
		WorkOrderListElementImpl listElement=null;

		if ((ctx != null) && (customerOrderId != null)) {
			query=new StringBuilder(50);
			query.append(customerOrderId);
			query.append('%');
			workOrderList=new ArrayList<WorkOrderListElement>();
			try {
				// Prepare the statement...
				pStmt=con.prepareStatement(QUERY_LIST_BY_CUSTOMER_ORDER_ID);
				pStmt.setString(1, query.toString());
				// Execute the statement
				rs=pStmt.executeQuery();
				// Process the resultSet...
				while (rs.next()) {
					// The resultSet contains a value.
					listElement=new WorkOrderListElementImpl();
					populateWorkOrderListElement(listElement,rs);
					workOrderList.add(listElement);
				}
				// Close the ResultSet
				rs.close();
				// Close the PreparedStatement
				pStmt.close();
			} catch (SQLException ex) {
				StringBuilder msg=new StringBuilder(128);
				msg.append("SQLException thrown while trying to create a ");
				msg.append("WorkOrderOld object.");
				log.error(msg.toString(),ex);
				msg=null;
			}
		} else {
			// If the ctx is null
			if (ctx==null) {
				StringBuilder msg=new StringBuilder(128);
				msg.append("Null UserContext object. passed as a paramter.");
				log.error(msg.toString());
				msg=null;
			} // if (ctx==null)
			// If the customer Order Id is null. 
			if (customerOrderId==null) {
				StringBuilder msg=new StringBuilder(80);
				msg.append("Null customerOrderId object passed.");
				log.error(msg.toString());
				msg=null;
			} // if (customerOrderId==null) 
		} // if ((ctx != null) && (customerOrderId != null)) 
		log.trace("[END]");
		return workOrderList;
	}
	//***** searchByLocatorId
	public List<WorkOrderListElement> searchByLocatorId(
		String locatorId, UserContext ctx) throws DAOException {
		log.trace("[BEGIN]");
		Connection con=null;	
		List<WorkOrderListElement> workOrderList=null;
		
		if (locatorId != null) {
			try {
				// Obtain a connection
				con=pm.getConnection();
				// Disable auto-commit
				con.setAutoCommit(false);
				// Invoke the "search" logic
				workOrderList=searchByLocatorId(locatorId,ctx,con);
				// Commit the transaction
				con.commit();
			} catch (DAOException ex) {
				// A DAOException was thrown, so rollback the transaction.
				try {
					con.rollback();
				} catch (SQLException ex1) {
					// SQLException was thrown either during the setAutoCommit()
					// or commit() methods
					log.error("SQLException thrown during rollback.",ex1);
				}
				// Re-throw DAOException
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
					} // END try/catch
				} // END if (con != null)
			} // END try/catch
		} // END if (locatorId != null) 
		log.trace("[END]");
		return workOrderList;
	}
	protected List<WorkOrderListElement> searchByLocatorId(
		String locatorId, UserContext ctx, Connection con)
		throws DAOException {
		log.trace("[BEGIN]");
		List<WorkOrderListElement> workOrderList=null;
		PreparedStatement pStmt=null;
		ResultSet rs=null;
		StringBuilder query=null;
		WorkOrderListElementImpl listElement=null;

		if ((ctx != null) && (locatorId != null)) {
			query=new StringBuilder(50);
			query.append(locatorId);
			query.append('%');
			workOrderList=new ArrayList<WorkOrderListElement>();
			try {
				// Prepare the statement...
				pStmt=con.prepareStatement(QUERY_LIST_BY_LOCATOR_ID);
				pStmt.setString(1, query.toString());
				// Execute the statement
				rs=pStmt.executeQuery();
				// Process the resultSet...
				while (rs.next()) {
					// The resultSet contains a value.
					listElement=new WorkOrderListElementImpl();
					populateWorkOrderListElement(listElement,rs);
					workOrderList.add(listElement);
				}
				// Close the ResultSet
				rs.close();
				// Close the PreparedStatement
				pStmt.close();
			} catch (SQLException ex) {
				StringBuilder msg=new StringBuilder(128);
				msg.append("SQLException thrown while trying to create a ");
				msg.append("WorkOrderListElement object.");
				log.error(msg.toString(),ex);
				msg=null;
			}
		} else {
			// If the ctx is null
			if (ctx==null) {
				StringBuilder msg=new StringBuilder(128);
				msg.append("Null UserContext object. passed as a paramter.");
				log.error(msg.toString());
				msg=null;
			} // if (ctx==null)
			// If the work Order Id is null. 
			if (locatorId==null) {
				StringBuilder msg=new StringBuilder(80);
				msg.append("Null locatorId object passed.");
				log.error(msg.toString());
				msg=null;
			} // if (locatorId==null) 
		} // if ((ctx != null) && (locatorId != null)) 
		log.trace("[END]");
		return workOrderList;
	}
	//***** searchByWorkOrderId
	public List<WorkOrderListElement> searchByWorkOrderId(
		String workOrderId, UserContext ctx) throws DAOException {
		log.trace("[BEGIN]");
		Connection con=null;	
		List<WorkOrderListElement> workOrderList=null;
		
		if (workOrderId != null) {
			try {
				// Obtain a connection
				con=pm.getConnection();
				// Disable auto-commit
				con.setAutoCommit(false);
				// Invoke the "search" logic
				workOrderList=searchByWorkOrderId(workOrderId,ctx,con);
				// Commit the transaction
				con.commit();
			} catch (DAOException ex) {
				// A DAOException was thrown, so rollback the transaction.
				try {
					con.rollback();
				} catch (SQLException ex1) {
					// SQLException was thrown either during the setAutoCommit()
					// or commit() methods
					log.error("SQLException thrown during rollback.",ex1);
				}
				// Re-throw DAOException
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
		} // END if (workOrderId != null) 
		log.trace("[END]");
		return workOrderList;
	}
	protected List<WorkOrderListElement> searchByWorkOrderId(
		String workOrderId, UserContext ctx, Connection con)
		throws DAOException {
		log.trace("[BEGIN]");
		List<WorkOrderListElement> workOrderList=null;
		PreparedStatement pStmt=null;
		ResultSet rs=null;
		StringBuilder query=null;
		WorkOrderListElementImpl listElement=null;

		if ((ctx != null) && (workOrderId != null)) {
			query=new StringBuilder(50);
			query.append(workOrderId);
			query.append('%');
			workOrderList=new ArrayList<WorkOrderListElement>();
			try {
				// Prepare the statement...
				pStmt=con.prepareStatement(QUERY_LIST_BY_WORK_ORDER_ID);
				pStmt.setString(1, query.toString());
				// Execute the statement
				rs=pStmt.executeQuery();
				// Process the resultSet...
				while (rs.next()) {
					// The resultSet contains a value.
					listElement=new WorkOrderListElementImpl();
					populateWorkOrderListElement(listElement,rs);
					workOrderList.add(listElement);
				}
				// Close the ResultSet
				rs.close();
				// Close the PreparedStatement
				pStmt.close();
			} catch (SQLException ex) {
				StringBuilder msg=new StringBuilder(128);
				msg.append("SQLException thrown while trying to create a ");
				msg.append("WorkOrderListElement object.");
				log.error(msg.toString(),ex);
				msg=null;
			}
		} else {
			// If the ctx is null
			if (ctx==null) {
				StringBuilder msg=new StringBuilder(128);
				msg.append("Null UserContext object. passed as a paramter.");
				log.error(msg.toString());
				msg=null;
			} // if (ctx==null)
			// If the work Order Id is null. 
			if (workOrderId==null) {
				StringBuilder msg=new StringBuilder(80);
				msg.append("Null workOrderId object passed.");
				log.error(msg.toString());
				msg=null;
			} // if (workOrderId==null) 
		} // if ((ctx != null) && (workOrderId != null)) 
		log.trace("[END]");
		return workOrderList;
	}
	//*************************************************************************
	//* END CRUD METHODS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN OTHER METHODS
	//*************************************************************************
	/**
	 * Populates the WorkOrderListElement with ResultSet data.
	 * 
	 * @param WorkOrderListElementImplThe WorkOrderListElement to be populated
	 * with data based upon the contents of the <code>ResultSet</code>
	 * 
	 * @param ResultSet The result of the SQL query used as the source of
	 * data for the <code>WorkOrderListElementImpl</code>.
	 */
	private static void populateWorkOrderListElement(
		WorkOrderListElementImpl listElement, ResultSet rs) 
	throws SQLException, DAOException {
		log.trace("[BEGIN]");
		listElement.setPoid(rs.getLong("POID"));
		listElement.setWorkOrderId(rs.getString("WORK_ORDER_ID"));		
		listElement.setWorkOrderType(rs.getString("WORKORDER_TYPE"));
		listElement.setConsigneeCity(rs.getString("CONSIGNEE_CITY"));
		listElement.setConsigneeName(rs.getString("CONSIGNEE_NAME"));
		listElement.setCustomerName(rs.getString("CUSTOMER_NAME"));
		// CUSTOMER_ORDER_ID
		try {
			listElement.setCustomerOrderId(rs.getString("CUSTOMER_ORDER_ID"));
		} catch (CustomerOrderIdTooLongException ex) {
			// This should never happen
			log.error("This shouldn't happen.",ex);
		}
		listElement.setStatus(rs.getInt("STATUS"));
		log.trace("[END]");
	}
	/**
	 * Helper method used to populate a WorkOrderOld object with data from a query.
	 * 
	 * @param WorkOrderImpl
	 * @see java.sql.ResultSet
	 * @see com.wildstartech.wfa.dao.user.justo.crm.dao.UserContext
	 * @see com.wildstartech.justo.crm.dao.sql.WorkOrderImpl
	 */
	private static void populateWorkOrder(WorkOrderImpl order, ResultSet rs, 
		UserContext ctx, Connection con) throws SQLException, DAOException {
		log.trace("[BEGIN]");
		
		int partyType=Integer.MIN_VALUE;
		long partyPoid=Long.MIN_VALUE;
		long tmpPoid=Long.MIN_VALUE;
		Company company=null;
		IdentifiableDocumentImpl tmpDocument=null;
		Party party=null;
		Person person=null;
		String tmpString=null;
		Timestamp tmpTimestamp=null;
		
		// WORK_ORDER_ID
		try {
			order.setWorkOrderId(rs.getString("WORK_ORDER_ID"));
		} catch (WorkOrderIdTooLongException ex) {
			log.error("This should never happen.",ex);
		}
		//***** AGENT
		partyPoid=rs.getLong("AGENT_POID");			// AGENT_POID
		partyType=rs.getInt("AGENT_TYPE");			// AGENT_TYPE
		switch(partyType) {
		case(Party.TYPE_COMPANY):
			// Locate the Agent if it is a Company
			company=companyDAO.findByPrimaryKey(partyPoid,ctx);
			party=new Party(company);
			break;
		case(Party.TYPE_PERSON):
			// Locate the Agent if it is a Person
			person=personDAO.findByPrimaryKey(partyPoid, ctx);
			party=new Party(person);
			break;
		}
		order.setAgent(party);
		//***** ARRIVAL_TIME
		tmpTimestamp=rs.getTimestamp("ARRIVAL_TIME");
		if (tmpTimestamp != null) {
			order.setArrivalTime(new Date(tmpTimestamp.getTime()));
		}		
		//***** ASSIGNED_TO_GROUP
		try {
			order.setAssignedToGroup(rs.getString("ASSIGNED_TO_GROUP"));
		} catch (AssignedToGroupNameTooLongException ex) {
			log.error("This should never happen.",ex);
		}
		//***** ASSIGNED_TO_INDIVIDUAL
		try {
			order.setAssignedToIndividual(
					rs.getString("ASSIGNED_TO_INDIVIDUAL"));
		} catch (AssignedToIndividualNameTooLongException ex) {
			log.error("This should never happen.",ex);
			
		}
		//***** BILL_OF_LADING
		tmpPoid=rs.getLong("BILL_OF_LADING");
		tmpDocument=(IdentifiableDocumentImpl) documentDAO.findByPrimaryKey(tmpPoid, ctx);
		order.setBillOfLading(tmpDocument);
		//***** BILLING_DATE
		order.setBillingDate(rs.getDate("BILLING_DATE"));
		//***** BILLING_REFERENCE_ID2
		try {
			order.setBillingReferenceId(rs.getString("BILLING_REFERENCE_ID2"));
		} catch (BillingReferenceIdTooLongException ex) {
			log.error("This should never happen.",ex);
		}
		//***** BILLING_CONTACT
		order.setBillingContactName(rs.getString("BILLING_CONTACT_NAME"));
		//***** BILLING_ADDRESS
		order.setBillingAddress(rs.getString("BILLING_ADDRESS"));
		//***** BILLING_ADDRESS2
		order.setBillingAddressSecondary(rs.getString("BILLING_ADDRESS2"));
		//***** BILLING_CITY
		order.setBillingCity(rs.getString("BILLING_CITY"));
		//***** BILLING_STATE
		tmpString=rs.getString("BILLING_STATE");
		if (tmpString != null) {
			order.setBillingState(StateImpl.getState(tmpString));
		} // END if (tmpString != null)
		//***** BILLING_ZIP
		order.setBillingZip(rs.getString("BILLING_ZIP"));
		//***** BILLING_PHONE
		order.setBillingPhone(rs.getString("BILLING_PHONE"));
		//***** BILLING_TERMS
		order.setBillingTerms(rs.getString("BILLING_TERMS"));
		//***** COMPLETE_TIME
		tmpTimestamp=rs.getTimestamp("COMPLETE_TIME");
		if (tmpTimestamp != null) {
			order.setCompleteTime(new Date(tmpTimestamp.getTime()));
		}
		// CONSIGNEE
		partyPoid=rs.getLong("CONSIGNEE_POID");		// CONSIGNEE_POID
		partyType=rs.getInt("CONSIGNEE_TYPE");		// CONSIGNEE_TYPE
		switch(partyType) {
		case(Party.TYPE_COMPANY):
			// Locate the Company if it is a Company
			company=companyDAO.findByPrimaryKey(partyPoid,ctx);
			party=new Party(company);
			break;
		case(Party.TYPE_PERSON):
			person=personDAO.findByPrimaryKey(partyPoid, ctx);
			party=new Party(person);
			break;
		}
		order.setConsignee(party);
		// CONSIGNEE_ADDRESS
		order.setConsigneeAddress(rs.getString("CONSIGNEE_ADDRESS"));
		// CONSIGNEE_ADDRESS2
		order.setConsigneeAddressSecondary(rs.getString("CONSIGNEE_ADDRESS2"));
		// CONSIGNEE_CITY
		order.setConsigneeCity(rs.getString("CONSIGNEE_CITY"));
		// CONSIGNEE_CONTACT
		order.setConsigneeContact(rs.getString("CONSIGNEE_CONTACT"));
		// CONSIGNEE_FAX
		order.setConsigneeFax(rs.getString("CONSIGNEE_FAX"));
		// CONSIGNEE_PHONE
		order.setConsigneePhone(rs.getString("CONSIGNEE_PHONE"));
		// CONSIGNEE_STATE
		tmpString=rs.getString("CONSIGNEE_STATE");
		if (tmpString != null) {
			order.setConsigneeState(StateImpl.getState(tmpString));
		} // END if (tmpString != null)
		// CONSIGNEE_ZIP
		order.setConsigneeZip(rs.getString("CONSIGNEE_ZIP"));
		//***** CUSTOMER
		party=order.getCustomer();					
		partyPoid=rs.getLong("CUSTOMER_POID");		// CUSTOMER_POID
		partyType=rs.getInt("CUSTOMER_TYPE");		// CUSTOMER_TYPE
		switch(partyType) {
		case(Party.TYPE_COMPANY):
			// Locate the Company if it is a Company
			company=companyDAO.findByPrimaryKey(partyPoid,ctx);
			party=new Party(company);			
			break;
		case(Party.TYPE_PERSON):
			person=personDAO.findByPrimaryKey(partyPoid,ctx);
			party=new Party(person);
			break;
		}
		order.setCustomer(party);
		// CUSTOMER_ORDER_ID
		try {
			order.setCustomerOrderId(rs.getString("CUSTOMER_ORDER_ID"));
		} catch (CustomerOrderIdTooLongException ex) {
			log.error("This should never happen.",ex);
		}
		// DELIVERY_DATE_ACTUAL
		tmpTimestamp=rs.getTimestamp("APPOINTMENT_TIME");
		if (tmpTimestamp != null) {
			order.setAppointmentTime(new Date(tmpTimestamp.getTime()));
		}
		//***** LOCATOR
		order.setLocatorId(rs.getString("LOCATOR"));
		//***** PICKUP_ADDRESS
		order.setPickupAddress(rs.getString("PICKUP_ADDRESS"));
		//***** PICKUP_ADDRESS2
		order.setPickupAddressSecondary(rs.getString("PICKUP_ADDRESS2"));
		//***** PICKUP_CITY
		order.setPickupCity(rs.getString("PICKUP_CITY"));
		//***** PICKUP_STATE
		tmpString=rs.getString("PICKUP_STATE");
		if (tmpString != null) {
			order.setPickupState(StateImpl.getState(tmpString));
		} // END if (tmpString != null)
		//***** PICKUP_ZIP
		order.setPickupZip(rs.getString("PICKUP_ZIP"));
		//***** PICKUP_PHONE
		order.setPickupPhone(rs.getString("PICKUP_PHONE"));
		//***** PROOF_OF_DELIVERY
		tmpPoid=rs.getLong("PROOF_OF_DELIVERY");
		tmpDocument=(IdentifiableDocumentImpl) documentDAO.findByPrimaryKey(tmpPoid, ctx);
		order.setProofOfDelivery(tmpDocument);
		//***** SHIPPING_METHOD
		order.setShippingMethod(rs.getString("SHIPPING_METHOD"));
		//***** STATUS
		order.setStatus(rs.getInt("STATUS"));
		//***** START_TIME
		tmpTimestamp=rs.getTimestamp("START_TIME");
		if (tmpTimestamp != null) {
			order.setStartTime(new Date(tmpTimestamp.getTime()));
		}
		//***** WORKORDER_TYPE
		order.setType(rs.getInt("WORKORDER_TYPE"));
		//********** Locate/load LineItem objects for the WorkOrderOld
		lineItemDAO.findAll(order,ctx,con);	
		//********** Locate/load Charge objects for the work order
		chargeDAO.findAll(order,ctx,con);
		log.trace("[END]");
	}
	//********** partyPoid
	private static long getPartyPoid(Party party) {
		log.trace("[BEGIN]");
		long poid=Long.MIN_VALUE;
		
		if (party != null) {
			// The party is not null
			switch(party.getType()) {
			case (Party.TYPE_COMPANY):
				CompanyImpl company=(CompanyImpl)party.getParty();
				if (company != null) {
					poid=company.getPoid();
				} else {
					poid=Long.MIN_VALUE;
				} // END if (company != null)
				break;
			case (Party.TYPE_PERSON):
				PersonImpl person=(PersonImpl)party.getParty();
				if (person != null) {
					poid=person.getPoid();
				} else {
					poid=Long.MIN_VALUE;
				} // END if (person != null)	
				break;
			default:
				poid=Long.MIN_VALUE;
				break;
			}
		} // END if (party != null) 
		log.trace("[END]");
		return poid;
	}
	/**
	 * Save the specified party object.
	 */
	private static void saveParty(Party party, UserContext ctx, Connection con) 
	throws DAOException {
		log.trace("[BEGIN]");
		long poid=Long.MIN_VALUE;
		
		if ((party != null) && (party.isAvailable())) {
			switch(party.getType()) {
			case (Party.TYPE_COMPANY):
				CompanyImpl company=(CompanyImpl)party.getParty();
				poid=company.getPoid();
				if (poid==Long.MIN_VALUE) {
					companyDAO.save(company,ctx,con);
				}
				break;
			case (Party.TYPE_PERSON):
				PersonImpl person=(PersonImpl)party.getParty();
				poid=person.getPoid();
				if (poid==Long.MIN_VALUE) {
					personDAO.save(person,ctx,con);
				} // END if (poid==Long.MIN_VALUE)
				
				break;
			default:
				poid=Long.MIN_VALUE;
				break;
			} // END switch(party.getType)
		} // END if ((party != null) && (party.isAvailable()))
		log.trace("[END]");
	}
	/**
	 * Returns a reference to the WorkOrderDAO.
	 * 
	 * @return WorkOrderDAO
	 * @see com.wildstartech.justo.crm.dao.WorkOrderDAO
	 */
	public static WorkOrderDAO getInstance() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return workOrderDAO;
	}
	/**
	 * Returns a reference to teh DAO_IDENTIFIER_KEY
	 * @return java.lang.String
	 */
	protected String getDAOIdentifierKey() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return DAO_IDENTIFIER_KEY;
	}
	//*************************************************************************
	//* END OTHER METHODS
	//*************************************************************************
}