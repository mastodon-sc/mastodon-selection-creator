package org.mastodon.revised.ui.selection.creator.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.mastodon.revised.model.feature.Feature;
import org.mastodon.revised.model.feature.FeatureModel;
import org.mastodon.revised.model.feature.FeatureProjection;
import org.mastodon.revised.ui.selection.creator.FilterItem;
import org.mastodon.revised.ui.selection.creator.util.DataThresholdUI;
import org.scijava.util.DoubleArray;

public class FeatureFilterItem< O > extends JPanel implements FilterItem
{

	private static final long serialVersionUID = 1L;

	private final FeatureModel featureModel;

	private final Class< ? > objectClass;

	public FeatureFilterItem( final FeatureModel featureModel, final Collection< O > objects, final Class< O > objectClass )
	{
		this.featureModel = featureModel;
		this.objectClass = objectClass;
		final List< Feature< ?, ? > > featureSet = new ArrayList<>(
				featureModel.getFeatureSet( objectClass ) );
		featureSet.sort( Comparator.comparing( Feature::getKey ) );

		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0 };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0 };
		setLayout( gridBagLayout );

		final FeatureKeySelector selector = new FeatureKeySelector();
		final GridBagConstraints gbc_selector = new GridBagConstraints();
		gbc_selector.insets = new Insets( 0, 0, 5, 0 );
		gbc_selector.gridwidth = 4;
		gbc_selector.fill = GridBagConstraints.HORIZONTAL;
		gbc_selector.gridx = 0;
		gbc_selector.gridy = 0;
		add( selector, gbc_selector );

		final JPanel panelHistogram = new JPanel();
		panelHistogram.setLayout( new BorderLayout() );
		final GridBagConstraints gbc_panelHistogram = new GridBagConstraints();
		gbc_panelHistogram.anchor = GridBagConstraints.BASELINE_LEADING;
		gbc_panelHistogram.fill = GridBagConstraints.HORIZONTAL;
		gbc_panelHistogram.gridwidth = 4;
		gbc_panelHistogram.insets = new Insets( 0, 0, 0, 5 );
		gbc_panelHistogram.gridx = 0;
		gbc_panelHistogram.gridy = 1;
		add( panelHistogram, gbc_panelHistogram );
		
		selector.listeners.add( (fk, pk) -> {
			new Thread( () -> {
				final double[] data;
				final Feature< ?, ? > feature = featureModel.getFeature( fk );
				if ( null == feature )
				{
					data = new double[] {};
				}
				else
				{
					@SuppressWarnings( "unchecked" )
					final FeatureProjection< O > projection = ( FeatureProjection< O > ) feature.getProjections().get( pk );
					if ( null == projection )
					{
						data = new double[] {};
					}
					else
					{
						final DoubleArray arr = new DoubleArray();
						for ( final O o : objects )
							if ( projection.isSet( o ) )
								arr.addValue( projection.value( o ) );
						data = arr.copyArray();
					}
				}

				final DataThresholdUI histogram = new DataThresholdUI( data );
				SwingUtilities.invokeLater( () -> {
					panelHistogram.removeAll();
					panelHistogram.add( histogram.getPanel(), BorderLayout.CENTER );
					panelHistogram.revalidate();
				} );
			} ).start();
		} );
	}

	private class FeatureKeySelector extends JPanel
	{

		private static final long serialVersionUID = 1L;

		private final JComboBox< String > cb1;

		private final JComboBox< String > cb2;

		private final JLabel arrow;

		private final List< BiConsumer< String, String > > listeners = new ArrayList<>();

		public FeatureKeySelector()
		{
			super( new FlowLayout( FlowLayout.LEADING, 10, 2 ) );
			cb1 = new JComboBox<>( collectKeys( featureModel.getFeatureSet( objectClass ) )
					.toArray( new String[] {} ) );
			cb1.setRenderer( new MyComboBoxRenderer() );
			cb1.addItemListener( ( e ) -> {
				if ( e.getStateChange() == ItemEvent.SELECTED )
				{
					regenCB2();
					notifyListers();
				}
			} );
			add( cb1 );
			arrow = new JLabel( "\u2192" );
			add( arrow );
			cb2 = new JComboBox<>();
			cb2.addItemListener( ( e ) -> {
				if ( e.getStateChange() == ItemEvent.SELECTED )
					notifyListers();
			} );
			add( cb2 );
			regenCB2();
		}

		public void setFeatureKeys( final String c1, final String c2 )
		{
			cb1.setSelectedItem( c1 );
			regenCB2();
			cb2.setSelectedItem( c2 );
		}

		private void regenCB2()
		{
			final String key = ( String ) cb1.getSelectedItem();
			final Feature< ?, ? > feature = featureModel.getFeature( key );
			final boolean visible;
			if (null == feature)
			{
				visible = false;
				cb2.setModel( new DefaultComboBoxModel<>() );
			}
			else
			{
				final List< String > projectionKeys = new ArrayList<>( feature.getProjections().keySet() );
				projectionKeys.sort( null );
				visible = projectionKeys.size() > 1;
				cb2.setModel( new DefaultComboBoxModel<>( projectionKeys.toArray( new String[] {} ) ) );
			}
			cb2.setVisible( visible );
			arrow.setVisible( visible );
		}

		private void notifyListers()
		{
			final String fk = ( String ) cb1.getSelectedItem();
			final String pk = ( String ) cb2.getSelectedItem();
			listeners.forEach( ( bc ) -> bc.accept( fk, pk ) );
		}

		private class MyComboBoxRenderer implements ListCellRenderer< String >
		{

			private final Color disabledBgColor;

			private final Color disabledFgColor;

			private final DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

			public MyComboBoxRenderer()
			{
				setOpaque( true );
				this.disabledBgColor = ( Color ) UIManager.get( "ComboBox.disabledBackground" );
				this.disabledFgColor = ( Color ) UIManager.get( "ComboBox.disabledForeground" );
			}

			@Override
			public Component getListCellRendererComponent( final JList< ? extends String > list, final String value, final int index, final boolean isSelected, final boolean cellHasFocus )
			{
				final JLabel c = ( JLabel ) defaultRenderer.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );
				if ( isSelected )
				{
					c.setBackground( list.getSelectionBackground() );
					c.setForeground( list.getSelectionForeground() );
				}
				else
				{
					if ( !isFeatureKeyIn( featureModel, value, objectClass ) )
					{
						c.setBackground( disabledBgColor );
						c.setForeground( disabledFgColor );
					}
					else
					{
						c.setBackground( list.getBackground() );
						c.setForeground( list.getForeground() );
					}
				}
				c.setText( value );
				return c;
			}
		}
	}

	@Override
	public JPanel getPanel()
	{
		return this;
	}

	private static boolean isFeatureKeyIn( final FeatureModel featureModel, final String featureKey, final Class< ? > clazz )
	{
		return collectKeys( featureModel.getFeatureSet( clazz ) ).contains( featureKey );
	}

	/**
	 * Collects they keys of a feature set. If the feature set is
	 * <code>null</code> then an empty collection is returned.
	 *
	 * @param featureSet
	 *            the feature set.
	 * @return the keys of the feature set.
	 */
	private static Collection< String > collectKeys( final Set< Feature< ?, ? > > featureSet )
	{
		if ( featureSet == null )
			return Collections.emptyList();
		final List< String > keys = featureSet.stream().map( Feature::getKey ).collect( Collectors.toList() );
		keys.sort( null );
		return keys;
	}
}
