package org.mastodon.mamut.selectioncreator.evaluation;

import java.util.BitSet;

import org.mastodon.RefPool;
import org.mastodon.collection.RefCollection;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.graph.Edge;

public class EdgeFeatureVariable< E extends Edge< ? > > extends AbstractFeatureVariable< E >
{

	public EdgeFeatureVariable(
			final FeatureSpec< ?, ? > featureSpec,
			final FeatureProjectionKey projectionKey,
			final FeatureProjection< E > projection,
			final RefCollection< E > collection,
			final RefPool< E > idMap )
	{
		super( featureSpec, projectionKey, projection, collection, idMap );
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
