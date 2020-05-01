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

import java.sql.Connection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wildstartech.wfa.dao.DAOException;
import com.wildstartech.wfa.dao.DateSequence;
import com.wildstartech.wfa.dao.DateSequenceDAO;
import com.wildstartech.wfa.dao.Sequence;
import com.wildstartech.wfa.dao.SequenceDAO;
import com.wildstartech.wfa.dao.user.UserContext;

public class SequenceDAOImpl extends WildDAOImpl implements SequenceDAO {
	//*************************************************************************
	//* BEGIN STATIC FIELD DECLARATIONS
	//*************************************************************************
	// Log
	private static Log log=LogFactory.getLog(SequenceDAO.class);
	/** Used to identify the DAO */
	protected static String DAO_IDENTIFIER_KEY=
		"com.wildstartech.justo.crm.dao.SequenceDAO";
	// Singleton implementation of the Data Access Object for Company objects.
	private static SequenceDAO sequenceDAO=new SequenceDAOImpl();
	//*************************************************************************
	//* END STATIC FIELD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN INSTANCE FIELD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* END INSTANCE FIELD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN CONSTRUCTOR DECLARATIONS
	//*************************************************************************
	private SequenceDAOImpl() {
		log.trace("[BEGIN]");
		log.trace("[END]");
	}
	//*************************************************************************
	//* END CONSTRUCTOR DECLARATIONS
	//*************************************************************************	
 	//*************************************************************************
	//* BEGIN ACCESSOR METHOD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* END ACCESSOR METHOD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN OTHER METHOD DECLARATIONS
	//*************************************************************************
	public static SequenceDAO getInstance() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return sequenceDAO;
	}
	public Connection getConnection() {
		return super.getConnection();
	}
	//*************************************************************************
	//* END OTHER METHOD DECLARATIONS
	//*************************************************************************
	protected String getDAOIdentifierKey() {
		return DAO_IDENTIFIER_KEY;
	}
	public Sequence create(String key) {
		// TODO Auto-generated method stub
		return null;
	}
	public void delete(Sequence seq, UserContext ctx) throws DAOException {
		// TODO Auto-generated method stub
	}
	public List<Sequence> findAll(UserContext ctx) throws DAOException {
		// TODO Auto-generated method stub
		return null;
	}
	public Sequence findByKey(String key, UserContext ctx)
			throws DAOException {
		// TODO Auto-generated method stub
		return null;
	}
	public void save(Sequence seq, UserContext ctx) throws DAOException {
		// TODO Auto-generated method stub
	}
}
