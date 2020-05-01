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
package com.wildstartech.wfa.journal.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wildstartech.justo.crm.JournalDescriptionTooLongException;
import com.wildstartech.justo.crm.JournalEntry;
import com.wildstartech.justo.crm.JournalCategoryTooLongException;

public class JournalEntryImpl extends WildObjectSQL implements JournalEntry {
	//*************************************************************************
	//* BEGIN STATIC FIELD DECLARATIONS
	//*************************************************************************
	/** Obtain the log */
	private static Log log=LogFactory.getLog(JournalEntry.class);
	/** Maximum length of a journal description. */
	public static final int MAX_LENGTH_DESCRIPTION=255;
	/** Maximum length of a journal category. */
	public static final int MAX_LENGTH_CATEGORY=50;
	/** Maximum length of MimeType */
	public static final int MAX_LENGTH_MIMETYPE=255;
	//*************************************************************************
	//* END STATIC FIELD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN INSTANCE FIELD DECLARATIONS
	//*************************************************************************
	/** Flag indicating whether or not the journal entry contains data. **/
	private boolean dataAvailable=false;
	/** The date/time the journal entry was created. */
	private Date entryDate=null;
	/** The category of the journal entry. */
	private String category=null;
	/** A brief summary of the contents of the journal entry. */
	private String description=null;
	/** The mime type of the journal entry. */
	private String mimeType=null;
	/** The type of object with which the object is associated. */
	private String relatedObjectType=null;
	/** The unique identifier of the related record. */
	private long relatedObjectPoid=Long.MIN_VALUE;
	//*************************************************************************
	//* END INSTANCE FIELD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN CONSTRUCTOR DECLARATIONS
	//*************************************************************************
	/** 
	 * Prevent use of the default constructor.
	 */
	private JournalEntryImpl() {
		log.trace("[BEGIN]");
		log.trace("[END]");
	}
	/**
	 * Single-string constructor.
	 * 
	 * Allows the instantiation of a JournalEntryImpl for WildObject that has
	 * not yet been saved.
	 */
	public JournalEntryImpl(String objectType) {
		log.trace("[BEGIN]");
		// Check the value passed as objectType
		if (objectType != null) {
			if (objectType.length() > 0) {
				this.relatedObjectType=objectType;
			} // END if (objectType.length() > 0) 
		} // END if (objectType != null)
		log.trace("[END]");
	}
	/**
	 * 
	 */
	public JournalEntryImpl(String objectType, long objectPoid) {
		log.trace("[BEGIN]");
		// Check the value passed as objectType
		if (objectType != null) {
			if (objectType.length() > 0) {
				this.relatedObjectType=objectType;
			} // END if (objectType.length() > 0) 
		} // END if (objectType != null)
		this.relatedObjectPoid=objectPoid;
		log.trace("[END]");
	}
	//*************************************************************************
	//* END CONSTRUCTOR DECLARATIONS
	//*************************************************************************	
 	//*************************************************************************
	//* BEGIN ACCESSOR METHOD DECLARATIONS
	//*************************************************************************
	//********** category
	public String getCategory() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return category;
	}
	public void setCategory(String category) 
	throws JournalCategoryTooLongException {
		if(category != null) {
			// The category is not null, but check for zero-length string
			if (category.length() == 0) {
				// The specified category is a zero-length string.
				this.category=null;
			} else {
				if (category.length() > MAX_LENGTH_CATEGORY) {
					JournalCategoryTooLongException ex=null;
					ex=new JournalCategoryTooLongException(category, 
							MAX_LENGTH_CATEGORY);
					ex.fillInStackTrace();
					throw ex;
				} else {
					this.category=category;
				} // END if (category.length() > MAX_LENGTH_CATEGORY)
			} // END if (category.length() == 0) 
		} else {
			this.category=null;	
		} // END if (category != null)
		// Determine the proper value of the hasData flag
		evaluateData();
	}
	//********** description
	/**
	 * @return String The description of the journal.
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * 
	 * @param String 
	 */
	public void setDescription(String description) 
	throws JournalDescriptionTooLongException {
		if (description != null) {
			// If the description parameter is not NULL...
			if (description.length() == 0) {
				// While the description is not null, it is a zero-length string
				this.description=null;
			} else {
				if (description.length() > MAX_LENGTH_DESCRIPTION) {
					// If the maximum length of the description is too big...
					JournalDescriptionTooLongException ex=null;
					ex=new JournalDescriptionTooLongException(description,
							MAX_LENGTH_DESCRIPTION);
					ex.fillInStackTrace();
					throw ex;
				} else {
					// The specified string is not null, and contains a value
					this.description=description;
				} // END if (description.length() > MAX_LENGTH_DESCRIPTION)
			} // END if (description.length() == 0)
		} else {
			// The specified string is null
			this.description=null;
		} // END if (description != null)
		// Determine the appropriate value for the hasData flag.
		evaluateData();
	}
	//********** entryDate
	/**
	 * Returns the date/time of the journal entry.
	 */
	public Date getEntryDate() {
		return entryDate;
	}
	/**
	 * 
	 */
	public void setEntryDate(Date entryDate) {
		this.entryDate=entryDate;
	}
	//********** dataAvailable
	/**
	 * Returns an indicator as to whether or not the journal entry has data.
	 */
	public boolean isDataAvailable() {
		return this.dataAvailable;
	}
	//********** inputStream
	public InputStream getContentAsInputStream() {
		// TODO Auto-generated method stub
		return null;
	}
	//********** mimeType
	public String getMimeType() {
		return mimeType;
	}
	public void setMimeType(String mimeType) {
		if (mimeType != null) {
			if (mimeType.length() > MAX_LENGTH_MIMETYPE) {
				// If the mimeType is too long, truncate it.
				mimeType.substring(0,MAX_LENGTH_MIMETYPE);
			} else {
				this.mimeType=mimeType;
			} // END if (mimeType.length() > MAX_LENGTH_MIMETYPE)
		} // END if (mimeType != null)
		this.mimeType=mimeType;
	}
	//********** outputStream
	public OutputStream getContentAsOutputStream() {
		// TODO Auto-generated method stub
		return null;
	}
	//********** relatedObjectType
	protected String getRelatedObjectType() {
		return relatedObjectType;
	}
	//********** relatedObjectPoid
	protected long getRelatedObjectPoid() {
		return relatedObjectPoid;
	}
	protected void setRelatedObjectPoid(long objectPoid) {
		this.relatedObjectPoid=objectPoid;
	}
	//*************************************************************************
	//* END ACCESSOR METHOD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN OTHER METHOD DECLARATIONS
	//*************************************************************************
	/**
	 * Determines the appropriate value for the hasData flag.
	 * 
	 * If there is, at least a non-zero length string set for either the 
	 * 'Category' or 'Description' field, then the <code>JournalEntry</code>
	 * is considered to "have data".
	 */
	private void evaluateData() {
		log.trace("[BEGIN]");
		if ((this.description == null) || (this.category == null)) {
			// Either the description or the category have data so, hasData is
			// null.
			this.dataAvailable=false;
		} else {
			// If the aforementioned criteria is not met, then either the 
			// Category or Description field has a non zero-length value,
			// so the JournalEntry will be considered to have data.
			this.dataAvailable=true;
		}
		log.trace("[END]");		
	}
	//*************************************************************************
	//* END OTHER METHOD DECLARATIONS
	//*************************************************************************
}