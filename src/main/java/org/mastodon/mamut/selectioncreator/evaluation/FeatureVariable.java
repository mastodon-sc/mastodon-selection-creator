/*-
 * #%L
 * mastodon-selection-creator
 * %%
 * Copyright (C) 2018 - 2024 Tobias Pietzsch, Jean-Yves Tinevez
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

import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;

public interface FeatureVariable< O >
{

	public static < O > FeatureVariable< O > emptyFeature()
	{
		return new EmptyFeatureVariable<>();
	}

	@SuppressWarnings( "unchecked" )
	public static < V extends Vertex< E >, E extends Edge< V > > FeatureVariable< V > vertexFeature( final ReadOnlyGraph< V, E > graph, final GraphIdBimap< V, E > idmap, final FeatureModel featureModel, final FeatureSpec< ?, ? > featureSpec, final FeatureProjectionKey projectionKey )
	{
		if ( !featureSpec.getTargetClass().isAssignableFrom( graph.vertexRef().getClass() ) )
			return emptyFeature();
		final Feature< ? > feature = featureModel.getFeature( featureSpec );
		if ( null == feature )
			return emptyFeature();

		final FeatureProjection< ? > projection = feature.project( projectionKey );
		return new VertexFeatureVariable< V >( featureSpec, projectionKey, ( FeatureProjection< V > ) projection, graph.vertices(), idmap.vertexIdBimap() );
	}

	@SuppressWarnings( "unchecked" )
	public static < V extends Vertex< E >, E extends Edge< V > > FeatureVariable< E > edgeFeature( final ReadOnlyGraph< V, E > graph, final GraphIdBimap< V, E > idmap, final FeatureModel featureModel, final FeatureSpec< ?, ? > featureSpec, final FeatureProjectionKey projectionKey )
	{
		if ( !featureSpec.getTargetClass().isAssignableFrom( graph.edgeRef().getClass() ) )
			return emptyFeature();
		final Feature< ? > feature = featureModel.getFeature( featureSpec );
		if ( null == feature )
			return emptyFeature();

		final FeatureProjection< ? > projection = feature.project( projectionKey );
		return new EdgeFeatureVariable<>( featureSpec, projectionKey, ( FeatureProjection< E > ) projection, graph.edges(), idmap.edgeIdBimap() );
	}

	public SelectionVariable notEqual( double value );

	public SelectionVariable equal( double value );

	public SelectionVariable greaterThanOrEqual( double threshold );

	public SelectionVariable lessThanOrEqual( double threshold );

	public SelectionVariable greaterThan( double threshold );

	public SelectionVariable lessThan( double threshold );

}
