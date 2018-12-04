package org.mastodon.revised.ui.selection.creator.parser.plugin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

public class SimpleHelp
{

	public static void showHelp(final URL helpURL, final String title)
	{

		final JEditorPane editorPane = new JEditorPane();
		editorPane.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
		editorPane.setEditable( false );

		if ( helpURL != null )
		{
			try
			{
				editorPane.setPage( helpURL );
			}
			catch ( final IOException e )
			{
				System.err.println( "Attempted to read a bad URL: " + helpURL );
				return;
			}
		}
		else
		{
			System.err.println( "Help file is null." );
			return;
		}

		final JScrollPane editorScrollPane = new JScrollPane( editorPane );

		editorScrollPane.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS );
		editorScrollPane.setPreferredSize( new Dimension( 600, 300 ) );
		editorScrollPane.setMinimumSize( new Dimension( 10, 10 ) );

		final JFrame f = new JFrame();
		f.setTitle( "Help for " + title );
		f.setSize( 600, 400 );
		f.setLocationRelativeTo( null );
		f.getContentPane().add( editorScrollPane, BorderLayout.CENTER );
		f.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		f.setResizable( true );
		f.setVisible( true );
	}
}
