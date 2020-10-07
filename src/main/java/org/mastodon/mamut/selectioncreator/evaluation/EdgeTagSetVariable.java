package org.mastodon.mamut.selectioncreator.evaluation;

import java.util.BitSet;

import org.mastodon.RefPool;
import org.mastodon.collection.RefCollection;
import org.mastodon.model.tag.ObjTagMap;
import org.mastodon.model.tag.TagSetStructure.Tag;
import org.mastodon.model.tag.TagSetStructure.TagSet;

public class EdgeTagSetVariable< E > extends AbstractTagSetVariable< E >
{

	public EdgeTagSetVariable( final TagSet tagSet, final ObjTagMap< E, Tag > tags, final RefCollection< E > collection, final RefPool< E > idMap )
	{
		super( tagSet, tags, collection, idMap );
	}

	@Override
	protected SelectionVariable make( final BitSet bitset )
	{
		return new SelectionVariable( new BitSet(), bitset );
	}
}
