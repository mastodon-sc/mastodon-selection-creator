# mastodon-selection-creator
A selection creator for Mastodon, as a Mastodon plugin.
Utility to create a selection model from parsing expressions, based on Curtis Rueden SciJava Parsington.

Expression are strings where a small language can be used to combine
conditions and filters on vertices and edges. 

Check this for documentation and explanation fo the syntax: ![the help file](src/main/resources/org/mastodon/revised/ui/selection/creator/parser/plugin/settings/Help.md).

Examples:
--------

|Expression|Meaning|
|--- |--- |
|`vertexFeature('Spot position', 'X') > 100.` |Get all the vertices whose X position is strictly greater than 100. The specified feature value must be computed prior to parsing for this to return a useful selection.|
|`tagSet('Reviewed by') == 'JY'`|Return the vertices and edges tagged by `JY` in the tag-set `Reviewed by`. Of course, both specified tag-set and tag must exist.|
|`vertexFeature('Spot N links') == 3 & vertexFeature('Spot frame') == 25`|Get the vertices that are in the frame 25 AND have 3 edges.|
|`vertexFeature('Spot N links') == 3 \| vertexFeature('Spot frame') == 25`|Get the vertices that have 3 edges plus the vertices in the frame 25.|
|`( vertexFeature('Spot N links') == 3 ) + ( vertexFeature('Spot frame') == 25 )`|Get the vertices that have 3 edges plus the vertices in the frame 25. Same as above, the `+` sign as the same meaning that `\|`, but different priority so we have to add brackets to avoid errors.|
|`morph( ( vertexFeature('Spot N links') == 3 & vertexFeature('Spot frame') == 14 ), ('toVertex', 'outgoingEdges') )`|Get the vertices of the frame 14 that have 3 edges, and return them plus their outgoing edges.|
|`selection & ( vertexFeature('Spot N links') == 1 )`|Get the currently selected vertices that have exactly 1 edge.|
|`morph(vertexSelection, 'incomingEdges')`|Get the incoming edges of the vertices in the selection.|
|`edgeSelection`|Just return the edges of the current selection.|
|`selection - ( vertexFeature('Spot N links') == 2 )`|Remove from the selection all the spots that have 2 links.|
|`vertexTagSet('Reviewed by') != 'JY'`|All the vertices that are NOT tagged with `JY` in the tag-set `Reviewed by`.|
|`!vertexTagSet('Reviewed by')`|All the vertices that are NOT tagged with any tag in the tag-set `Reviewed by`.|
|`~vertexTagSet('Reviewed by')`|All the vertices that are tagged with any tag in the tag-set `Reviewed by`.|
