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

import com.wildstartech.wfa.dao.impl.WildObjectSQL;
import com.wildstartech.wfa.journal.impl.JournalEntryImpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wildstartech.wfa.assignment.AssignedToGroupNameTooLongException;
import com.wildstartech.wfa.assignment.AssignedToIndividualNameTooLongException;
import com.wildstartech.wfa.company.Company;
import com.wildstartech.wfa.document.Document;
import com.wildstartech.wfa.document.IdentifiableDocument;
import com.wildstartech.wfa.journal.JournalEntry;
import com.wildstartech.wfa.Party;
import com.wildstartech.wfa.person.Person;
import com.wildstartech.wfa.resources.logistics.ltl.WorkOrderLineItem;
import com.wildstartech.wfa.dao.DAOException;
import com.wildstartech.wfa.document.impl.IdentifiableDocumentImpl;
import com.wildstartech.wfa.journal.impl.JournalDAOImpl;
import com.wildstartech.wfa.location.address.State;
import com.wildstartech.wfa.location.address.us.StateImpl;
import com.wildstartech.wfa.logistics.ltl.AccessorialCharge;
import com.wildstartech.wfa.logistics.ltl.CustomerOrderIdTooLongException;
import com.wildstartech.wfa.logistics.ltl.WorkOrderOld;
import com.wildstartech.wfa.logistics.ltl.billoflading.BillingReferenceIdTooLongException;
import com.wildstartech.wfa.logistics.ltl.workorder.WorkOrderIdTooLongException;

public class WorkOrderImpl extends WildObjectSQL implements WorkOrderOld {
	//*************************************************************************
	//* BEGIN STATIC FIELD DECLARATIONS
	//*************************************************************************
	public static final int MAX_LENGTH_ASSIGNED_TO_GROUP=255;
	public static final int MAX_LENGTH_ASSIGNED_TO_INDIVIDUAL=255;
	public static final int MAX_LENGTH_BILLING_REFERENCE_ID=10;
	public static final int MAX_LENGTH_BILLING_TERMS=255;
	public static final int MAX_LENGTH_ADDRESS=255;
	public static final int MAX_LENGTH_CITY=50;
	public static final int MAX_LENGTH_ZIP=10;
	public static final int MAX_LENGTH_PHONE=21;
	public static final int MAX_LENGTH_CONSIGNEE_CONTACT=255;
	public static final int MAX_LENGTH_CONSIGNEE_NAME=255;
	public static final int MAX_LENGTH_CUSTOMER_ORDER_ID=30;
	public static final int MAX_LENGTH_SHIPPING_METHOD=255;
	public static final int MAX_LENGTH_WORK_ORDER_ID=50;
	private static JournalDAOImpl journalDAO=
		(JournalDAOImpl) JournalDAOImpl.getInstance();
	private static Log log=LogFactory.getLog(WorkOrderOld.class);
	private static String[] statusList=buildStatusList();
	//*************************************************************************
	//* END STATIC FIELD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN INSTANCE FIELD DECLARATIONS
	//*************************************************************************
	private int status;
	private int type;
	private Date appointmentTime;
	private Date arrivalTime;
	private Date billingDate;
	private Date completeTime;
	private Date startTime;
	private IdentifiableDocumentImpl billOfLading;
	private IdentifiableDocumentImpl proofOfDelivery;
	private JournalEntry journalEntry;
	private List<JournalEntry> journalEntries;
	private List<AccessorialCharge> accessorialCharges;
	private List<AccessorialCharge> accessorialChargesToDelete;
	private List<WorkOrderLineItem> lineItems;
	private List<WorkOrderLineItem> lineItemsToDelete;
	private Party agent;
	private Party consignee;
	private Party customer;
	private State billingState;
	private State consigneeState;
	private State pickupState;
	private String assignedToGroup;
	private String assignedToIndividual;
	private String billingAddress;
	private String billingAddressSecondary;
	private String billingCity;
	private String billingContactName;
	private String billingPhone;
	private String billingReferenceId;
	private String billingTerms;
	private String billingZip;
	private String consigneeAddress;
	private String consigneeAddressSecondary;
	private String consigneeCity;
	private String consigneeContact;
	private String consigneeFax;
	private String consigneePhone;
	private String consigneeZip;
	private String customerOrderId;
	private String locatorId;
	private String pickupAddress;
	private String pickupAddressSecondary;
	private String pickupCity;
	private String pickupPhone;
	private String pickupZip;
	private String shippingMethod;
	private String workOrderId;

