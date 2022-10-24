/*-
 * #%L
 * mastodon-selection-creator
 * %%
 * Copyright (C) 2018 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.mamut.selectioncreator.evaluation;

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
