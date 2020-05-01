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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wildstartech.justo.crm.DescriptionTooLongException;
import com.wildstartech.justo.crm.InvalidDimensionException;
import com.wildstartech.justo.crm.ProductIdTooLongException;
import com.wildstartech.justo.crm.WorkOrder;
import com.wildstartech.justo.crm.WorkOrderLineItem;

public class WorkOrderLineItemImpl extends WildObjectSQL
implements WorkOrderLineItem {
	//*************************************************************************
	//* BEGIN STATIC FIELD DECLARATIONS
	//*************************************************************************
	private static Log log=LogFactory.getLog(WorkOrderLineItem.class);
	/* A length dimension cannot be 10,000 or larger. */
	private static final float MAX_LENGTH=10000;
	/* A width dimension cannot be 10,000 or larger. */
	private static final float MAX_WIDTH=10000;
	/* A height dimension cannot be 10,000 or larger. */
	private static final float MAX_HEIGHT=10000;
	/* A weight dimension cannot be 10,000 or larger. */
	private static final float MAX_WEIGHT=10000;
	//*************************************************************************
	//* END STATIC FIELD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN INSTANCE FIELD DECLARATIONS
	//*************************************************************************
	private int itemNumber;
	private int quantity;
	private float length;
	private float width;
	private float height;
	private float weight;
	private float price;
	private float totalPrice;
	private String description;
	private String productId;
	private WorkOrderOld workOrder;
	//*************************************************************************
	//* END INSTANCE FIELD DECLARATIONS
	//*************************************************************************	
	//*************************************************************************
	//* BEGIN CONSTRUCTOR DECLARATIONS
	//*************************************************************************	
	public WorkOrderLineItemImpl(WorkOrderOld workOrder) {
		log.trace("[BEGIN]");
		this.workOrder=workOrder;
		this.workOrder.addLineItem(this);
		itemNumber=Integer.MIN_VALUE;
		quantity=Integer.MIN_VALUE;
		description=null;
		log.trace("[END]");
	}
	//*************************************************************************
	//* END CONSTRUCTOR DECLARATIONS
	//*************************************************************************		
 	//*************************************************************************
	//* BEGIN ACCESSOR METHOD DECLARATIONS
	//*************************************************************************
	//********** workOrder
	public WorkOrderOld getWorkOrder() {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("workOrderId="+this.workOrder.getWorkOrderId());
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
		return this.workOrder;
	}
	//********** description
	public String getDescription() {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("description="+this.description);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
		return this.description;
	}

	public void setDescription(String description) 
	throws DescriptionTooLongException {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Current Description: "+this.description);
		} // END if (log.isTraceEnabled())
		if (description != null) {
			if (description.length() > MAX_DESCRIPTION_LENGTH) {
				throw new DescriptionTooLongException(description);
			} else {
				this.description=description.toUpperCase();
			}
		} else {
			// The passed parameter is null, so reset the current description to
			// a blank string;
			this.description="";
		} // END if (description != null)
		if (log.isTraceEnabled()) {
			log.trace("New Description: "+this.description);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
	}
	//********** itemNumber
	public int getItemNumber() {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("itemNumber="+this.itemNumber);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
		return this.itemNumber;
	}

	public void setItemNumber(int itemNumber) {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Current Item Number: "+this.itemNumber);
			log.trace("New Item Number: "+itemNumber);
		} // END if (log.isTraceEnabled())
		this.itemNumber=itemNumber;
		log.trace("[END]");
	}
	//********** productId
	public String getProductId() {
		log.trace("[BEGIN]");
		log.trace("productId="+this.productId);
		log.trace("[BEGIN]");
		return this.productId;
	}
	public void setProductId(String productId) 
	throws ProductIdTooLongException {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Current Product ID: "+this.productId);
		} // END if (log.isTraceEnabled())
		if (productId != null) {
			if (productId.length() > MAX_PRODUCT_ID_LENGTH) {
				// The productId value is too long.
				ProductIdTooLongException ex;
				ex=new ProductIdTooLongException(productId);
				ex.fillInStackTrace();
				throw ex;
			} else {
				// Convert the productId to upper case.
				this.productId=productId.toUpperCase();
			} // END if (productId.length() > MAX_PRODUCT_ID_LENGTH)
		} else {
			this.productId="";
		} // END (productId != null)
		if (log.isTraceEnabled()) {
			log.trace("New Product ID: "+this.productId);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
	}
	//********** quantity
	public int getQuantity() {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Quantity: "+this.quantity);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
		return this.quantity;
	}

	public void setQuantity(int qty) {
		quantity=qty;
	}
	//********** length
	public float getLength() {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Length: "+this.length);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
		return this.length;
	}
	public void setLength(float length) throws InvalidDimensionException {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Current Length: "+this.length);
		} // END if (log.isTraceEnabled())
		// Check the dimension.
		if (length < 0) {
			this.length=0;
		} else if (length > MAX_LENGTH) {
			throw new InvalidDimensionException(
					InvalidDimensionException.LENGTH, length);
		} else {
			this.length=length;
		} // END if (length < 0) || (length > MAX_LENGTH)
		if (log.isTraceEnabled()) {
			log.trace("New Length: "+this.length);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
	}
	//********** width	
	public float getWidth() {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Width: "+this.width);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
		return this.width;
	}
	public void setWidth(float width)  throws InvalidDimensionException {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Current Width: "+this.width);
		} // END if (log.isTraceEnabled())
		// Check the dimension.
		if (width < 0) {
			this.width=0;
		} else if (length > MAX_WIDTH) {
			throw new InvalidDimensionException(
					InvalidDimensionException.WIDTH, width);
		} else {
			this.width=width;
		} // END if (width < 0) || (length > MAX_WIDTH)
		if (log.isTraceEnabled()) {
			log.trace("New Width: "+this.width);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
	}
	//********** height
	public float getHeight() {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Height: "+this.height);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
		return this.width;
	}
	public void setHeight(float width)  throws InvalidDimensionException {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Current Height: "+this.height);
		} // END if (log.isTraceEnabled())
		// Check the dimension.
		if (height < 0) {
			this.height=0;
		} else if (height > MAX_HEIGHT) {
			throw new InvalidDimensionException(
					InvalidDimensionException.HEIGHT, height);
		} else {
			this.height=height;
		} // END if (height < 0) || (length > MAX_HEIGHT)
		if (log.isTraceEnabled()) {
			log.trace("New Height: "+this.height);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
	}
	//********** weight
	public float getWeight() {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Weight: "+this.weight);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
		return this.height;
	}
	public void setWeight(float weight) throws InvalidDimensionException {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Current Weight: "+this.weight);
		} // END if (log.isTraceEnabled())
		// Check the dimension.
		if (weight < 0) {
			this.weight=0;
		} else if (weight > MAX_HEIGHT) {
			throw new InvalidDimensionException(
					InvalidDimensionException.WEIGHT, weight);
		} else {
			this.weight=weight;
		} // END if (height < 0) || (length > MAX_HEIGHT)
		if (log.isTraceEnabled()) {
			log.trace("New Weight: "+this.weight);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
	}
	//********** price
	public float getPrice() {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Price: "+this.price);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
		return this.price;
	}
	public void setPrice(float price) {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Current Price: "+this.price);
		} // END if (log.isTraceEnabled())
		this.price=price;
		if (log.isTraceEnabled()) {
			log.trace("New Price: "+this.price);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
	}
	//********** totalPrice
	public float getTotalPrice() {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Total Price: "+this.totalPrice);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
		return this.totalPrice;
	}
	public void setTotalPrice(float totalPrice) {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Current Total Price: "+this.totalPrice);
		} // END if (log.isTraceEnabled())
		this.totalPrice=totalPrice;
		if (log.isTraceEnabled()) {
			log.trace("New Total Price: "+this.totalPrice);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
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