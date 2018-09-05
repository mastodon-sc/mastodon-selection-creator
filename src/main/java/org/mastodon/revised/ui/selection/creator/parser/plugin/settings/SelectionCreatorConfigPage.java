package org.mastodon.revised.ui.selection.creator.parser.plugin.settings;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Locale;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import org.mastodon.app.ui.settings.ModificationListener;
import org.mastodon.app.ui.settings.SelectAndEditProfileSettingsPage;
import org.mastodon.app.ui.settings.SettingsPanel;
import org.mastodon.app.ui.settings.style.StyleProfile;
import org.mastodon.app.ui.settings.style.StyleProfileManager;
import org.mastodon.util.Listeners;

public class SelectionCreatorConfigPage extends SelectAndEditProfileSettingsPage< StyleProfile< SelectionCreatorSettings > >
{
	/**
	 * @param treePath
	 * 		path of this page in the settings tree.
	 */
	public SelectionCreatorConfigPage( final String treePath, final SelectionCreatorSettingsManager selectionCreatorSettingsManager )
	{
		super(
				treePath,
				new StyleProfileManager<>( selectionCreatorSettingsManager, new SelectionCreatorSettingsManager( false ) ),
				new SelectionCreatorSettingsEditPanel( selectionCreatorSettingsManager.getDefaultStyle() ) );
	}

	static class SelectionCreatorSettingsEditPanel implements SelectionCreatorSettings.UpdateListener, SelectAndEditProfileSettingsPage.ProfileEditPanel< StyleProfile< SelectionCreatorSettings > >
	{
		private final Listeners.SynchronizedList< ModificationListener > modificationListeners;

		private final SelectionCreatorSettings editedStyle;

		private final SelectionCreatorSettingsPanel styleEditorPanel;

		public SelectionCreatorSettingsEditPanel( final SelectionCreatorSettings initialStyle )
		{
			editedStyle = initialStyle.copy( "Edited" );
			styleEditorPanel = new SelectionCreatorSettingsPanel( editedStyle );
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

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{

		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		Locale.setDefault( Locale.ROOT );
		System.setProperty( "apple.laf.useScreenMenuBar", "true" );

		final SelectionCreatorSettingsManager styleManager = new SelectionCreatorSettingsManager();

		final SettingsPanel settings = new SettingsPanel();
		settings.addPage( new SelectionCreatorConfigPage( "Selection creator parser", styleManager ) );

		final JDialog dialog = new JDialog( ( Frame ) null, "Settings" );
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
		dialog.setVisible( true );
	}
}
