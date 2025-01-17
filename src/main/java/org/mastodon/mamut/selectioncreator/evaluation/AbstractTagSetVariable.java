/*-
 * #%L
 * mastodon-selection-creator
 * %%
 * Copyright (C) 2018 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
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

import java.util.BitSet;
import java.util.Collection;

import org.mastodon.RefPool;
import org.mastodon.collection.RefCollection;
import org.mastodon.model.tag.ObjTagMap;
import org.mastodon.model.tag.TagSetStructure.Tag;
import org.mastodon.model.tag.TagSetStructure.TagSet;

public abstract class AbstractTagSetVariable< V > implements TagSetVariable
{

	private final ObjTagMap< V, Tag > tags;

	private final RefCollection< V > collection;

	private final RefPool< V > idMap;

	private final TagSet tagSet;

	public AbstractTagSetVariable( final TagSet tagSet, final ObjTagMap< V, Tag > tags, final RefCollection< V > collection, final RefPool< V > idMap )
	{
		this.tagSet = tagSet;
		this.tags = tags;
		this.collection = collection;
		this.idMap = idMap;
	}

	@Override
	public TagSet getTagSet()
	{
		return tagSet;
	}

	@Override
	public SelectionVariable equal( final Tag tag )
	{
		final Collection< V > tagged = tags.getTaggedWith( tag );
		final BitSet bitset = new BitSet();
		for ( final V v : tagged )
			bitset.set( idMap.getId( v ) );
		return make( bitset );
	}

	@Override
	public SelectionVariable notEqual( final Tag tag )
	{
		final BitSet bitset = new BitSet();
		for ( final V v : collection )
			if ( !tag.equals( tags.get( v ) ) )
				bitset.set( idMap.getId( v ) );
		return make( bitset );
	}

	@Override
	public SelectionVariable unset()
	{
		final BitSet bitset = new BitSet();
		for ( final V v : collection )
			if ( null == tags.get( v ) )
				bitset.set( idMap.getId( v ) );
		return make( bitset );
	}

	@Override
	public SelectionVariable set()
	{
		final BitSet bitset = new BitSet();
		for ( final V v : collection )
			if ( null != tags.get( v ) )
				bitset.set( idMap.getId( v ) );
		return make( bitset );
	}

	protected abstract SelectionVariable make( BitSet bitset );
}
