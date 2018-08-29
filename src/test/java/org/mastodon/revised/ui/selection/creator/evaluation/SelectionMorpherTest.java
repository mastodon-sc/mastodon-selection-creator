package org.mastodon.revised.ui.selection.creator.evaluation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefSet;
import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.TestSimpleEdge;
import org.mastodon.graph.TestSimpleGraph;
import org.mastodon.graph.TestSimpleVertex;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.algorithm.traversal.DepthFirstSearch;
import org.mastodon.graph.algorithm.traversal.GraphSearch.SearchDirection;
import org.mastodon.graph.algorithm.traversal.SearchListener;
import org.mastodon.revised.ui.selection.creator.evaluation.SelectionMorpher.Morpher;

public class SelectionMorpherTest
{

	private static final int N_LINEAR = 10;

	private TestSimpleGraph graph;

	private TestSimpleVertex v0;

	private TestSimpleVertex v1;

	private TestSimpleVertex middleDivision;

	private TestSimpleVertex v2;

	private TestSimpleVertex middleMerge;

	private TestSimpleVertex v3;

	private SelectionMorpher< TestSimpleVertex, TestSimpleEdge > selectionMorpher;

	private GraphIdBimap< TestSimpleVertex, TestSimpleEdge > idmap;

	private RefSet< TestSimpleVertex > track0Vertices;

	private RefSet< TestSimpleEdge > track0Edges;

	private RefSet< TestSimpleVertex > track1Vertices;

	private RefSet< TestSimpleEdge > track1Edges;

	private RefSet< TestSimpleVertex > track2Vertices;

	private RefSet< TestSimpleEdge > track2Edges;

	@Before
	public void setUp() throws Exception
	{
		graph = new TestSimpleGraph( 4 * N_LINEAR + 4 );
		final TestSimpleVertex vref = graph.vertexRef();

		// Track 0: Linear track.
		v0 = graph.addVertex().init( 0 );
		final TestSimpleVertex lastLinear = extendBranchFrom( graph, v0, N_LINEAR - 1, v0.getId() + 1, vref );

		// Track 1: One division.
		v1 = graph.addVertex().init( lastLinear.getId() + 1 );
		middleDivision = extendBranchFrom( graph, v1, N_LINEAR / 2, v1.getId() + 1, graph.vertexRef() );
		final TestSimpleVertex lastBranch1 = extendBranchFrom( graph, middleDivision, N_LINEAR / 2, middleDivision.getId() + 1, vref );
		final TestSimpleVertex lastBranch2 = extendBranchFrom( graph, middleDivision, N_LINEAR / 2, lastBranch1.getId() + 1, vref );

		// Track 2: One merge.
		v2 = graph.addVertex().init( lastBranch2.getId() + 1 );
		middleMerge = extendBranchFrom( graph, v2, N_LINEAR / 2 + 1, v2.getId() + 1, graph.vertexRef() );
		v3 = graph.addVertex().init( middleMerge.getId() + 1 );
		final TestSimpleVertex toMerge = extendBranchFrom( graph, v3, N_LINEAR / 2, v3.getId() + 1, graph.vertexRef() );
		graph.addEdge( toMerge, middleMerge );
		extendBranchFrom( graph, middleMerge, N_LINEAR / 2, toMerge.getId() + 1, vref );

		graph.releaseRef( vref );

		idmap = new GraphIdBimap<>( graph.getVertexPool(), graph.getEdgePool() );
		selectionMorpher = new SelectionMorpher<>( graph, idmap );

		/*
		 * Create track sets.
		 */

		final DepthFirstSearch< TestSimpleVertex, TestSimpleEdge > dfs = new DepthFirstSearch<>( graph, SearchDirection.UNDIRECTED );

		track0Vertices = RefCollections.createRefSet( graph.vertices() );
		track0Edges = RefCollections.createRefSet( graph.edges() );
		dfs.setTraversalListener( new CollectionAdder( track0Vertices, track0Edges ) );
		dfs.start( v0 );

		track1Vertices = RefCollections.createRefSet( graph.vertices() );
		track1Edges = RefCollections.createRefSet( graph.edges() );
		dfs.setTraversalListener( new CollectionAdder( track1Vertices, track1Edges ) );
		dfs.start( v1 );

		track2Vertices = RefCollections.createRefSet( graph.vertices() );
		track2Edges = RefCollections.createRefSet( graph.edges() );
		dfs.setTraversalListener( new CollectionAdder( track2Vertices, track2Edges ) );
		dfs.start( v2 );
	}

