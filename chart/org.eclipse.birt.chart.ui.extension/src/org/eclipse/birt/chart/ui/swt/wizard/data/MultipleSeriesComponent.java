/*******************************************************************************
 * Copyright (c) 2004, 2007 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/

package org.eclipse.birt.chart.ui.swt.wizard.data;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.birt.chart.model.attribute.SortOption;
import org.eclipse.birt.chart.model.data.DataPackage;
import org.eclipse.birt.chart.model.data.SeriesDefinition;
import org.eclipse.birt.chart.ui.extension.i18n.Messages;
import org.eclipse.birt.chart.ui.swt.DefaultSelectDataComponent;
import org.eclipse.birt.chart.ui.swt.interfaces.ISelectDataComponent;
import org.eclipse.birt.chart.ui.swt.interfaces.ISelectDataCustomizeUI;
import org.eclipse.birt.chart.ui.swt.wizard.ChartAdapter;
import org.eclipse.birt.chart.ui.swt.wizard.ChartWizardContext;
import org.eclipse.birt.chart.ui.util.ChartUIConstants;
import org.eclipse.birt.chart.ui.util.ChartUIUtil;
import org.eclipse.birt.chart.ui.util.UIHelper;
import org.eclipse.emf.common.util.EList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

/**
 * This UI component is made up of data text fields for grouping series of each
 * axis.
 */

public class MultipleSeriesComponent extends DefaultSelectDataComponent
{

	private EList[] seriesDefnsArray;

	private ChartWizardContext context = null;

	private String sTitle = null;

	private static final String LABEL_GROUPING_YSERIES = Messages.getString( "MultipleSeriesComponent.Label.OptionalYSeriesGrouping" ); //$NON-NLS-1$
	private static final String LABEL_GROUPING_OVERLAY = Messages.getString( "MultipleSeriesComponent.Label.OptionalOverlayGrouping" ); //$NON-NLS-1$
	private static final String LABEL_GROUPING_WITHOUTAXIS = Messages.getString( "MultipleSeriesComponent.Label.OptionalGrouping" ); //$NON-NLS-1$

	private ISelectDataCustomizeUI selectDataUI = null;

	private ArrayList components = new ArrayList( );

	private boolean isSingle = false;

	// THIS FLAG TO INDICATE ONLY FIRST SERIES GROUPING IS VALID. CHART ENGINE
	// NOT SUPPORT MULIPLE GROUPING.
	private boolean useFirstOnly = true;

	public MultipleSeriesComponent( EList[] seriesDefnsArray,
			ChartWizardContext context, String sTitle,
			ISelectDataCustomizeUI selectDataUI )
	{
		super( );
		this.seriesDefnsArray = seriesDefnsArray;
		this.context = context;
		this.sTitle = sTitle;
		this.selectDataUI = selectDataUI;
	}
	
	public MultipleSeriesComponent( EList seriesDefns,
			ChartWizardContext context, String sTitle,
			ISelectDataCustomizeUI selectDataUI )
	{
		this( new EList[]{
			seriesDefns
		}, context, sTitle, selectDataUI );
		isSingle = true;
	}

	public Composite createArea( Composite parent )
	{
		Composite cmp = new Composite( parent, SWT.NONE );
		{
			GridLayout gridLayout = new GridLayout( );
			gridLayout.marginWidth = 0;
			gridLayout.marginHeight = 0;
			cmp.setLayout( gridLayout );
			cmp.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
		}

		Label topAngle = new Label( cmp, SWT.NONE );
		{
			topAngle.setImage( UIHelper.getImage( ChartUIConstants.IMAGE_RA_TOPLEFT ) );
		}

		for ( int i = 0; i < seriesDefnsArray.length; i++ )
		{
			createRightGroupArea( cmp, i, seriesDefnsArray[i] );
			if ( useFirstOnly )
			{
				break;
			}
		}

		Label bottomAngle = new Label( cmp, SWT.NONE );
		{
			bottomAngle.setImage( UIHelper.getImage( ChartUIConstants.IMAGE_RA_BOTTOMLEFT ) );
		}

		return cmp;
	}

	private void createRightGroupArea( Composite parent, final int axisIndex,
			final EList seriesDefn )
	{
		final String strDesc = getGroupingDescription( axisIndex );
		ISelectDataComponent subUIGroupY = new DefaultSelectDataComponent( ) {

			public Composite createArea( Composite parent )
			{
				Composite cmpGroup = new Composite( parent, SWT.NONE );
				GridLayout glContent = new GridLayout( );
				glContent.marginHeight = 0;
				glContent.marginWidth = 0;
				glContent.horizontalSpacing = 2;
				cmpGroup.setLayout( glContent );
				cmpGroup.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );

				Label lblRightYGrouping = new Label( cmpGroup, SWT.WRAP );
				{
					GridData gd = new GridData( GridData.FILL_HORIZONTAL );
					lblRightYGrouping.setLayoutData( gd );
					lblRightYGrouping.setText( strDesc );
				}

				int selectedSeriesIndex = 0;
				if ( !useFirstOnly )
				{
					selectedSeriesIndex = selectDataUI.getSeriesIndex( )[axisIndex];
				}

				final SeriesDefinition sd = ( (SeriesDefinition) seriesDefn.get( selectedSeriesIndex ) );

				if ( seriesDefn != null && !seriesDefn.isEmpty( ) )
				{
					// Only display current selected series
					ISelectDataComponent subUI = selectDataUI.getAreaComponent( ISelectDataCustomizeUI.GROUPING_SERIES,
							sd,
							context,
							sTitle );
					subUI.addListener( new Listener( ) {

						public void handleEvent( Event event )
						{
							final String query = event.text;
							// Copy the group query to other query definitions.
							ChartAdapter.beginIgnoreNotifications( );
							ChartUIUtil.setAllGroupingQueryExceptFirst( context.getModel( ),
									query );
							ChartAdapter.endIgnoreNotifications( );

						}
					} );
					subUI.createArea( cmpGroup );
					components.add( subUI );
				}

				// Set ascending if group is unsorted
				if ( !sd.eIsSet( DataPackage.eINSTANCE.getSeriesDefinition_Sorting( ) ) )
				{
					initSorting( );
				}
				return cmpGroup;
			}

		};
		subUIGroupY.createArea( parent );
		components.add( subUIGroupY );
	}

	/**
	 * Set ascending for all series definitions
	 * 
	 */
	private void initSorting( )
	{
		ChartAdapter.beginIgnoreNotifications( );
		List sds = ChartUIUtil.getAllOrthogonalSeriesDefinitions( context.getModel( ) );
		for ( int i = 0; i < sds.size( ); i++ )
		{
			SeriesDefinition sdf = (SeriesDefinition) sds.get( i );
			sdf.setSorting( SortOption.ASCENDING_LITERAL );
		}
		ChartAdapter.endIgnoreNotifications( );
	}

	public void selectArea( boolean selected, Object data )
	{
		for ( int i = 0; i < components.size( ); i++ )
		{
			( (ISelectDataComponent) components.get( i ) ).selectArea( selected,
					data );
		}
	}

	public void dispose( )
	{
		for ( int i = 0; i < components.size( ); i++ )
		{
			( (ISelectDataComponent) components.get( i ) ).dispose( );
		}
		super.dispose( );
	}

	private String getGroupingDescription( int axisIndex )
	{
		if ( isSingle )
		{
			return LABEL_GROUPING_WITHOUTAXIS;
		}
		if ( axisIndex == 0 )
		{
			return LABEL_GROUPING_YSERIES;
		}
		return LABEL_GROUPING_OVERLAY;
	}
}
