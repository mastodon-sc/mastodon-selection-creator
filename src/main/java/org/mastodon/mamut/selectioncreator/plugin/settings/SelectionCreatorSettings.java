/*-
 * #%L
 * mastodon-selection-creator
 * %%
 * Copyright (C) 2018 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.mamut.selectioncreator.plugin.settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.scijava.listeners.Listeners;

import bdv.ui.settings.style.Style;

public class SelectionCreatorSettings implements Style< SelectionCreatorSettings >
{

	public interface UpdateListener
	{
		public void settingsChanged();
	}

	private String name;

	private String expression;

	private String description;

	private final Listeners.List< UpdateListener > updateListeners;

	private SelectionCreatorSettings()
	{
		this.updateListeners = new Listeners.SynchronizedList<>();
	}

	public void set( final SelectionCreatorSettings scs )
	{
		this.name = scs.name;
		this.expression = scs.expression;
		this.description = scs.description;
		notifyListeners();
	}

	public String description()
	{
		return description;
	}

	public String expression()
	{
		return expression;
	}

	public void setExpression( final String expression )
	{
		if ( !Objects.equals( this.expression, expression ) )
		{
			this.expression = expression;
			notifyListeners();
		}
	}

	public void setDescription( final String description )
	{
		if ( !Objects.equals( this.description, description ) )
		{
			this.description = description;
			notifyListeners();
		}
	}

	@Override
	public SelectionCreatorSettings copy()
	{
		return copy( null );
	}

	@Override
	public SelectionCreatorSettings copy( final String newName )
	{
		final SelectionCreatorSettings sats = new SelectionCreatorSettings();
		sats.set( this );
		if ( newName != null )
			sats.setName( newName );
		return sats;
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public synchronized void setName( final String name )
	{
		if ( !Objects.equals( this.name, name ) )
		{
			this.name = name;
			notifyListeners();
		}
	}

	private void notifyListeners()
	{
		for ( final UpdateListener l : updateListeners.list )
			l.settingsChanged();
	}

	public Listeners< UpdateListener > updateListeners()
	{
		return updateListeners;
	}

	@Override
	public String toString()
	{
		return super.toString()
				+ "\n - name: " + name
				+ "\n - expression: " + expression
				+ "\n - description: " + description;
	}

	/**
	 * List of examples to serve as built-in defaults.
	 */
	public static final List< SelectionCreatorSettings > EXAMPLES;
	static
	{
		final List< SelectionCreatorSettings > exs = new ArrayList<>();

		final SelectionCreatorSettings ex1 = new SelectionCreatorSettings();
		ex1.name = "X larger than 100";
		ex1.expression = "vertexFeature('Spot position', 'X') > 100.";
		ex1.description = "Get all the vertices whose X position is strictly greater than 100. "
				+ "The specified feature value must be computed prior to parsing for this "
				+ "to return a useful selection.";
		exs.add( ex1 );

		final SelectionCreatorSettings ex2 = new SelectionCreatorSettings();
		ex2.name = "Reviewed by JY";
		ex2.expression = "tagSet('Reviewed by') == 'JY'";
		ex2.description = "Return the vertices and edges tagged by JY in the tag-set "
				+ "Reviewed by. Of course, both specified tag-set and tag must exist.";
		exs.add( ex2 );

		final SelectionCreatorSettings ex3 = new SelectionCreatorSettings();
		ex3.name = "Vertices with 3 edges in frame 25";
		ex3.expression = "vertexFeature('Spot N links') == 3 & vertexFeature('Spot frame') == 25";
		ex3.description = "Get the vertices that are in the frame 25 AND have 3 edges.";
		exs.add( ex3 );

		final SelectionCreatorSettings ex4 = new SelectionCreatorSettings();
		ex4.name = "Vertices with 3 edges plus those in frame 25";
		ex4.expression = "vertexFeature('Spot N links') == 3 | vertexFeature('Spot frame') == 25";
		ex4.description = "Get the vertices that have 3 edges plus the vertices in the frame 25.";
		exs.add( ex4 );

		final SelectionCreatorSettings ex5 = new SelectionCreatorSettings();
		ex5.name = "Add outgoing edges";
		ex5.expression = "morph( ( vertexFeature('Spot N links') == 3 & vertexFeature('Spot frame') == 14 ), "
				+ "('toVertex', 'outgoingEdges') )";
		ex5.description = "Get the vertices of the frame 14 that have 3 edges, and return them "
				+ "plus their outgoing edges.";
		exs.add( ex5 );

		final SelectionCreatorSettings ex6 = new SelectionCreatorSettings();
		ex6.name = "Filter current selection";
		ex6.expression = "selection & ( vertexFeature('Spot N links') == 1 )";
		ex6.description = "Get the currently selected vertices that have exactly 1 edge.";
		exs.add( ex6 );

		final SelectionCreatorSettings ex7 = new SelectionCreatorSettings();
		ex7.name = "Get incoming edges of selected vertices";
		ex7.expression = "morph(vertexSelection, 'incomingEdges')";
		ex7.description = "Get the incoming edges of the vertices in the selection.";
		exs.add( ex7 );

		final SelectionCreatorSettings ex8 = new SelectionCreatorSettings();
		ex8.name = "Edges of current selection";
		ex8.expression = "edgeSelection";
		ex8.description = "Just return the edges of the current selection.";
		exs.add( ex8 );

		final SelectionCreatorSettings ex9 = new SelectionCreatorSettings();
		ex9.name = "Prune current selection";
		ex9.expression = "selection - ( vertexFeature('Spot N links') == 2 )";
		ex9.description = "Remove from the selection all the spots that have 2 links.";
		exs.add( ex9 );

		final SelectionCreatorSettings ex10 = new SelectionCreatorSettings();
		ex10.name = "Vertices not tagged with JY";
		ex10.expression = "vertexTagSet('Reviewed by') != 'JY'";
		ex10.description = "All the vertices that are NOT tagged with JY in the tag-set "
				+ "Reviewed by.";
		exs.add( ex10 );

		final SelectionCreatorSettings ex11 = new SelectionCreatorSettings();
		ex11.name = "Edges not tagged";
		ex11.expression = "!edgeTagSet('Reviewed by')";
		ex11.description = "All the edges that are NOT tagged with any tag in the tag-set "
				+ "Reviewed by.";
		exs.add( ex11 );

		final SelectionCreatorSettings ex12 = new SelectionCreatorSettings();
		ex12.name = "Vertices with a tag";
		ex12.expression = "~vertexTagSet('Reviewed by')";
		ex12.description = "All the vertices that are tagged with any tag in the tag-set "
				+ "Reviewed by.";
		exs.add( ex12 );

		EXAMPLES = Collections.unmodifiableList( exs );
	}
}
