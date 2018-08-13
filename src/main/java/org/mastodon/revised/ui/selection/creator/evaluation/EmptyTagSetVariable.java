package org.mastodon.revised.ui.selection.creator.evaluation;

import java.util.BitSet;

import org.mastodon.revised.model.tag.TagSetStructure.Tag;
import org.mastodon.revised.model.tag.TagSetStructure.TagSet;

public class EmptyTagSetVariable implements TagSetVariable
{

	@Override
	public TagSet getTagSet()
	{
		return null;
	}

	@Override
	public SelectionVariable equal( final Tag tag )
	{
		return new SelectionVariable( new BitSet(), new BitSet() );
	}

	@Override
	public SelectionVariable notEqual( final Tag tag )
	{
		return new SelectionVariable( new BitSet(), new BitSet() );
	}

	@Override
	public SelectionVariable unset()
	{
		return new SelectionVariable( new BitSet(), new BitSet() );
	}

	@Override
	public SelectionVariable set()
	{
		return new SelectionVariable( new BitSet(), new BitSet() );
	}

}
