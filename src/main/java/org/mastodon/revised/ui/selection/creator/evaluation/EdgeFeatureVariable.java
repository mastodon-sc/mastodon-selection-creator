package org.mastodon.revised.ui.selection.creator.evaluation;

import java.util.BitSet;

import org.mastodon.RefPool;
import org.mastodon.collection.RefCollection;
import org.mastodon.graph.Edge;
import org.mastodon.revised.model.feature.FeatureProjection;

public class EdgeFeatureVariable< E extends Edge< ? > > extends AbstractFeatureVariable< E >
{

	public EdgeFeatureVariable( final String featureKey, final String projectionKey, final FeatureProjection< E > projection, final RefCollection< E > collection, final RefPool< E > idMap )
	{
		super( featureKey, projectionKey, projection, collection, idMap );
	}

	@Override
	protected SelectionVariable make( final BitSet mainBitSet )
	{
		return new SelectionVariable( new BitSet(), mainBitSet );
	}

	@Override
	public String toString()
	{
		return "Edge" + super.toString();
	}

}
