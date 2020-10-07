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
