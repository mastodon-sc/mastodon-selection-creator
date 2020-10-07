# Selection creator parser.

This parser allows for entering *expressions* that will be evaluated into a *Mastodon selection*. 

Expression follows a syntax similar to boolean equations, with several special functions to read numerical feature values from objects, their tags, or to change (morph) a selection on vertices to edges, etc. It is more powerful and flexible than a GUI to create selection, as the expression can cover any possible combination of criteria.

Of course, for an expression that includes feature values or tags to work, they must be computed or specified before. The parser will return a hopefully explanatory error message when this is not the case.

# Examples.

Expressions are strings where a small language can be used to combine conditions and filters on vertices and edges.

| Expression                                                   | Meaning                                                      |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| ` vertexFeature('Spot position', 'X') > 100.  `              | Get all the vertices whose X position is strictly greater than 100. The specified feature value must be computed prior to parsing for this to return a useful selection. |
| ` tagSet('Reviewed by') == 'JY'  `                           | Return the vertices and edges tagged by 'JY' in the tag-set 'Reviewed by'. Of course, both specified tag-set and tag must exist. |
| ` vertexFeature('Spot N links') == 3 & vertexFeature('Spot frame') == 25  ` | Get the vertices that are in the frame 25 AND have 3 edges.  |
| ` vertexFeature('Spot N links') == 3 | vertexFeature('Spot frame') == 25  ` | Get the vertices that have 3 edges plus the vertices in the frame 25. |
| ` ( vertexFeature('Spot N links') == 3 ) + ( vertexFeature('Spot frame') == 25 )  ` | Get the vertices that have 3 edges plus the vertices in the frame 25. Same as above, the '+' sign as the same meaning that '\|', but different priority so we have to add brackets to avoid errors. |
| ` morph(      ( vertexFeature('Spot N links') == 3 & vertexFeature('Spot frame') == 14 ),      ('toVertex', 'outgoingEdges') )  ` | Get the vertices of the frame 14 that have 3 edges, and return them plus their outgoing edges. |
| ` selection & ( vertexFeature('Spot N links') == 1 )  `      | Get the currently selected vertices that have exactly 1 edge. |
| ` morph(vertexSelection, 'incomingEdges')  `                 | Get the incoming edges of the vertices in the selection.     |
| ` edgeSelection  `                                           | Just return the edges of the current selection.              |
| ` selection - ( vertexFeature('Spot N links') == 2 )  `      | Remove from the selection all the spots that have 2 links.   |
| ` vertexTagSet('Reviewed by') != 'JY'  `                     | All the vertices that are NOT tagged with 'JY' in the tag-set 'Reviewed by'. |
| ` !vertexTagSet('Reviewed by')  `                            | All the vertices that are NOT tagged with any tag in the tag-set 'Reviewed by'. |
| ` ~vertexTagSet('Reviewed by')  `                            | All the vertices that are tagged with any tag in the tag-set 'Reviewed by'. |

# Documentation.

## The `vertexFeature` function.

