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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wildstartech.wfa.logistics.ltl.CustomerOrderIdTooLongException;
import com.wildstartech.wfa.logistics.ltl.WorkOrderListElement;

public class WorkOrderListElementImpl implements WorkOrderListElement {
	/* Obtain reference to Log */
	private static Log log=LogFactory.getLog(WorkOrderListElement.class);

	private long workOrderPoid=Long.MIN_VALUE;
	private String consigneeCity=null;
	private String consigneeName=null;
	private String customerName=null;
	private String customerOrderId=null;
	private String status=null;
	private String workOrderId=null;
	private String workOrderType=null;
	
	//********* consigneeCity
	public String getConsigneeCity() {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Consignee City: "+this.consigneeCity);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
		return this.consigneeCity;
	}
	protected void setConsigneeCity(String consigneeCity) {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Current Consignee City: "+this.consigneeCity);
		} // END if (log.isTraceEnabled())
		this.consigneeCity=consigneeCity;
		if (log.isTraceEnabled()) {
			log.trace("New Consignee City: "+this.consigneeCity);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
	}
	//********* consigneeName
	public String getConsigneeName() {
		if (log.isTraceEnabled()) {
			StringBuilder msg=new StringBuilder(80);
			msg.append("[BEGIN]\nconsigneeName: ");
			msg.append(this.consigneeName);
			msg.append("\n[END]");
		} // END if (log.isTraceEnabled()) 
		return consigneeName;
	}
	protected void setConsigneeName(String consigneeName) {
		log.trace("[BEGIN]");
		this.consigneeName=consigneeName;
		log.trace("[END]");
	}
	//********* customerName
	public String getCustomerName() {
		log.trace("[BEGIN]");
		return customerName;
	}
	protected void setCustomerName(String customerName) {
		log.trace("[BEGIN]");
		this.customerName=customerName;
		log.trace("[END]");
	}
	//********** customerOrderId
	/**
	 * 
	 */
	public String getCustomerOrderId() {
		if (log.isTraceEnabled()) {
			StringBuilder msg=new StringBuilder(80);
			msg.append("[BEGIN]\ncustomerOrderId: ");
			msg.append(this.customerOrderId);
			msg.append("\n[END]");
		} // END if (log.isTraceEnabled()) 
		return customerOrderId;
	}
	/**
	 * 
	 * 
	 * @param customerOrderId
	 * @throws CustomerOrderIdTooLongException
	 */
	protected void setCustomerOrderId(String customerOrderId) 
	throws CustomerOrderIdTooLongException {
		log.trace("[BEGIN]");
		if (customerOrderId != null) {
			if(customerOrderId.length() < 
			   WorkOrderImpl.MAX_LENGTH_CUSTOMER_ORDER_ID) {
				this.customerOrderId=customerOrderId;
			} else {
				throw new CustomerOrderIdTooLongException(customerOrderId,
						WorkOrderImpl.MAX_LENGTH_CUSTOMER_ORDER_ID);
			} // END if(customerOrderId.length() < WorkOrderOld.MAX_LENGTH_CUSTOMER_ORDER_ID)
		} else {
			customerOrderId="";
		} // END if(customerOrderId != null)
		log.trace("[END]");
	}
	//********** status
	public String getStatus() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return status;
	}
	protected void setStatus(int status) {
		log.trace("[BEGIN]");
		this.status=WorkOrderImpl.getStatusAsString(status);
		log.trace("[END]");
	}
	//********** workOrderId
	public String getWorkOrderId() {
		if (log.isTraceEnabled()) {
			StringBuilder msg=new StringBuilder(80);
			msg.append("[BEGIN]\nworkOrderId: ");
			msg.append(this.workOrderId);
			msg.append("\n[END]");
		} // END if (log.isTraceEnabled()) 
		return this.workOrderId;
	}
	protected void setWorkOrderId(String workOrderId) {
		log.trace("[BEGIN]");
		this.workOrderId=workOrderId;
		log.trace("[END]");
	}
	//********** workOrderType
	public String getWorkOrderType() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return workOrderType;
	}
	protected void setWorkOrderType(String workOrderType) {
		log.trace("[BEGIN]");
		this.workOrderType=workOrderType;
		log.trace("[END]");
	}
	//********** workOrderPoid
	public long getPoid() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return workOrderPoid;
	}
	protected void setPoid(long workOrderPoid) {
		log.trace("[BEGIN]");
		this.workOrderPoid=workOrderPoid;
		log.trace("[END]");
	}
	//*************************************************************************
	//* END ACCESSOR METHOD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN OTHER METHOD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* END OTHER METHOD DECLARATIONS
	//*************************************************************************
}