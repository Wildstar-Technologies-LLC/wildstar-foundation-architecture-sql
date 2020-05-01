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

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.derby.jdbc.ClientConnectionPoolDataSource;

import com.wildstartech.wfa.dao.PersistenceManager;

public class PersistenceManagerDerbyNetwork extends PersistenceManagerSQL {
	//**************************************************************************
	//* BEGIN STATIC FIELD DECLARATIONS
	//**************************************************************************
	/* Obtain reference to logger */
	private static Log log=LogFactory.getLog(PersistenceManager.class);
	/* Store Singleton reference to the class */
	private static PersistenceManagerSQL pm=
		new PersistenceManagerDerbyNetwork();
	//**************************************************************************
	//* END STATIC FIELD DECLARATIONS
	//**************************************************************************	
	//**************************************************************************
	//* BEGIN INSTANCE FIELD DECLARATIONS
	//**************************************************************************
	// Reference to a DataSource object used to access the relational database
	private ClientConnectionPoolDataSource dataSource;
	//**************************************************************************
	//* END INSTANCE FIELD DECLARATIONS
	//**************************************************************************
	//**************************************************************************
	//* BEGIN CONSTRUCTOR DECLARATIONS
	//**************************************************************************
	private PersistenceManagerDerbyNetwork() {
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
		// Don't need to do anything with the network client since
		// the DBMS runs separately.
	}

	/**
	 * Relies on JDBC driver providing connection pooling features. 
	 */
	private void init() {
		Connection con=null;
		int serverPort=1527;
		String databaseName="Justo";
		String description="Just Blanketwrap";
		String dataSourceName="JustoCRM";
		String serverName="localhost";
		String userName="justoAdmin";
		String password="password";
		
		dataSource=new ClientConnectionPoolDataSource();
	
		dataSource.setDatabaseName(databaseName);
		dataSource.setDescription(description);
		dataSource.setServerName(serverName);
		dataSource.setPortNumber(serverPort);
		dataSource.setDataSourceName(dataSourceName);
		dataSource.setUser(userName);
		dataSource.setPassword(password);
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
		// ClientConectionPoolDataSource as the DataSource used by the 
		// getConnection method.
		setDataSource(dataSource);
	}
	//**************************************************************************
	//* END OTHER METHOD DECLARATIONS
	//**************************************************************************
}
