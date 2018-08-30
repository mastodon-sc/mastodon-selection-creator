package org.mastodon.revised.ui.selection.creator.parser.evaluation;

import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.revised.model.feature.Feature;
import org.mastodon.revised.model.feature.FeatureModel;
import org.mastodon.revised.model.feature.FeatureProjection;

public interface FeatureVariable< O >
{

	public static < O > FeatureVariable< O > emptyFeature()
	{
		return new EmptyFeatureVariable<>();
	}

	@SuppressWarnings( "unchecked" )
	public static < V extends Vertex< E >, E extends Edge< V > > FeatureVariable< V > vertexFeature( final ReadOnlyGraph< V, E > graph, final GraphIdBimap< V, E > idmap, final FeatureModel featureModel, final String featureKey, final String projectionKey )
	{
		final Feature< ?, ? > feature = featureModel.getFeature( featureKey );
		if ( null == feature )
			return emptyFeature();
		if ( !featureModel.getFeatureSet( graph.vertexRef().getClass() ).contains( feature ) )
			return emptyFeature();

		final FeatureProjection< ? > projection = feature.getProjections().get( projectionKey );
		return new VertexFeatureVariable< V >( featureKey, projectionKey, ( FeatureProjection< V > ) projection, graph.vertices(), idmap.vertexIdBimap() );
	}

	@SuppressWarnings( "unchecked" )
	public static < V extends Vertex< E >, E extends Edge< V > > FeatureVariable< E > edgeFeature( final ReadOnlyGraph< V, E > graph, final GraphIdBimap< V, E > idmap, final FeatureModel featureModel, final String featureKey, final String projectionKey )
	{
		final Feature< ?, ? > feature = featureModel.getFeature( featureKey );
		if ( null == feature )
			return emptyFeature();
		if ( !featureModel.getFeatureSet( graph.edgeRef().getClass() ).contains( feature ) )
			return emptyFeature();

		final FeatureProjection< ? > projection = feature.getProjections().get( projectionKey );
		return new EdgeFeatureVariable< E >( featureKey, projectionKey, ( FeatureProjection< E > ) projection, graph.edges(), idmap.edgeIdBimap() );
	}

	public SelectionVariable notEqual( double value );

	public SelectionVariable equal( double value );

	public SelectionVariable greaterThanOrEqual( double threshold );

	public SelectionVariable lessThanOrEqual( double threshold );

	public SelectionVariable greaterThan( double threshold );

	public SelectionVariable lessThan( double threshold );

}
