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
package com.wildstartech.wfa.document.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wildstartech.wfa.document.IdentifiableDocument;
import com.wildstartech.wfa.document.DocumentNameTooLongException;
import com.wildstartech.wfa.dao.DAOException;
import com.wildstartech.wfa.document.DocumentDAO;
import com.wildstartech.wfa.dao.WildDAO;
import com.wildstartech.wfa.dao.impl.WildDAOImpl;
import com.wildstartech.wfa.dao.user.UserContext;

public class DocumentDAOImpl extends WildDAOImpl implements DocumentDAO {
	// Log
	private static Log log=LogFactory.getLog(DocumentDAO.class);
	// SQL QUERY FOR INSERT
	private static String QUERY_INSERT=
		"INSERT INTO DOCUMENTS (POID, CREATED_BY, DATE_CREATED, MODIFIED_BY, "+
		"DATE_MODIFIED, UUID, MIME_TYPE, FILE_NAME, FILE_SIZE, " +
		"COMPRESSED_SIZE, FILE_DATA) " +
		"VALUES (?,?,?,?,?,?,?,?,?,?,?)";
	// SQL QUERY FOR UPDATE
	private static String QUERY_UPDATE=
		"UPDATE DOCUMENTS SET MODIFIED_BY=?, DATE_MODIFIED=?, UUID=?, " +
		"MIME_TYPE=?, FILE_NAME=?, FILE_SIZE=?, COMPRESSED_SIZE=?, " +
		"FILE_DATA=? WHERE POID=?";
	// SQL QUERY FOR UPDATE W/O FILE
	private static String QUERY_UPDATE_NO_FILE=
		"UPDATE DOCUMENTS SET MODIFIED_BY=?, DATE_MODIFIED=?, UUID=?, " +
		"MIME_TYPE=?, FILE_NAME=? WHERE POID=?";
	// SQL QUERY FOR DELETE
	private static String QUERY_DELETE="DELETE FROM DOCUMENTS WHERE POID=?";
	// Base SQL query for SELECT statements
	private static String QUERY_SELECT_BASE=
		"SELECT POID, CREATED_BY, DATE_CREATED, MODIFIED_BY, DATE_MODIFIED, "+
		"UUID, MIME_TYPE, FILE_NAME, FILE_SIZE, COMPRESSED_SIZE " +
		"FROM DOCUMENTS ";
	// SQL query for all documents
	private static String QUERY_SELECT_ALL=QUERY_SELECT_BASE;
	// SQL query for UUID lookup
	private static String QUERY_SELECT_UUID=QUERY_SELECT_BASE+" WHERE UUID=?";
	// SQL query for primary key lookup.
	private static String QUERY_SELECT_POID=QUERY_SELECT_BASE+" WHERE POID=?";
	// SQL QUERY SELECT FOR UPDATE
	private static String QUERY_SELECT_FOR_UPDATE=
		QUERY_SELECT_POID+" FOR UPDATE";
	// SQL QUERY TO RETRIEVE FILE DATA
	private static String QUERY_SELECT_FILE_DATA=
		"SELECT FILE_DATA FROM DOCUMENTS WHERE POID=?";
	/** Used to identify the DAO */
	protected static String DAO_IDENTIFIER_KEY=
		"com.wildstartech.justo.crm.dao.DocumentDAO";
	/** Indicates the maximum length of the universally unique identifier. */
	private static final int MAX_LENGTH_UUID=36;
	// Singleton implementation of the Data Access Object for Document objects.
	private static DocumentDAOImpl documentDAO=new DocumentDAOImpl();
	private DocumentDAOImpl() {
		super();
		log.trace("[BEGIN]");
		log.trace("[END]");
	}
	/**
	 * Using the contents of a ResultSet, build a document.
	 */
	private static void setDocumentInfo(IdentifiableDocumentImpl doc, ResultSet rs) 
	throws SQLException {
		log.trace("[BEGIN]");
		String tmpString=null;
		if ((doc != null) && (rs != null)) {
			log.trace(
			  "Both the IdentifiableDocumentImpl and Result set contain data.");
			try {
				doc.setContentType(rs.getString("MIME_TYPE"));
				doc.setName(rs.getString("FILE_NAME"));
				doc.setSize(rs.getInt("FILE_SIZE"));
				doc.setCompressedSize(rs.getInt("COMPRESSED_SIZE"));
				tmpString=rs.getString("UUID");
				if ((tmpString==null) || 
					(tmpString.length() == 0)) {
					tmpString=buildUUID(doc.getPoid());
				} // END if ((tmpString==null) || (tmpString.length() == 0))
				doc.setUUID(tmpString);
			} catch (DocumentNameTooLongException ex) {
				log.error("This should never happen!",ex);
			} // END try/catch
		} else {
			if (doc == null) {
				log.error("The specified document object is null.");
			} // END if (doc == null)
			if (rs == null) {
				log.error("The specified ResultSet is null.");
			} // END if (rs == null)
		} // END if ((doc != null) && (rs != null))
		log.trace("[END]");
	}
	//***** create
	public IdentifiableDocument create() {
		log.trace("[BEGIN]");
		IdentifiableDocumentImpl document=new IdentifiableDocumentImpl();
		log.trace("[END]");
		return document;
	}
	public IdentifiableDocument create(File doc) {
		log.trace("[BEGIN]");
		IdentifiableDocumentImpl document=new IdentifiableDocumentImpl(doc);
		log.trace("[END]");
		return document;
	}
	//***** delete
	public void delete(IdentifiableDocument doc, UserContext ctx) 
	throws DAOException {
		log.trace("[BEGIN]");
		Connection con=null;
		
		try {
			// Obtain a connection
			con=pm.getConnection();
			// Disable auto-commit
			con.setAutoCommit(false);
			// Invoke the "save" logic
			delete(doc,ctx,con);
			// Commit the transaction
			con.commit();
		} catch (DAOException ex) {
			// A DAOException was thrown, so rollback the transaction.
			log.error("DAOException thrown.",ex);
			try {
				con.rollback();
			} catch (SQLException ex1) {
				// SQLException was thrown either during the setAutoCommit() or
				// commit() methods
				log.error("SQLException thrown during rollback.",ex1);
			}
		} catch (SQLException ex) {
			log.error("SQLException thrown.",ex);
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException ex) {
					StringBuilder msg=new StringBuilder(128);
					msg.append("SQLException thrown trying to close the ");
					msg.append("connection.");
					log.error(msg.toString(),ex);
					msg=null;
				}
			} // END if (con != null)
		}
		log.trace("[END]");
	}
	public void delete(IdentifiableDocument doc,UserContext ctx,Connection con) 
	throws DAOException {
		log.trace("[BEGIN]");
		long poid;
		int numRows;
		IdentifiableDocumentImpl tmpDocument=null;
		PreparedStatement pStmt=null;
		
		// Cast the Document as a IdentifiableDocumentImpl
		tmpDocument=(IdentifiableDocumentImpl) doc;
		// Validate the parameter
		checkObject(doc);
		// Get the company object's poid
		poid=tmpDocument.getPoid();
		if (poid==Long.MIN_VALUE) {
			// The company object has not yet been saved.
			StringBuilder msg=new StringBuilder(80);
			msg.append("Trying to delete a document that has not yet been ");
			msg.append("saved.");
			log.error(msg.toString());
		} else {
			// Obtain a connection to the database.
			con=pm.getConnection();
			// The company object has been saved.
			try {
				// Build the query
				con.setAutoCommit(false);
				pStmt=con.prepareStatement(QUERY_DELETE);
				pStmt.setDouble(1,poid);
				// Execute the DELETE query
				numRows=pStmt.executeUpdate();
				if (numRows == 1) {
					con.commit();
				} else {
					con.rollback();
				} // END if (numRows == 1)
			} catch (SQLException ex) {
				// Print the error information.
				log.error("SQLException thrown.",ex);
			} // END try/catch
			// Since the object has been deleted, set the poid equal to a
			// minimum value.
			tmpDocument.setPoid(Long.MIN_VALUE);
		} // END if(poid==Long.MIN_VALUE)	
		log.trace("[END]");
	}
	//***** findAll
	public List<IdentifiableDocument> findAll(UserContext ctx) 
	throws DAOException {
		log.trace("[BEGIN]");
		List<IdentifiableDocument> docList=
			new ArrayList<IdentifiableDocument>();
		Connection con=null;
		IdentifiableDocumentImpl doc=null;
		PreparedStatement pStmt=null;
		ResultSet rs=null;
		
		try {
			// Obtain a connection
			con=pm.getConnection();
			// Prepare the SQL statement
			pStmt=con.prepareStatement(QUERY_SELECT_ALL);
			// Execute the SQL command
			rs=pStmt.executeQuery();
			while (rs.next()) {
				doc=new IdentifiableDocumentImpl();
				setWildObjectData(doc,rs);
				setDocumentInfo(doc,rs);
				docList.add(doc);
			} // END while (rs.next())
		} catch (SQLException ex) {
			log.error("SQLException thrown while searching.",ex);
		} finally {
			try {
				// Close the ResultSet
				rs.close();
				// Close the PreparedStatement
				pStmt.close();
				// Close the connection
				con.close();
			} catch (SQLException ex) {
				log.error(
				"SQLException thrown while trying to cleanup the connection.",
				ex);
			} // END try/catch
		} // END try/catch/finally
		log.trace("[END]");
		return docList;
	}
	//***** findByName
	public List<IdentifiableDocument> findByName(String name, UserContext ctx) 
	throws DAOException {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return null;
	}
	public IdentifiableDocument findByUUID(String uuid, UserContext ctx) {
		log.trace("[BEGIN]");
		Connection con=null;
		IdentifiableDocumentImpl doc=null;
		PreparedStatement pStmt=null;
		ResultSet rs=null;
		
		if (uuid != null) {
			// A uuid value was specified, so let's go looking.
			if (log.isTraceEnabled()) {
				log.trace("UUID: "+uuid);
			} // END if (log.isTraceEnabled())
			try {
				// Obtain a connection...
				con=pm.getConnection();
				// Prepare the SQL statement
				pStmt=con.prepareStatement(QUERY_SELECT_UUID);
				// Populate the statement with the specified uuid.
				pStmt.setString(1,uuid);
				// Execute the SQL command
				rs=pStmt.executeQuery();
				if (rs.next()) {
					doc=new IdentifiableDocumentImpl();
					setWildObjectData(doc,rs);
					setDocumentInfo(doc,rs);
				} // END if (rs.next())
				
			} catch (SQLException ex) {
				log.error("SQLException thrown.",ex);
			} finally {
				try {
					// Close the ResultSet
					rs.close();
					// Close the PreparedStatement
					pStmt.close();
					// Close the connection.
					con.close();
				} catch (SQLException ex) {
					log.error(
						"SQLException thrown while closing the connection.");					
				}
			} // END try/catch
		} // END if (uuid != null)
		log.trace("[END]");
		return doc;
	}
	/**
	 * At some point in time, this method will be depracated.
	 * @param key
	 * @param ctx
	 * @return
	 * @throws DAOException
	 */
	public IdentifiableDocument findByPrimaryKey(long key, UserContext ctx)
	throws DAOException {
		log.trace("[BEGIN]");
		Connection con=null;
		IdentifiableDocumentImpl doc=null;
		PreparedStatement pStmt=null;
		ResultSet rs=null;
		
		if (key != Long.MIN_VALUE) {
			// Potentially valid key, let's see what we find.
			try {
				// Obtain a connection
				con=pm.getConnection();
				// Prepare the SQL statement.
				pStmt=con.prepareStatement(QUERY_SELECT_POID);
				// Populate the statement with the specified key.
				pStmt.setLong(1, key);
				// Execute the SQL command
				rs=pStmt.executeQuery();
				if (rs.next()) {
					doc=new IdentifiableDocumentImpl();
					setWildObjectData(doc,rs);
					setDocumentInfo(doc,rs);
				} // END if (rs.next())
			} catch (SQLException ex) {
				log.error("SQLException thrown.",ex);
			} // END try/catch
		} // END if (key != Long.MIN_VALUE)
		log.trace("[END]");
		return doc;
	}
	//***** fileData
	/**
	 * Creates a temporary file which stores the contents of the data.
	 */
	protected void getFileData(IdentifiableDocumentImpl doc) {
		log.trace("[BEGIN]");
		byte[] buffer;
		int bytesAvailable=0;
		int bytesRead=0;
		int bufferSize=2048;
		long poid=Long.MIN_VALUE;
		Connection con=null;
		InputStream in=null;
		OutputStream out=null;
		PreparedStatement pStmt=null;
		ResultSet rs=null;
		
		// Get thePersistent Object Identifier for the Document
		poid=doc.getPoid();
		if (poid != Long.MIN_VALUE) {
			// The poid is potentially valid, so continue.
			try {
				con=pm.getConnection();
				pStmt=con.prepareStatement(QUERY_SELECT_FILE_DATA);
				pStmt.setLong(1,poid);
				rs=pStmt.executeQuery();
				
				if (rs.next()) {
					buffer=new byte[bufferSize];
					in=rs.getBinaryStream("FILE_DATA");
					// Obtain the outputstream to which data will be written.
					out=doc.getCompressedOutputStream();
					
					bytesAvailable=in.available();
					// Copy the file data.
					while (bytesAvailable > 0) {
						bytesRead=in.read(buffer);
						if (bytesRead > 0) {
							// Since data was read, write it out...
							out.write(buffer,0,bytesRead);
						} // END if (bytesRead > 0)
						// Determine how many bytes are available
						bytesAvailable=in.available();
					} // END while (bytesAvailable > 0)
					try {
						// Flush the OutputStream
						out.flush();
						// Close the OutputStream
						out.close();
						// Close the InputStream
						in.close();
					} catch (IOException ex) {
						log.error("IOException thrown while cleaning up.",ex);
					} // END try/catch
				} else {
					// No matching record could be found...
					if (log.isErrorEnabled()) {
						StringBuilder msg=new StringBuilder(80);
						msg.append("Could not retrieve file data for ");
						msg.append("document: ").append(poid);
						log.error(msg.toString());
						msg=null;
					} // END if (log.isErrorEnabled()) 
				} // END if (rs.next())
			} catch (SQLException ex) {
				log.error("An SQLException was thrown.",ex);
			} catch (IOException ex) {
				log.error("IOException while retrieving file data.",ex);
			} // END try/catch
		} else {
			// The poid is for an unsaved Document, so log an error
			if (log.isErrorEnabled()) {
				StringBuilder msg=new StringBuilder(80);
				msg.append("The IdentifiableDocumentImpl object passed as a parameter ");
				msg.append("has not yet been saved.");
				log.error(msg.toString());
				msg=null;
			} // END if (log.isErrorEnabled())
		} // END if (poid != Long.MIN_VALUE)
		
		log.trace("[END]");
	}
	//***** save
	/**
	 * Passivate the referenced <code>IdentifiableDocument</code> object.
	 * @param Document
	 * @throws DAOException
	 */
	public void save(IdentifiableDocument doc, UserContext ctx) 
	throws DAOException {
		log.trace("[BEGIN]");
		Connection con=null;
		
		try {
			// Obtain a connection
			con=pm.getConnection();
			// Disable auto-commit
			con.setAutoCommit(false);
			// Invoke the "save" logic
			save(doc,ctx,con);
			// Commit the transaction
			con.commit();
		} catch (DAOException ex) {
			// A DAOException was thrown, so rollback the transaction.
			try {
				con.rollback();
			} catch (SQLException ex1) {
				// SQLException was thrown either during the setAutoCommit() or
				// commit() methods
				log.error("SQLException thrown during rollback.",ex1);
			}
		} catch (SQLException ex) {
			log.error("SQLException thrown.",ex);
		} finally {
			if (con != null) {
				try {
					con.close();
				} catch (SQLException ex) {
					StringBuilder msg=new StringBuilder(128);
					msg.append("SQLException thrown trying to close the ");
					msg.append("connection.");
					log.error(msg.toString(),ex);
				}
			}
		}
		log.trace("[END]");
	}
	protected void save(IdentifiableDocument doc,UserContext ctx,Connection con) 
	throws DAOException {
		log.trace("[BEGIN]");
		long poid;
		IdentifiableDocumentImpl tmpDocument;
		
		// Check the object to ensure valid parameters are passed.
		checkObject(doc);
		// Check to ensure that the UserContext is not null...
		checkObject(ctx,WildDAO.VALIDATE_NULL);
		// Get the poid for the object
		tmpDocument=(IdentifiableDocumentImpl) doc;
		// Business rule enforcement workflow would go here.
		// Perform the actual work to save the object
		poid=tmpDocument.getPoid();
		if (poid == Long.MIN_VALUE) {
			// Based upon the poid value, the object has not yet been saved.
			_create(tmpDocument,ctx,con);			
		} else {
			// Since a valid poid value is assigned, update the existing record.
			_update(tmpDocument,ctx,con);
		} // if (poid == Long.MIN_VALUE)
		log.trace("[END]");
	}
	private void 
		_create(IdentifiableDocumentImpl doc, UserContext ctx, Connection con) 
	throws DAOException {
		log.trace("[BEGIN]");
		long poid;
		long currentUserPoid;
		int numRows;
		InputStream inputStream=null;
		PreparedStatement pStmt=null;
		String tmpString=null;
		Timestamp currentTime=null;
	
		// Get the current user and date/time
		currentUserPoid=getCurrentUser(ctx);
		currentTime=new Timestamp(System.currentTimeMillis());
		
		try {
			// Get the next poid
			poid=getNextPoid(con);
			// Prepare/compile the statement
			pStmt=con.prepareStatement(QUERY_INSERT);
			// Populate the prepared statement with values
			pStmt.setLong(1,poid);						// POID
			pStmt.setLong(2,currentUserPoid);			// CREATED_BY
			pStmt.setTimestamp(3,currentTime);			// DATE_CREATED
			pStmt.setLong(4,currentUserPoid);			// MODIFIED_BY
			pStmt.setTimestamp(5,currentTime);			// DATE_MODIFIED
			pStmt.setString(6, buildUUID(poid));		// UUID
			// MIME_TYPE
			tmpString=doc.getContentType();
			if (tmpString == null) {
				pStmt.setNull(7, Types.VARCHAR);
			} else {
				pStmt.setString(7,tmpString);
			}
			// FILE_NAME
			tmpString=doc.getName();
			if (tmpString == null) {
				pStmt.setNull(8, Types.VARCHAR);
			} else {
				pStmt.setString(8,tmpString);
			}
			// FILE_DATA
			inputStream=doc.getCompressedInputStream();
			if (inputStream.available() > 0) {
				// Set the file size
				pStmt.setInt(9, doc.getSize());
				// Set the compressed fileSize
				pStmt.setInt(10, doc.getCompressedSize());
				// The inputStream has data available, so save it.
				pStmt.setBinaryStream(11, inputStream, doc.getCompressedSize());
			} else {
				// Set the fileSize
				pStmt.setInt(9,0);
				// Set the compressed fileSize
				pStmt.setInt(10,0);
				// The inputStream doesn't have any data, so set it to null.
				pStmt.setNull(11, Types.BLOB);
			}
			// Execute the INSERT query
			numRows=pStmt.executeUpdate();
			if(numRows!=1) {
				log.error("Update affected more than 1 row.");
			}
			// Close the PreparedStatement
			pStmt.close();
			// Store the persistent object identifier with the Document object
			doc.setPoid(poid);
		} catch (SQLException ex) {
			// Print the error information
			log.error("SQLException thrown.",ex);
		} catch (Throwable ex) {
			log.error("Unknown Exception thrown.",ex);
		}
		log.trace("[END]");
	}
	private void _update(IdentifiableDocumentImpl doc, UserContext ctx, Connection con) 
	throws DAOException {
		log.trace("[BEGIN]");
		boolean docFileChanged=false;
		long poid=Long.MIN_VALUE;
		long tmpPoid=Long.MIN_VALUE;
		long currentUserPoid=Long.MIN_VALUE;
		int numRows=0;
		InputStream inputStream=null;
		PreparedStatement pStmt=null;
		ResultSet rs=null;
		String tmpString=null;
		Timestamp currentTime=null;
	
		// Get the current user and date/time
		currentUserPoid=getCurrentUser(ctx);
		currentTime=new Timestamp(System.currentTimeMillis());
		
		try {
			poid=doc.getPoid();
			// Select the document for updating....
			pStmt=con.prepareStatement(QUERY_SELECT_FOR_UPDATE);
			pStmt.setLong(1, poid);
			rs=pStmt.executeQuery();
			while (rs.next()) {
				tmpPoid=rs.getLong(1);
			} // END while (rs.next();
			rs.close();			// Close the resultset
			pStmt.close();		// Close the PreparedStatement
			if (poid==tmpPoid) {
				// The record was found in the database.
				// Determine if the document file has chagned.
				docFileChanged=doc.isChanged();
				if (docFileChanged) {
					pStmt=con.prepareStatement(QUERY_UPDATE);
				} else {
					pStmt=con.prepareStatement(QUERY_UPDATE_NO_FILE);
				}
				//********** MODIFIED_BY
				pStmt.setLong(1,currentUserPoid);			
				//**********  DATE_MODIFIED
				pStmt.setTimestamp(2,currentTime);			
				//********** UUID
				tmpString=doc.getUUID();
				if ((tmpString == null) || 
					(tmpString.length() < MAX_LENGTH_UUID)) {
					tmpString=buildUUID(doc.getPoid());
				} // END if ((tmpString == null) || (tmpString.length() < 36))
				pStmt.setString(3,doc.getUUID());			
				//********** MIME_TYPE
				tmpString=doc.getContentType();
				if (tmpString == null) {
					pStmt.setNull(4, Types.VARCHAR);
				} else {
					pStmt.setString(4,tmpString);
				}
				//********** FILE_NAME
				tmpString=doc.getName();
				if (tmpString == null) {
					pStmt.setNull(5, Types.VARCHAR);
				} else {
					pStmt.setString(5,tmpString);
				}
				//********** FILE_SIZE and FILE_DATA
				if (docFileChanged) {
					// Obtain an inputstream to read data..
					inputStream=doc.getCompressedInputStream();
					if (inputStream.available() > 0) {
						// The inputStream has data availalbe so...
						// Store the file size...
						pStmt.setInt(6, doc.getSize());
						// Store the compressed file size...
						pStmt.setInt(7, doc.getCompressedSize());
						// Save the data...
						pStmt.setBinaryStream(8, inputStream, doc.getSize());
					} else {
						// The inputStream doesn't have any data so...
						// Store the file size
						pStmt.setInt(6, 0);
						// Store the compressed file size
						pStmt.setInt(7, 0);
						// Set the file data to null
						pStmt.setNull(8, Types.BLOB);
					} // END if (inputStream.available() > 0)
					// POID for the Document
					pStmt.setLong(8,poid);
				} else {
					/* The document information has not changed, so the last
					 * parameter in the SQL statement is the POID. */
					pStmt.setLong(6, poid);
				} // END if (docFileChanged) 
				// Execute the INSERT query
				numRows=pStmt.executeUpdate();
				if(numRows!=1) {
					log.error("Update affected more than 1 row.");
				}
				// Close the PreparedStatement
				pStmt.close();
			} else {
				log.error("Couldn't find the specified document.");
			} // END if (poid==tmpPoid) {
		} catch (SQLException ex) {
			// Print the error information
			log.error("SQLException thrown.",ex);
		} catch (Throwable ex) {
			log.error("Unknown Exception thrown.",ex);
		} // END try/catch
		log.trace("[END]");
	}
	//*************************************************************************
	//* END CRUD METHODS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN OTHER METHOD DECLARATIONS
	//*************************************************************************
	protected String getDAOIdentifierKey() {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("DAO_IDENTIFIER_KEY="+DAO_IDENTIFIER_KEY);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
		return DAO_IDENTIFIER_KEY;
	}
	/**
	 * Returns a reference to the DocumentDAO.
	 */
	public static DocumentDAO getInstance() {
		log.trace("[BEGIN]");
		log.trace("[END]");
		return documentDAO;
	}
	//*************************************************************************
	//* END OTHER METHOD DECLARATIONS
	//*************************************************************************
}
