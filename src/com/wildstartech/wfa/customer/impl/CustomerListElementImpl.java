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

import com.wildstartech.wfa.company.impl.CompanyDAOImpl;
import com.wildstartech.wfa.dao.impl.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wildstartech.wfa.customer.impl.CustomerListElement;

public class CustomerListElementImpl extends WildObjectSQL 
implements CustomerListElement {
	/* Logger */
	private Log log=LogFactory.getLog(CustomerListElement.class);
	private boolean companyFlag;
	private String accountNumber;
	private String name;
	private String type;
	private long customerPoid;
	/** 
	 * Default, no-argument constructor.
	 */
	public CustomerListElementImpl() {
		super();
		log.trace("[BEGIN]");
		this.accountNumber="";
		this.name="";
		this.type="";
		this.customerPoid=Long.MIN_VALUE;
		log.trace("[END]");
	}
	/**
	 * Convenience constructor allowing full instanation of class.
	 */
	public CustomerListElementImpl(String name, String accountNumber, 
			String type, long customerPoid) {
		super();
		log.trace("[BEGIN]");
		setName(name);
		setAccountNumber(accountNumber);
		setType(type);
		setCustomerPoid(customerPoid);
		log.trace("[END]");
	}
	//********** accountNumber
	/**
	 * Returns the customer's account number.
	 */
	public String getAccountNumber() {
		if (log.isTraceEnabled()) {
			StringBuilder msg=new StringBuilder(80);
			msg.append("[BEGIN]\n");
			msg.append("accountNumber: ");
			msg.append(this.accountNumber);
			msg.append("\n[END]");
			log.trace(msg.toString());
			msg=null;
		} // END if (log.isTraceEnabled())
		return accountNumber;
	}
	/**
	 * Set the value to be used as the customer name.
	 */
	protected void setAccountNumber(String accountNumber) {
		log.trace("[BEGIN]");
		if (accountNumber != null) {
			// The accountNumber is not null, so check the length
			if (accountNumber.length() > 
				CustomerImpl.MAX_LENGTH_ACCOUNT_NUMBER) {
				// The specified accountNumber is too long, so truncate it...
				this.accountNumber=
					accountNumber.substring(0,
						CustomerImpl.MAX_LENGTH_ACCOUNT_NUMBER);
			} else {
				// Set the accountNubmer equal to the specified value
				this.accountNumber=accountNumber;
			} /* END if (accountNumber.length() > 
			     CustomerImpl.MAX_LENGTH_ACCOUNT_NUMBER) */
		} else {
			/* The specified account number was null, so set name equal to an
			   empty string. */
			this.accountNumber="";
		}
		
		log.trace("[END]");
	}
	//********** customerPoid
	protected long getCustomerPoid() {
		if (log.isTraceEnabled()) {
			StringBuilder msg=new StringBuilder(80);
			msg.append("[BEGIN]\n)");
			msg.append("Customer Poid: ");
			msg.append(this.customerPoid);
			msg.append("\n[END]");
			log.trace(msg.toString());
			msg=null;
		} // END if (log.isTraceEnabled())
		return this.customerPoid;
	}
	protected void setCustomerPoid(long customerPoid) {
		if (log.isTraceEnabled()) {
			StringBuilder msg=new StringBuilder(80);
			msg.append("[BEGIN]\n");
			msg.append("Current Customer POID: ");
			msg.append(this.customerPoid);
			msg.append("\nNew Customer POID: ");
			msg.append(customerPoid);
			log.trace(msg.toString());
			msg=null;
		} // END if (log.isTraceEnabled())
		this.customerPoid=customerPoid;
		log.trace("[END]");
	}
	//********** companyFlag
	public boolean isCompany() {
		return this.companyFlag;
	}
	//********** name
	/**
	 * Return the customer's name.
	 */
	public String getName() {
		if (log.isTraceEnabled()) {
			StringBuilder msg=new StringBuilder(80);
			msg.append("[BEGIN]\n");
			msg.append("name: ");
			msg.append(this.name);
			msg.append("\n[END]");
			log.trace(msg.toString());
			msg=null;
		} // END if (log.isTraceEnabled())
		return this.name;
	}
	/**
	 * Set the <code>name</code> property to the specified value. 
	 * 
	 * @param name
	 */
	protected void setName(String name) {
		if (log.isTraceEnabled()) {
			StringBuilder msg=new StringBuilder(80);
			msg.append("[BEGIN]\nCurrent Name: ");
			msg.append(this.name);
			msg.append("\nNew Name: ");
			msg.append(name);
			log.trace(msg.toString());
			msg=null;
		} // END if (log.isTraceEnabled()) 
		
		if (name != null) {
			// The name is not null
			if (name.length() > CustomerImpl.MAX_LENGTH_NAME) {
				// The specified name is too long, so truncate it.
				this.name=name.substring(0, CustomerImpl.MAX_LENGTH_NAME);
			} else {
				// The name is OK, so use it as specified.
				this.name=name;
			} // END if (name.length() > CustomerImpl.MAX_LENGTH_NAME)
		} else {
			// The specified name is null, so set name equal to an empty string.
			this.name="";
		} // END if (name != null)
		log.trace("[END]");
	}
	//********** type
	/**
	 * Return the type of the customer record.
	 */
	public String getType() {
		if (log.isTraceEnabled()) {
			StringBuilder msg=new StringBuilder(80);
			msg.append("[BEGIN]\n");
			msg.append("type: ");
			msg.append(this.type);
			msg.append("\n[END]");
			log.trace(msg.toString());
			msg=null;
		} // END if (log.isTraceEnabled())
		return this.type;
	}
	/**
	 * Set the <code>type</code> property to the specified value. 
	 * 
	 * @param type
	 */
	protected void setType(String type) {
		if (log.isTraceEnabled()) {
			StringBuilder msg=new StringBuilder(80);
			msg.append("[BEGIN]\nCurrent type: ");
			msg.append(this.type);
			msg.append("\nNew type: ");
			msg.append(type);
			log.trace(msg.toString());
			msg=null;
		} // END if (log.isTraceEnabled())
		
		if (type != null) {
			// The name is not null
			if (type.length() > CustomerImpl.MAX_LENGTH_TYPE) {
				// The specified type is too long, so truncate it.
				this.type=type.substring(0, CustomerImpl.MAX_LENGTH_TYPE);
			} else {
				// The name is OK, so use it as specified.
				this.type=type;
			} // END if (type.length() > CustomerImpl.MAX_LENGTH_TYPE)
			// Check to see if the customer is a Company...
			if (this.type.compareTo(CompanyDAOImpl.DAO_IDENTIFIER_KEY) ==0) {
				this.companyFlag=true;
			}
		} else {
			// The specified type is null, so set name equal to an empty string.
			this.type="";
			this.companyFlag=false;
		} // END if (type != null) 
	}
}