	private static final TestSimpleVertex extendBranchFrom( final TestSimpleGraph graph, final TestSimpleVertex start, final int length, final int startIndex, final TestSimpleVertex vref )
	{
		final TestSimpleVertex vref1 = graph.vertexRef();
		final TestSimpleVertex vref2 = graph.vertexRef();
		final TestSimpleEdge eref = graph.edgeRef();
		TestSimpleVertex source = null;
		for ( int i = 0; i < length; i++ )
		{
			if ( null == source )
			{
				vref1.refTo( start );
				source = vref1;
			}
			final TestSimpleVertex target = graph.addVertex( vref2 ).init( startIndex + i );
			graph.addEdge( source, target, eref );
			source.refTo( target );
		}
		vref.refTo( source );

		graph.releaseRef( vref1 );
		graph.releaseRef( vref2 );
		graph.releaseRef( eref );

		return vref;
	}

	@Test
	public void testMorphWholeTrack()
	{
		final Morpher wholeTrack = SelectionMorpher.Morpher.WHOLE_TRACK;

		// Select one vertex in the linear track.
		final BitSet bs0 = new BitSet();
		bs0.set( v0.getInternalPoolIndex() );
		final SelectionVariable sv0v = new SelectionVariable( bs0, new BitSet() );
		final SelectionVariable morphed0v = selectionMorpher.morph( sv0v, Collections.singleton( wholeTrack ) );
		assertSelectionIs( morphed0v, track0Vertices, track0Edges );

		// Select all edges in the linear track.
		final BitSet bs0e = new BitSet();
		for ( final TestSimpleEdge e : track0Edges )
			bs0e.set( e.getInternalPoolIndex() );
		final SelectionVariable sv0e = new SelectionVariable( new BitSet(), bs0e );
		final SelectionVariable morphed0e = selectionMorpher.morph( sv0e, Collections.singleton( wholeTrack ) );
		assertSelectionIs( morphed0e, track0Vertices, track0Edges );

		// Select one vertex in the division track.
		final BitSet bs1 = new BitSet();
		bs1.set( v1.getInternalPoolIndex() );
		final SelectionVariable sv1v = new SelectionVariable( bs1, new BitSet() );
		final SelectionVariable morphed1v = selectionMorpher.morph( sv1v, Collections.singleton( wholeTrack ) );
		assertSelectionIs( morphed1v, track1Vertices, track1Edges );

		// Select one vertex in the merge track.
		final BitSet bs2 = new BitSet();
		bs2.set( v2.getInternalPoolIndex() );
		final SelectionVariable sv2v = new SelectionVariable( bs2, new BitSet() );
		final SelectionVariable morphed2v = selectionMorpher.morph( sv2v, Collections.singleton( wholeTrack ) );
		assertSelectionIs( morphed2v, track2Vertices, track2Edges );
	}

	@Test
	public void testToVertex()
	{
		final Morpher toVertex = SelectionMorpher.Morpher.TO_VERTEX;
		final SelectionVariable sv = SelectionVariable.fromGraph( graph, idmap );
		final SelectionVariable morphed = selectionMorpher.morph( sv, Collections.singleton( toVertex ) );
		assertEquals( "There should not be an edge in the selection. Found " + morphed.selectedEdges.cardinality(),
				0, morphed.selectedEdges.cardinality() );
		assertEquals( "All vertices should be in the selection. Found " + morphed.selectedVertices.cardinality(),
				graph.vertices().size(), morphed.selectedVertices.cardinality() );
	}

	@Test
	public void testToEdge()
	{
		final Morpher toEdge = SelectionMorpher.Morpher.TO_EDGE;
		final SelectionVariable sv = SelectionVariable.fromGraph( graph, idmap );
		final SelectionVariable morphed = selectionMorpher.morph( sv, Collections.singleton( toEdge ) );
		assertEquals( "There should not be a vertex in the selection. Found " + morphed.selectedVertices.cardinality(),
				0, morphed.selectedVertices.cardinality() );
		assertEquals( "All edges should be in the selection. Found " + morphed.selectedEdges.cardinality(),
				graph.edges().size(), morphed.selectedEdges.cardinality() );
	}

