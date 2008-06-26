/*******************************************************************************
 * Copyright (c) 2007 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/

package org.eclipse.birt.report.designer.internal.ui.views.attributes.page;

import org.eclipse.birt.report.designer.internal.ui.views.attributes.provider.IDescriptorProvider;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.provider.TextPropertyDescriptorProvider;
import org.eclipse.birt.report.designer.internal.ui.views.attributes.section.TextSection;
import org.eclipse.birt.report.model.api.ParameterGroupHandle;
import org.eclipse.birt.report.model.api.elements.ReportDesignConstants;

/**
 * 
 */

public class ParameterGroupPage extends GeneralPage
{

	/* (non-Javadoc)
	 * @see org.eclipse.birt.report.designer.internal.ui.views.attributes.page.GeneralPage#buildContent()
	 */
	@Override
	void buildContent( )
	{
		// TODO Auto-generated method stub
		
		IDescriptorProvider nameProvider = new TextPropertyDescriptorProvider( ParameterGroupHandle.NAME_PROP,
				ReportDesignConstants.PARAMETER_GROUP_ELEMENT );

		TextSection nameSection = new TextSection( nameProvider.getDisplayName( ),
				container,
				true );
		nameSection.setProvider( nameProvider );
		nameSection.setLayoutNum( 2 );
		nameSection.setWidth( 200 );
		nameSection.setGridPlaceholder( 4, true );
		addSection( PageSectionId.PARAMTER_GROUP_NAME, nameSection );
		
		IDescriptorProvider displayNameProvider = new TextPropertyDescriptorProvider( ParameterGroupHandle.DISPLAY_NAME_PROP,
				ReportDesignConstants.PARAMETER_GROUP_ELEMENT );

		TextSection displayNameSection = new TextSection( displayNameProvider.getDisplayName( ),
				container,
				true );
		displayNameSection.setProvider( displayNameProvider );
		displayNameSection.setLayoutNum( 2 );
		displayNameSection.setWidth( 200 );
		displayNameSection.setGridPlaceholder( 2, true );
		addSection( PageSectionId.PARAMTER_GROUP_DISPLAY_NAME, displayNameSection );		
		
		
		createSections( );
		layoutSections( );
	}

}
