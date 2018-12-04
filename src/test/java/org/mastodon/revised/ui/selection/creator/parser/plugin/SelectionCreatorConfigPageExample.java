package org.mastodon.revised.ui.selection.creator.parser.plugin;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Locale;

import javax.swing.JDialog;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import org.mastodon.app.ui.settings.SettingsPanel;
import org.mastodon.revised.ui.selection.creator.parser.plugin.settings.SelectionCreatorConfigPage;
import org.mastodon.revised.ui.selection.creator.parser.plugin.settings.SelectionCreatorSettingsManager;

public class SelectionCreatorConfigPageExample
{

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{

		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		Locale.setDefault( Locale.ROOT );
		System.setProperty( "apple.laf.useScreenMenuBar", "true" );

		final SelectionCreatorSettingsManager styleManager = new SelectionCreatorSettingsManager();

		final SettingsPanel settings = new SettingsPanel();
		settings.addPage( new SelectionCreatorConfigPage( "Selection creator parser", styleManager, ( str ) -> str  ) );

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
