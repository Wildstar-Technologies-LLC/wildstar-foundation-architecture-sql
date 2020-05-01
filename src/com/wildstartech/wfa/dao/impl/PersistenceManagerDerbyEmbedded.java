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

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.derby.jdbc.EmbeddedConnectionPoolDataSource;

import com.wildstartech.wfa.dao.PersistenceManager;
import com.wildstartech.wfa.dao.customer.CustomerDAO;

public class PersistenceManagerDerbyEmbedded extends PersistenceManagerSQL {
	//**************************************************************************
	//* BEGIN STATIC FIELD DECLARATIONS
	//**************************************************************************
	/* Store Singleton reference to the class */
	private static PersistenceManagerSQL pm=
		new PersistenceManagerDerbyEmbedded();
	/* Obtain reference to logger */
	private static Log log=LogFactory.getLog(PersistenceManager.class);
	//**************************************************************************
	//* END STATIC FIELD DECLARATIONS
	//**************************************************************************	
	//**************************************************************************
	//* BEGIN INSTANCE FIELD DECLARATIONS
	//**************************************************************************
	// Reference to a DataSource object used to access the relational database
	private EmbeddedConnectionPoolDataSource dataSource;
	//**************************************************************************
	//* END INSTANCE FIELD DECLARATIONS
	//**************************************************************************
	//**************************************************************************
	//* BEGIN CONSTRUCTOR DECLARATIONS
	//**************************************************************************
	private PersistenceManagerDerbyEmbedded() {
		super();
		init();
	}
	//**************************************************************************
	//* END CONSTRUCTOR DECLARATIONS
	//**************************************************************************
	//**************************************************************************
	//* BEGIN ACCESSOR METHOD DECLARATIONS
	//**************************************************************************	
	/**
	 * Returns a reference to the persistence manager.
	 */
	public static PersistenceManager getInstance() {	
		return pm;
	}
	//**************************************************************************
	//* END ACCESSOR METHOD DECLARATIONS
	//**************************************************************************
	//**************************************************************************
	//* BEGIN OTHER METHOD DECLARATIONS
	//**************************************************************************
	/* (non-Javadoc)
	 * @see com.wildstartech.justo.crm.PersistenceManager#close()
	 */
	public void close() {		
		// Initiate the shutdown of the database.
		dataSource.setShutdownDatabase("shutdown");
	}

	/**
	 * Relies on JDBC driver providing connection pooling features. 
	 */
	private void init() {
		Connection con=null;
		// By default, the databse will be created in the ~/Justo folder of the
		// user account under which the application server is running.
		StringBuilder databasePath=new StringBuilder();
		databasePath.append(System.getProperty("user.home"));
		databasePath.append(File.separatorChar);
		databasePath.append("Justo");
		//String databasePath="/Users/derekberube/Documents/Database/Justo/justoCRM";
		String description="Just Blanketwrap";
		String dataSourceName="JustoCRM";
		String userName="justoAdmin";
		String password="password";
		
		dataSource=new EmbeddedConnectionPoolDataSource();
	
		dataSource.setDatabaseName(databasePath.toString());
		dataSource.setDescription(description);
		dataSource.setDataSourceName(dataSourceName);
		dataSource.setUser(userName);
		dataSource.setPassword(password);
		dataSource.setCreateDatabase("create");
		try {
			con=dataSource.getConnection();
		} catch (SQLException ex) {
			StringBuilder msg=new StringBuilder(128);
			msg.append("SQLException thrown while obtaining DataSource");
			msg.append(" connection.");
			log.error(msg.toString(),ex);
		}
		
		// If the connection is not Null, then let's try to get the connection
		// information.
		if (con == null) {
			log.error("Connection is null");
		}
		// Invoke the parent class setDataSource method to store the Derby
		// EmbeddedConectionPoolDataSource as the DataSource used by the 
		// getConnection method.
		setDataSource(dataSource);
	}
	//**************************************************************************
	//* END OTHER METHOD DECLARATIONS
	//**************************************************************************
}
