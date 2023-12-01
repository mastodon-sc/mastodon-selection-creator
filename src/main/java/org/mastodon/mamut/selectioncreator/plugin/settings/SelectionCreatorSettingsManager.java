/*-
 * #%L
 * mastodon-selection-creator
 * %%
 * Copyright (C) 2018 - 2023 Tobias Pietzsch, Jean-Yves Tinevez
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

import java.util.Collections;
import java.util.List;

import org.mastodon.app.ui.AbstractStyleManagerYaml;
import org.yaml.snakeyaml.Yaml;

public class SelectionCreatorSettingsManager extends AbstractStyleManagerYaml< SelectionCreatorSettingsManager, SelectionCreatorSettings >
{

	private static final String EXPRESSION_FILE = System.getProperty( "user.home" ) + "/.mastodon/plugins/selection-creator/selection-creator-expressions.yaml";
	private static final String LEGACY_EXPRESSION_FILE = System.getProperty( "user.home" ) + "/.mastodon/selectioncreatorexpressions.yaml";

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
		handleLegacyFile( LEGACY_EXPRESSION_FILE );
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

	@Override
	protected Yaml createYaml()
	{
		return SelectionCreatorSettingsIO.createYaml();
	}
}
