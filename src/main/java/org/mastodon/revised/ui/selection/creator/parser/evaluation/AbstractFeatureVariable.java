package org.mastodon.revised.ui.selection.creator.parser.evaluation;

import java.util.BitSet;
import java.util.function.DoublePredicate;

import org.mastodon.RefPool;
import org.mastodon.collection.RefCollection;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureSpec;

public abstract class AbstractFeatureVariable< O > implements FeatureVariable< O >
{

	protected final FeatureProjection< O > projection;

	protected final RefCollection< O > collection;

	protected final RefPool< O > idMap;

	private final FeatureSpec< ?, ? > featureSpec;

	private final FeatureProjectionKey projectionKey;

	protected AbstractFeatureVariable(
			final FeatureSpec< ?, ? > featureSpec,
			final FeatureProjectionKey projectionKey,
			final FeatureProjection< O > projection,
			final RefCollection< O > collection,
			final RefPool< O > idMap )
	{
		this.featureSpec = featureSpec;
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
		return "Feature( " + featureSpec.getKey() + " \u2192 " + projectionKey + ", " + collection.size() + " )";
	}
}
