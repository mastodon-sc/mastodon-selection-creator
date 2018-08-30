package org.mastodon.revised.ui.selection.creator.parser.evaluation;

import java.util.BitSet;

import org.mastodon.RefPool;
import org.mastodon.collection.RefCollection;
import org.mastodon.revised.model.tag.ObjTagMap;
import org.mastodon.revised.model.tag.TagSetStructure.Tag;
import org.mastodon.revised.model.tag.TagSetStructure.TagSet;

public class VertexTagSetVariable< V > extends AbstractTagSetVariable< V >
{

	public VertexTagSetVariable( final TagSet tagSet, final ObjTagMap< V, Tag > tags, final RefCollection< V > collection, final RefPool< V > idMap )
	{
		super( tagSet, tags, collection, idMap );
	}

	@Override
	protected SelectionVariable make( final BitSet bitset )
	{
		return new SelectionVariable( bitset, new BitSet() );
	}

}
