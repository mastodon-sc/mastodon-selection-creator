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
package org.mastodon.mamut.selectioncreator.plugin.settings;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.mastodon.io.IOUtils;
import org.yaml.snakeyaml.Yaml;

import bdv.ui.settings.style.AbstractStyleManager;

public class SelectionCreatorSettingsManager extends AbstractStyleManager< SelectionCreatorSettingsManager, SelectionCreatorSettings >
{

	private static final String EXPRESSION_FILE = System.getProperty( "user.home" ) + "/.mastodon/selectioncreatorexpressions.yaml";

	public SelectionCreatorSettingsManager()
	{
		this( true );
	}

	public SelectionCreatorSettingsManager( final boolean loadStyles )
	{
		if ( loadStyles )
			loadStyles();
	}

	public void loadStyles()
	{
		loadStyles( EXPRESSION_FILE );
	}

	public void loadStyles( final String filename )
	{
		userStyles.clear();
		final Set< String > names = builtinStyles.stream().map( SelectionCreatorSettings::getName ).collect( Collectors.toSet() );
		try
		{
			final FileReader input = new FileReader( filename );
			final Yaml yaml = SelectionCreatorSettingsIO.createYaml();
			final Iterable< Object > objs = yaml.loadAll( input );
			String defaultStyleName = null;
			for ( final Object obj : objs )
			{
				if ( obj instanceof String )
				{
					defaultStyleName = ( String ) obj;
				}
				else if ( obj instanceof SelectionCreatorSettings )
				{
					final SelectionCreatorSettings scs = ( SelectionCreatorSettings ) obj;
					if ( null != scs )
					{
						// sanity check: style names must be unique
						if ( names.add( scs.getName() ) )
							userStyles.add( scs );
						else
							System.out.println( "Discarded style with duplicate name \"" + scs.getName() + "\"." );
					}
				}
			}
			setSelectedStyle( styleForName( defaultStyleName ).orElseGet( () -> builtinStyles.get( 0 ) ) );
		}
		catch ( final FileNotFoundException e )
		{
			System.out.println( "SelectionCreatorSettings style file " + filename + " not found. Using builtin styles." );
		}
	}

	@Override
	protected List< SelectionCreatorSettings > loadBuiltinStyles()
	{
		return Collections.unmodifiableList( SelectionCreatorSettings.EXAMPLES );
	}

	@Override
	public void saveStyles()
	{
		saveStyles( EXPRESSION_FILE );
	}

	public void saveStyles( final String filename )
	{
		try
		{
			IOUtils.mkdirs( filename );
			final FileWriter output = new FileWriter( filename );
			final Yaml yaml = SelectionCreatorSettingsIO.createYaml();
			final ArrayList< Object > objects = new ArrayList<>();
			objects.add( selectedStyle.getName() );
			objects.addAll( userStyles );
			yaml.dumpAll( objects.iterator(), output );
			output.close();
		}
		catch ( final IOException e )
		{
			e.printStackTrace();
		}
	}
}
