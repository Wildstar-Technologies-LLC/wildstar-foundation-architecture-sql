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
package com.wildstartech.wfa.role.impl;

import com.wildstartech.wfa.dao.impl.WildObjectSQL;
import com.wildstartech.wfa.role.Role;
import com.wildstartech.wfa.role.RoleNameTooLongException;

public class RoleImpl extends WildObjectSQL implements Role {
	private String name;
	public RoleImpl() {
		super();
	}
	public void setName(String name) throws RoleNameTooLongException {
		if (name.length() < MAX_ROLE_NAME_LENGTH) {
			this.name=name.toUpperCase();
		} else {
			throw new RoleNameTooLongException(name);
		}		
	}
	public String getName() {
		return name;
	}
}
