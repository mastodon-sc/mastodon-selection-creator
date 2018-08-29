package org.mastodon.revised.ui.selection.util;

import java.awt.Color;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.object.ObjectGraph;
import org.mastodon.graph.object.ObjectVertex;
import org.mastodon.model.SelectionModel;
import org.mastodon.revised.mamut.MamutProject;
import org.mastodon.revised.mamut.MamutProjectIO;
import org.mastodon.revised.mamut.WindowManager;
import org.mastodon.revised.mamut.feature.MamutFeatureComputer;
import org.mastodon.revised.mamut.feature.MamutFeatureComputerService;
import org.mastodon.revised.mamut.feature.SpotGaussFilteredIntensityComputer;
import org.mastodon.revised.model.feature.FeatureModel;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.ModelGraph;
import org.mastodon.revised.model.mamut.ModelUtils;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.revised.model.tag.TagSetModel;
import org.mastodon.revised.model.tag.TagSetStructure;
import org.mastodon.revised.model.tag.TagSetStructure.Tag;
import org.mastodon.revised.model.tag.TagSetStructure.TagSet;
import org.mastodon.revised.ui.ProgressListener;
import org.mastodon.revised.ui.selection.creator.evaluation.SelectionEvaluator;
import org.mastodon.revised.ui.selection.creator.evaluation.SelectionVariable;
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

		// Create a JY tag.
		final TagSetStructure tss = new TagSetStructure();
		tss.createTagSet( "Reviewed by" ).createTag( "JY", Color.GREEN.getRGB() );
		tagSetModel.setTagSetStructure( tss );
		// Re-acquire new tag set (because the model stores a copy)
		final TagSet tagSet2 = tagSetModel.getTagSetStructure().getTagSets().get( 0 );
		final Tag jyTag = tagSet2.getTags().get( 0 );

		// Assign JY tag to random vertices and edges.
		final Random ran = new Random( 1l );
		for ( final Spot v : graph.vertices() )
			if ( ran.nextBoolean() )
				tagSetModel.getVertexTags().set( v, jyTag );
		for ( final Link e : graph.edges() )
			if ( ran.nextBoolean() )
				tagSetModel.getEdgeTags().set( e, jyTag );

		// Compute feature values.
		final MamutFeatureComputerService service = context.getService( MamutFeatureComputerService.class );
		final Collection< MamutFeatureComputer > featureComputers = service.getFeatureComputers();
		final Set< MamutFeatureComputer > fcs = featureComputers.stream()
				.filter( ( mfc ) -> mfc.getKey() != SpotGaussFilteredIntensityComputer.KEY )
				.collect( Collectors.toSet() );
		service.compute( model, featureModel, fcs, voidLogger() );

		final List< String > tests = Arrays.asList( new String[] {
				"paf > 3",

				"morph( "
				+ 		"( vertexFeature('Spot N links') == 3 & vertexFeature('Spot frame') == 14 )"
				+ ", ('toVertex', 'outgoingEdges') )",

				"vertexFeature('Spot N links') == 3 | vertexFeature('Spot frame') == 25",

				"( vertexFeature('Spot N links') == 3 ) + ( vertexFeature('Spot frame') == 25 )",

				"vertexFeature('Spot N links') == 3 & vertexFeature('Spot frame') == 25",

				"tagSet('Reviewed by') == 'JY'",

				"vertexFeature('Spot position', 'X') > 100. "
		} );

		final SyntaxTree tree = new ExpressionParser().parseTree( tests.get( 0 ) );
		final SelectionEvaluator< Spot, Link > evaluator = new SelectionEvaluator<>( graph, graphIdBimap, tagSetModel, featureModel, selectionModel );
		try
		{
			final Object result = evaluator.evaluate( tree );
			if (result instanceof SelectionVariable)
			{
				System.out.println( "Evaluation successful. Content of the selection variable:" );
				final SelectionVariable sv = ( SelectionVariable ) result;
				final Spot ref = graph.vertexRef();
				System.out.println( ModelUtils.dump(
						sv.vertices( graph.vertices().getRefPool() ),
						sv.edges( graph.edges().getRefPool() ),
						featureModel,
						ref ) );
				graph.releaseRef( ref );
			}
			else
			{
				System.out.println( "Got unexpected result: " + result );
			}
		}
		catch ( final IllegalArgumentException iae )
		{
			System.out.println( "Incorrect syntax: " + evaluator.getErrorMessage() );
			iae.printStackTrace();
		}
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

	private static ProgressListener voidLogger()
	{
		return new ProgressListener()
		{

			@Override
			public void showStatus( final String string )
			{}

			@Override
			public void showProgress( final int current, final int total )
			{}

			@Override
			public void clearStatus()
			{}
		};
	}
}
