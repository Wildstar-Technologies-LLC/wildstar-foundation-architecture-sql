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

import com.wildstartech.wfa.dao.impl.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wildstartech.wfa.customer.AccountNumberTooLongException;
import com.wildstartech.wfa.customer.Customer;
import com.wildstartech.wfa.Party;

public class CustomerImpl extends WildObjectSQL implements Customer {
	private static Log log=LogFactory.getLog(Customer.class);
	public static final int MAX_LENGTH_ACCOUNT_NUMBER=30;
	public static final int MAX_LENGTH_NAME=255;
	public static final int MAX_LENGTH_TYPE=255;
	private Party customer;
	private String accountNumber;
	private long poid;
	/**
	 * Default, no-argument constructor.
	 */
	public CustomerImpl() {
		super();
		log.trace("[BEGIN]");
		this.customer=new Party();
		this.accountNumber="";
		log.trace("[END]");
	}
	//***** accountNumber
	/**
	 * Return the account number for the customer.
	 */
	public String getAccountNumber() {
		if(log.isTraceEnabled()) {
			StringBuilder msg=new StringBuilder(80);
			msg.append("[BEGIN]\n");
			msg.append("Account Number: ");
			msg.append(this.accountNumber);
			msg.append("\n[END]");
			log.trace(msg.toString());
			msg=null;
		}
		return this.accountNumber;
	}
	/**
	 * @param java.lang.String accountNumber The value to be used as the 
	 * customer's account number.
	 * @throws AccountNumberTooLongException
	 */
	public void setAccountNumber(String accountNumber)
	throws AccountNumberTooLongException {
		if (log.isTraceEnabled()) {
			StringBuilder msg=new StringBuilder(80);
			msg.append("[BEGIN]\n");
			msg.append("accountNumber: ");
			msg.append(accountNumber);
			log.trace(msg.toString());
			msg=null;
		}
		if(accountNumber==null) {
			log.trace("The specified account number is null.");
			this.accountNumber="";
		} else {
			if (accountNumber.length() <= MAX_LENGTH_ACCOUNT_NUMBER) {
				// The specified account number is ok.
				this.accountNumber=accountNumber;
			} else {
				// The specified account number is too large
				AccountNumberTooLongException ex=
					new AccountNumberTooLongException(accountNumber,
							MAX_LENGTH_ACCOUNT_NUMBER);
				log.error("AccountNumberTooLongException thrown.",ex);
				throw ex;
			}
		}
		log.trace("[END]");
	}
	//***** customer
	/**
	 * Return a reference to the <code>Party</code> object.
	 */
	public Party getCustomer() {
		if (log.isTraceEnabled()) {
			StringBuilder msg=new StringBuilder(80);
			msg.append("[BEGIN]\n");
			msg.append("customer: ");
			msg.append(customer);
			msg.append("\n[END]");
			log.trace(msg.toString());
			msg=null;
		}
		return this.customer;
	}
	public void setCustomer(Party party) {
		if (log.isTraceEnabled()) {
			StringBuilder msg=new StringBuilder(80);
			msg.append("[BEGIN]\n");
			msg.append("Party: ");
			msg.append(party);
			log.trace(msg.toString());
			msg=null;
		}
		this.customer=party;
		log.trace("[END]");

	}
	//***** customerName
	public String getName() {
		if (log.isTraceEnabled()) {
			StringBuilder msg=new StringBuilder(255);
			msg.append("[BEGIN]\n");
			msg.append("customerName: "+customer.getName());
			msg.append("\n[END]");
			log.trace(msg.toString());
			msg=null;
		} // END if (log.isTraceEnabled())
		return customer.getName();
	}
	//***** isCompany
	/**
	 * Indicates whether or not the <code>Customer</code> is a company.
	 */
	public boolean isCompany() {
		log.trace("[BEGIN]");
		boolean isCompany=false;
		switch(this.customer.getType()) {
		case(Party.TYPE_COMPANY):
			isCompany=true;
			break;
		default: 
			isCompany=false;
			break;
		}
		log.trace("[END]");
		return isCompany;
	}
}