
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.birt.report.data.oda.pojo.i18n.Messages;
import org.eclipse.birt.report.data.oda.pojo.impl.Driver;
import org.eclipse.birt.report.data.oda.pojo.util.MethodParameterType;
import org.eclipse.datatools.connectivity.oda.IParameterMetaData;
import org.eclipse.datatools.connectivity.oda.OdaException;


/**
 * Maintain all parameters of a pojo query
 */
public class QueryParameters
{
	private QueryParameter[] qps;
	
	//used to speed up find index by name. index is 1-based
	private Map<String, Integer> nameIndexMap = new HashMap<String, Integer>( );
	

	
	/**
	 * 
	 * @param index: 1-based
	 * @return
	 */
	public String getParamName( int index )
	{
		if ( index >=1 && index <= qps.length )
		{
			return qps[index-1].name;
		}
		return null;
	}
	
	private QueryParameters( QueryParameter[] qps )
	{
		assert qps != null; 
		this.qps = qps;
		int i = 1;
		for ( QueryParameter qp : qps )
		{
			nameIndexMap.put( qp.name, i++ );
		}
	}

	
	
	public int findInParameter( String parameterName ) throws OdaException
	{
		Integer index = nameIndexMap.get( parameterName );
		if ( index == null )
		{
			throw new OdaException( Messages.getString( "QueryParameters.InexistentParameter", parameterName )); //$NON-NLS-1$
		}
		return index.intValue( );
	}
	
	public IParameterMetaData getParameterMetaData( )
	{
		return new IParameterMetaData( )
		{
			public int getParameterCount( ) throws OdaException
			{
				return qps.length;
			}

			public int getParameterMode( int param ) throws OdaException
			{
				return IParameterMetaData.parameterModeIn;
			}

			public String getParameterName( int param ) throws OdaException
			{
				return qps[param-1].name;
			}

			public int getParameterType( int param ) throws OdaException
			{
				return Driver.getNativeDataTypeCode( getParameterTypeName( param ) );
			}

			public String getParameterTypeName( int param ) throws OdaException
			{
				return qps[param-1].nativeTypeName;
			}

			public int getPrecision( int param ) throws OdaException
			{
				return -1;
			}

			public int getScale( int param ) throws OdaException
			{
				return -1;
			}

			public int isNullable( int param ) throws OdaException
			{
				return IParameterMetaData.parameterNullableUnknown;
			}
		};
	}
	                       
	public static QueryParameters create( PojoQuery pq ) throws OdaException
	{
		ReferenceGraph rg = pq.getReferenceGraph( );
		ColumnReferenceNode[] crns = rg.getColumnReferences( );
		
		//save parameters detected during traversing all columns
		Map<String, String> methodParamNameTypeMap = new HashMap<String, String>( );
		
		List<QueryParameter> paramList = new ArrayList<QueryParameter>( );
		
		for ( ColumnReferenceNode crn : crns )
		{
			Stack<List<QueryParameter>> params = getQueryParameters( crn, methodParamNameTypeMap );
			while ( !params.empty( ) )
			{
				paramList.addAll( params.pop( ) );
			}
		}
		return new QueryParameters( paramList.toArray( new QueryParameter[0] ) );
	}
	
	/**
	 * One element in stack means variable parameter sequence of a method 
	 * @param crn
	 * @param methodParamNameTypeMap
	 * @return
	 * @throws OdaException
	 */
	private static Stack<List<QueryParameter>> getQueryParameters( ColumnReferenceNode crn, Map<String, String> methodParamNameTypeMap ) throws OdaException
	{
		Stack<List<QueryParameter>> params = new Stack<List<QueryParameter>>( );
		ReferenceNode rn = crn;
		while ( rn != null )
		{
			if ( rn.getReference( ) instanceof MethodSource )
			{
				MethodSource ms = (MethodSource)rn.getReference( );
				
				//new parameters in this method
				List<QueryParameter> ps = new ArrayList<QueryParameter>( );
				for ( IMethodParameter mp : ms.getParameters( ) )
				{
					if ( mp instanceof VariableParameter )
					{
						VariableParameter vp = (VariableParameter)mp;
						String detectedType = methodParamNameTypeMap.get( vp.getName( ) );
						if ( detectedType == null )
						{
							//a new named parameter is detected
							methodParamNameTypeMap.put( vp.getName( ), vp.getDataType( ) );
							ps.add( new QueryParameter( vp.getName( ), MethodParameterType.getNativeOdaDataTypeName( vp.getDataType( ) ) ) );
						}
						else if ( !vp.getDataType( ).equals( detectedType ))
						{
							throw new OdaException( Messages.getString( "QueryParameters.OneMethodParameterWith2Types",  //$NON-NLS-1$
									new Object[]{vp.getName( ), vp.getDataType( ), detectedType} ));
						}
					} 
				} //end for
				params.push( ps );
			} //end if
			rn = rn.getParent( );
		}//end while
		return params;
	}
	
	private static class QueryParameter
	{
		private String name;
		private String nativeTypeName;
		
		
		public QueryParameter( String name, String nativeTypeName )
		{
			this.name = name;
			this.nativeTypeName = nativeTypeName;
		}
	}
}