	@Test
	public void testIncomingEdges()
	{
		final Morpher incomingEdges = SelectionMorpher.Morpher.INCOMING_EDGES;
		final BitSet bs = new BitSet();
		bs.set( middleMerge.getInternalPoolIndex() );
		final SelectionVariable sv = new SelectionVariable( bs, new BitSet() );
		final SelectionVariable morphed = selectionMorpher.morph( sv, Collections.singleton( incomingEdges ) );
		assertEquals( "There should not be a vertex in the selection. Found " + morphed.selectedVertices.cardinality(),
				0, morphed.selectedVertices.cardinality() );
		assertEquals( "There should be 2 edges in the selection. Found " + morphed.selectedEdges.cardinality(),
				2, morphed.selectedEdges.cardinality() );
	}

	@Test
	public void testOutgoingEdges()
	{
		final Morpher outgoingEdges = SelectionMorpher.Morpher.OUTGOING_EDGES;
		final BitSet bs = new BitSet();
		bs.set( middleDivision.getInternalPoolIndex() );
		final SelectionVariable sv = new SelectionVariable( bs, new BitSet() );
		final SelectionVariable morphed = selectionMorpher.morph( sv, Collections.singleton( outgoingEdges ) );
		assertEquals( "There should not be a vertex in the selection. Found " + morphed.selectedVertices.cardinality(),
				0, morphed.selectedVertices.cardinality() );
		assertEquals( "There should be 2 edges in the selection. Found " + morphed.selectedEdges.cardinality(),
				2, morphed.selectedEdges.cardinality() );
	}

	@Test
	public void testSourceVertex()
	{
		final Morpher sourceVertex = SelectionMorpher.Morpher.SOURCE_VERTEX;
		final TestSimpleEdge e = v0.outgoingEdges().iterator().next();
		final BitSet bs = new BitSet();
		bs.set( e.getInternalPoolIndex() );
		final SelectionVariable sv = new SelectionVariable( new BitSet(), bs );
		final SelectionVariable morphed = selectionMorpher.morph( sv, Collections.singleton( sourceVertex ) );
		assertEquals( "There should be 1 vertex in the selection. Found " + morphed.selectedVertices.cardinality(),
				1, morphed.selectedVertices.cardinality() );
		assertEquals( "There should be 0 edges in the selection. Found " + morphed.selectedEdges.cardinality(),
				0, morphed.selectedEdges.cardinality() );

		final Iterator< TestSimpleVertex > vit = morphed.vertexIterator( idmap.vertexIdBimap() );
		while ( vit.hasNext() )
			assertEquals( "The vertex in the selection should be v0.", v0, vit.next() );
	}

	@Test
	public void testTargetVertex()
	{
		final Morpher targetVertex = SelectionMorpher.Morpher.TARGET_VERTEX;
		final TestSimpleEdge e = v0.outgoingEdges().iterator().next();
		final BitSet bs = new BitSet();
		bs.set( e.getInternalPoolIndex() );
		final SelectionVariable sv = new SelectionVariable( new BitSet(), bs );
		final SelectionVariable morphed = selectionMorpher.morph( sv, Collections.singleton( targetVertex ) );
		assertEquals( "There should be 1 vertex in the selection. Found " + morphed.selectedVertices.cardinality(),
				1, morphed.selectedVertices.cardinality() );
		assertEquals( "There should be 0 edges in the selection. Found " + morphed.selectedEdges.cardinality(),
				0, morphed.selectedEdges.cardinality() );

		final TestSimpleVertex target = e.getTarget();
		final Iterator< TestSimpleVertex > vit = morphed.vertexIterator( idmap.vertexIdBimap() );
		while ( vit.hasNext() )
			assertEquals( "The vertex in the selection should be the target.", target, vit.next() );
	}

	@Test
	public void testSourceAndTargetVertex()
	{
		final List< Morpher > morphers = Arrays.asList( new SelectionMorpher.Morpher[] {
				SelectionMorpher.Morpher.SOURCE_VERTEX, SelectionMorpher.Morpher.TARGET_VERTEX } );

		final TestSimpleEdge e = v0.outgoingEdges().iterator().next();
		final BitSet bs = new BitSet();
		bs.set( e.getInternalPoolIndex() );
		final SelectionVariable sv = new SelectionVariable( new BitSet(), bs );
		final SelectionVariable morphed = selectionMorpher.morph( sv, morphers );

		assertEquals( "There should be 2 vertices in the selection. Found " + morphed.selectedVertices.cardinality(),
				2, morphed.selectedVertices.cardinality() );
		assertEquals( "There should be 0 edges in the selection. Found " + morphed.selectedEdges.cardinality(),
				0, morphed.selectedEdges.cardinality() );
	}

