package org.mastodon.revised.ui.selection.creator.parser.evaluation;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.algorithm.traversal.DepthFirstSearch;
import org.mastodon.graph.algorithm.traversal.GraphSearch.SearchDirection;
import org.mastodon.graph.algorithm.traversal.SearchListener;

import gnu.trove.set.hash.TIntHashSet;

/**
 * Class that can morph a {@link SelectionVariable} into another one, based on
 * graph hierarchical rules.
 *
 * @author Jean-Yves Tinevez
 *
 * @param <V>
 *            the type of vertices in the graph.
 * @param <E>
 *            the type of edges in the graph.
 */
public class SelectionMorpher< V extends Vertex< E >, E extends Edge< V > >
{

	private final ReadOnlyGraph< V, E > graph;

	private final GraphIdBimap< V, E > idmap;

	/**
	 * Enum specifying what morphing to perform.
	 *
	 * @see SelectionMorpher#morph(SelectionVariable, Collection)
	 */
	public static enum Morpher
	{
		/**
		 * Includes the vertices of the source selection in the target
		 * selection. When this morpher is not present, the selected vertices
		 * are removed from the target selection.
		 */
		TO_VERTEX( "toVertex" ),
		/**
		 * Includes the incoming edges of the selected vertices.
		 */
		INCOMING_EDGES( "incomingEdges" ),
		/**
		 * Includes the outgoing edges of the selected vertices.
		 */
		OUTGOING_EDGES( "outgoingEdges" ),
		/**
		 * Includes the edges of the source selection in the target selection.
		 * When this morpher is not present, the selected edges are removed from
		 * the target selection.
		 */
		TO_EDGE( "toEdge" ),
		/**
		 * Include the source vertices of the selected edges.
		 */
		SOURCE_VERTEX( "sourceVertex" ),
		/**
		 * Include the target vertices of the selected edges.
		 */
		TARGET_VERTEX( "targetVertex" ),
		/**
		 * Include the whole track of selected vertices and edges.
		 */
		WHOLE_TRACK( "wholeTrack" );

		private final String label;

		private Morpher( final String label )
		{
			this.label = label;
		}

		@Override
		public String toString()
		{
			return label;
		}
	}

	/**
	 * Instantiates a new {@link SelectionMorpher} that will rely on the
	 * specified graph for hierarchical rules.
	 *
	 * @param graph
	 *            the graph.
	 * @param idmap
	 *            map between graph objects and their IDs.
	 */
	public SelectionMorpher( final ReadOnlyGraph< V, E > graph, final GraphIdBimap< V, E > idmap )
	{
		this.graph = graph;
		this.idmap = idmap;
	}

	/**
	 * Returns a new {@link SelectionVariable} containing the <b>union</b> of
	 * the results all morphers specified applied to the specified selection.
	 *
	 * @param selection
	 *            the selection to morph.
	 * @param morphers
	 *            the collection of morphers.
	 * @return a new {@link SelectionVariable}.
	 */
	public SelectionVariable morph( final SelectionVariable selection, final Collection< Morpher > morphers )
	{
		if ( morphers.isEmpty() )
			return selection;

		final List< SelectionVariable > svs = new ArrayList<>();
		for ( final Morpher morpher : morphers )
		{
			switch ( morpher )
			{
			case TO_VERTEX:
				svs.add( toVertex( selection ) );
				break;
			case INCOMING_EDGES:
				svs.add( incomingEdges( selection ) );
				break;
			case OUTGOING_EDGES:
				svs.add( outgoingEdges( selection ) );
				break;
			case SOURCE_VERTEX:
				svs.add( sourceVertex( selection ) );
				break;
			case TARGET_VERTEX:
				svs.add( targetVertex( selection ) );
				break;
			case TO_EDGE:
				svs.add( toEdge( selection ) );
				break;
			case WHOLE_TRACK:
				svs.add( wholeTrack( selection ) );
				break;
			default:
				throw new IllegalArgumentException( "Unknown morpher: " + morpher );
			}
		}

		return svs.stream().reduce( ( t, u ) -> t.inPlaceAdd( u ) ).get();
	}