This function returns the specified feature projection values for all the vertices in the model. You need to specify the feature key, then projection key, in between single quotes (') like this:

`vertexFeature('feature key', 'projection key')`

For scalar features (that have no multiplicity and only one projection ) it is not required to specify the projection key. For instance: 

`vertexFeature('Spot N links')`

will work.

Features with multiplicity need to specify the channel in the projection key. For instance:

`vertexFeature('Spot gaussian-filtered intensity', 'Mean ch0')`

The output of this function is the list of all feature values. You then need to call a boolean operation to create a selection from it. For instance, here is how to select all the spots that have exactly 2 links:

`vertexFeature('Spot N links') == 2`

## The `edgeFeature` function.

This function works exactly like `vertexFeature` but for edge features.

## Comparison operators.

These two functions (`vertexFeature` and  `edgeFeature`) return a list of numerical values that you need to compare with a numerical value to get a boolean. The boolean determines whether the object (vertex or edge) will be selected (true) or not (false).

All classical comparison operations are supported: 

- `>` strictly greater than
- `<` strictly smaller than
- `>=` greater than
- `<=` smaller than
- `==` equal to
- `!=` different from

## Boolean operators.

Once you have the boolean values, you can combine them using boolean operators. For instance:

`vertexFeature('Spot N links') == 3 & vertexFeature('Spot frame') == 25`

will select the vertices that are in the frame 25 AND have 3 edges.

Again, the classical ones are supported:

- `&` logical and. 
- `|` logical or.
- `+` adds the two selections. Works like logical or.
- `-` removes the right-hand term from the left-hand term. For instance `A - B` will deselect in `A` the objects that are selected in `B`.
- `!` not operator. Inverse selection.

The parser can get confused because of operator priority. If you get error messages such as this one: 

> Evaluation failed. Incorrect syntax: Improper use of the 'sub' operator, not defined for SelectionVariable and VertexFeatureVariable. Use brackets to clarify operator priority.

do what it says and try to use brackets to explicitly specify the operator priority.

## The `tagSet` function.

This function extracts from object the tag they have within a specific tag-set. You then can write a comparison on the tag string value.

For instance, lets suppose that we have a tag-set called `Reviewed by` with the following 3 tags: `Pavel`, `Tobias`, `JY`. To select all the objects (vertices or edges) that are tagged with `JY` in this tag set, you have to use the `==` operator, with the string `JY` as comparison:

` tagSet('Reviewed by') == 'JY'`

Note that the tags and tag-sets are specified with their key within single quotes ('') like for numerical features.

Of course, the `>` and `<` etc. comparison operators do not apply here. However there are other comparisons possible:

- `tagSet('TS') == 'A'` will select all the objects (vertices and edges) in tag-set `TS` that are tagged with the tag`A`.
- `tagSet('TS') != 'A'` will select all the objects in tag-set `TS` that are NOT tagged with the tag`A`. Any other tags or not tags will work.
- `!tagSet('TS')` will select all the objects in tag-set `TS` that are not tagged.
- `~tagSet('TS')` will select all the objects in tag-set `TS` that are tagged with any tag (it is the complement of the result above).

### The `vertexTagSet` and `edgeTagSet` functions.

They work exactly like their `tagSet` counterpart described in the previous paragraph, but only applies respectively to vertices or edges.

## The `selection` variable.

This function simply returns the list of objects that are currently selected in Mastodon. It can then be combined with another expression using boolean functions, or morphed (see below). For instance, to remove from the selection all the spots that have 2 links, use this expression:

`selection - ( vertexFeature('Spot N links') == 2 )`

## The `vertexSelection` and  `edgeSelection` variables.

Again, they work like their `selection` counterpart, but are limited to return only the vertices, respectively the edges, of the data.

## The `morph` function.

This function is a bit special as it is able to change the type of objects that are selected, based on object relations. It is with this function that you will select the edges of a spot. For instance:

`morph(vertexSelection, 'incomingEdges')`

will select the incoming edges of the vertices in the selection. The vertices themselves will not be included in the final selection.

The `morph` function need two inputs:

1. a selection (it can result from `tagSet('TS') == 'A'`, `vertexFeature('F', 'FP') > 3`, `selection`, ...);
2. a list of tokens that specify how to morph the selection.

In the above example, the token was `incomingEdges` which takes all the incoming edges of the vertices currently selected, and nothing else (so not the vertices initially selected).

The supported morph tokens are the following ones:

- `toVertex` includes the vertices currently selected in the morph result. When this token is not present, the selected vertices are removed from the target selection.
- `incomingEdges` includes the incoming edges of the selected vertices.
- `outgoingEdges` includes the outgoing edges of the selected vertices.
- `toEdge`  includes the edges of the source selection in the target selection. When this morpher is not present, the selected edges are removed from the target selection.
- `sourceVertex` includes the source vertices of the selected edges.
- `targetVertex` includes the target vertices of the selected edges.
- `wholeTrack` includes the whole track of selected vertices and edges.

You can combine several morph tokens, if you put them as a list between brackets. For instance:

```
morph(
     ( vertexFeature('Spot N links') == 3 & vertexFeature('Spot frame') == 14 ),
     ('toVertex', 'outgoingEdges') )
```

will select the vertices of the frame 14 that have 3 edges, and return them plus their outgoing edges.

# Syntax.

The capitalisation of functions and variables do not matter, but it is prettier like this.