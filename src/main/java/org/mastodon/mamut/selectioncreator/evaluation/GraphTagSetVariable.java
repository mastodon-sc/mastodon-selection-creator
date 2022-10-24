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
package org.mastodon.mamut.selectioncreator.evaluation;

import org.mastodon.collection.RefCollection;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.model.tag.ObjTagMap;
import org.mastodon.model.tag.TagSetStructure.Tag;
import org.mastodon.model.tag.TagSetStructure.TagSet;

public class GraphTagSetVariable< V, E > implements TagSetVariable
{

	private final VertexTagSetVariable< V > vertexTagSetVariable;

	private final EdgeTagSetVariable< E > edgeTagSetVariable;

	public GraphTagSetVariable( final TagSet tagSet, final ObjTagMap< V, Tag > vertexTags, final ObjTagMap< E, Tag > edgeTags, final RefCollection< V > vertices, final RefCollection< E > edges, final GraphIdBimap< V, E > graphIdBimap )
	{
		this.vertexTagSetVariable = new VertexTagSetVariable<>( tagSet, vertexTags, vertices, graphIdBimap.vertexIdBimap() );
		this.edgeTagSetVariable = new EdgeTagSetVariable<>( tagSet, edgeTags, edges, graphIdBimap.edgeIdBimap() );
	}

	@Override
	public TagSet getTagSet()
	{
		return vertexTagSetVariable.getTagSet();
	}

	@Override
	public SelectionVariable equal( final Tag tag )
	{
		final SelectionVariable variable = vertexTagSetVariable.equal( tag );
		variable.inPlaceAdd( edgeTagSetVariable.equal( tag ) );
		return variable;
	}

	@Override
	public SelectionVariable notEqual( final Tag tag )
	{
		final SelectionVariable variable = vertexTagSetVariable.notEqual( tag );
		variable.inPlaceAdd( edgeTagSetVariable.notEqual( tag ) );
		return variable;
	}

	@Override
	public SelectionVariable unset()
	{
		final SelectionVariable variable = vertexTagSetVariable.unset();
		variable.inPlaceAdd( edgeTagSetVariable.unset() );
		return variable;
	}

	@Override
	public SelectionVariable set()
	{
		final SelectionVariable variable = vertexTagSetVariable.set();
		variable.inPlaceAdd( edgeTagSetVariable.set() );
		return variable;
	}
}
