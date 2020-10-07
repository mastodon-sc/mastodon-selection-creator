package org.mastodon.mamut.selectioncreator;

import java.awt.Color;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.object.ObjectGraph;
import org.mastodon.graph.object.ObjectVertex;
import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.feature.MamutFeatureComputerService;
import org.mastodon.mamut.feature.SpotGaussFilteredIntensityFeature;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.project.MamutProject;
import org.mastodon.mamut.project.MamutProjectIO;
import org.mastodon.model.SelectionModel;
import org.mastodon.model.tag.TagSetModel;
import org.mastodon.model.tag.TagSetStructure;
import org.mastodon.model.tag.TagSetStructure.Tag;
import org.mastodon.model.tag.TagSetStructure.TagSet;
import org.scijava.Context;
import org.scijava.parsington.SyntaxTree;

import mpicbg.spim.data.SpimDataException;

public class SelectionCreatorParserExample
{

	public static void main( final String[] args ) throws IOException, SpimDataException
	{
		final Context context = new Context();
		final MamutProject project = new MamutProjectIO().load( "../mastodon/samples/mamutproject.mastodon" );
		final WindowManager windowManager = new WindowManager( context );
		windowManager.getProjectManager().open( project );
		final Model model = windowManager.getAppModel().getModel();
		final SelectionModel< Spot, Link > selectionModel = windowManager.getAppModel().getSelectionModel();

		final ModelGraph graph = model.getGraph();
		final GraphIdBimap< Spot, Link > graphIdBimap = model.getGraphIdBimap();
		final TagSetModel< Spot, Link > tagSetModel = model.getTagSetModel();
		final FeatureModel featureModel = model.getFeatureModel();

		// Create some tags.
		final TagSetStructure tss = new TagSetStructure();
		final TagSet tagSet = tss.createTagSet( "Reviewed by" );
		tagSet.createTag( "JY", Color.GREEN.getRGB() );
		tagSet.createTag( "Tobias", Color.BLUE.getRGB() );
		tagSetModel.setTagSetStructure( tss );
		// Re-acquire new tag set (because the model stores a copy)
		final TagSet tagSet2 = tagSetModel.getTagSetStructure().getTagSets().get( 0 );
		final Tag jyTag = tagSet2.getTags().get( 0 );
		final Tag tobiasTag = tagSet2.getTags().get( 1 );

		// Assign JY tag to random vertices and edges.
		final Random ran = new Random( 1l );
		for ( final Spot v : graph.vertices() )
			if ( ran.nextBoolean() )
				tagSetModel.getVertexTags().set( v, ran.nextBoolean() ? jyTag : tobiasTag );
		for ( final Link e : graph.edges() )
			if ( ran.nextBoolean() )
				tagSetModel.getEdgeTags().set( e, ran.nextBoolean() ? jyTag : tobiasTag );

		// Compute feature values.
		final MamutFeatureComputerService service = context.getService( MamutFeatureComputerService.class );
		final Collection< FeatureSpec< ?, ? > > featureComputers = service.getFeatureSpecs();
		final Set< FeatureSpec< ?, ? > > fcs = featureComputers.stream()
				.filter( ( mfc ) -> mfc.getKey() != SpotGaussFilteredIntensityFeature.KEY )
				.collect( Collectors.toSet() );
		final Map< FeatureSpec< ?, ? >, Feature< ? > > map = service.compute( fcs );
		featureModel.pauseListeners();
		for ( final Feature< ? > feature : map.values() )
			featureModel.declareFeature( feature );
		featureModel.resumeListeners();

		// Create random selection of vertices.
		selectionModel.clearSelection();
		for ( final Spot v : graph.vertices() )
			if ( ran.nextBoolean() )
				selectionModel.setSelected( v, true );

		final List< String > tests = Arrays.asList( new String[] {

				/*
				 * All the vertices that are tagged with any tag in the tag-set
				 * 'Reviewed by'.
				 */
				"~vertexTagSet('Reviewed by')",

				/*
				 * All the vertices that are NOT tagged with any tag in the
				 * tag-set 'Reviewed by'.
				 */
				"!vertexTagSet('Reviewed by')",

				/*
				 * All the vertices that are NOT tagged with 'JY' in the tag-set
				 * 'Reviewed by'.
				 */
				"vertexTagSet('Reviewed by') != 'JY'",

				/*
				 * Remove from the selection all the spots that have 2 links.
				 */
				"selection - ( vertexFeature('Spot N links') == 2 )",

				/*
				 * Just return the edges of the current selection.
				 */
				"edgeSelection",

				/*
				 * Get the incoming edges of the vertices in the selection.
				 */
				"morph(vertexSelection, 'incomingEdges')",

				/*
				 * Get the currently selected vertices that have exactly 1 link.
				 */
				"selection & ( vertexFeature('Spot N links') == 1 )",

				/*
				 * Get the vertices of the frame 14 that have 3 links, and
				 * return them plus their outgoing edges.
				 */
				"morph( "
						+ "( vertexFeature('Spot N links') == 3 & vertexFeature('Spot frame') == 14 )"
						+ ", ('toVertex', 'outgoingEdges') )",

				/*
				 * Get the vertices that have 3 links plus the vertices in the
				 * frame 25.
				 */
				"vertexFeature('Spot N links') == 3 | vertexFeature('Spot frame') == 25",

				/*
				 * Get the vertices that have 3 links plus the vertices in the
				 * frame 25. Same as above, the '+' sign as the same meaning
				 * that '|', but different priority so we have to add brackets
				 * to avoid errors.
				 */
				"( vertexFeature('Spot N links') == 3 ) + ( vertexFeature('Spot frame') == 25 )",

				/*
				 * Get the vertices that are in the frame 25 AND have 3 links.
				 */
				"vertexFeature('Spot N links') == 3 & vertexFeature('Spot frame') == 25",

				/*
				 * Return the vertices and edges tagged by 'JY' in the tag-set
				 * 'Reviewed by'.
				 */
				"tagSet('Reviewed by') == 'JY'",

				/*
				 * Get all the vertices whose X position is strictly greater
				 * than 100.
				 */
				"vertexFeature('Spot position', 'X') > 100. "
		} );
		final String expression = tests.get( 2 );

		final SelectionParser< Spot, Link > parser = new SelectionParser<>( graph, graphIdBimap, tagSetModel, featureModel, selectionModel );
		System.out.println( "\n\n\n_________________________________" );
		System.out.println( "Evaluating expression: " + expression );
		final boolean ok = parser.parse( expression );
		if ( ok )
		{
			System.out.println( "Parsing succesful." );
			System.out.println( String.format( "Found %d vertices and %d edges in the resulting selection.",
					selectionModel.getSelectedVertices().size(), selectionModel.getSelectedEdges().size() ) );
//			windowManager.createTable( false );
//			windowManager.createTable( true );
		}
		else
		{
			System.out.println( "Error during parsing: " + parser.getErrorMessage() );
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
}
