package org.mastodon.revised.ui.selection.creator.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.ListCellRenderer;

import org.mastodon.revised.model.tag.TagSetStructure;
import org.mastodon.revised.model.tag.TagSetStructure.Tag;
import org.mastodon.revised.model.tag.TagSetStructure.TagSet;
import org.mastodon.revised.ui.selection.creator.FilterItem;
import org.mastodon.util.Listeners;

public class TagSetFilterUI implements FilterItem
{

	private final JPanel panel;

	private final Listeners.List< UpdateListener > listeners = new Listeners.List<>();

	public TagSetFilterUI( final TagSetStructure tagSetStructure )
	{
		this.panel = new JPanel();

		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 5, 5, 5, 5, 0 };
		gridBagLayout.rowHeights = new int[] { 5, 5, 5, 5, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 1.0, 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panel.setLayout( gridBagLayout );

		final JComboBox< TagSet > comboBoxTagSet = new JComboBox<>(
				tagSetStructure.getTagSets().toArray( new TagSet[] {} ) );
		comboBoxTagSet.setRenderer( new MyTagSetRenderer() );

		final GridBagConstraints gbc_comboBoxTagSet = new GridBagConstraints();
		gbc_comboBoxTagSet.insets = new Insets( 5, 5, 5, 5 );
		gbc_comboBoxTagSet.gridwidth = 4;
		gbc_comboBoxTagSet.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBoxTagSet.gridx = 0;
		gbc_comboBoxTagSet.gridy = 0;
		panel.add( comboBoxTagSet, gbc_comboBoxTagSet );

		final JRadioButton rdbtnHasTag = new JRadioButton( "has" );
		final GridBagConstraints gbc_rdbtnHasTag = new GridBagConstraints();
		gbc_rdbtnHasTag.gridwidth = 2;
		gbc_rdbtnHasTag.fill = GridBagConstraints.HORIZONTAL;
		gbc_rdbtnHasTag.insets = new Insets( 5, 5, 5, 5 );
		gbc_rdbtnHasTag.gridx = 0;
		gbc_rdbtnHasTag.gridy = 1;
		panel.add( rdbtnHasTag, gbc_rdbtnHasTag );

		final JRadioButton rdbtnHasNot = new JRadioButton( "has not" );
		final GridBagConstraints gbc_rdbtnHasNot = new GridBagConstraints();
		gbc_rdbtnHasNot.fill = GridBagConstraints.HORIZONTAL;
		gbc_rdbtnHasNot.insets = new Insets( 5, 5, 5, 5 );
		gbc_rdbtnHasNot.gridx = 2;
		gbc_rdbtnHasNot.gridy = 1;
		panel.add( rdbtnHasNot, gbc_rdbtnHasNot );

		final JRadioButton rdbtnUnset = new JRadioButton( "unset" );
		final GridBagConstraints gbc_rdbtnUnset = new GridBagConstraints();
		gbc_rdbtnUnset.insets = new Insets( 5, 5, 5, 5 );
		gbc_rdbtnUnset.fill = GridBagConstraints.HORIZONTAL;
		gbc_rdbtnUnset.gridx = 3;
		gbc_rdbtnUnset.gridy = 1;
		panel.add( rdbtnUnset, gbc_rdbtnUnset );

		final ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add( rdbtnHasTag );
		buttonGroup.add( rdbtnHasNot );
		buttonGroup.add( rdbtnUnset );
		rdbtnHasTag.setSelected( true );

		final JLabel lblTag = new JLabel( "tag:" );
		final GridBagConstraints gbc_lblTag = new GridBagConstraints();
		gbc_lblTag.insets = new Insets( 5, 5, 5, 5 );
		gbc_lblTag.anchor = GridBagConstraints.EAST;
		gbc_lblTag.gridx = 0;
		gbc_lblTag.gridy = 2;
		panel.add( lblTag, gbc_lblTag );

		final JComboBox< Tag > comboBoxTag = new JComboBox<>();
		comboBoxTag.setRenderer( new MyTagRenderer() );

		final GridBagConstraints gbc_comboBoxTag = new GridBagConstraints();
		gbc_comboBoxTag.insets = new Insets( 5, 5, 5, 5 );
		gbc_comboBoxTag.gridwidth = 3;
		gbc_comboBoxTag.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBoxTag.gridx = 1;
		gbc_comboBoxTag.gridy = 2;
		panel.add( comboBoxTag, gbc_comboBoxTag );

		final JLabel lblInfoText = new JLabel( "info text" );
		final GridBagConstraints gbc_lblInfoText = new GridBagConstraints();
		gbc_lblInfoText.anchor = GridBagConstraints.WEST;
		gbc_lblInfoText.gridwidth = 4;
		gbc_lblInfoText.insets = new Insets( 5, 5, 5, 5 );
		gbc_lblInfoText.gridx = 0;
		gbc_lblInfoText.gridy = 3;
		panel.add( lblInfoText, gbc_lblInfoText );

		final ActionListener al =  ( e ) -> {
			final boolean visible = !rdbtnUnset.isSelected();
			comboBoxTag.setVisible( visible );
			lblTag.setVisible( visible );
		};
		rdbtnUnset.addActionListener( al );
		rdbtnHasNot.addActionListener( al );
		rdbtnHasTag.addActionListener( al );
		al.actionPerformed( null );

		final ItemListener il = ( e ) -> {
			final TagSet tagset = ( TagSet ) comboBoxTagSet.getSelectedItem();
			if (null == tagset)
			{
				comboBoxTag.setModel( new DefaultComboBoxModel<>() );
			}
			else
			{
				final List< Tag > tags = new ArrayList<>( tagset.getTags() );
				tags.sort( Comparator.comparing( Tag::label ) );
				comboBoxTag.setModel( new DefaultComboBoxModel<>( tags.toArray( new Tag[] {} ) ) );
			}
		};
		comboBoxTagSet.addItemListener( il );
		il.itemStateChanged( null );
	}

