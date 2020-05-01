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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import com.wildstartech.wfa.dao.GroupNameTooLongException;
import com.wildstartech.wfa.dao.group.PersistentGroup;
import com.wildstartech.wfa.dao.user.PersistentUser;
import com.wildstartech.wfa.dao.user.UserNameComparator;

public class GroupImpl extends WildObjectSQL implements PersistentGroup {
	//*************************************************************************
	//* BEGIN INSTANCE FIELD DECLARATIONS
	//*************************************************************************
	private String groupName;
	private TreeSet<PersistentUser> userList;
	//*************************************************************************
	//* END INSTANCE FIELD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN CONSTRUCTOR DECLARATIONS
	//*************************************************************************
	public GroupImpl() {
		setPoid(Long.MIN_VALUE);
		groupName=null;
		Comparator userNameComparator=UserNameComparator.getInstance();
		userList=new TreeSet<PersistentUser>(userNameComparator);
	}
	//*************************************************************************
	//* END CONSTRUCTOR DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN ACCESSOR METHOD DECLARATIONS
	//*************************************************************************
	/**
	 * Sets the name of the group to the specified value.
	 */
	public void setName(String name) throws GroupNameTooLongException {
		if (name != null) {
			if (name.length() > MAX_GROUP_NAME_LENGTH) {
				throw new GroupNameTooLongException(name);
			} else {
				groupName=name;
			}
		} // END if (name != null)
	}
	/**
	 * Return the name assigned to the group.
	 */
	public String getName() {
		return groupName;
	}
	//*************************************************************************
	//* END ACCESSOR METHOD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN OTHER METHOD DECLARATIONS
	//*************************************************************************
	/**
	 * 
	 */
	public void addUser(PersistentUser user) {
		if (user != null) {
			if (!userList.contains(user)) {
				// The userList doesn't already contain the user.
				userList.add(user);
			}
		} // END if(user!=null)
	}	
	/**
	 * 
	 */
	public List<PersistentUser> getUsers() {
		ArrayList<PersistentUser> users=new ArrayList<PersistentUser>();
		for (PersistentUser user: userList) {
			users.add(user);
		}
		return users;
	}
	/**
	 * Remove the specified user from the group list.
	 */
	public void removeUser(PersistentUser user) {
		if (user != null) {
			_removeUser((UserImpl)user,true);
		}	
	}
	/**
	 * Remove the specified user from the group list.
	 * 
	 * 
	 * @param PersistentUser user
	 * @param boolean propagate A boolean value indicating whether or not
	 * the system should attempt to propage the user removal to the user
	 * object.
	 */
	protected void _removeUser(UserImpl user, boolean propagate) {
		if (user != null) {
			// The user parameter is not NULL, so continue
			if (userList.contains(user)) {
				// The user list contains the specified user
				userList.remove(user);	// Remove it.
				if (propagate) {
					user._removeGroup(this,false);
				}
			}
		}
	}
	/**
	 * Indicates whether or not the specified user is a member of the group.
	 * 
	 * @return boolean
	 */
	public boolean contains(PersistentUser user) {
		return userList.contains(user);
	}
	//*************************************************************************
	//* END OTHER METHOD DECLARATIONS
	//*************************************************************************
}
