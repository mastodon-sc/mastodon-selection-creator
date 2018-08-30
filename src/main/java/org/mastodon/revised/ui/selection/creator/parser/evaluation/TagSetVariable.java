package org.mastodon.revised.ui.selection.creator.parser.evaluation;

import org.mastodon.revised.model.tag.TagSetStructure.Tag;
import org.mastodon.revised.model.tag.TagSetStructure.TagSet;

public interface TagSetVariable
{

	public SelectionVariable equal( Tag tag );

	public SelectionVariable notEqual( Tag tag );

	public SelectionVariable unset();

	public SelectionVariable set();

	public TagSet getTagSet();

}
