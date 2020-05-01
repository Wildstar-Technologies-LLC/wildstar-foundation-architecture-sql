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

import java.text.NumberFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wildstartech.wfa.finance.ChargeDescriptionTooLongException;
import com.wildstartech.wfa.logistics.ltl.AccessorialCharge;
import com.wildstartech.wfa.logistics.ltl.WorkOrderOld;

/******************************************************************************
 * 
 * Add class description here.
 * 
 * @author Derek Berube, Wildstar Technologies, LLC.
 * @version 1.0 Mar 10, 2007
 */
public class AccessorialChargeImpl extends ChargeImpl
implements AccessorialCharge {
	//*************************************************************************
	//* BEGIN STATIC FIELD DECLARATIONS
	//*************************************************************************
	private static final Log log=LogFactory.getLog(AccessorialCharge.class);
	public static final int MAX_LENGTH_DESCRIPTION=255;
	private static final NumberFormat currencyFormat=
		NumberFormat.getCurrencyInstance();
	//*************************************************************************
	//* END STATIC FIELD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN INSTANCE FIELD DECLARATIONS
	//*************************************************************************
	private int quantity;
	private WorkOrderOld workOrder;
	//*************************************************************************
	//* END INSTANCE FIELD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN CONSTRUCTOR DECLARATIONS
	//*************************************************************************
	protected AccessorialChargeImpl(WorkOrderOld workOrder) {
		super();
		
		this.workOrder=workOrder;
		this.quantity=0;
		try {
			setDescription("");
		} catch (ChargeDescriptionTooLongException ex) {
			log.error("This should never happen.");
		}
		setAmount(0);
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
	//********** quantity
	public int getQuantity() {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Quantity: "+this.quantity);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
		return this.quantity;
	}
	public void setQuantity(int quantity) {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Current Quantity: "+this.quantity);
		} // END if (log.isTraceEnabled())
		if (quantity < 0) {
			this.quantity=0;
		} else {
			this.quantity=quantity;
		} // END if (quantity < 0)
		if (log.isTraceEnabled()) {
			log.trace("New Quantity: "+this.quantity);
			log.trace("Total Amount: "+
					currencyFormat.format(getAmount()*this.quantity));
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
	}
	//********** totalCharge
	public float getTotalCharge() {
		float amount=getAmount();
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Total Amount: "+
					currencyFormat.format(amount*this.quantity));
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
		
		return this.quantity*amount;
	}
	//********** workOrder
	public WorkOrderOld getWorkOrder() {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Work Order : "+ this.workOrder);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
		return this.workOrder;
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
