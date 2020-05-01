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
package com.wildstartech.wfa.person.impl;

import com.wildstartech.wfa.dao.impl.WildObjectSQL;
import java.util.Date;

import com.wildstartech.wfa.person.Person;
import com.wildstartech.wfa.person.PersonNameTooLongException;

public class PersonImpl extends WildObjectSQL implements Person {
	private Date birthdate=null;
	private String prefix=null;
	private String firstName=null;
	private String lastName=null;
	private String suffix=null;
	private String displayName=null;
	private int gender=Integer.MIN_VALUE;
	/**
	 * Default, no-argument constructor.
	 */
	public PersonImpl() {
	}
	/**
	 * Constructor taking a single string as a parameter.
	 */
	public PersonImpl(String name) throws PersonNameTooLongException {
		setName(name);
	}
	/**
	 * Constructor taking first and last name parameters.
	 */
	public PersonImpl(String firstName, String lastName) 
	throws PersonNameTooLongException {
		// Manage the firstName parameter
		if (firstName != null) {
			if (firstName.length() >= MAX_LENGTH_FIRST_NAME) {
				throw new PersonNameTooLongException(firstName, 
						MAX_LENGTH_FIRST_NAME);
			} else {
				this.firstName=firstName.toUpperCase();
			} // END if (firstName.length() >= MAX_LENGTH_FIRST_NAME)
		} else {
			this.firstName=null;
		} // END if (firstName != null)
		// Manage the lastName parameter
		if (lastName!= null) {
			if (lastName.length() >= MAX_LENGTH_FIRST_NAME) {
				throw new PersonNameTooLongException(lastName, 
						MAX_LENGTH_LAST_NAME);
			} else {
				this.lastName=lastName.toUpperCase();
			} // END if (lastName.length() >= MAX_LENGTH_FIRST_NAME)
		} else {
			this.lastName=null;
		} // END if (lastName!= null)
		
		// Set the Display Name
		StringBuilder sb=new StringBuilder(MAX_LENGTH_DISPLAY_NAME);
		sb.append(firstName).append(" ").append(lastName);
		displayName=sb.toString();
	}
	//********** birthdate
	public Date getBirthdate() {
		return birthdate;
	}
	public void setBirthdate(Date birthdate) {
		this.birthdate=birthdate;
	}
	//********** displayName
	/**
	 * Returns the current value of the <code>displayName</code> field.
	 * 
	 * @return java.lang.String The current value stored in the 
	 * <code>displayName</code> field.
	 */
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String name) throws PersonNameTooLongException {
		// Check to see if the parameter is null
		if (name != null) {
			if (name.length() > MAX_LENGTH_DISPLAY_NAME) {
				throw 
				   new PersonNameTooLongException(name,MAX_LENGTH_DISPLAY_NAME);
			} else {
				displayName=name.toUpperCase();
			} // END if (name.length() > MAX_LENGTH_DISPLAY_NAME)
		} else {
			StringBuilder tmpDisplayName=new StringBuilder();
			tmpDisplayName.append(firstName).append(" ").append(lastName);
			displayName=tmpDisplayName.toString();
		} // END if (name!=null)
	}
	//********* firstName
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) 
	throws PersonNameTooLongException {
		boolean updateDisplayName=checkDisplayName();

		// Manage firstName assignment process
		if ((firstName != null) && (firstName.compareTo("") != 0)) {
			if (firstName.length() > MAX_LENGTH_FIRST_NAME) {
				throw new PersonNameTooLongException(firstName, 
						MAX_LENGTH_FIRST_NAME);
			} else {
				// The firstName parameter is not null and it is within the
				// maximum values.
				this.firstName=firstName.toUpperCase();
			} // END if (firstName.length() > MAX_LENGTH_FIRST_NAME)
		} else {
			this.firstName=null;
		} // END  if ((firstName != null) && (firstName.compareTo("") != 0))
		
		/* If the updateDisplayName flag is true, then determine the new value 
		 * for the displayName field. */
		if (updateDisplayName) { calculateDisplayName(); }
	}
	//********** Gender
	/**
	 * Returns the gender of the <code>Person</code> object.
	 */
	public int getGender() {
		return gender;
	}
	/**
	 * Set the gender of the <code>Person</code> object.
	 * 
	 * If an invalid gender value is specified, then the system will default to
	 * male.
	 */
	public void setGender(int gender) {
		if ((gender==GENDER_FEMALE) || (gender==GENDER_MALE)) {
			this.gender=gender;
		} else {
			this.gender=Integer.MIN_VALUE;
		} // END if ((gender==GENDER_FEMALE)||(gender==GENDER_MALE))
	}
	//********* lastName
	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) throws PersonNameTooLongException {
		boolean updateDisplayName=checkDisplayName();
		
		// Manage lastName assignment process
		if (lastName != null) {
			if (lastName.length() > MAX_LENGTH_LAST_NAME) {
				throw new PersonNameTooLongException(lastName, 
						MAX_LENGTH_LAST_NAME);
			} else {
				// The firstName parameter is not null and it is within the
				// maximum values.
				this.lastName=lastName.toUpperCase();
			} // END if (lastName.length() > MAX_LENGTH_LAST_NAME)
		} else {
			this.lastName=null;
		} // END if (lastName != null) 
		
		/* If the updateDisplayName flag is true, then determine the new value 
		 * for the displayName field. */
		if (updateDisplayName) { calculateDisplayName(); }
	}
	//********* name
	public void setName(String name) throws PersonNameTooLongException {
		int pos=0;
		
		if (name != null) {
			// Parse the name parameter
			pos=name.indexOf(' ');
			if (pos == -1) {
				setLastName(name);
			} else {
				setFirstName(name.substring(0,pos));
				setLastName(name.substring(pos+1));
			} // END if (pos == -1)
		} // END if (name != null)
	}
	//********* prefix
	public String getPrefix() {
		return prefix;
	}
	public void setPrefix(String prefix) throws PersonNameTooLongException {
		if ((prefix != null) && (prefix.compareTo("") != 0)) {
			if (prefix.length() > MAX_LENGTH_PREFIX) {
				throw new PersonNameTooLongException(prefix, 
						MAX_LENGTH_PREFIX);
			} else {
				// The firstName parameter is not null and it is within the
				// maximum values.
				this.prefix=prefix.toUpperCase();
			} // END if (prefix.length() > MAX_LENGTH_PREFIX)
		} else {
			this.prefix=null;
		} // if ((prefix != null) && (prefix.compareTo("") != 0))
	}
	//********* suffix
	public String getSuffix() {
		return suffix;
	}
	public void setSuffix(String suffix) throws PersonNameTooLongException {
		if ((suffix != null) && (suffix.compareTo("") != 0)) {
			if (suffix.length() > MAX_LENGTH_SUFFIX) {
				throw new PersonNameTooLongException(suffix, 
						MAX_LENGTH_SUFFIX);
			} else {
				// The firstName parameter is not null and it is within the
				// maximum values.
				this.suffix=suffix.toUpperCase();
			} // END if(suffix.length() > MAX_LENGTH_SUFFIX)
		} else {
			this.suffix=null;
		} // END if ((suffix != null) && (suffix.compareTo("") != 0)) 
	}
	/**
	 * String representation of the <code>Person</code> object.
	 */
	public String toString() {
		StringBuilder sb=new StringBuilder(MAX_LENGTH_DISPLAY_NAME+25);
		sb.append(this.displayName);
		sb.append('[');
		sb.append(getPoid());
		sb.append(']');
		return sb.toString();
		
	}
	/*
	 * Automatically "calculates" the value for the <code>displayName</code>.
	 */
	private void calculateDisplayName() {
		StringBuilder tmpDisplayName=new StringBuilder(MAX_LENGTH_DISPLAY_NAME);
		tmpDisplayName.append(this.firstName).append(" ").append(this.lastName);
		this.displayName=tmpDisplayName.toString();
	}
	/*
	 * Determines if the display name was "calculated" or manually set.
	 * 
	 * @return boolean A boolean value indicating whether or not the  value 
	 * stored in the <code>displayName</code> field was automatically or 
	 * manually specified.
	 */
	private boolean checkDisplayName() {
		boolean updateDisplayName=false;
		StringBuilder tmpDisplayName=null;
		if (displayName!=null) {
			tmpDisplayName=new StringBuilder(MAX_LENGTH_DISPLAY_NAME);
			// Determine if displayName needs to be rebuilt
			tmpDisplayName.append(this.firstName);
			tmpDisplayName.append(" ").append(this.lastName);
			if(tmpDisplayName.toString().compareTo(this.displayName) == 0) {
				updateDisplayName=true;
			}
		} else {
			updateDisplayName=true;
		} // END if (displayName != null)
		return updateDisplayName;
	}
}