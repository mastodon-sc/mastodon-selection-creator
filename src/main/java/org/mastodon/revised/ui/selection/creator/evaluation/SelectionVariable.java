package org.mastodon.revised.ui.selection.creator.evaluation;

import java.util.BitSet;

import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.Vertex;
import org.mastodon.model.SelectionModel;

/**
 * We have to use 4 BitSets, because we want to track also the IDs of the
 * non-selected object. It is not possible to assume that non-selected objects
 * will map to cleared bits in the BitSet, for there might be unassigned ID
 * values.
 *
 * @author Jean-Yves Tinevez
 *
 */
public class SelectionVariable
{

	private final BitSet selectedVertices;

	private final BitSet selectedEdges;

	/**
	 * Empty selection variable.
	 */
	SelectionVariable()
	{
		this( new BitSet(), new BitSet() );
	}

	SelectionVariable( final BitSet selectedVertices, final BitSet selectedEdges )
	{
		this.selectedVertices = selectedVertices;
		this.selectedEdges = selectedEdges;
	}

	/**
	 * Creates a {@link SelectionVariable} from a {@link SelectionModel}.
	 *
	 * @param selectionModel
	 *            the selection model.
	 * @param idmap
	 *            the mapping from graph objects to their id.
	 * @return a new {@link SelectionVariable}.
	 */
	public static < V extends Vertex< E >, E extends Edge< V > > SelectionVariable fromSelectionModel( final SelectionModel< V, E > selectionModel, final GraphIdBimap< V, E > idmap )
	{
		// Vertices.
		final BitSet sv = new BitSet();
		for ( final V v : selectionModel.getSelectedVertices() )
			sv.set( idmap.getVertexId( v ) );

		// Edges.
		final BitSet se = new BitSet();
		for ( final E e : selectionModel.getSelectedEdges() )
			se.set( idmap.getEdgeId( e ) );

		return new SelectionVariable( sv, se );
	}

	public SelectionVariable inPlaceAdd( final SelectionVariable sv )
	{
		selectedVertices.or( sv.selectedVertices );
		selectedEdges.or( sv.selectedEdges );
		return this;
	}

	public SelectionVariable inPlaceSub( final SelectionVariable sv )
	{
		selectedVertices.andNot( sv.selectedVertices );
		selectedEdges.andNot( sv.selectedEdges );
		return this;
	}

	public SelectionVariable inPlaceAnd( final SelectionVariable sv )
	{
		selectedVertices.and( sv.selectedVertices );
		selectedEdges.and( sv.selectedEdges );
		return this;
	}

	@Override
	public String toString()
	{
		return "Selection( " + selectedVertices.cardinality() + ", " + selectedEdges.cardinality() + " )";
	}

	public SelectionVariable copy()
	{
		return new SelectionVariable( ( BitSet ) selectedVertices.clone(), ( BitSet ) selectedEdges.clone() );
	}

	public void clearVertices()
	{
		selectedVertices.clear();
	}

	public void clearEdges()
	{
		selectedEdges.clear();
	}
}
