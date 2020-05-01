/* ============================================================================= 
 * Copyright (c) 2005 Wildstar Technologies, LLC.  All rights reserved.
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

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wildstartech.wfa.dao.PasswordTooLongException;
import com.wildstartech.wfa.dao.UserNameTooLongException;
import com.wildstartech.wfa.dao.group.PersistentGroup;
import com.wildstartech.wfa.dao.user.PersistentUser;

/**
 * Implementation of the <code>User<code> interface.
 * 
 * @author Derek Berube, Wildstar Technologies, LLC.
 * @version 1.0 Mar 21, 2006
 */
public class UserImpl extends WildObjectSQL implements PersistentUser {
	//*************************************************************************
	//* BEGIN STATIC FIELD DECLARATIONS
	//*************************************************************************
	// Logger
	private static Log log=LogFactory.getLog(PersistentUser.class);
	/**
	 * Constant used to identify the maximum length of a User's name.
	 */
	public static final int MAX_LENGTH_USER_NAME = 254;
	/**
	 * Constant used to identify the maximum length of a User's password.
	 */
	public static final int MAX_LENGTH_PASSWORD = 254;
	//*************************************************************************
	//* END STATIC FIELD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN INSTANCE FIELD DECLARATIONS
	//*************************************************************************
	// Indicates whether or not the user is an agent.
	private boolean agent=false;
	// Indicates whether or not the user's password has changed.
	private boolean passwordChanged=false;
	// Stores the user's name.
	private String name;
	// Stores the user's password.
	private String password;
	private SortedSet<PersistentGroup> groupList;
	//*************************************************************************
	//* END INSTANCE  FIELD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN CONSTRUCTOR METHOD DECLARATIONS
	//*************************************************************************
	public UserImpl() {
		super();
		log.trace("[BEGIN]");
		groupList=new TreeSet<PersistentGroup>();
		log.trace("[END]");
	}
	public UserImpl(String val, String pwd) 
	throws UserNameTooLongException, PasswordTooLongException {
		log.trace("[BEGIN]");
		name=val;
		password=pwd;
		log.trace("[END]");
	}
	//*************************************************************************
	//* END CONSTRUCTOR METHOD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN ACCESSOR METHOD DECLARATIONS
	//*************************************************************************
	//********** agent
	/**
	 * Indicates whether or not the user is an agent.
	 */
	public boolean isAgent() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return agent;
	}
	/**
	 * Specifies whether or not the user is an agent.
	 */
	public void setAgent(boolean agent) {
		if (log.isTraceEnabled()) {
			StringBuilder msg=new StringBuilder(255);
			msg.append("[BEGIN]\nCurrent Agent Status: ");
			msg.append(this.agent);
			msg.append("\nNew Agent Status: ");
			msg.append(agent);
			log.trace(msg.toString());
		} // END if (log.isTraceEnabled())
		this.agent=agent;
		log.trace("[END]");
	}
	//********** name
	/**
	 * Return the value currently designated as the User for this object.
	 */
	public String getName() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return name;
	}
	/**
	 * Sets the value to be used as the name for the User object.
	 */
	public void setName(String name) throws UserNameTooLongException {
		if (log.isTraceEnabled()) {
			StringBuilder msg=new StringBuilder(255);
			msg.append("[BEGIN]\nCurrent Name: ");
			msg.append(this.name);
			msg.append("\nNew Name: ");
			msg.append(name);
			log.trace(msg.toString());
		} // END if (log.isTraceEnabled())	
		if ((name == null) || (name.length() <= MAX_LENGTH_USER_NAME)) {
			// The name is allowable...
			this.name=name;
		} else {
			throw new UserNameTooLongException(name,MAX_LENGTH_USER_NAME);
		}
		log.trace("[END]");
	}
	//********** password
	/**
	 * Returns the password.
	 */
	public String getPassword() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return password;
	}
	/**
	 * The password is stored in memory as a full length password.
	 * @throws PasswordTooLongException
	 */
	public void setPassword(String password) throws PasswordTooLongException {
		if (log.isTraceEnabled()) {
			StringBuilder msg=new StringBuilder(255);
			msg.append("[BEGIN]\nCurrent Password: ");
			msg.append(this.password);
			msg.append("\nNew Password: ");
			msg.append(password);
			log.trace(msg.toString());
		} // END if (log.isTraceEnabled())
		if ((password == null) || (password.length() <= MAX_LENGTH_PASSWORD)) {
			// The password is allowable...
			passwordChanged=true;
			this.password=password;
		} else {
			throw new PasswordTooLongException(MAX_LENGTH_PASSWORD);
		}
		log.trace("[END]");
	}
	//********** passwordChanged
	/**
	 * Indicates whether or not the password was modified.
	 * 
	 * @return boolean Indicates whether or not the password has been modified.
	 */
	public boolean isPasswordChanged() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return passwordChanged;
	}
	//*************************************************************************
	//* END ACCESSOR METHOD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN OTHER METHOD DECLARATIONS
	//*************************************************************************
	/**
	 * Add a group to the user's list of assocaited groups.
	 */
	public void addGroup(PersistentGroup group) {
		if (log.isTraceEnabled()) {
			StringBuilder msg=new StringBuilder(255);
			msg.append("[BEGIN]\nNew Group: ");
			msg.append(group);
			log.trace(msg.toString());
		} // END if (log.isTraceEnabled())
		if (group != null) {
			// The specified group is not a null object, so continue...
			if (!groupList.contains(group)) {
				// If the current group list doesn't already contain the group
				// add it.
				log.trace("Adding group.");
				groupList.add(group);
			} else {
				// The user is already a member of the designated group.
				log.trace("User already member of designated group.");
			} // END if(!groupList.contains(group))
		} // END if(group != null)
		log.trace("UserImpl.addGroup() [END]");
	}
	/**
	 * Remove a specified group from the list of associated groups.
	 */
	public void removeGroup(PersistentGroup group) {
		if (log.isTraceEnabled()) {
			StringBuilder msg=new StringBuilder(255);
			msg.append("[BEGIN]\nRemove Group: ");
			msg.append(group);
			log.trace(msg.toString());
		} // END if (log.isTraceEnabled())
		if (group != null) {
			_removeGroup((GroupImpl)group,true);
		} // END if(group != null)
		log.trace("UserImpl.removeGroup("+group+") [BEGIN]");
	}
	/**
	 * Removes the specified group from the list of associated groups with cascade.
	 */
	public void _removeGroup(GroupImpl group, boolean propagate) {
		if (log.isTraceEnabled()) {
			StringBuilder msg=new StringBuilder(255);
			msg.append("[BEGIN]\nRemove Group: ");
			msg.append(group);
			msg.append("\nPropagate: ");
			msg.append(propagate);
			log.trace(msg.toString());
		} // END if (log.isTraceEnabled())
		if (group != null) {
			// If the specified group is not null, then proceed.
			if (groupList.contains(group)) {
				// The groupList contains the specified group.
				groupList.remove(group);
				if (propagate) {
					GroupImpl grp=(GroupImpl)group;
					grp._removeUser(this,false);
				}
			} // END if(groupList.contains(group)
		} // END if(group != null)
		log.trace("[END]");
	}
	/**
	 * Convenience to see if the user is a member of the specified group.
	 */
	public boolean isMemberOfGroup(PersistentGroup group) {
		log.trace("[BEGIN]");
		boolean value=false;
		if (group != null) {
			// The group passed as a parameter is not Null
			value=groupList.contains(group);
		}
		log.trace("[END]");
		return value;
	}
	/**
	 * Return a list of groups to which the user belongs.
	 */
	public List<PersistentGroup> getGroups() {
		log.trace("[BEGIN]");
		ArrayList<PersistentGroup> groups=new ArrayList<PersistentGroup>();
		
		for (PersistentGroup group: groupList) {
			groups.add(group);
		}
		log.trace("[END]");
		return groups;
	}
	/**
	 * Used for comparing one User object to another.
	 * 
	 * One user object is considered to be the same as another user object if
	 * their names match.
	 */
	public int compareTo(Object obj) throws ClassCastException {
		log.trace("[BEGIN]");
		int comparison;
		long objPoid, poid;
		UserImpl objUser=null;
		
		if (obj instanceof UserImpl) {
			objUser=(UserImpl)obj;
			objPoid=objUser.getPoid();
			poid=getPoid();
			if (poid==objPoid) {
				comparison=0;
			} else if (poid < objPoid) {
				comparison=-1;
			} else {
				comparison=1;
			}
		} else {
			// The specified object is not an UserImpl object.
			throw new ClassCastException();
		}
		log.trace("[END]");
		return comparison;
	}
	//*************************************************************************
	//* END OTHER METHOD DECLARATIONS
	//*************************************************************************
}
