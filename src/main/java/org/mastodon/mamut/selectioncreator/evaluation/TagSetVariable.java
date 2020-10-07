package org.mastodon.mamut.selectioncreator.evaluation;

import org.mastodon.model.tag.TagSetStructure.Tag;
import org.mastodon.model.tag.TagSetStructure.TagSet;

public interface TagSetVariable
{

	public SelectionVariable equal( Tag tag );

	public SelectionVariable notEqual( Tag tag );

	public SelectionVariable unset();

	public SelectionVariable set();

	public TagSet getTagSet();

}
