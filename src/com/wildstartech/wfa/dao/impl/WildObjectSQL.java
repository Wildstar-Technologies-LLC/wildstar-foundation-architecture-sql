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
package com.wildstartech.wfa.dao.impl;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wildstartech.wfa.dao.WildObject;
import com.wildstartech.wfa.dao.user.PersistentUser;

public class WildObjectSQL extends WildObject {
	private Log log=LogFactory.getLog(WildObject.class);
	long createdByPoid=Long.MIN_VALUE;
	long changedByPoid=Long.MIN_VALUE;
	/**
	 * Sets the unique identifier for the object. 
	 */
	protected void setPoid(long poid) {
		if (log.isTraceEnabled()) {
			StringBuilder msg=new StringBuilder(80);
			msg.append("[BEGIN]\nCurrent Value POID: ");
			msg.append(super.getPoid());
			msg.append("\nNew Value POID: ");
			msg.append(poid);
			log.trace(msg.toString());
			msg=null;
		} // END if (log.isTraceEnabled())
		super.setPoid(poid);
		log.trace("[END]");
	}
	protected void setDateCreated(Date date) {
		if (log.isTraceEnabled()) {
			StringBuilder msg=new StringBuilder(80);
			msg.append("[BEGIN]\nCurrent Value dateCreated: ");
			msg.append(super.getDateCreated());
			msg.append("\nNew Value dateCreated: ");
			msg.append(date);
			log.trace(msg.toString());
			msg=null;
		} // END if (log.isTraceEnabled())
		super.setDateCreated(date);
		log.trace("[END]");
	}
	protected void setCreatedBy(PersistentUser user) {
		if (log.isTraceEnabled()) {
			StringBuilder msg=new StringBuilder(80);
			msg.append("[BEGIN]\nCurrent Value createdBy: ");
			msg.append(super.getCreatedBy());
			msg.append("\nNew Value createdBy: ");
			msg.append(user);
			log.trace(msg.toString());
			msg=null;
		} // END if (log.isTraceEnabled())
		if (user == null) {
			createdByPoid=Long.MIN_VALUE;
		} else {
			// If the user object is not NULL
			if (user instanceof UserImpl) {
				createdByPoid=((UserImpl) user).getPoid();
			} else {
				// The user object is not an instance of the UserImpl class
				ClassCastException ex=new ClassCastException();
				ex.fillInStackTrace();
				log.error("ClassCastException thrown.",ex);
				throw ex;
			}
		}
		super.setCreatedBy(user);
		log.trace("[END]");
	}
	protected void setCreatedBy(long poid) {
		if (log.isTraceEnabled()) {
			StringBuilder msg=new StringBuilder(80);
			msg.append("[BEGIN]\nCurrent Value createdBy: ");
			msg.append(super.getCreatedBy());
			msg.append("\nNew Value createdBy(long): ");
			msg.append(poid);
			log.trace(msg.toString());
			msg=null;
		} // END if (log.isTraceEnabled())
		if (poid == Long.MIN_VALUE) {
			super.setCreatedBy(null);
		} else {
			UserImpl user=(UserImpl) super.getCreatedBy();
			if (user != null) {
				if (user.getPoid() != poid) {
				
				}
			}
		} // END if (poid == LONG.MIN_VALUE)
		log.trace("[END]");
	}
	protected void setDateChanged(Date date) {
		if (log.isTraceEnabled()) {
			StringBuilder msg=new StringBuilder(80);
			msg.append("[BEGIN]\nCurrent Value dateChanged: ");
			msg.append(super.getDateChanged());
			msg.append("\nNew Value dateChanged: ");
			msg.append(date);
			log.trace(msg.toString());
			msg=null;
		} // END if (log.isTraceEnabled())
		super.setDateChanged(date);
		log.trace("[END]");
	}
	protected void setChangedBy(PersistentUser user) {
		if (log.isTraceEnabled()) {
			StringBuilder msg=new StringBuilder(80);
			msg.append("[BEGIN]\nCurrent Value changedBy: ");
			msg.append(super.getChangedBy());
			msg.append("\nNew Value changedBy: ");
			msg.append(user);
			log.trace(msg.toString());
			msg=null;
		} // END if (log.isTraceEnabled())
		super.setChangedBy(user);
		log.trace("[END]");
	}
	protected void setChangedBy(long poid) {
		if (log.isTraceEnabled()) {
			StringBuilder msg=new StringBuilder(80);
			msg.append("[BEGIN]\nCurrent Value changedBy(long): ");
			msg.append(super.getChangedBy());
			msg.append("\nNew Value changedBy(long): ");
			msg.append(poid);
			log.trace(msg.toString());
			msg=null;
		} // END if (log.isTraceEnabled())
		log.trace("[END]");
	}
}
