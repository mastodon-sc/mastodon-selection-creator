package org.mastodon.mamut.selectioncreator.evaluation;

import java.util.BitSet;

import org.mastodon.RefPool;
import org.mastodon.collection.RefCollection;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.graph.Vertex;

public class VertexFeatureVariable< V extends Vertex< ? > > extends AbstractFeatureVariable< V >
{

	public VertexFeatureVariable(
			final FeatureSpec< ?, ? > featureKey,
			final FeatureProjectionKey projectionKey,
			final FeatureProjection< V > projection,
			final RefCollection< V > collection,
			final RefPool< V > idMap )
	{
		super( featureKey, projectionKey, projection, collection, idMap );
	}

	@Override
	protected SelectionVariable make( final BitSet mainBitSet )
	{
		return new SelectionVariable( mainBitSet, new BitSet() );
	}

	@Override
	public String toString()
	{
		return "Vertex" + super.toString();
	}
}
