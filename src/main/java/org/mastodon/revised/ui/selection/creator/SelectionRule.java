package org.mastodon.revised.ui.selection.creator;

import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;

public interface SelectionRule< V extends Vertex< E >, E extends Edge< V > >
{

	public boolean test( V v );

	public boolean test( E e );

}
