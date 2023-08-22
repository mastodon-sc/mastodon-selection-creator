/*-
 * #%L
 * mastodon-selection-creator
 * %%
 * Copyright (C) 2018 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.mamut.selectioncreator.plugin;

import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.mastodon.app.ui.ViewMenuBuilder.MenuItem;
import org.mastodon.feature.FeatureModel;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.mamut.KeyConfigScopes;
import org.mastodon.mamut.MamutMenuBuilder;
import org.mastodon.mamut.PreferencesDialog;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.plugin.MamutPlugin;
import org.mastodon.mamut.selectioncreator.SelectionParser;
import org.mastodon.mamut.selectioncreator.plugin.settings.SelectionCreatorConfigPage;
import org.mastodon.mamut.selectioncreator.plugin.settings.SelectionCreatorSettingsManager;
import org.mastodon.model.SelectionModel;
import org.mastodon.model.tag.TagSetModel;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider;
import org.scijava.ui.behaviour.io.gui.CommandDescriptions;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;

@Plugin( type = SelectionParserPlugin.class )
public class SelectionParserPlugin implements MamutPlugin
{

	public static final String[] MENU_PATH = new String[] { "Plugins" };

	private static final String SHOW_SELECTION_CREATOR_WINDOW = "selection-creator dialog";

	private static final String[] SHOW_SELECTION_CREATOR_WINDOW_KEYS = new String[] { "not mapped" };

	private static Map< String, String > menuTexts = new HashMap<>();

	private SelectionParser< Spot, Link > selectionParser;

	private ProjectModel appModel;

	private SelectionCreatorConfigPage page;

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
			super( KeyConfigScopes.MAMUT, KeyConfigContexts.MASTODON );
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
	public void setAppPluginModel( final ProjectModel appModel )
	{
		this.appModel = appModel;

		final Model model = appModel.getModel();
		final ModelGraph graph = model.getGraph();
		final GraphIdBimap< Spot, Link > graphIdBimap = model.getGraphIdBimap();
		final TagSetModel< Spot, Link > tagSetModel = model.getTagSetModel();
		final FeatureModel featureModel = model.getFeatureModel();
		final SelectionModel< Spot, Link > selectionModel = appModel.getSelectionModel();
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
		page = new SelectionCreatorConfigPage( "Selection creator parser", styleManager, evaluator );
		page.apply();

		final PreferencesDialog settings = appModel.getWindowManager().getPreferencesDialog();
		settings.addPage( page );
	}

	private final AbstractNamedAction toggleSelectionCreatorWindowVisibility =
			new AbstractNamedAction( SHOW_SELECTION_CREATOR_WINDOW )
			{

				private static final long serialVersionUID = 1L;

				@Override
				public void actionPerformed( final ActionEvent e )
				{
					if ( appModel == null )
						return;

					final PreferencesDialog dialog = appModel.getWindowManager().getPreferencesDialog();
					if ( null == dialog )
						return;

					dialog.showPage( page.getTreePath() );
					dialog.setVisible( true );
				}
			};
}
