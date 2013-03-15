/*******************************************************************************
 * Copyright (c) 2013 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/
package org.eclipse.birt.report.data.oda.pojo.querymodel;

import java.util.Map;

import org.eclipse.datatools.connectivity.oda.OdaException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * An identity for the source of a column mapping 
 */
public interface IMappingSource
{
	/**
	 * @return the source name
	 */
	String getName( );
	
	/**
	 * 
	 * @param from
	 * @return the mapped value from the <code>from</code> object
	 * @throws OdaException
	 */
	Object fetchValue( Object from, ClassLoader pojoClassLoader ) throws OdaException;
	
	Element createElement( Document doc );
	
	void prepareParameterValues( Map<String, Object> paramValues, ClassLoader pojoClassLoader ) throws OdaException;
}
