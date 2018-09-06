package org.mastodon.revised.ui.selection.creator.parser.plugin;

import java.io.IOException;
import java.util.Locale;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.mastodon.revised.mamut.MainWindow;
import org.mastodon.revised.mamut.MamutProject;
import org.mastodon.revised.mamut.MamutProjectIO;
import org.mastodon.revised.mamut.WindowManager;
import org.scijava.Context;

import mpicbg.spim.data.SpimDataException;

public class SelectionCreatorParserPluginExample
{

	public static void main( final String[] args ) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException, IOException, SpimDataException
	{

		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		Locale.setDefault( Locale.ROOT );
		System.setProperty( "apple.laf.useScreenMenuBar", "true" );

		final String projectPath = "../TrackMate3/samples/mamutproject/";
		final MamutProject project = new MamutProjectIO().load( projectPath );

		final Context context = new Context();
		final WindowManager windowManager = new WindowManager( context );
		final MainWindow mw = new MainWindow( windowManager );
		windowManager.getProjectManager().open( project );
		mw.setVisible( true );
	}
}
