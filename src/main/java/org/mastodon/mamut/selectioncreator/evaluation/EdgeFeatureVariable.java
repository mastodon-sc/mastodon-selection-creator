/*-
 * #%L
 * mastodon-tracking
 * %%
 * Copyright (C) 2018 - 2021 Tobias Pietzsch, Jean-Yves Tinevez
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
