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

import com.wildstartech.wfa.dao.impl.WildObjectSQL;
import java.text.NumberFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wildstartech.wfa.finance.Charge;
import com.wildstartech.wfa.finance.ChargeDescriptionTooLongException;

public class ChargeImpl extends WildObjectSQL implements Charge {
	//*************************************************************************
	//* BEGIN STATIC FIELD DECLARATIONS
	//*************************************************************************
	/** Log used for debugging information. */
	private static final Log log=LogFactory.getLog(Charge.class);
	public static final int MAX_LENGTH_DESCRIPTION=255;
	private static final NumberFormat currencyFormat=
		NumberFormat.getCurrencyInstance();
	//*************************************************************************
	//* END STATIC FIELD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN INSTANCE FIELD DECLARATIONS
	//*************************************************************************
	private float amount;
	private String description;
	//*************************************************************************
	//* END INSTANCE FIELD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN CONSTRUCTOR DECLARATIONS
	//*************************************************************************
	public ChargeImpl() {
		super();
		this.amount=0;
		this.description="";
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
	//********** description
	public String getDescription() {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Description: "+this.description);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
		return this.description;
	}
	public void setDescription(String description)
	throws ChargeDescriptionTooLongException {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Current Description: "+this.description);
		} // END if (log.isTraceEnabled())
		if (description != null) {
			if (description.length() > MAX_LENGTH_DESCRIPTION) {
				// Throw the new description
				throw new ChargeDescriptionTooLongException(description,
					MAX_LENGTH_DESCRIPTION);
			} else {
				this.description=description.toUpperCase();
			} // END if (description.length() > MAX_LENGTH_DESCRIPTION)
		} else {
			this.description="";
		} // END description
		if (log.isTraceEnabled()) {
			log.trace("New Description: "+this.description);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
	}
	//********** amount
	public float getAmount() {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Per Unit Amount: "+
					currencyFormat.format(this.amount));
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
		return this.amount;
	}
	public void setAmount(float amount) {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Current Amount: "+
					currencyFormat.format(this.amount));
		} // END if (log.isTraceEnabled())
		this.amount=amount;
		if (log.isTraceEnabled()) {
			log.trace("New Amount: "+
					currencyFormat.format(this.amount));
			
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
