package org.mastodon.revised.ui.selection.creator.evaluation;

import java.util.BitSet;

import org.mastodon.RefPool;
import org.mastodon.collection.RefCollection;
import org.mastodon.graph.Vertex;
import org.mastodon.revised.model.feature.FeatureProjection;

public class VertexFeatureVariable< V extends Vertex< ? > > extends AbstractFeatureVariable< V >
{

	public VertexFeatureVariable( final String featureKey, final String projectionKey, final FeatureProjection< V > projection, final RefCollection< V > collection, final RefPool< V > idMap )
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