    public WorkOrderImpl() {
		super();
		log.trace("[BEGIN]");
		this.accessorialCharges=new ArrayList<AccessorialCharge>();
		this.appointmentTime=null;
		this.arrivalTime=null;
		this.startTime=null;
		this.completeTime=null;
		this.agent=new Party();
		this.consignee=new Party();
		this.customer=new Party();
		this.lineItems=new ArrayList<WorkOrderLineItem>();
		this.locatorId=null;
		this.journalEntry=null;
		this.status=0;
		this.type=TYPE_DELIVERY;
		this.consigneeState=StateImpl.getState("CA");
		log.trace("[END]");
	}
	/**
	 * Build a list of String values for the 'Status' field.
	 * 
	 * @param String[] A String array
	 */
	private static String[] buildStatusList() {
		String[] statusList=new String[9];
		statusList[0]="Offered";
		statusList[1]="Received In";
		statusList[2]="Pending";
		statusList[3]="Scheduled";
		statusList[4]="Delivered";
		statusList[5]="Audit Required";
		statusList[6]="Audited";
		statusList[7]="Billed";
		statusList[8]="Cancelled";
		return statusList;
	}
	/**
	 * Return the value of the 'Status' field as a String.
	 * 
	 * @param int The integer value representing the Status of the work order.
	 * @return String Return the string equivalent of the status.
	 */
	public static String getStatusAsString(int status) {
		log.trace("[BEGIN]");
		String statusString=null;
		
		if(status <0 ) {
			statusString=statusList[0];
		} else if (status > statusList.length){
			statusString=statusList[statusList.length-1];
		} else {
			statusString=statusList[status];
		}
		log.trace("[END]");
		return statusString;
	}
	//********** accessorialCharge
	public AccessorialCharge createAccessorialCharge() {
		log.trace("[BEGIN]");
		AccessorialChargeImpl accessorialCharge=new AccessorialChargeImpl(this);
		this.accessorialCharges.add(accessorialCharge);
		log.trace("[END]");
		return accessorialCharge;
	}
	public List<AccessorialCharge> getAccessorialCharges() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return this.accessorialCharges;
	}
	public List<AccessorialCharge> getAccessorialChargesToDelete() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return this.accessorialChargesToDelete;
	}
	public AccessorialCharge removeAccessorialCharge(AccessorialCharge charge) {
		log.trace("[BEGIN]");
		if (this.accessorialCharges.contains(charge)) {
			// The list of accessorialCharges contains the specified charge
			if (this.accessorialChargesToDelete == null) {
				// The list of accessorial charges to delete is not
				// active.
				this.accessorialChargesToDelete=
					new ArrayList<AccessorialCharge>();
			} // END if (this.accessorialChargesToDelete == null)
			this.accessorialCharges.remove(charge);
			this.accessorialChargesToDelete.add(charge);
		} else {
			charge=null;
		} // END if (this.accessorialCharges.contains(charge))
		log.trace("[END]");
		return charge;
	}
	//********** agent
	public Party getAgent() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return agent;
	}
	public void setAgent(Party agent) {
		log.trace("[BEGIN]");
		this.agent=agent;
		log.trace("[END]");
	}
	public void setAgent(Company company) {
		log.trace("[BEGIN]");
		agent.setParty(company);	
		log.trace("[END]");
	}
	public void setAgent(Person person) {
		log.trace("[BEGIN]");
		agent.setParty(person);
		log.trace("[END]");
	}
	//********** appointmentTime
	public Date getAppointmentTime() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return this.appointmentTime;
	}
	public void setAppointmentTime(Date date) {
		log.trace("[BEGIN]");
		this.appointmentTime=date;
		log.trace("[END]");
	}
	//********** arrivalTime
	public Date getArrivalTime() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return this.arrivalTime;
	}
	public void setArrivalTime(Date date) {
		log.trace("[BEGIN]");
		this.arrivalTime=date;
		log.trace("[END]");
	}
	//********** assignedToGroup
	public String getAssignedToGroup() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return assignedToGroup;
	}
	public void setAssignedToGroup(String group) 
	throws AssignedToGroupNameTooLongException {
		log.trace("[BEGIN]");
		if(group != null) {
			if(group.length() > MAX_LENGTH_ASSIGNED_TO_GROUP) {
				throw new AssignedToGroupNameTooLongException(group,
						MAX_LENGTH_ASSIGNED_TO_GROUP);
			} else {
				this.assignedToGroup=group;
			} // END if(group.length() > WorkOrderOld.MAX_LENGTH_ASSIGNED_TO_GROUP)
		} else {
			group=null;
		} // END if(group!=null)
		log.trace("[END]");
	}
	//********** assignedToIndividual
	public String getAssignedToIndividual() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return assignedToIndividual;
	}
	public void setAssignedToIndividual(String individual) 
	throws AssignedToIndividualNameTooLongException {
		log.trace("[BEGIN]");
		if(individual != null) {
			if(individual.length() > MAX_LENGTH_ASSIGNED_TO_INDIVIDUAL) {
				throw new AssignedToIndividualNameTooLongException(individual,
						MAX_LENGTH_ASSIGNED_TO_INDIVIDUAL);
			} else {
				this.assignedToIndividual=individual;
			} // END if(individual.length() > WorkOrderOld.MAX_LENGTH_ASSIGNED_TO_INDIVIDUAL)
		} else {
			assignedToIndividual=null;
		} // END if(group!=null)
		log.trace("[END]");
	}
	//********** billOfLading
	public IdentifiableDocumentImpl getBillOfLading() {
		if (log.isTraceEnabled()) {
			
		} // END if (log.isTraceEnabled()) 
		return this.billOfLading;
	}
	public void setBillOfLading(IdentifiableDocument billOfLading) {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Current Document: "+this.billOfLading);
		} // END if (log.isTraceEnabled())
		// Store the current bill of lading
		this.billOfLading=(IdentifiableDocumentImpl) billOfLading;
		if (log.isTraceEnabled()) {
			log.trace("New Document: "+this.billOfLading);
			log.trace("[END]");
		} // END if (log.isTraceEnabled()) 
	}
	//********** billingAddress
	public String getBillingAddress() {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Current Billing Address: "+this.billingAddress);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
		return this.billingAddress;
	}
	public void setBillingAddress(String address) {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Current Billing Address: "+this.billingAddress);
		} // END if (log.isTraceEnabled())
		if (address != null) {
			if (address.length() > MAX_LENGTH_ADDRESS) {
				StringBuilder msg=new StringBuilder(128);
				msg.append("Billing address value, \"");
				msg.append(address);
				msg.append("\", too long.\n");
				log.error(msg.toString());
				msg=null;
			} else {
				this.billingAddress=address.toUpperCase();
			} // END if (address.length() > MAX_LENGTH_ADDRESS)
		} else {
			this.billingAddress="";
		} // END if (address != null)
		if (log.isTraceEnabled()) {
			log.trace("New Billing Address: "+this.billingAddress);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
	}
	//********** billingAddressSecondary
	public String getBillingAddressSecondary() {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Current Billing Address Secondary: "+
					this.billingAddressSecondary);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
		return this.billingAddressSecondary;
	}
	public void setBillingAddressSecondary(String address) {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Current Billing Address Secondary: "+
					this.billingAddressSecondary);
		} // END if (log.isTraceEnabled())
		if (address != null) {
			if (address.length() > MAX_LENGTH_ADDRESS) {
				StringBuilder msg=new StringBuilder(128);
				msg.append("Billing address secondary value, \"");
				msg.append(address);
				msg.append("\", too long.\n");
				log.error(msg.toString());
				msg=null;
			} else {
				this.billingAddressSecondary=address.toUpperCase();
			} // END if (address.length() > MAX_LENGTH_ADDRESS)
		} else {
			this.billingAddressSecondary="";
		} // END if (address != null)
		if (log.isTraceEnabled()) {
			log.trace("New Billing Address Secondary: "+
					this.billingAddressSecondary);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
	}
	//********** billingCity
	public String getBillingCity() {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Current Billing City: "+this.billingCity);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
		return this.billingCity;
	}
	public void setBillingCity(String city) {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Current Billing City: "+this.billingCity);
		} // END if (log.isTraceEnabled())
		if(city!=null) {
			if(city.length() > MAX_LENGTH_CITY) {
				StringBuilder msg=new StringBuilder(128);
				msg.append("Billing City value too long: ");
				msg.append(city);
				log.error(msg.toString());
				msg=null;
			} else {
				this.billingCity=city.toUpperCase();
			} // END if (city.length() > MAX_LENGTH_CITY)
		} else {
			this.billingCity="";
		} // END if(city!=null)
		if (log.isTraceEnabled()) {
			log.trace("New Billing City: "+this.billingCity);
			log.trace("[END]");
		} // END  if (log.isTraceEnabled())
	}
	//********** billingContactName
	public String getBillingContactName() {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Current Billing Contact Name: "+this.billingContactName);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
		return this.billingContactName;
	}
	public void setBillingContactName(String name) {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Current Billing Contact Name: "+this.billingContactName);
		} // END if (log.isTraceEnabled())
		this.billingContactName=name;
		if (log.isTraceEnabled()) {
			log.trace("New Billing Contact Name: "+this.billingContactName);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
	}
	//********** billingDate
	public Date getBillingDate() {
		log.trace("[BEGIN]");
		if (log.isTraceEnabled()) {
			log.trace("billingDate: "+this.billingDate);
		} // END if (log.isTraceEnabled())
		log.trace("[END]");
		return this.billingDate;
	}
	public void setBillingDate(Date billingDate) {
		log.trace("[BEGIN]");
		if (log.isTraceEnabled()) {
			log.trace("Current billingDate: "+this.billingDate);
			log.trace("New billingDate: "+billingDate);
		} // END if (log.isTraceEnabled())
		this.billingDate=billingDate;
		log.trace("[END]");
	}
	//********** billingPhone
	public String getBillingPhone() {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Current Billing Phone: "+this.billingPhone);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
		return this.billingPhone;
	}
	public void setBillingPhone(String phone) {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Current Billing Phone: "+this.billingPhone);
		} // END if (log.isTraceEnabled())
		if (phone != null) {
			if (phone.length() > MAX_LENGTH_PHONE) {
				this.billingPhone=
						phone.substring(MAX_LENGTH_PHONE);
			} else {
					this.billingPhone=phone;
			} // END if (phone.length() > MAX_LENGTH_PHONE)
		} else {
			this.billingPhone="";
		} // END if (phone != null)
		if (log.isTraceEnabled()) {
			log.trace("New Billing Phone: "+this.billingPhone);
			log.trace("[BEGIN]");
		} // END if (log.isTraceEnabled())
	}
	//********** billingReferenceId
	public String getBillingReferenceId() {
		log.trace("[BEGIN]");
		if (log.isTraceEnabled()) {
			log.trace("billingReferenceId: "+this.billingReferenceId);
		} // END if (log.isTraceEnabled())
		log.trace("[END]");
		return this.billingReferenceId;		
	}
	public void setBillingReferenceId(String billingReferenceId) 
	throws BillingReferenceIdTooLongException {
		log.trace("[BEGIN]");
		if (log.isTraceEnabled()) {
			log.trace("Current billingReferenceId: "+this.billingReferenceId);
			log.trace("New billingReferenceId: "+billingReferenceId);
		} // END if (log.isTraceEnabled())
		if (billingReferenceId != null) {
			if (billingReferenceId.length() <= MAX_LENGTH_BILLING_REFERENCE_ID) {
				this.billingReferenceId=billingReferenceId;
			} else {
				// The specified value is too long...
				BillingReferenceIdTooLongException be=
					new BillingReferenceIdTooLongException(billingReferenceId,
							MAX_LENGTH_BILLING_REFERENCE_ID);
				throw be;
			} // END if (billingReferenceId.length() < MAX_LENGTH_BILLING...
			this.billingReferenceId=billingReferenceId;
		} else {
			this.billingReferenceId="";
		} // END if (billingReferenceId != null) 
		log.trace("[END]");		
	}
	//********** billingState
	public State getBillingState() {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Current Billing State: "+this.billingState);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
		return this.billingState;
	}
	public void setBillingState(State state) {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Current Billing State: "+this.billingState);
		} // END if (log.isTraceEnabled())
		this.billingState=state;
		if (log.isTraceEnabled()) {
			log.trace("New Billing State: "+this.billingState);
			log.trace("[BEGIN]");
		} // END if (log.isTraceEnabled())
	}
	//********** billingTerms
	public String getBillingTerms() {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Current Billing Terms: "+this.billingTerms);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
		return this.billingTerms;
	}
	public void setBillingTerms(String billingTerms) {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Current Billing Terms: "+this.billingTerms);
		} // END if (log.isTraceEnabled())
		if (billingTerms!= null) {
			if (billingTerms.length() > MAX_LENGTH_BILLING_TERMS) {
				this.billingTerms=
					billingTerms.substring(0,MAX_LENGTH_BILLING_TERMS);
			} else {
				this.billingTerms=billingTerms;
			}
		} else {
			this.billingTerms="";
		} // END if (billingTerms != null)
		 
		if (log.isTraceEnabled()) {
			log.trace("New Billing Terms: "+this.billingTerms);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
	}
	//********** billingZip
	public String getBillingZip() {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Current Billing Zip: "+this.billingZip);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
		return this.billingZip;
	}
	public void setBillingZip(String zip) {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Current Billing Zip: "+this.billingZip);
		} // END if (log.isTraceEnabled())
		if (zip != null) {
			if(zip.length() > MAX_LENGTH_ZIP) {
				StringBuilder msg=new StringBuilder(128);
				msg.append("Billing zip code value too long: ");
				msg.append(zip);
				log.error(msg.toString());
				msg=null;
			} else {
				this.billingZip=zip;
			}
		} else {
			this.billingZip = "";
		} // END if(zip != null)
		if (log.isTraceEnabled()) {
			log.trace("New Billing Zip: "+this.billingZip);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
	}
	//********** completeTime
	public Date getCompleteTime() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return this.completeTime;
	}
	public void setCompleteTime(Date date) {
		log.trace("[BEGIN]");
		this.completeTime=date;
		log.trace("[END]");
	}
	//********** consginee
	public Party getConsignee() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return consignee;
	}
	public void setConsignee(Party consignee) {
		log.trace("[BEGIN]");
		this.consignee=consignee;
		log.trace("[END]");
	}
	public void setConsignee(Company company) {
		log.trace("[BEGIN]");
		consignee.setParty(company);	
		log.trace("[END]");
	}
	public void setConsignee(Person person) {
		log.trace("[BEGIN]");
		consignee.setParty(person);
		log.trace("[END]");
	}
	//********** consigneeAddress
	public String getConsigneeAddress() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return consigneeAddress;
	}
	public void setConsigneeAddress(String addr) {
		log.trace("[BEGIN]");
		if (addr != null) {
			if (addr.length() > MAX_LENGTH_ADDRESS) {
				StringBuilder msg=new StringBuilder(128);
				msg.append("WorkOrderImpl.setConsigneeAddress() value too ");
				msg.append("long.\n");
				msg.append(addr);
				log.error(msg.toString());
				msg=null;
			} else {
				consigneeAddress=addr.toUpperCase();
			}
		} else {
			consigneeAddress="";
		}
		log.trace("[END]");
	}
	//********** consigneeAddressSecondary
	public String getConsigneeAddressSecondary() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return consigneeAddressSecondary;
	}
	public void setConsigneeAddressSecondary(String addr) {
		log.trace("[BEGIN]");
		if (addr != null) {
			if(addr.length() > MAX_LENGTH_ADDRESS) {
				StringBuilder msg=new StringBuilder(128);
				msg.append("WorkOrderImpl.setConsigneeAddressSecondary() ");
				msg.append("value too long.\n");
				msg.append(addr);
				log.error(msg.toString());
				msg=null;
			} else {
				consigneeAddressSecondary=addr.toUpperCase();
			}
		} else {
			consigneeAddressSecondary="";
		}
		log.trace("[END]");
	}
	//********** consigneeCity
	public String getConsigneeCity() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return consigneeCity;
	}
	public void setConsigneeCity(String city) {
		log.trace("[BEGIN]");
		if(city!=null) {
			if(city.length() > MAX_LENGTH_CITY) {
				StringBuilder msg=new StringBuilder(128);
				msg.append("Consignee City value too long: ");
				msg.append(city);
				log.error(msg.toString());
				msg=null;
			} else {
				consigneeCity=city.toUpperCase();
			}
		} else {
			
		} // END if(city!=null)
		log.trace("[END]");
	}
	//********** consigneeContact
	public String getConsigneeContact() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return consigneeContact;
	}
	public void setConsigneeContact(String name) {
		log.trace("[BEGIN]");
		if(name != null) {
			if(name.length() > MAX_LENGTH_CONSIGNEE_CONTACT) {
				// Handle the fact that the contact is too long
				StringBuilder msg=new StringBuilder(128);
				msg.append("WorkOrderImpl.setConsigneeContact() value too ");
				msg.append("long.\n");
				msg.append(name);
				log.error(msg.toString());
				msg=null;
			} else {
				consigneeContact=name.toUpperCase();
			} // END if(name.length() > MAX_CONSIGNEE_CONTACT_LENGTH
		} else {
			consigneeContact="";
		}
		log.trace("[END]");
	}	
	//********** consigneeFax
	public String getConsigneeFax() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return consigneeFax;
	}
	public void setConsigneeFax(String consigneeFax) {
		log.trace("[BEGIN]");
		if(consigneeFax != null) {
			if(consigneeFax.length() > MAX_LENGTH_PHONE) {
				this.consigneeFax=consigneeFax.substring(21);
			} else {
				this.consigneeFax=consigneeFax;
			}
		} else {
			this.consigneeFax=null;
		} // END if(consigneeFax != null)
		log.trace("[END]");
	}	
	//********** consigneePhone
	public String getConsigneePhone() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return consigneePhone;
	}
	public void setConsigneePhone(String consigneePhone) {
		log.trace("[BEGIN]");
		if (consigneePhone != null) {
			if (consigneePhone.length() > MAX_LENGTH_PHONE) {
				this.consigneePhone=
					consigneePhone.substring(MAX_LENGTH_PHONE);
			} else {
				this.consigneePhone=consigneePhone;
			}
		} else {
			consigneePhone=null;
		} // END if(consigneePhone != null)
		log.trace("[END]");
	}	
	//********** consigneeState
	public State getConsigneeState() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return consigneeState;
	}
	public void setConsigneeState(State state) {
		log.trace("[BEGIN]");
		consigneeState=state;	
		log.trace("[END]");
	}
	//********** consigneeZip
	public String getConsigneeZip() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return consigneeZip;
	}
	public void setConsigneeZip(String zip) {
		log.trace("[BEGIN]");
		if (zip != null) {
			if(zip.length() > MAX_LENGTH_ZIP) {
				StringBuilder msg=new StringBuilder(128);
				msg.append("Zip Code value too long: ");
				msg.append(zip);
				log.error(msg.toString());
				msg=null;
			} else {
				consigneeZip=zip;
			}
		} else {
			zip = "";
		} // END if(zip != null)
		log.trace("[END]");		
	}
	//********** customer
	public Party getCustomer() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return customer;
	}
	public void setCustomer(Party customer) {
		log.trace("[BEGIN]");
		this.customer=customer;
		log.trace("[END]");
	}
	public void setCustomer(Company company) {
		log.trace("[BEGIN]");
		this.customer.setParty(company);
		log.trace("[END]");
	}
	public void setCustomer(Person person) {
		log.trace("[BEGIN]");
		this.customer.setParty(person);
		log.trace("[END]");
	}
	//********** customerOrderId
	public String getCustomerOrderId() {
		return customerOrderId;
	}
	public void setCustomerOrderId(String orderId) 
	throws CustomerOrderIdTooLongException {
		log.trace("[BEGIN]");
		if (orderId.length() > MAX_LENGTH_CUSTOMER_ORDER_ID) {
			throw new CustomerOrderIdTooLongException(orderId,
				MAX_LENGTH_CUSTOMER_ORDER_ID);
		} else {
			customerOrderId=orderId;
		}
		log.trace("[END]");
	}
	//********** journals
	public JournalEntry createJournalEntry() {
		log.trace("[BEGIN]");
		if (this.journalEntry==null) {
			this.journalEntry=
				new JournalEntryImpl(WorkOrderDAOImpl.DAO_IDENTIFIER_KEY, 
									 getPoid());
		}
		log.trace("[END]");
		return this.journalEntry;
	}
	protected JournalEntry getJournalEntry() {
		log.trace("[BEGIN]");	
		log.trace("[END]");
		return this.journalEntry;
	}
	public List<JournalEntry> getJournalEntries() {
		log.trace("[BEGIN]");
		if (this.journalEntries==null) {
			// Some lazy instantiation here.  Only look up the journal 
			// entries if they're needed...
			if (getPoid() > Long.MIN_VALUE) {
				try {
					this.journalEntries=
						journalDAO.findAll(WorkOrderDAOImpl.DAO_IDENTIFIER_KEY, 
							getPoid(),null);
				} catch (DAOException ex) {
					log.error("DAOException thrown.",ex);
				}
			}
		} // END if (this.journalEntries==null)
		log.trace("[END]");
		return this.journalEntries;
	}
	//********** locatorId
	public String getLocatorId() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return this.locatorId;
	}
	public void setLocatorId(String locatorId) {
		log.trace("[BEGIN])");
		if (locatorId != null) {
			this.locatorId=locatorId;
		} else {
			this.locatorId="";
		} // END if (locatorId != null) 
		log.trace("[END]");
	}
	//********** pickupAddress
	public String getPickupAddress() {
		if(log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Pickup Address: "+this.pickupAddress);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
		return this.pickupAddress;
	}
	public void setPickupAddress(String address) {
		if(log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Current Value of pickupAddress:"+this.pickupAddress);
		} // END if(log.isTraceEnabled())
		if (address != null) {
			if (address.length() > MAX_LENGTH_ADDRESS) {
				// The specified address is longer than the max length
				this.pickupAddress=address.substring(0,MAX_LENGTH_ADDRESS);
			} else {
				// The specified address is shorter than the max length
				this.pickupAddress=address;
			} // END if (address.length() > MAX_LENGTH_ADDRESS)
		} else {
			// The specified address is null, so store an empty string
			this.pickupAddress="";
		} // END if (city != null)
		if(log.isTraceEnabled()) {
			log.trace("New Value of pickupAddress: "+this.pickupAddress);
			log.trace("[END]");
		} // END if(log.isTraceEnabled())
	}
	//********** pickupAddressSecondary
	public String getPickupAddressSecondary() {
		if(log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Pickup Address Secondary: "+this.pickupAddressSecondary);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
		return this.pickupAddressSecondary;
	}
	public void setPickupAddressSecondary(String address) {
		if(log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Current Value of pickupAddressSecondary:"+
					this.pickupAddressSecondary);
		} // END if(log.isTraceEnabled())
		if (address != null) {
			if (address.length() > MAX_LENGTH_ADDRESS) {
				// The specified address is longer than the max length
				this.pickupAddressSecondary=
					address.substring(0,MAX_LENGTH_ADDRESS);
			} else {
				// The specified address is shorter than the max length
				this.pickupAddressSecondary=address;
			} // END if (address.length() > MAX_LENGTH_ADDRESS)
		} else {
			// The specified address is null, so store an empty string
			this.pickupAddressSecondary="";
		} // END if (city != null)
		if(log.isTraceEnabled()) {
			log.trace("New Value of pickupAddressSecondary:"+
					this.pickupAddressSecondary);
			log.trace("[END]");
		} // END if(log.isTraceEnabled())
	}
	//********** pickupCity
	public String getPickupCity() {
		if(log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Pickup City: "+this.pickupCity);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())	
		return this.pickupCity;
	}
	public void setPickupCity(String city) {
		if(log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Current Value of pickupCity:"+this.pickupCity);
		} // END if(log.isTraceEnabled())
		if (city != null) {
			if (city.length() > MAX_LENGTH_CITY) {
				// The specified city parameter is longer than the max length
				this.pickupCity=city.substring(0,MAX_LENGTH_CITY);
			} else {
				// The specified city parameter is shorter than the max length
				this.pickupCity=city;
			} // END if (city.length() > MAX_LENGTH_CITY)
		} else {
			// The specified city parameter is null, so store an empty string
			this.pickupCity="";
		} // END if (city != null)
		if(log.isTraceEnabled()) {
			log.trace("New Value of pickupCity:"+this.pickupCity);
			log.trace("[END]");
		} // END if(log.isTraceEnabled())
	}
	//********** pickupPhone
	public String getPickupPhone() {
		if(log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("pickupPhone: "+this.pickupPhone);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
		return this.pickupPhone;
	}
	public void setPickupPhone(String phone) {
		if(log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Current Value of pickupPhone:"+this.pickupPhone);
		} // END if(log.isTraceEnabled())
		if (phone != null) {
			if (phone.length() > MAX_LENGTH_PHONE) {
				this.pickupPhone=phone.substring(0,MAX_LENGTH_PHONE);
			} else {
				// The specified phone number is long enough.
				this.pickupPhone=phone;
			} // END if (phone.length() > MAX_LENGTH_PHONE)
		} else {
			// The specified phone number is null
			this.pickupPhone="";
		} // END if (phone != null)
		if(log.isTraceEnabled()) {
			log.trace("New Value of pickupPhone:"+this.pickupPhone);
			log.trace("[END]");
		} // END if(log.isTraceEnabled())
	}
	//********** pickupState
	public State getPickupState() {
		if(log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("pickupState: "+this.pickupState);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
		return this.pickupState;
	}
	public void setPickupState(State state) {
		if(log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Current Value of pickupState:"+this.pickupState);
		} // END if(log.isTraceEnabled())
		this.pickupState=state;
		if(log.isTraceEnabled()) {
			log.trace("New Value of :"+"");
			log.trace("[END]");
		} // END if(log.isTraceEnabled())
	}
	//********** pickupZip
	public String getPickupZip() {
		if(log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Pickup Zip: "+this.pickupZip);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
		return this.pickupZip;
	}
	public void setPickupZip(String zip) {
		if(log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Current Value of pickupZip:"+this.pickupZip);
		} // END if(log.isTraceEnabled())
		if (zip != null) {
			if (zip.length() > MAX_LENGTH_ZIP) {
				this.pickupZip=zip.substring(0,MAX_LENGTH_ZIP);
			} else {
				this.pickupZip=zip;
			} // END if (zip.length() > MAX_LENGTH_ZIP)
		} else {
			this.pickupZip="";
		} // END if (zip != null)
		if(log.isTraceEnabled()) {
			log.trace("New Value of pickupZip:"+this.pickupZip);
			log.trace("[END]");
		} // END if(log.isTraceEnabled())
	}
	//********** proofOfDelivery
	public IdentifiableDocument getProofOfDelivery() {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace(this.proofOfDelivery);
		} // END if (log.isTraceEnabled())
		log.trace("[END]");
		return this.proofOfDelivery;
	}
	public void setProofOfDelivery(IdentifiableDocument doc) {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Current Document: "+this.proofOfDelivery);
		} // END if (log.isTraceEnabled())
		this.proofOfDelivery=(IdentifiableDocumentImpl) doc;
		if (log.isTraceEnabled()) {
			log.trace("New Document: "+this.proofOfDelivery);
			log.trace("[END]");
		} // END if (log.isTraceEnabled()) 
	}
	//********** shippingMethod
	public String getShippingMethod() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return this.shippingMethod;
	}
	public void setShippingMethod(String method) {
		log.trace("[BEGIN]");
		if(method!=null) {
			if(method.length() > MAX_LENGTH_SHIPPING_METHOD) {
				// Handle specified method too long
				StringBuilder msg=new StringBuilder();
				msg.append("Specified method too long:\n");
				msg.append(method);
				log.error(msg.toString());
				msg=null;
			} else {
				 this.shippingMethod=method.toUpperCase();
			}
		} else {
			 this.shippingMethod="";
		} // END if(method==null)
		log.trace("[END]");
	}
	//********** startTime
	public Date getStartTime() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return this.startTime;
	}

	public void setStartTime(Date date) {
		log.trace("[BEGIN]");
		this.startTime=date;
		log.trace("[END]");
	}	
	//********* status
	public int getStatus() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return  this.status;
	}
	public void setStatus(int status) {
		log.trace("[BEGIN]");
		log.trace("[END]");
		this.status=status;	
	}
	//********* type
	public int getType() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return  this.type;
	}
	public void setType(int type) {
		log.trace("[BEGIN]");	
		if (type==TYPE_PICKUP) {
			this.type=TYPE_PICKUP;
		} else {
			this.type=TYPE_DELIVERY;
		}
		log.trace("[END]");
	}
	//********** workOrderId
	public String getWorkOrderId() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return this.workOrderId;
	}
	public void setWorkOrderId(String workOrderId)
	throws WorkOrderIdTooLongException {
		log.trace("[BEGIN]");
		if (workOrderId != null) {
			if(workOrderId.length() > MAX_LENGTH_WORK_ORDER_ID) {
				WorkOrderIdTooLongException ex=
					new WorkOrderIdTooLongException(workOrderId,
						MAX_LENGTH_WORK_ORDER_ID);
				ex.fillInStackTrace();
				throw ex;
			} else {
				this.workOrderId=workOrderId;
			} // END if(workOrderId.length()>WorkOrderOld.MAX_LENGTH_WORK_ORDER_ID
		} else {
			this.workOrderId=null;
		} // END if (workOrderId != null)
		log.trace("[END]");
	}
	//*************************************************************************
	//* END ACCESSOR METHOD DECLARATIONS
	//*************************************************************************	
	//*************************************************************************
	//* BEGIN OTHER METHOD DECLARATIONS
	//*************************************************************************
	/**
	 * 
	 */
	public void addLineItem(WorkOrderLineItem lineItem) {
		log.trace("[BEGIN]");
		if(lineItem != null) {
			log.trace("LineItem is not null");
			// The lineItem object is not null
			if (!lineItems.contains(lineItem)) {
				log.trace("LineItem added");
				// The lineItems list does not contain the specified lineItem
				lineItems.add((WorkOrderLineItemImpl)lineItem);
				log.trace("LineItem Count: "+lineItems.size());
			} // END if(!lineItems.contains(lineItem))
		} // END if(lineItem != null)
		log.trace("[END]");
	}
	/**
	 * 
	 */
	public void addLineItem(int pos, WorkOrderLineItem lineItem) 
	throws ArrayIndexOutOfBoundsException {
		log.trace("[BEGIN]");
		int tmpItemNumber;
		WorkOrderLineItemImpl tmpLineItem;
		
		if (lineItem != null) {
			// Check to see if the list of LineItem objects already contains the
			// specified LineItem element.
			if ((pos >= 1) && (pos <= lineItems.size())) {
				// Adding a line item, so increment the lineItem values for those 
				// lineItems after the specified index position.
				for(int i=pos; i < lineItems.size(); i++) {
					tmpLineItem=(WorkOrderLineItemImpl) lineItems.get(i);
					tmpItemNumber=tmpLineItem.getItemNumber();
					tmpLineItem.setItemNumber(tmpItemNumber+1);
				}
				// lineItem List is zero (0) based.
				lineItems.set(pos-1,(WorkOrderLineItemImpl)lineItem);
			} else {
				// Specified index is out of the array's bounds
				throw new ArrayIndexOutOfBoundsException(pos);
			} // END if((pos>=1)&&(pos<=lineItems.size()))
		} // END if(lineItem!=null)
		log.trace("[END]");
	}
	/**
	 * Remove the specified WorkOrderLineItem from the list.
	 */
	public void remove(WorkOrderLineItem lineItem) {
		log.trace("[BEGIN]");
		if (lineItems.contains(lineItem)) {
			if (lineItemsToDelete == null) {
				lineItemsToDelete=new ArrayList<WorkOrderLineItem>();
			} // END if (lineItemsToDelete == null)
			lineItems.remove(lineItem);
			lineItemsToDelete.add(lineItem);
		} // END if (lineItems.contains(lineItem))
		log.trace("[END]");
	}
	/**
	 * Remove the specified WorkOrderLineItem from the list.
	 * 
	 * @param pos
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public void remove(int pos) throws ArrayIndexOutOfBoundsException {
		if ((pos >= 1) && (pos <= lineItems.size())) {
			// Removing the line Item from the list.
			if (lineItemsToDelete == null) {
				// The list of work order line items doesn't exist...
				lineItemsToDelete=new ArrayList<WorkOrderLineItem>();
			}
			lineItemsToDelete.add(lineItems.remove(pos-1));
		} else {
			// Specified index is out of the array's bounds
			throw new ArrayIndexOutOfBoundsException(pos);
		}
	}

	public List<WorkOrderLineItem> getLineItems() {
		log.trace("[BEGIN]");
		log.trace("Number of Line Items: "+lineItems.size());
		log.trace("[END]");
		return lineItems;
	}
	
	protected List<WorkOrderLineItem> getLineItemsToDelete() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return lineItemsToDelete;
	}
	/**
	 * Return a new, default <code>WorkOrderLineItem</code> object.
	 * @return WorkOrderLineItem
	 */
	public WorkOrderLineItem createLineItem() {
		log.trace("[BEGIN]");
		WorkOrderLineItemImpl lineItem=new WorkOrderLineItemImpl(this);
		log.trace("[END]");
		return lineItem;
	}
	//*************************************************************************
	//* END OTHER METHOD DECLARATIONS
	//*************************************************************************
}
