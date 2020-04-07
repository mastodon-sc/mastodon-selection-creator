package org.mastodon.revised.ui.selection.creator.parser.plugin.settings;

import java.util.function.Function;

import javax.swing.JPanel;

import org.mastodon.app.ui.settings.ModificationListener;
import org.mastodon.app.ui.settings.SelectAndEditProfileSettingsPage;
import org.mastodon.app.ui.settings.style.StyleProfile;
import org.mastodon.app.ui.settings.style.StyleProfileManager;
import org.scijava.listeners.Listeners;

public class SelectionCreatorConfigPage extends SelectAndEditProfileSettingsPage< StyleProfile< SelectionCreatorSettings > >
{
	/**
	 * @param treePath
	 *                                            path of this page in the settings
	 *                                            tree.
	 * @param selectionCreatorSettingsManager
	 *                                            the selection expression manager.
	 * @param evaluator
	 *                                            the expression evaluator: takes a
	 *                                            string (the expression) and
	 *                                            returns a message to the user.
	 */
	public SelectionCreatorConfigPage(
			final String treePath,
			final SelectionCreatorSettingsManager selectionCreatorSettingsManager,
			final Function< String, String > evaluator )
	{
		super(
				treePath,
				new StyleProfileManager<>( selectionCreatorSettingsManager, new SelectionCreatorSettingsManager( false ) ),
				new SelectionCreatorSettingsEditPanel( selectionCreatorSettingsManager.getDefaultStyle(), evaluator ) );
	}

	static class SelectionCreatorSettingsEditPanel implements SelectionCreatorSettings.UpdateListener, SelectAndEditProfileSettingsPage.ProfileEditPanel< StyleProfile< SelectionCreatorSettings > >
	{
		private final Listeners.SynchronizedList< ModificationListener > modificationListeners;

		private final SelectionCreatorSettings editedStyle;

		private final SelectionCreatorSettingsPanel styleEditorPanel;

		public SelectionCreatorSettingsEditPanel( final SelectionCreatorSettings initialStyle, final Function< String, String > evaluator )
		{
			editedStyle = initialStyle.copy( "Edited" );
			styleEditorPanel = new SelectionCreatorSettingsPanel( editedStyle, evaluator );
			modificationListeners = new Listeners.SynchronizedList<>();
			editedStyle.updateListeners().add( this );
		}

		private boolean trackModifications = true;

		@Override
		public void settingsChanged()
		{
			if ( trackModifications )
				modificationListeners.list.forEach( ModificationListener::modified );
		}

		@Override
		public void loadProfile( final StyleProfile< SelectionCreatorSettings > profile )
		{
			trackModifications = false;
			editedStyle.set( profile.getStyle() );
			trackModifications = true;
		}

		@Override
		public void storeProfile( final StyleProfile< SelectionCreatorSettings > profile )
		{
			trackModifications = false;
			editedStyle.setName( profile.getStyle().getName() );
			trackModifications = true;
			profile.getStyle().set( editedStyle );
		}

		@Override
		public Listeners< ModificationListener > modificationListeners()
		{
			return modificationListeners;
		}

		@Override
		public JPanel getJPanel()
		{
			return styleEditorPanel;
		}
	}
}
