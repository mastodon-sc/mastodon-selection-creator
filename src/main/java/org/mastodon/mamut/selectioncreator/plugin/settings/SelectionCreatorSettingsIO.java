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
package org.mastodon.mamut.selectioncreator.plugin.settings;

import java.util.LinkedHashMap;
import java.util.Map;

import org.mastodon.io.yaml.AbstractWorkaroundConstruct;
import org.mastodon.io.yaml.WorkaroundConstructor;
import org.mastodon.io.yaml.WorkaroundRepresent;
import org.mastodon.io.yaml.WorkaroundRepresenter;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

/**
 * Facilities to dump / load {@link SelectionCreatorSettings} to / from a YAML file.
 *
 * @author Jean-Yves Tinevez
 *
 */
public class SelectionCreatorSettingsIO
{
	private static class SelectionCreatorSettingsRepresenter extends WorkaroundRepresenter
	{
		public SelectionCreatorSettingsRepresenter()
		{
			putRepresent( new RepresentSelectionCreatorSettings( this ) );
		}
	}

	private static class SelectionCreatorSettingsConstructor extends WorkaroundConstructor
	{
		public SelectionCreatorSettingsConstructor()
		{
			super( Object.class );
			putConstruct( new ConstructSelectionCreatorSettings( this ) );
		}
	}

	/**
	 * Returns a YAML instance that can dump / load a collection of
	 * {@link SemiAutomaticTrackerSettings} to / from a .yaml file.
	 *
	 * @return a new YAML instance.
	 */
	static Yaml createYaml()
	{
		final DumperOptions dumperOptions = new DumperOptions();
		final Representer representer = new SelectionCreatorSettingsRepresenter();
		final Constructor constructor = new SelectionCreatorSettingsConstructor();
		final Yaml yaml = new Yaml( constructor, representer, dumperOptions );
		return yaml;
	}

	private static final Tag SELECTION_CREATOR_SETTINGS_TAG = new Tag( "!selectioncreatorsettings" );

	private static class RepresentSelectionCreatorSettings extends WorkaroundRepresent
	{
		public RepresentSelectionCreatorSettings( final WorkaroundRepresenter r )
		{
			super( r, SELECTION_CREATOR_SETTINGS_TAG, SelectionCreatorSettings.class );
		}

		@Override
		public Node representData( final Object data )
		{
			final SelectionCreatorSettings s = ( SelectionCreatorSettings ) data;
			final Map< String, Object > mapping = new LinkedHashMap< >();

			mapping.put( "name", s.getName() );
			mapping.put( "expression", s.expression() );
			mapping.put( "description", s.description() );

			final Node node = representMapping( getTag(), mapping, getDefaultFlowStyle() );
			return node;
		}
	}

	private static class ConstructSelectionCreatorSettings extends AbstractWorkaroundConstruct
	{
		public ConstructSelectionCreatorSettings( final WorkaroundConstructor c )
		{
			super( c, SELECTION_CREATOR_SETTINGS_TAG );
		}

		@Override
		public Object construct( final Node node )
		{
			try
			{
				final Map< Object, Object > mapping = constructMapping( ( MappingNode  ) node );
				final String name = ( String ) mapping.get( "name" );
				final SelectionCreatorSettings s = SelectionCreatorSettings.EXAMPLES.get( 0 ).copy( name );

				s.setName( getString( mapping, "name" ) );
				s.setExpression( getString( mapping, "expression" ) );
				s.setDescription( getString( mapping, "description" ) );

				return s;
			}
			catch( final Exception e )
			{
				e.printStackTrace();
			}
			return null;
		}
	}
}
