package org.mastodon.revised.ui.selection.creator.evaluation;

import org.mastodon.collection.RefCollection;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.revised.model.tag.ObjTagMap;
import org.mastodon.revised.model.tag.TagSetStructure.Tag;
import org.mastodon.revised.model.tag.TagSetStructure.TagSet;

public class GraphTagSetVariable< V, E > implements TagSetVariable
{

	private final VertexTagSetVariable< V > vertexTagSetVariable;

	private final EdgeTagSetVariable< E > edgeTagSetVariable;

	public GraphTagSetVariable( final TagSet tagSet, final ObjTagMap< V, Tag > vertexTags, final ObjTagMap< E, Tag > edgeTags, final RefCollection< V > vertices, final RefCollection< E > edges, final GraphIdBimap< V, E > graphIdBimap )
	{
		this.vertexTagSetVariable = new VertexTagSetVariable<>( tagSet, vertexTags, vertices, graphIdBimap.vertexIdBimap() );
		this.edgeTagSetVariable = new EdgeTagSetVariable<>( tagSet, edgeTags, edges, graphIdBimap.edgeIdBimap() );
	}

	@Override
	public TagSet getTagSet()
	{
		return vertexTagSetVariable.getTagSet();
	}

	@Override
	public SelectionVariable equal( final Tag tag )
	{
		final SelectionVariable variable = vertexTagSetVariable.equal( tag );
		variable.inPlaceAdd( edgeTagSetVariable.equal( tag ) );
		return variable;
	}

	@Override
	public SelectionVariable notEqual( final Tag tag )
	{
		final SelectionVariable variable = vertexTagSetVariable.notEqual( tag );
		variable.inPlaceAdd( edgeTagSetVariable.notEqual( tag ) );
		return variable;
	}

	@Override
	public SelectionVariable unset()
	{
		final SelectionVariable variable = vertexTagSetVariable.unset();
		variable.inPlaceAdd( edgeTagSetVariable.unset() );
		return variable;
	}

	@Override
	public SelectionVariable set()
	{
		final SelectionVariable variable = vertexTagSetVariable.set();
		variable.inPlaceAdd( edgeTagSetVariable.set() );
		return variable;
	}
}
