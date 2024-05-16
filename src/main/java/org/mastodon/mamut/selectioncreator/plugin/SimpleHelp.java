/*-
 * #%L
 * mastodon-selection-creator
 * %%
 * Copyright (C) 2018 - 2024 Tobias Pietzsch, Jean-Yves Tinevez
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