	/**
	 * Returns the selection that includes all the vertices and edges of the
	 * tracks to which the source selection belongs.
	 *
	 * @param selection
	 *            the source selection.
	 * @return a new selection
	 */
	public SelectionVariable wholeTrack( final SelectionVariable selection )
	{
		/*
		 * Harvest all the vertices and edges IDs to iterate. We use a
		 * TIntHashSet as a pseudo-queue.
		 */
		final TIntHashSet vertices = new TIntHashSet();
		for ( int i = selection.selectedVertices.nextSetBit( 0 ); i >= 0; i = selection.selectedVertices.nextSetBit( i + 1 ) )
		{
			vertices.add( i );
			if ( i == Integer.MAX_VALUE )
				break;
		}
		final TIntHashSet edges = new TIntHashSet();
		for ( int i = selection.selectedEdges.nextSetBit( 0 ); i >= 0; i = selection.selectedEdges.nextSetBit( i + 1 ) )
		{
			edges.add( i );
			if ( i == Integer.MAX_VALUE )
				break;
		}

		/*
		 * The search listener: Adds to the selection and prunes from the todo
		 * lists.
		 */
		final BitSet verticesToSelect = new BitSet();
		final BitSet edgesToSelect = new BitSet();
		final SearchListener< V, E, DepthFirstSearch< V, E > > listener = new SearchListener< V, E, DepthFirstSearch< V, E > >()
		{

			@Override
			public void processVertexLate( final V vertex, final DepthFirstSearch< V, E > search )
			{}

			@Override
			public void processVertexEarly( final V vertex, final DepthFirstSearch< V, E > search )
			{
				final int id = idmap.getVertexId( vertex );
				verticesToSelect.set( id );
				vertices.remove( id );
			}

			@Override
			public void processEdge( final E edge, final V from, final V to, final DepthFirstSearch< V, E > search )
			{
				final int id = idmap.getEdgeId( edge );
				edgesToSelect.set( id );
				edges.remove( id );
			}

			@Override
			public void crossComponent( final V from, final V to, final DepthFirstSearch< V, E > search )
			{}
		};

		// Iterate and prune over vertex IDs.
		final DepthFirstSearch< V, E > dfs = new DepthFirstSearch<>( graph, SearchDirection.UNDIRECTED );
		dfs.setTraversalListener( listener );
		final V vref = graph.vertexRef();
		while ( !vertices.isEmpty() )
		{
			final int next = vertices.iterator().next();
			final V v = idmap.getVertex( next, vref );
			dfs.start( v );
		}

		// Iterate and prune over edge IDs.
		final E eref = graph.edgeRef();
		while ( !edges.isEmpty() )
		{
			final int next = edges.iterator().next();
			final E e = idmap.getEdge( next, eref );
			dfs.start( e.getSource( vref ) );
		}

		graph.releaseRef( eref );
		graph.releaseRef( vref );
		return new SelectionVariable( verticesToSelect, edgesToSelect );
	}

	/**
	 * Returns the selection that includes only the target vertex of the
	 * selected edges.
	 *
	 * @param selection
	 *            the source selection.
	 * @return a new selection
	 */
	public SelectionVariable targetVertex( final SelectionVariable selection )
	{
		final V ref = idmap.vertexIdBimap().createRef();
		final BitSet targetVertices = new BitSet();
		final Iterator< E > it = selection.edgeIterator( idmap.edgeIdBimap() );
		while ( it.hasNext() )
			targetVertices.set( idmap.getVertexId( it.next().getTarget( ref ) ) );

		idmap.vertexIdBimap().releaseRef( ref );
		return new SelectionVariable( targetVertices, new BitSet() );
	}

	/**
	 * Returns the selection that includes only the source vertex of the
	 * selected edges.
	 *
	 * @param selection
	 *            the source selection.
	 * @return a new selection
	 */
	public SelectionVariable sourceVertex( final SelectionVariable selection )
	{
		final V ref = idmap.vertexIdBimap().createRef();
		final BitSet sourceVertices = new BitSet();
		final Iterator< E > it = selection.edgeIterator( idmap.edgeIdBimap() );
		while ( it.hasNext() )
			sourceVertices.set( idmap.getVertexId( it.next().getSource( ref ) ) );

		idmap.vertexIdBimap().releaseRef( ref );
		return new SelectionVariable( sourceVertices, new BitSet() );
	}

	/**
	 * Returns the selection that includes only the outgoing edges of the
	 * selected vertices.
	 *
	 * @param selection
	 *            the source selection.
	 * @return a new selection
	 */
	public SelectionVariable outgoingEdges( final SelectionVariable selection )
	{
		final BitSet outgoingEdgeIds = new BitSet();
		final Iterator< V > it = selection.vertexIterator( idmap.vertexIdBimap() );
		while ( it.hasNext() )
		{
			final V v = it.next();
			for ( final E e : v.outgoingEdges() )
				outgoingEdgeIds.set( idmap.getEdgeId( e ) );
		}
		return new SelectionVariable( new BitSet(), outgoingEdgeIds );
	}

	/**
	 * Returns the selection that includes only the incoming edges of the
	 * selected vertices.
	 *
	 * @param selection
	 *            the source selection.
	 * @return a new selection
	 */
	public SelectionVariable incomingEdges( final SelectionVariable selection )
	{
		final BitSet incomingEdgeIds = new BitSet();
		final Iterator< V > it = selection.vertexIterator( idmap.vertexIdBimap() );
		while ( it.hasNext() )
		{
			final V v = it.next();
			for ( final E e : v.incomingEdges() )
				incomingEdgeIds.set( idmap.getEdgeId( e ) );
		}
		return new SelectionVariable( new BitSet(), incomingEdgeIds );
	}

	/**
	 * Returns the selection that includes the vertices of the source selection
	 * in the target selection, but no edges.
	 *
	 * @param selection
	 *            the source selection.
	 * @return a new selection
	 */
	public SelectionVariable toVertex( final SelectionVariable selection )
	{
		final SelectionVariable copy = selection.copy();
		copy.clearEdges();
		return copy;
	}

	/**
	 * Returns the selection that includes the edges of the source selection in
	 * the target selection, but no vertices.
	 *
	 * @param selection
	 *            the source selection.
	 * @return a new selection
	 */
	public SelectionVariable toEdge( final SelectionVariable selection )
	{
		final SelectionVariable copy = selection.copy();
		copy.clearVertices();
		return copy;
	}
}
