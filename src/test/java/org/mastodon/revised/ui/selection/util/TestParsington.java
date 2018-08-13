package org.mastodon.revised.ui.selection.util;

import java.io.IOException;

import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.algorithm.TreeOutputter;
import org.mastodon.graph.object.ObjectGraph;
import org.mastodon.graph.object.ObjectVertex;
import org.mastodon.model.SelectionModel;
import org.mastodon.revised.mamut.MamutProject;
import org.mastodon.revised.mamut.MamutProjectIO;
import org.mastodon.revised.mamut.WindowManager;
import org.mastodon.revised.model.feature.FeatureModel;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.ModelGraph;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.revised.model.tag.TagSetModel;
import org.mastodon.revised.ui.selection.creator.evaluation.SelectionEvaluator;
import org.scijava.Context;
import org.scijava.parse.ExpressionParser;
import org.scijava.parse.SyntaxTree;

import mpicbg.spim.data.SpimDataException;

public class TestParsington
{

	public static void main( final String[] args ) throws IOException, SpimDataException
	{
		final Context context = new Context();
		final MamutProject project = new MamutProjectIO().load( "../TrackMate3/samples/mamutproject" );
		final WindowManager windowManager = new WindowManager( context );
		windowManager.getProjectManager().open( project );
		final Model model = windowManager.getAppModel().getModel();
		final SelectionModel< Spot, Link > selectionModel = windowManager.getAppModel().getSelectionModel();

		final ModelGraph graph = model.getGraph();
		final GraphIdBimap< Spot, Link > graphIdBimap = model.getGraphIdBimap();
		final TagSetModel< Spot, Link > tagSetModel = model.getTagSetModel();
		final FeatureModel featureModel = model.getFeatureModel();

		System.out.println( tagSetModel.getTagSetStructure() ); // DEBUG

		final SyntaxTree tree = new ExpressionParser().parseTree(
				"morph( "
				+ "vertexFeature(SpotPosition, X) > 3. "
				+ "& "
						+ "tagSet('Reviewed by') == JY"
						+ ", (toVertex, toIncomingEdges) )" );
		final String str = TreeOutputter.output( toMastodon( tree ) );
		System.out.println( str );

		new SelectionEvaluator<>( graph, graphIdBimap, tagSetModel, featureModel, selectionModel )
				.evaluate( tree );
	}

	public static ObjectGraph< String > toMastodon( final SyntaxTree syntaxTree )
	{
		final ObjectGraph< String > graph = new ObjectGraph<>();
		build( syntaxTree, graph );
		return graph;
	}

	private static ObjectVertex< String > build( final SyntaxTree tree, final ObjectGraph< String > graph )
	{
		final ObjectVertex< String > source = graph.addVertex().init( tree.token().toString() );
		for ( int i = 0; i < tree.count(); i++ )
		{
			final ObjectVertex< String > target = build( tree.child( i ), graph );
			graph.addEdge( source, target );
		}
		return source;
	}
}
