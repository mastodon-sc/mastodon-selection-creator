package org.mastodon.revised.ui.selection.creator.components;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;

import org.mastodon.graph.Edge;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.revised.model.feature.FeatureModel;
import org.mastodon.revised.model.tag.TagSetModel;
import org.mastodon.revised.ui.selection.creator.FilterItem;

public class FilterPanel< V extends Vertex< E >, E extends Edge< V > > extends JPanel
{

	private static final long serialVersionUID = 1L;

	private final ReadOnlyGraph< V, E > graph;

	private final TagSetModel< V, E > tagSetModel;

	private final FeatureModel featureModel;

	private final Class< V > vertexClass;

	private final Class< E > edgeClass;

	private final JPanel panelItems;

	private final JPanel panelAddRemoveButtons;

	private final Component verticalGlue;

	private FilterOnType currentFilterOnType;

	public FilterPanel(
			final ReadOnlyGraph< V, E > graph,
			final TagSetModel< V, E > tagSetModel,
			final FeatureModel featureModel,
			final Class< V > vertexClass,
			final Class< E > edgeClass )
	{
		this.graph = graph;
		this.tagSetModel = tagSetModel;
		this.featureModel = featureModel;
		this.vertexClass = vertexClass;
		this.edgeClass = edgeClass;
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE };
		setLayout( gridBagLayout );

		final JLabel lblBasedOn = new JLabel( "Filtering:" );
		final GridBagConstraints gbc_lblBasedOn = new GridBagConstraints();
		gbc_lblBasedOn.anchor = GridBagConstraints.EAST;
		gbc_lblBasedOn.insets = new Insets( 5, 5, 5, 5 );
		gbc_lblBasedOn.gridx = 0;
		gbc_lblBasedOn.gridy = 0;
		add( lblBasedOn, gbc_lblBasedOn );

		final JPanel panelType = new JPanel();
		final FlowLayout flowLayout = ( FlowLayout ) panelType.getLayout();
		flowLayout.setAlignment( FlowLayout.LEFT );
		final GridBagConstraints gbc_panelType = new GridBagConstraints();
		gbc_panelType.insets = new Insets( 5, 5, 5, 0 );
		gbc_panelType.fill = GridBagConstraints.BOTH;
		gbc_panelType.gridx = 1;
		gbc_panelType.gridy = 0;
		add( panelType, gbc_panelType );
		final JRadioButton rdbtnVertices = new JRadioButton( "vertices" );
		final JRadioButton rdbtnEdges = new JRadioButton( "edges" );
		panelType.add( rdbtnVertices );
		panelType.add( rdbtnEdges );
		final ButtonGroup typeButtonGroup = new ButtonGroup();
		typeButtonGroup.add( rdbtnVertices );
		typeButtonGroup.add( rdbtnEdges );
		rdbtnVertices.addActionListener( ( e ) -> refresh( FilterOnType.VERTICES ) );
		rdbtnEdges.addActionListener( ( e ) -> refresh( FilterOnType.EDGES ) );

		panelItems = new JPanel();
		final GridBagConstraints gbc_panelSelect = new GridBagConstraints();
		gbc_panelSelect.anchor = GridBagConstraints.NORTH;
		gbc_panelSelect.gridwidth = 2;
		gbc_panelSelect.fill = GridBagConstraints.HORIZONTAL;
		gbc_panelSelect.gridx = 0;
		gbc_panelSelect.gridy = 1;
		gbc_panelSelect.insets = new Insets( 5, 5, 5, 0 );
		add( panelItems, gbc_panelSelect );
		panelItems.setLayout( new BoxLayout( panelItems, BoxLayout.PAGE_AXIS ) );
		this.verticalGlue = Box.createVerticalGlue();
		panelItems.add( verticalGlue );

		this.panelAddRemoveButtons = new JPanel();
		final GridBagConstraints gbc_panelAddRemoveButtons = new GridBagConstraints();
		gbc_panelAddRemoveButtons.anchor = GridBagConstraints.EAST;
		gbc_panelAddRemoveButtons.gridwidth = 2;
		gbc_panelAddRemoveButtons.insets = new Insets( 0, 0, 5, 5 );
		gbc_panelAddRemoveButtons.gridx = 0;
		gbc_panelAddRemoveButtons.gridy = 2;
		add( panelAddRemoveButtons, gbc_panelAddRemoveButtons );
		final FlowLayout fl_panelAddRemoveButtons = ( FlowLayout ) panelAddRemoveButtons.getLayout();
		fl_panelAddRemoveButtons.setAlignment( FlowLayout.RIGHT );

		final JButton buttonRemoveItem = new JButton( "-" );
		panelAddRemoveButtons.add( buttonRemoveItem );
		final JButton buttonAddItem = new JButton( "+" );
		panelAddRemoveButtons.add( buttonAddItem );

		final JLabel lblThenSelect = new JLabel( "Then select:" );
		final GridBagConstraints gbc_lblThenSelect = new GridBagConstraints();
		gbc_lblThenSelect.insets = new Insets( 0, 0, 0, 5 );
		gbc_lblThenSelect.gridx = 0;
		gbc_lblThenSelect.gridy = 3;
		add( lblThenSelect, gbc_lblThenSelect );

		final JPanel panelSelect = new JPanel();
		final GridBagConstraints gbc_panelSelect2 = new GridBagConstraints();
		gbc_panelSelect2.fill = GridBagConstraints.BOTH;
		gbc_panelSelect2.gridx = 1;
		gbc_panelSelect2.gridy = 3;
		add( panelSelect, gbc_panelSelect2 );

	}

	private void refresh( final FilterOnType newFilterOnType )
	{
		if ( currentFilterOnType == newFilterOnType )
			return;

		currentFilterOnType = newFilterOnType;
		panelItems.removeAll();
		// TODO remove all in model.


		panelItems.revalidate();
	}

	private void addItem( final ItemType itemType )
	{
		panelItems.remove( verticalGlue );
		panelItems.remove( panelAddRemoveButtons );

		final FilterItem item;
		switch ( itemType )
		{
		case TAG_SET:
			item = new TagSetFilterUI( tagSetModel.getTagSetStructure() );
			break;
		case SPOT_FEATURE:
			item = new FeatureFilterItem< V >( featureModel, graph.vertices(), vertexClass );
			break;
		case LINK_FEATURE:
		default:
			item = new FeatureFilterItem< E >( featureModel, graph.edges(), edgeClass );
			break;
		}

		final JPanel panelItem = new JPanel();
		panelItem.setLayout( new BoxLayout( panelItem, BoxLayout.PAGE_AXIS ) );
		panelItem.add( item.getPanel() );
		panelItem.add( new JSeparator() );
		panelItem.add( Box.createVerticalStrut( 5 ) );
		panelItem.add( panelAddRemoveButtons );

		panelItems.add( panelItem );
		panelItems.add( verticalGlue );
		panelItems.revalidate();
	}

	private static enum ItemType
	{
		TAG_SET,
		SPOT_FEATURE,
		LINK_FEATURE;
	}

	private static enum FilterOnType
	{
		VERTICES, EDGES;
	}
}
