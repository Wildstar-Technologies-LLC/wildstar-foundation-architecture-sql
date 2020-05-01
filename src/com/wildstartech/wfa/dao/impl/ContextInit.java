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


import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wildstartech.wfa.dao.PersistenceManager;
import com.wildstartech.wfa.dao.PersistenceManagerFactory;


/**
 * Add one sentence class summary here.
 * Add class description here.
 * 
 * @author derek
 * @version 1.0 Aug 10, 2005
 */
public class ContextInit implements ServletContextListener {
	// Store a reference to the PersistenceManager so it can be properly 
	// closed.
	PersistenceManager pm=null;
	private Log log;
	/**
	 * Default, no argument constructor.
	 */
	public ContextInit() {
		log=LogFactory.getLog(ContextInit.class);
	}
	/**
	 * 
	 */
	public void contextInitialized(ServletContextEvent sce) {
		log.trace("Context Initialized. ");
		log.trace("Attempting to load PersistenceManager.");
		// Obtain a reference to the persistence manager.
		pm=PersistenceManagerFactory.getInstance().getPersistenceManager();
		
	}
	/**
	 * When the context is destroyed,
	 */
	public void contextDestroyed(ServletContextEvent sce) {
		// Obtain a reference to the ServletContext
		log.trace("Context Destroyed");
		log.trace("Attempting to close PersistenceManager.");
		// If the PersistenceManager has been created, then try to close it...
		if (pm != null) {
			// If the PersistenceManager is obtained, then close it
			pm.close();
			log.trace("PersistenceManager closed.");		
		} // END if (pm != null)
	}	
}