	@Test
	public void testSourceAndTargetVertexAndEdge()
	{
		final List< Morpher > morphers = Arrays.asList( new SelectionMorpher.Morpher[] {
				SelectionMorpher.Morpher.SOURCE_VERTEX, SelectionMorpher.Morpher.TARGET_VERTEX, Morpher.TO_EDGE } );

		final TestSimpleEdge e = v0.outgoingEdges().iterator().next();
		final BitSet bs = new BitSet();
		bs.set( e.getInternalPoolIndex() );
		final SelectionVariable sv = new SelectionVariable( new BitSet(), bs );
		final SelectionVariable morphed = selectionMorpher.morph( sv, morphers );

		assertEquals( "There should be 2 vertices in the selection. Found " + morphed.selectedVertices.cardinality(),
				2, morphed.selectedVertices.cardinality() );
		assertEquals( "There should be 1 edge in the selection. Found " + morphed.selectedEdges.cardinality(),
				1, morphed.selectedEdges.cardinality() );
	}

	private void assertSelectionIs( final SelectionVariable sv, final Collection< TestSimpleVertex > vertices, final Collection< TestSimpleEdge > edges )
	{
		final RefSet< TestSimpleVertex > vs = RefCollections.createRefSet( graph.vertices(), vertices.size() );
		for ( final TestSimpleVertex v : vertices )
			vs.add( v );

		final RefSet< TestSimpleEdge > es = RefCollections.createRefSet( graph.edges(), edges.size() );
		for ( final TestSimpleEdge e : edges )
			es.add( e );

		final Iterator< TestSimpleVertex > vit = sv.vertexIterator( idmap.vertexIdBimap() );
		while ( vit.hasNext() )
		{
			final TestSimpleVertex v = vit.next();
			assertTrue( "Selected vertex should belong to track.", vs.contains( v ) );
			vs.remove( v );
		}
		assertTrue( "All track should be selected, " + vs.size() + " vertices were not.", vs.isEmpty() );

		final Iterator< TestSimpleEdge > eit = sv.edgeIterator( idmap.edgeIdBimap() );
		while ( eit.hasNext() )
		{
			final TestSimpleEdge e = eit.next();
			assertTrue( "Selected vertex should belong to track.", es.contains( e ) );
			es.remove( e );
		}
		assertTrue( "All track should be selected, " + es.size() + " edges were not.", es.isEmpty() );
	}

	public static final < V extends Vertex< E >, E extends Edge< V > > String echo( final SelectionVariable sv, final GraphIdBimap< V, E > idmap )
	{
		final StringBuilder str = new StringBuilder();
		str.append( sv.toString() );
		str.append( "\n- Vertices:" );
		final Iterator< V > vit = sv.vertexIterator( idmap.vertexIdBimap() );
		while ( vit.hasNext() )
			str.append( "\n  - " + vit.next().toString() );
		str.append( "\n- Edges:" );
		final Iterator< E > eit = sv.edgeIterator( idmap.edgeIdBimap() );
		while ( eit.hasNext() )
			str.append( "\n  - " + eit.next().toString() );

		return str.toString();
	}

	private static class CollectionAdder implements SearchListener< TestSimpleVertex, TestSimpleEdge, DepthFirstSearch< TestSimpleVertex, TestSimpleEdge > >
	{

		private final Collection< TestSimpleVertex > vertices;

		private final Collection< TestSimpleEdge > edges;

		public CollectionAdder( final Collection< TestSimpleVertex > vertices, final Collection< TestSimpleEdge > edges )
		{
			this.vertices = vertices;
			this.edges = edges;
		}

		@Override
		public void processVertexLate( final TestSimpleVertex vertex, final DepthFirstSearch< TestSimpleVertex, TestSimpleEdge > search )
		{}

		@Override
		public void processVertexEarly( final TestSimpleVertex vertex, final DepthFirstSearch< TestSimpleVertex, TestSimpleEdge > search )
		{
			vertices.add( vertex );
		}

		@Override
		public void processEdge( final TestSimpleEdge edge, final TestSimpleVertex from, final TestSimpleVertex to, final DepthFirstSearch< TestSimpleVertex, TestSimpleEdge > search )
		{
			edges.add( edge );
		}

		@Override
		public void crossComponent( final TestSimpleVertex from, final TestSimpleVertex to, final DepthFirstSearch< TestSimpleVertex, TestSimpleEdge > search )
		{}
	};
}
