package org.mastodon.revised.ui.selection.creator.parser.evaluation;

import java.util.BitSet;
import java.util.function.DoublePredicate;

import org.mastodon.RefPool;
import org.mastodon.collection.RefCollection;
import org.mastodon.revised.model.feature.FeatureProjection;

public abstract class AbstractFeatureVariable< O > implements FeatureVariable< O >
{

	protected final FeatureProjection< O > projection;

	protected final RefCollection< O > collection;

	protected final RefPool< O > idMap;

	private final String featureKey;

	private final String projectionKey;

	protected AbstractFeatureVariable( final String featureKey, final String projectionKey, final FeatureProjection< O > projection, final RefCollection< O > collection, final RefPool< O > idMap )
	{
		this.featureKey = featureKey;
		this.projectionKey = projectionKey;
		this.projection = projection;
		this.collection = collection;
		this.idMap = idMap;
	}

	@Override
	public SelectionVariable lessThan( final double threshold )
	{
		return make( test( ( v ) -> v < threshold ) );
	}

	@Override
	public SelectionVariable greaterThan( final double threshold )
	{
		return make( test( ( v ) -> v > threshold ) );
	}

	@Override
	public SelectionVariable lessThanOrEqual( final double threshold )
	{
		return make( test( ( v ) -> v <= threshold ) );
	}

	@Override
	public SelectionVariable greaterThanOrEqual( final double threshold )
	{
		return make( test( ( v ) -> v >= threshold ) );
	}

	@Override
	public SelectionVariable equal( final double value )
	{
		return make( test( ( v ) -> v == value ) );
	}

	@Override
	public SelectionVariable notEqual( final double value )
	{
		return make( test( ( v ) -> v != value ) );
	}

	protected abstract SelectionVariable make( BitSet mainBitSet );

	private BitSet test( final DoublePredicate tester )
	{
		final BitSet target = new BitSet();
		for ( final O v : collection )
			if ( projection.isSet( v ) && tester.test( projection.value( v ) ) )
				target.set( idMap.getId( v ) );
		return target;
	}

	@Override
	public String toString()
	{
		return "Feature( " + featureKey + " \u2192 " + projectionKey + ", " + collection.size() + " )";
	}

}
