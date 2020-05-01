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
package com.wildstartech.wfa.dao.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wildstartech.wfa.dao.user.UserPreference;
import com.wildstartech.wfa.dao.user.UserPreferenceKeyInvalidException;
import com.wildstartech.wfa.dao.user.UserPreferenceKeyTooLongException;
import com.wildstartech.wfa.dao.user.UserPreferencePropertyInvalidException;

public class UserPreferenceImpl extends WildObjectSQL 
implements UserPreference {
	//*************************************************************************
	//* BEGIN STATIC FIELD DECLARATIONS
	//*************************************************************************
	/** Maximum allowable length of the key. */
	protected int MAX_LENGTH_KEY=255;
	/** Maximum allowable length of the object property. */
	protected int MAX_LENGTH_VALUE=255;
	/** Logger */
	private Log log=LogFactory.getLog(UserPreference.class);
	//*************************************************************************
	//* END STATIC FIELD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN INSTANCE FIELD DECLARATIONS
	//*************************************************************************
	private UserImpl user;
	private Object property;
	private String key;
	//*************************************************************************
	//* END INSTANCE FIELD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN CONSTRUCTOR DECLARATIONS
	//*************************************************************************
	/**
	 * Used to prevent use of default constructor.
	 */
	private UserPreferenceImpl() {
		super();
		log.trace("[BEGIN]");
		log.trace("[END]");
	}
	
	protected UserPreferenceImpl(UserImpl user) {
		this.user=user;
	}
	/**
	 * Single parameter constructor.
	 * 
	 * @param java.lang.String key The <code>String</code> value to be used as
	 * the key for the <code>UserPreference</code> object.
	 */
	protected UserPreferenceImpl(String key) 
	throws UserPreferenceKeyInvalidException, 
		   UserPreferenceKeyTooLongException {
		log.trace("[BEGIN");
		setKey(key);
		log.trace("[END]");
	}
	/**
	 * Multiple string constructor.
	 * 
	 * @param String key
	 * @param Object property
	 */
	protected UserPreferenceImpl(String key, Object property) 
	throws UserPreferenceKeyTooLongException,
		   UserPreferenceKeyInvalidException,
		   UserPreferencePropertyInvalidException {
		log.trace("[BEGIN");
		setKey(key);
		setProperty(property);
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
	//***** key
	public String getKey() {
		return key;
	}
	public void setKey(String key) 
	throws UserPreferenceKeyTooLongException, 
		   UserPreferenceKeyInvalidException {
		if ((key != null) && (key.length() >0)) {
			if (key.length() > MAX_LENGTH_KEY) {
				UserPreferenceKeyTooLongException ex=new 
					UserPreferenceKeyTooLongException(key,MAX_LENGTH_KEY);
				ex.fillInStackTrace();
				log.error("UserPreferenceKeyTooLongException thrown.",ex);
				throw ex;
			} else {
				// The key value is of an acceptable length
				this.key=key;
			} // END if (key.length() > MAX_LENGTH_KEY)
		} else {
			// The key parameter passed is either null or zero-length
			UserPreferenceKeyInvalidException ex=
				new UserPreferenceKeyInvalidException(
					"UserPeferenceKeyNullException",null);
			ex.fillInStackTrace();
			log.error("UserPreferenceKeyNullException thrown.",ex);
			throw ex;
		} // END if (key != null)
	}
	//***** property
	public Object getProperty() {
		return property;
	}
	public void setProperty(Object property) 
	throws UserPreferencePropertyInvalidException {
		log.trace("[BEGIN]");
		this.property=property;
		log.trace("[END]");
	}
	//***** user
	protected void setUser(UserImpl user) {
		this.user=user;
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
