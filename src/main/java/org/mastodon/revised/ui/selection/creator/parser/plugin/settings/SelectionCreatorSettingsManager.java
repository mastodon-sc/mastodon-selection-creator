package org.mastodon.revised.ui.selection.creator.parser.plugin.settings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.mastodon.app.ui.settings.style.AbstractStyleManager;
import org.yaml.snakeyaml.Yaml;

public class SelectionCreatorSettingsManager extends AbstractStyleManager< SelectionCreatorSettingsManager, SelectionCreatorSettings >
{

	private static final String EXPRESSION_FILE = System.getProperty( "user.home" ) + "/.mastodon/selectioncreatorexpressions.yaml";

	/**
	 * A {@code SelectionCreatorSettings} that has the same properties as the default
	 * RenderSettings. In contrast to defaultStyle this will always
	 * refer to the same object, so a consumers can just use this one
	 * SelectionCreatorSettings to listen for changes and for painting.
	 */
	private final SelectionCreatorSettings forwardDefaultStyle;

	private final SelectionCreatorSettings.UpdateListener updateForwardDefaultListeners;

	public SelectionCreatorSettingsManager()
	{
		this( true );
	}

	public SelectionCreatorSettingsManager( final boolean loadStyles )
	{
		forwardDefaultStyle = SelectionCreatorSettings.EXAMPLES.get( 0 ).copy();
		updateForwardDefaultListeners = () -> forwardDefaultStyle.set( defaultStyle );
		defaultStyle.updateListeners().add( updateForwardDefaultListeners );
		if ( loadStyles )
			loadStyles();
	}

	@Override
	public synchronized void setDefaultStyle( final SelectionCreatorSettings settings )
	{
		defaultStyle.updateListeners().remove( updateForwardDefaultListeners );
		defaultStyle = settings;
		forwardDefaultStyle.set( defaultStyle );
		defaultStyle.updateListeners().add( updateForwardDefaultListeners );
	}

	/**
	 * Returns a final {@link SelectionCreatorSettings} instance that always has the same
	 * properties as the default style.
	 */
	public SelectionCreatorSettings getForwardDefaultStyle()
	{
		return forwardDefaultStyle;
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
			final Yaml yaml = new Yaml();
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
			setDefaultStyle( styleForName( defaultStyleName ).orElseGet( () -> builtinStyles.get( 0 ) ) );
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
			mkdirs( filename );
			final FileWriter output = new FileWriter( filename );
			final Yaml yaml = new Yaml();
			final ArrayList< Object > objects = new ArrayList<>();
			objects.add( defaultStyle.getName() );
			objects.addAll( userStyles );
			yaml.dumpAll( objects.iterator(), output );
			output.close();
		}
		catch ( final IOException e )
		{
			e.printStackTrace();
		}
	}

	/*
	 * STATIC UTILITIES
	 */

	private static boolean mkdirs( final String fileName )
	{
		final File dir = new File( fileName ).getParentFile();
		return dir == null ? false : dir.mkdirs();
	}

}
