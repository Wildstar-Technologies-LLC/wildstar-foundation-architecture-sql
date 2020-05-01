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
package com.wildstartech.wfa.company.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.wildstartech.wfa.company.Company;
import com.wildstartech.wfa.company.CompanyNameTooLongException;
import com.wildstartech.wfa.dao.impl.WildObjectSQL;
import com.wildstartech.wfa.person.Person;

public class CompanyImpl extends WildObjectSQL implements Company {
	//*************************************************************************
	//* BEGIN STATIC FIELD DECLARATIONS
	//*************************************************************************
	private static Log log=LogFactory.getLog(Company.class);
	/**
	 * Define a constant for the maximum allowable length of Company Name.
	 */
	protected static final int MAX_LENGTH_COMPANY_NAME=255; 
	//*************************************************************************
	//* END STATIC FIELD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN INSTANCE FIELD DECLARATIONS
	//*************************************************************************
	private List<Person> employeeList;
	private String name;
	//*************************************************************************
	//* END INSTANCE FIELD DECLARATIONS
	//*************************************************************************	
	//*************************************************************************
	//* BEGIN CONSTRUCTOR METHOD DECLARATIONS
	//*************************************************************************	
	public CompanyImpl() {
		super();
		log.trace("[BEGIN]");
		employeeList=new ArrayList<Person>();
		log.trace("[END]");
	}
	//*************************************************************************
	//* END CONSTRUCTOR METHOD DECLARATIONS
	//*************************************************************************		
	//*************************************************************************
	//* BEGIN ACCESSOR METHOD DECLARATIONS
	//*************************************************************************
	//********** employeeList
	public void addEmployee(Person employee) {
		log.trace("[BEGIN]");
		if (log.isTraceEnabled())
			log.trace("employee="+employee);
		if (employee!=null) {
			employeeList.add(employee);
		} // END if (employee!=null)
		log.trace("[END]");
	}
	public List<Person> getEmployeeList() {
		if (log.isTraceEnabled()) {
			StringBuilder msg=new StringBuilder(80);
			msg.append("[BEGIN]");
			msg.append("Number of Employees: ");
			msg.append(this.employeeList.size());
			msg.append("\n[END]");
			log.trace(msg.toString());
			msg=null;
		}
		return employeeList;
	}
	public void setEmployeeList(List<Person> employeeList) {
		if (log.isTraceEnabled()) {
			StringBuilder msg=new StringBuilder(80);
			msg.append("[BEGIN]\n");
			msg.append("Current Employee List Size: ");
			msg.append(this.employeeList.size());
			msg.append("\nNew Employee List Size: ");
			msg.append(employeeList.size());
			log.trace(msg.toString());
			msg=null;
		}
		this.employeeList=new ArrayList<Person>();
		
		if (employeeList != null) {
			// Iterate through the list of employees and add them to the
			// company's employee list.
			for (Person employee: employeeList) {
				// Log information
				if (log.isTraceEnabled()) {
					StringBuilder msg=new StringBuilder(80);
					msg.append("Adding Employee: ");
					msg.append(employee);
					log.trace(msg.toString());
					msg=null;
				}
				employeeList.add(employee);
			}
			
		} // END if (employeeList != null)
		log.trace("[END]");
	}
	//********** name
	public void setName(String name) throws CompanyNameTooLongException {
		log.trace("[BEGIN]");
		if (name != null) {
			if (name.length() < MAX_LENGTH_COMPANY_NAME) {
				this.name=name.toUpperCase();
			} else {
				CompanyNameTooLongException ex=null;
				ex=new CompanyNameTooLongException(name,
					MAX_LENGTH_COMPANY_NAME);
				log.error(ex);
				throw ex;
			} // END if (name.length() < MAX_LENGTH_COMPANY_NAME)
		} else {
			this.name="";
		} // END if (name != null) 
	}
	public String getName() {
		if (log.isTraceEnabled()) {
			StringBuilder msg=new StringBuilder(80);
			msg.append("[BEGIN]\n");
			msg.append("Company Name: ");
			msg.append(this.name);
			msg.append("\n[END]");
			log.trace(msg.toString());
			msg=null;
		}
		return name;
	}
	//*************************************************************************
	//* END ACCESSOR METHOD DECLARATIONS
	//*************************************************************************
	//*************************************************************************
	//* BEGIN OTHER METHOD DECLARATIONS
	//*************************************************************************	
	/**
	 * String representation of the <code>Company</code> object.
	 */
	public String toString() {
		StringBuilder sb=new StringBuilder(MAX_LENGTH_COMPANY_NAME+25);
		sb.append(name);
		sb.append('[');
		sb.append(getPoid());
		sb.append(']');
		return sb.toString();
	}
	//*************************************************************************
	//* END OTHER METHOD DECLARATIONS
	//*************************************************************************		
}