	@Override
	public JPanel getPanel()
	{
		return panel;
	}

	public Listeners< UpdateListener > listeners()
	{
		return listeners;
	}

	private class MyTagSetRenderer implements ListCellRenderer< TagSet >
	{

		private final DefaultListCellRenderer renderer;

		public MyTagSetRenderer()
		{
			this.renderer = new DefaultListCellRenderer();
		}
		@Override
		public Component getListCellRendererComponent( final JList< ? extends TagSet > list, final TagSet tagset, final int index, final boolean isSelected, final boolean hasFocus )
		{
			final JLabel c = ( JLabel ) renderer.getListCellRendererComponent( list, tagset, index, isSelected, hasFocus );
			c.setText( tagset == null ? "" : tagset.getName() );
			return c;
		}
	}

	private class MyTagRenderer implements ListCellRenderer< Tag >
	{

		private final DefaultListCellRenderer renderer;

		public MyTagRenderer()
		{
			this.renderer = new DefaultListCellRenderer();
		}

		@Override
		public Component getListCellRendererComponent( final JList< ? extends Tag > list, final Tag tag, final int index, final boolean isSelected, final boolean hasFocus )
		{
			final JLabel c = ( JLabel ) renderer.getListCellRendererComponent( list, tag, index, isSelected, hasFocus );
			if ( null != tag )
			{
				c.setText( tag.label() );
				c.setIcon( new ColorIcon( new Color( tag.color(), true ) ) );
			}
			else
			{
				c.setIcon( null );
				c.setText( "" );
			}
			return c;
		}
	}

	/**
	 * Adapted from http://stackoverflow.com/a/3072979/230513
	 */
	private static class ColorIcon implements Icon
	{
		private final int size = 16;

		private final int pad = 2;

		private final Color color;

		public ColorIcon( final Color color )
		{
			this.color = color;
		}

		@Override
		public void paintIcon( final Component c, final Graphics g, final int x, final int y )
		{
			final Graphics2D g2d = ( Graphics2D ) g;
			g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
			g2d.setColor( color );
			// g2d.fillOval( x, y, size, size );
			g2d.fill( new RoundRectangle2D.Float( x + pad, y + pad, size, size, 5, 5 ) );
		}

		@Override
		public int getIconWidth()
		{
			return size + 2 * pad;
		}

		@Override
		public int getIconHeight()
		{
			return size + 2 * pad;
		}
	}
}
