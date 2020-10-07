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
