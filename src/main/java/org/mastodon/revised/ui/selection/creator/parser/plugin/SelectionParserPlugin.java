package org.mastodon.revised.ui.selection.creator.parser.plugin;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.swing.JDialog;
import javax.swing.WindowConstants;

import org.mastodon.app.ui.ViewMenuBuilder.MenuItem;
import org.mastodon.app.ui.settings.SettingsPanel;
import org.mastodon.feature.FeatureModel;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.model.SelectionModel;
import org.mastodon.plugin.MastodonPlugin;
import org.mastodon.plugin.MastodonPluginAppModel;
import org.mastodon.revised.mamut.KeyConfigContexts;
import org.mastodon.revised.mamut.MamutMenuBuilder;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.ModelGraph;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.revised.model.tag.TagSetModel;
import org.mastodon.revised.ui.keymap.CommandDescriptionProvider;
import org.mastodon.revised.ui.keymap.CommandDescriptions;
import org.mastodon.revised.ui.selection.creator.parser.SelectionParser;
import org.mastodon.revised.ui.selection.creator.parser.plugin.settings.SelectionCreatorConfigPage;
import org.mastodon.revised.ui.selection.creator.parser.plugin.settings.SelectionCreatorSettingsManager;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;

@Plugin( type = SelectionParserPlugin.class )
public class SelectionParserPlugin implements MastodonPlugin
{

	public static final String[] MENU_PATH = new String[] { "Plugins" };

	private static final String SHOW_SELECTION_CREATOR_WINDOW = "selection-creator dialog";

	private static final String[] SHOW_SELECTION_CREATOR_WINDOW_KEYS = new String[] { "not mapped" };

	private static Map< String, String > menuTexts = new HashMap<>();

	private JDialog dialog;

	private SelectionParser< Spot, Link > selectionParser;

	static
	{
		menuTexts.put( SHOW_SELECTION_CREATOR_WINDOW, "Selection Creator" );
	}

	/*
	 * Command descriptions for all provided commands
	 */
	@Plugin( type = Descriptions.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			super( KeyConfigContexts.MASTODON );
		}

		@Override
		public void getCommandDescriptions( final CommandDescriptions descriptions )
		{
			descriptions.add(
					SHOW_SELECTION_CREATOR_WINDOW,
					SHOW_SELECTION_CREATOR_WINDOW_KEYS,
					"Shows the Selection-creator window." );
		}
	}

	@Override
	public Map< String, String > getMenuTexts()
	{
		return menuTexts;
	}

	@Override
	public List< MenuItem > getMenuItems()
	{
		return Arrays.asList(
				makeFullMenuItem( MamutMenuBuilder.item( SHOW_SELECTION_CREATOR_WINDOW ) ) );
	}

	private static final MenuItem makeFullMenuItem( final MenuItem item )
	{
		MenuItem menuPath = item;
		for ( int i = MENU_PATH.length - 1; i >= 0; i-- )
			menuPath = MamutMenuBuilder.menu( MENU_PATH[ i ], menuPath );
		return menuPath;
	}

	@Override
	public void installGlobalActions( final Actions actions )
	{
		actions.namedAction( toggleSelectionCreatorWindowVisibility, SHOW_SELECTION_CREATOR_WINDOW_KEYS );
	}

	@Override
	public void setAppModel( final MastodonPluginAppModel appModel )
	{
		final Model model = appModel.getAppModel().getModel();
		final ModelGraph graph = model.getGraph();
		final GraphIdBimap< Spot, Link > graphIdBimap = model.getGraphIdBimap();
		final TagSetModel< Spot, Link > tagSetModel = model.getTagSetModel();
		final FeatureModel featureModel = model.getFeatureModel();
		final SelectionModel< Spot, Link > selectionModel = appModel.getAppModel().getSelectionModel();
		selectionParser = new SelectionParser<>( graph, graphIdBimap, tagSetModel, featureModel, selectionModel );

		final Function< String, String > evaluator = ( expression ) -> {
			final boolean ok = selectionParser.parse( expression );
			final String message = ok
					? "Evaluation successful. Selection has now " + selectionModel.getSelectedVertices().size()
							+ " spots and " + selectionModel.getSelectedEdges().size() + " edges."
					: "Evaluation failed. " + selectionParser.getErrorMessage();
			return message;
		};

		final SelectionCreatorSettingsManager styleManager = new SelectionCreatorSettingsManager();
		final SelectionCreatorConfigPage page = new SelectionCreatorConfigPage(
				"Selection creator parser", styleManager, evaluator );
		page.apply();
		final SettingsPanel settings = new SettingsPanel();
		settings.addPage( page );

		dialog = new JDialog( ( Frame ) null, "Mastodon selection creator" );
		dialog.getContentPane().add( settings, BorderLayout.CENTER );
		settings.onOk( () -> dialog.setVisible( false ) );
		settings.onCancel( () -> dialog.setVisible( false ) );

		dialog.setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );
		dialog.addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing( final WindowEvent e )
			{
				settings.cancel();
			}
		} );
		dialog.pack();

	}

	private final AbstractNamedAction toggleSelectionCreatorWindowVisibility =
			new AbstractNamedAction( SHOW_SELECTION_CREATOR_WINDOW )
			{

				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed( final ActionEvent e )
				{
					if ( null == dialog )
						return;
					dialog.setVisible( !dialog.isVisible() );
				}
			};
}
