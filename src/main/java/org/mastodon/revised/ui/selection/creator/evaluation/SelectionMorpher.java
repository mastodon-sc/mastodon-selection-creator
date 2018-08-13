package org.mastodon.revised.ui.selection.creator.evaluation;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;

import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;

public class SelectionMorpher< V extends Vertex< E >, E extends Edge< V > >
{

	private ReadOnlyGraph< V, E > graph;

	private GraphIdBimap< V, E > idmap;

	public static enum Morpher
	{
		/**
		 * Includes the vertices of the source selection in the target selection. When
		 * this morpher is not present, the selected vertices are removed from the
		 * target selection.
		 */
		TO_VERTEX,
		/**
		 * Includes the incoming edges of the selected vertices.
		 */
		INCOMING_EDGES,
		/**
		 * Includes the outgoing edges of the selected vertices.
		 */
		OUTGOING_EDGES,
		/**
		 * Includes the edges of the source selection in the target selection. When this
		 * morpher is not present, the selected edges are removed from the target
		 * selection.
		 */
		TO_EDGE,
		/**
		 * Include the source vertices of the selected edges.
		 */
		SOURCE_VERTEX,
		/**
		 * Include the target vertices of the selected edges.
		 */
		TARGET_VERTEX,
		/**
		 * Include the whole track of selected vertices and edges.
		 */
		WHOLE_TRACK;

	}


	public SelectionMorpher( final ReadOnlyGraph< V, E > graph, final GraphIdBimap< V, E > idmap )
	{
		this.graph = graph;
		this.idmap = idmap;
	}

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
				return toVertex( selection );
			case INCOMING_EDGES:
				return incomingEdges( selection );
			case OUTGOING_EDGES:
				return outgoingEdges( selection );
			case SOURCE_VERTEX:
				return sourceVertex( selection );
			case TARGET_VERTEX:
				return targetVertex( selection );
			case TO_EDGE:
				return toEdge( selection );
			case WHOLE_TRACK:
				return wholeTrack( selection );
			default:
				throw new IllegalArgumentException( "Unknown morpher: " + morpher );
			}
		}

		return svs.stream().reduce( ( t, u ) -> t.inPlaceAdd( u ) ).get();
	}

	private SelectionVariable incomingEdges( final SelectionVariable selection )
	{
		final SelectionVariable sv = new SelectionVariable( new BitSet(), new BitSet() );
		return null;
	}

	private SelectionVariable toVertex( final SelectionVariable sv )
	{
		final SelectionVariable copy = sv.copy();
		copy.clearEdges();
		return copy;
	}

}
