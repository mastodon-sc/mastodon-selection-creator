package org.mastodon.revised.ui.selection.creator.parser;

import org.mastodon.feature.FeatureModel;
import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.model.SelectionModel;
import org.mastodon.revised.model.tag.TagSetModel;
import org.mastodon.revised.ui.selection.creator.parser.evaluation.SelectionEvaluator;
import org.mastodon.revised.ui.selection.creator.parser.evaluation.SelectionVariable;
import org.scijava.parse.ExpressionParser;
import org.scijava.parse.SyntaxTree;

/**
 * Class to create a selection model from parsing expressions.
 * <p>
 * Expression are strings where a small language can be used to combine
 * conditions and filters on vertices and edges. Examples:

 * <table summary="Selection parser examples" border="1">
 * <tr>
 * <th>Expression</th>
 * <th>Meaning</th>
 * </tr>
 * <tr>
 * <td>
 *
 * <pre>
 * vertexFeature('Spot position', 'X') &gt; 100.
 * </pre>
 *
 * </td>
 * <td>Get all the vertices whose X position is strictly greater than 100. The
 * specified feature value must be computed prior to parsing for this to return
 * a useful selection.</td>
 * </tr>
 * <tr>
 * <td>
 *
 * <pre>
 * tagSet('Reviewed by') == 'JY'
 * </pre>
 *
 * </td>
 * <td>Return the vertices and edges tagged by 'JY' in the tag-set 'Reviewed
 * by'. Of course, both specified tag-set and tag must exist.</td>
 * </tr>
 * <tr>
 * <td>
 *
 * <pre>
 * vertexFeature('Spot N links') == 3 &amp; vertexFeature('Spot frame') == 25
 * </pre>
 *
 * </td>
 * <td>Get the vertices that are in the frame 25 AND have 3 edges.</td>
 * </tr>
 * <tr>
 * <td>
 *
 * <pre>
 * vertexFeature('Spot N links') == 3 | vertexFeature('Spot frame') == 25
 * </pre>
 *
 * </td>
 * <td>Get the vertices that have 3 edges plus the vertices in the frame
 * 25.</td>
 * </tr>
 * <tr>
 * <td>
 *
 * <pre>
 * ( vertexFeature('Spot N links') == 3 ) + ( vertexFeature('Spot frame') == 25 )
 * </pre>
 *
 * </td>
 * <td>Get the vertices that have 3 edges plus the vertices in the frame 25.
 * Same as above, the '+' sign as the same meaning that '|', but different
 * priority so we have to add brackets to avoid errors.</td>
 * </tr>
 * <tr>
 * <td>
 *
 * <pre>
 * morph(
 *     ( vertexFeature('Spot N links') == 3 &amp; vertexFeature('Spot frame') == 14 ),
 *     ('toVertex', 'outgoingEdges') )
 * </pre>
 *
 * </td>
 * <td>Get the vertices of the frame 14 that have 3 edges, and return them plus
 * their outgoing edges.</td>
 * </tr>
 *
 * <tr>
 * <td>
 *
 * <pre>
 * selection &amp; ( vertexFeature('Spot N links') == 1 )
 * </pre>
 *
 * </td>
 * <td>Get the currently selected vertices that have exactly 1 edge.</td>
 * </tr>
 *
 * <tr>
 * <td>
 *
 * <pre>
 * morph(vertexSelection, 'incomingEdges')
 * </pre>
 *
 * </td>
 * <td>Get the incoming edges of the vertices in the selection.</td>
 * </tr>
 *
 * <tr>
 * <td>
 *
 * <pre>
 * edgeSelection
 * </pre>
 *
 * </td>
 * <td>Just return the edges of the current selection.</td>
 * </tr>
 *
 * <tr>
 * <td>
 *
 * <pre>
 * selection - ( vertexFeature('Spot N links') == 2 )
 * </pre>
 *
 * </td>
 * <td>Remove from the selection all the spots that have 2 links.</td>
 * </tr>
 *
 * <tr>
 * <td>
 *
 * <pre>
 * vertexTagSet('Reviewed by') != 'JY'
 * </pre>
 *
 * </td>
 * <td>All the vertices that are NOT tagged with 'JY' in the tag-set 'Reviewed
 * by'.</td>
 * </tr>
 *
 * <tr>
 * <td>
 *
 * <pre>
 * !vertexTagSet('Reviewed by')
 * </pre>
 *
 * </td>
 * <td>All the vertices that are NOT tagged with any tag in the tag-set
 * 'Reviewed by'.</td>
 * </tr>
 *
 * <tr>
 * <td>
 *
 * <pre>
 * ~vertexTagSet('Reviewed by')
 * </pre>
 *
 * </td>
 * <td>All the vertices that are tagged with any tag in the tag-set 'Reviewed
 * by'.</td>
 * </tr>
 *
 * </table>
 *
 * @author Jean-Yves Tinevez
 *
 * @param <V>
 *            the type of vertices in the graph.
 * @param <E>
 *            the type of edges in the graph.
 */
public class SelectionParser< V extends Vertex< E >, E extends Edge< V > >
{

	private final ReadOnlyGraph< V, E > graph;

	private final GraphIdBimap< V, E > graphIdBimap;

	private final TagSetModel< V, E > tagSetModel;

	private final FeatureModel featureModel;

	private final SelectionModel< V, E > selectionModel;

	private String errorMessage;

	public SelectionParser(
			final ReadOnlyGraph< V, E > graph,
			final GraphIdBimap< V, E > graphIdBimap,
			final TagSetModel< V, E > tagSetModel,
			final FeatureModel featureModel,
			final SelectionModel< V, E > selectionModel )
	{
		this.graph = graph;
		this.graphIdBimap = graphIdBimap;
		this.tagSetModel = tagSetModel;
		this.featureModel = featureModel;
		this.selectionModel = selectionModel;
	}

	/**
	 * Replaces the content of the selection model by the result the evaluation
	 * of the specified expression.
	 *
	 * @param expression
	 *            the expression to evaluate.
	 * @return <code>true</code> if the expression was correctly evaluated and
	 *         the selection model updated. If <code>false</code>, an
	 *         explanatory error message can be access with
	 *         {@link #getErrorMessage()}.
	 */
	public boolean parse( final String expression )
	{
		errorMessage = null;
		final SyntaxTree tree = new ExpressionParser().parseTree( expression );
		final SelectionEvaluator< V, E > evaluator = new SelectionEvaluator<>( graph, graphIdBimap, tagSetModel, featureModel, selectionModel );
		try
		{
			final Object result = evaluator.evaluate( tree );
			if ( result instanceof SelectionVariable )
			{
				final SelectionVariable sv = ( SelectionVariable ) result;
				sv.toSelectionModel( selectionModel, graphIdBimap );
				return true;
			}
			else
			{
				errorMessage = "Got unexpected result: " + result;
				return false;
			}
		}
		catch ( final IllegalArgumentException iae )
		{
			final String err = evaluator.getErrorMessage();
			if ( err != null )
				errorMessage = "Incorrect syntax: " + err;
			else
				errorMessage = iae.getMessage();
			return false;
		}
	}

	/**
	 * Returns the error message possible triggered by the last call to
	 * {@link #parse(String)}.
	 *
	 * @return the error message, <code>null</code> if the
	 *         {@link #parse(String)} successfully returned.
	 */
	public String getErrorMessage()
	{
		return errorMessage;
	}
}
