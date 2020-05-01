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
package com.wildstartech.wfa.person.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wildstartech.justo.crm.Person;
import com.wildstartech.wfa.person.PersonListElement;

public class PersonListElementImpl implements PersonListElement {
	//*************************************************************************
	//* BEGIN STATIC FIELD DECLARATIONS
	//*************************************************************************
	private Log log=LogFactory.getLog(PersonListElement.class);
	//*************************************************************************
	//* END STATIC FIELD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN INSTANCE FIELD DECLARATIONS
	//*************************************************************************
	private long poid;
	private String displayName;
	private String firstName;
	private String lastName;
	//*************************************************************************
	//* END INSTANCE FIELD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN CONSTRUCTOR DECLARATIONS
	//************************************************************************
	public PersonListElementImpl() {
		log.trace("[BEGIN]");
		this.firstName="";
		this.lastName="";
		this.poid=Long.MIN_VALUE;
		log.trace("[END]");
	}
	protected PersonListElementImpl(PersonImpl person) {
		log.trace("[BEGIN]");
		this.firstName=person.getFirstName();
		this.lastName=person.getLastName();
		this.poid=person.getPoid();
		log.trace("[END]");
	}
	protected PersonListElementImpl(String firstName, String lastName, long poid) {
		log.trace("[BEGIN]");
		// handle the firstName parameter.
		if(firstName == null) {
			this.firstName="";
		} else  {
			this.firstName=firstName;
		} // END if(firstName == null)
		// handle the lastName parameter.
		if(lastName == null) {
			this.lastName="";
		} else  {
			this.lastName=firstName;
		} // END if(lastName == null)
		// handle the poid parameter
		this.poid=poid;
		log.trace("[END]");
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
	/**
	 * Returns the person's display name.
	 */
	public String getDisplayName() {
		log.trace("[BEGIN]");
		if (log.isTraceEnabled()) log.trace("Display Name: "+this.displayName);
		log.trace("[END]");
		return this.displayName;
	}
	/**
	 * Returns the person's first name.
	 */
	public String getFirstName() {
		log.trace("[BEGIN]");
		if (log.isTraceEnabled()) log.trace("First Name: "+this.firstName);
		log.trace("[END");
		return this.firstName;
	}
	/**
	 * Returns the person's last name.
	 */
	public String getLastName() {
		log.trace("[BEGIN]");
		if (log.isTraceEnabled()) log.trace("Last Name: "+this.lastName);
		log.trace("[END");
		return this.lastName;
	}
	
	//*************************************************************************
	//* END ACCESSOR METHOD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN OTHER METHOD DECLARATIONS
	//*************************************************************************
	//***** uniqueId
	/**
	 * Returns the unique identifier of the person.
	 */
	public String getUniqueId() {
		return String.valueOf(this.poid);
	}
	//*************************************************************************
	//* END OTHER METHOD DECLARATIONS
	//*************************************************************************
}
