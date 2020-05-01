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

import com.wildstartech.wfa.identifiable.UniqueIdentifier;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wildstartech.wfa.document.IdentifiableDocument;
import com.wildstartech.wfa.document.DocumentNameTooLongException;
import com.wildstartech.wfa.ByteCounterOutputStream;
import com.wildstartech.wfa.dao.impl.WildObjectSQL;

public class IdentifiableDocumentImpl extends WildObjectSQL 
implements IdentifiableDocument {
	//*************************************************************************
	//* BEGIN STATIC FIELD DECLARATIONS
	//*************************************************************************
	// Reference to the log file.
	private static final Log log=LogFactory.getLog(IdentifiableDocument.class);
	/**
	 * The maximum allowable lenght of the name of a file.
	 */
	public static final int MAX_LENGTH_DOCUMENT_NAME=255;
	/**
	 * Default MIME-Type
	 */
	private static String MIME_TYPE_DEFAULT="application/octet-stream";
	/*
	 * Default buffer size used.
	 */
	private static final int bufSize=2048;
	/*
	 * Used to format the size of the file.
	 */
	private static NumberFormat numberFormat=NumberFormat.getInstance();
	/*
	 * Used to retrieve the actual content of the file.
	 */
	private static DocumentDAOImpl documentDAO=
		(DocumentDAOImpl) DocumentDAOImpl.getInstance();
	/**
	 * The maximum allowable compressed size of a  file.
	 */
	public static final int MAX_COMPRESSED_FILE_SIZE=2147483647;
	/* Stores an indicator as to whether or not the file data has chagned. */
	private boolean changed;
	/* Stores the compressed size of the document. */
	private int compressedSize;
	/* Stores the uncompressed size of the document. */
	private int size;
	/* Store the file */
	private File file;
	/* Reference to the compressed outputstream */
	private ByteCounterOutputStream cOut;
	/* Reference to the OutputStream */
	private ByteCounterOutputStream out;
	/* Stores the unique identifier of the document. */
	private UniqueIdentifier uuid;
	/* Stores the name of the document. */
	private String name;
	/* Stores the mime-type of the document. */
	private String mimeType;
	//*************************************************************************
	//* END INSTANCE FIELD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN CONSTRUCTOR DECLARATIONS
	//*************************************************************************
	public IdentifiableDocumentImpl() {
		log.trace("[BEGIN]");
		this.changed=false;
		this.uuid="";
		this.name="";
		this.mimeType=MIME_TYPE_DEFAULT;
		this.compressedSize=0;
		this.size=0;
		if (log.isTraceEnabled()) {
			log.trace("Name: "+this.name);
			log.trace("MIME Type: "+this.mimeType);
			log.trace("Compressed Size: "+
				numberFormat.format(this.compressedSize));
			log.trace("Size: "+numberFormat.format(this.size));
			log.trace("Changed: "+this.changed);
		} // END if (log.isTraceEnabled()) 
		log.trace("[END]");
	}
	/**
	 * Constructor taking a <code>File</code> object as a paramter.
	 */
	public IdentifiableDocumentImpl(File file) {
		log.trace("[BEGIN]");
		this.changed=false;
		if (file != null) {
			// The file is not null
			if (file.isDirectory()) {
				// The specified file is a directory.
				this.name="";
				this.mimeType=MIME_TYPE_DEFAULT;
				this.size=0;
				this.compressedSize=0;
			} else {
				// The specified file is not a directory
				this.mimeType=MIME_TYPE_DEFAULT;
				this.name=file.getName();
				setFile(file);
			} // END if (file.isDirectory())
		} // END if (file != null)
		log.trace("[END]");
	}
	//*************************************************************************
	//* END CONSTRUCTOR DECLARATIONS
	//*************************************************************************	
	//*************************************************************************
	//* BEGIN STATIC METHOD DECLARATIONS
	//*************************************************************************
	/**
	 * Static method to retrieve a temporary file.
	 * 
	 * Returns NULL if no temporary file is created.  This shouldn't happen.
	 */
	private void createTemporaryFile() {
		log.trace("[BEGIN]");
		try {
			this.file=File.createTempFile("IdentifiableDocumentImpl", ".gz", null);
			// Flag that the temporary file should be deleted on exit of the JVM 
			this.file.deleteOnExit();
			// Time to convert the passed file to a compressed file.
			this.cOut=new ByteCounterOutputStream(
				new FileOutputStream(this.file));
			// Create the high-level output stream.
			this.out=new ByteCounterOutputStream(
				new GZIPOutputStream(this.cOut));
		} catch (IOException ex) {
			log.error("IOException thrown trying to create a temporary file.");
		} // END try/catch
		if (log.isTraceEnabled()) {
			if (file != null) {
				log.trace("Temporary File Created: "+
					file.getAbsolutePath());
			} else {
				log.error("No Temporary FiIle Created.");
			} // END if (file != null)
		} // END if (log.isTraceEnabled())
		log.trace("[END]");
	}
	//*************************************************************************
	//* BEGIN STATIC METHOD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN ACCESSOR METHOD DECLARATIONS
	//*************************************************************************
	//********** compressedSize
	/**
	 * Returns the compressed size of the document.
	 */
	protected int getCompressedSize() {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Compressed Size: "+this.compressedSize);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
		int bytesWritten=0;
		
		if (this.cOut==null) {
			bytesWritten=this.out.getBytesWritten();
		} else {
			bytesWritten=this.cOut.getBytesWritten();
		} // END if (cOut==null)
		
		return bytesWritten;
	}
	/**
	 * Sets the compressed size of the document.
	 */
	protected void setCompressedSize(int compressedSize) {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Current Compressed Size: "+
					numberFormat.format(this.compressedSize));
		} // END if (log.isTraceEnabled())
		this.compressedSize=compressedSize;
		if (log.isTraceEnabled()) {
			log.trace("New Compressed Size: "+
					numberFormat.format(this.compressedSize));
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
	}
	//********** changed
	/**
	 * Indiciates whether or not the underlying file has changed.
	 */
	protected boolean isChanged() {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("File Changed: "+this.changed);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
		return this.changed;
	}
	//********** file
	/**
	 * Use the specified file as the source for the document.
	 */
	public void setFile(File file) {
		log.trace("[BEGIN]");
		byte[] fileBuffer=null;
		int bytesAvailable=0;
		int bytesRead=0;
		BufferedInputStream in=null;
		FileInputStream tmpFileStream=null;
		StringBuilder sb=null;
		
		// Check to see if the specified file is null
		if (file != null) {
			// The file is not null
			try {
				// Establish a fileBuffer
				fileBuffer=new byte[bufSize];
				// Create a temporary file...
				createTemporaryFile();
				// Obtain an inputFile
				in=new BufferedInputStream(new FileInputStream(file),bufSize);
				// Convert the source file into a compressed file 
				bytesAvailable=in.available();
				while (bytesAvailable > 0) {
					if (log.isTraceEnabled()) {
						log.trace("Bytes Available: "+bytesAvailable);
					} // END if (log.isTraceEnabled)
					if (bytesAvailable > bufSize) {
						bytesRead+=in.read(fileBuffer, 0, bufSize);
						// Write the bytes read to the output stream
						this.out.write(fileBuffer,0,bufSize);
					} else {
						bytesRead+=in.read(fileBuffer, 0, bytesAvailable);
						// Write the bytes read to the output stream
						this.out.write(fileBuffer,0,bytesAvailable);
					} // END if (bytesAvailable > bufSize) 
					// Check to see the number of bytes that are avaialble
					bytesAvailable=in.available();
				} // END while (in.available())
				this.size=bytesRead;	// Store the size of the file read
				this.out.flush();		// Flush the OutputStream buffer
				this.out.close();		// Close the OutputStream
				in.close();				// Close the InputStream
				// Get the size of the compressed file.
				tmpFileStream=new FileInputStream(this.file);
				this.compressedSize=tmpFileStream.available();
				tmpFileStream.close();
				// Do a little logging...
				if (log.isTraceEnabled()) {
					log.trace("Original File Size: "+this.size);
					log.trace("Compressed File Size: "+this.compressedSize);
				} // END if (log.isTraceEnabled())
			} catch (IOException ex) {
				if (sb == null) {
					// The StringBuilder is null, so initialize it...
					sb=new StringBuilder(255);
				} else {
					// Clear the StringBuilder
					sb.delete(0, sb.length());
				} // END if (sb == null)
				sb.append("An IOException was thrown while attempting to ");
				sb.append("create a temporary file.");
				log.error(sb.toString());
			} // END try/catch
		} // END if (file != null)
		log.trace("[END]");
	}
	//********** inputStream
	/**
	 * Returns an inputstream which can be used to access the file.
	 */
	public InputStream getInputStream() {
		log.trace("[BEGIN]");
		InputStream in=null;
		
		if (file == null) {
			// File has not yet been obtained, so grab it from the database.
			documentDAO.getFileData(this);
		} // END if (file != null) 
			
		try {
			if (this.compressedSize == this.size) {
				/* The compressed size and file size are the same, so the
				 * file is NOT compressed.
				 */
				in=new FileInputStream(this.file);
			} else {
				in=new GZIPInputStream(new FileInputStream(this.file));
			}
		} catch (FileNotFoundException ex) {
			log.error(
				"FileNotFoundException thrown obtaining InputStream.",ex);
		} catch (IOException ex) {
			log.error("IOException thrown obtaining InputStream.",ex);
		} // END try/catch
		log.trace("[BEGIN]");
		return in;
	}
	//********** name
	/**
	 * Returns the name of the file.
	 */
	public String getName() {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Name: "+this.name);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
		return this.name;
	}
	/**
	 * Specifies the name to be used for the file.
	 */
	public void setName(String name) throws DocumentNameTooLongException {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Current Name: "+this.name);
		} // END if (log.isTraceEnabled()) 
		if (name != null) {
			if (name.length() > MAX_LENGTH_DOCUMENT_NAME) {
				throw new DocumentNameTooLongException(name,
					MAX_LENGTH_DOCUMENT_NAME);
			} else {
				this.name=name;
			} // END if (name.length() > MAX_LENGTH_DOCUMENT_NAME) 
		} else {
			// The name of the file cannot be null, so throw a filename 
			// exception.
			this.name="blank";
		}
		if (log.isTraceEnabled()) {
			log.trace("New Name: "+this.name);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
	}
	//********** mimeType
	public String getMimeType() {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("MimeType: "+this.mimeType);
			log.trace("[END]");
		} // END if (log.isTraceEnabled()) 
		return this.mimeType;
	}
	public void setMimeType(String mimeType) {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Current Mime-Type: "+this.mimeType);
		} // END if (log.isTraceEnabled())
		if (mimeType==null) {
			// The specified parameter is null.
			this.mimeType="application/octet-stream";
		} else {
			this.mimeType=mimeType;
		} // END if (mimeType == null)
		if (log.isTraceEnabled()) {
			log.trace("New Mime-Type: "+this.mimeType);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
	}
	//********** OutputStream
	/**
	 * Returns an output stream which can be used to write data into the file.
	 */
	public OutputStream getOutputStream() {
		log.trace("[BEGIN]");
		if (this.out == null) {
			// The OutputStream has not yet been allocated...	
			if (this.file == null ) {
				log.trace("The current file field is null");
				/* If the current file property of the object is null, then the
				   File has not yet been created (new object) or retrieved
				   (existing object). */
				createTemporaryFile();
			} else {
				/* The OutputStream has not yet been created; however, the 
				 * File Object has. */
				if (log.isTraceEnabled()) {
					log.trace("The file field has a value ("+
						this.file.getAbsolutePath()+").");	
				} // END if (log.isTraceEnabled())
			} // END if (this.file == null)
		} // END if (this.out == null)
		log.trace("[END]");
		return this.out;
	}
	//********** compressedStream
	/**
	 * Returns the raw GZIP compressed InputSream
	 */
	protected InputStream getCompressedInputStream() {
		log.trace("[BEGIN]");
		InputStream in=null;
		if (this.file == null) {
			// A file has not yet been assoicated with the document, so
			// create it.
			createTemporaryFile();
		} // END if (this.file == null)
		if (this.file != null) {
			try {
				in=new FileInputStream(this.file);
			} catch (FileNotFoundException ex) {
				log.error("FileNotFoundException thrown.",ex);
			}
		} // END if (this.file != null)
		log.trace("[END]");
		return in;
	}
	/**
	 * Returns the raw GZIP compressed OutputStream.
	 */
	protected OutputStream getCompressedOutputStream() {
		log.trace("[BEGIN]");
		if (cOut == null) {
			// The compressed output stream has not yet been allocated...
			if (this.file == null) {
				// A file has not yet been assoicated with the document, so
				// create it.
				createTemporaryFile();
			} // END if (this.file == null)
			if (this.file != null) {
				try {
					this.cOut=new ByteCounterOutputStream(
						new FileOutputStream(this.file));
				} catch (FileNotFoundException ex) {
					log.error("FileNotFoundException thrown.",ex);
				} // END try/catch
			} // END if (this.file != null)
		} // END if (cOut == null)
		log.trace("[END]");
		return cOut;
	}
	//********** size
	/** 
	 * Returns the uncompressed size of the document.
	 */
	public int getSize() {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("File Size: "+this.size);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
		int size=0;
		if (out==null) {
			size=this.size;
		} else {
			if (out.getBytesWritten() < this.size) {
				size=this.size;
			} else {
				size=out.getBytesWritten();
			} // END if (out.getBytesWritten() < this.size)
		}
		return size;
	}
	/**
	 * Sets the uncompressed size of the document.
	 */
	protected void setSize(int size) {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Current Uncompressed Size: "+
					numberFormat.format(this.size));
		} // END if (log.isTraceEnabled())
		this.size=size;
		if (log.isTraceEnabled()) {
			log.trace("New Uncompressed Size: "+
					numberFormat.format(this.size));
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
	}
	//********** uuid
	/**
	 * Returns the unique identifier for the document.
	 */
	public UniqueIdenifier getUUID() {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("UUID: "+this.uuid);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
		return this.uuid;
	}
	protected void setUUID(UniqueIdentifier uuid) {
		if (log.isTraceEnabled()) {
			log.trace("[BEGIN]");
			log.trace("Current uuid"+this.uuid);
		} // END if (log.isTraceEnabled())
		this.uuid=uuid;
		if (log.isTraceEnabled()) {
			
			log.trace("Current uuid"+this.uuid);
			log.trace("[END]");
		} // END if (log.isTraceEnabled())
	}
	/**
	 * Ensures that the File property contains a valid data file.
	 */
	private void getFileData() {
		log.trace("[BEGIN]");
		
		// Check to see if the file object is null...
		if (this.file == null) {
			/* Check the poid for the IdentifiableDocumentImpl file.  If it is equal to the
			 * minimum value, then the document has not yet been saved.  If the
			 * poid is not equal to Long.MIN_VALUE, then retrieve the file's
			 * data from the database.
			 */
			if (getPoid() == Long.MIN_VALUE) {
				// The file has not yet been saved, so create a new, 
				// temporary file.
				createTemporaryFile();
			} else {
				// The file has been saved, so retrieve the file's data...
				documentDAO.getFileData(this);
			} // END if (getPoid() == Long.MIN_VALUE)
		} // END if (this.file == null)
		log.trace("[END]");
	}
	/**
	 * Print a text representation of the object;
	 */
	public String toString() {
		StringBuilder sb=new StringBuilder(255);
		sb.append(this.name);
		return sb.toString();
	}

    public Object findByUUID(UniqueIdentifier uuid) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
	//*************************************************************************
	//* END OTHER METHOD DECLARATIONS
	//*************************************************************************
}
