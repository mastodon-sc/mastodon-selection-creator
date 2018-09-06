package org.mastodon.revised.ui.selection.creator.parser.evaluation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.model.SelectionModel;
import org.mastodon.revised.model.feature.Feature;
import org.mastodon.revised.model.feature.FeatureModel;
import org.mastodon.revised.model.tag.ObjTagMap;
import org.mastodon.revised.model.tag.TagSetModel;
import org.mastodon.revised.model.tag.TagSetStructure.Tag;
import org.mastodon.revised.model.tag.TagSetStructure.TagSet;
import org.mastodon.revised.ui.selection.creator.parser.evaluation.SelectionMorpher.Morpher;
import org.scijava.parse.Function;
import org.scijava.parse.Operator;
import org.scijava.parse.Operators;
import org.scijava.parse.SyntaxTree;
import org.scijava.parse.Tokens;
import org.scijava.parse.Variable;
import org.scijava.parse.eval.AbstractStackEvaluator;
import org.scijava.parse.eval.DefaultEvaluator;

public class SelectionEvaluator< V extends Vertex< E >, E extends Edge< V > > extends AbstractStackEvaluator
{

	private final DefaultEvaluator defaultEvaluator;

	private final ReadOnlyGraph< V, E > graph;

	private final GraphIdBimap< V, E > idmap;

	private final FeatureModel featureModel;

	private final SelectionModel< V, E > selectionModel;

	private final TagSetModel< V, E > tagSetModel;

	private final Map< String, Morpher > morpherMap = new HashMap<>();

	private String errorMessage;

	public SelectionEvaluator( final ReadOnlyGraph< V, E > graph, final GraphIdBimap< V, E > idmap, final TagSetModel< V, E > tagSetModel, final FeatureModel featureModel, final SelectionModel< V, E > selectionModel )
	{
		this.graph = graph;
		this.idmap = idmap;
		this.tagSetModel = tagSetModel;
		this.featureModel = featureModel;
		this.selectionModel = selectionModel;
		this.defaultEvaluator = new DefaultEvaluator();
		for ( final Morpher morpher : SelectionMorpher.Morpher.values() )
			morpherMap.put( morpher.toString(), morpher );
	}

	@Override
	public Object evaluate( final SyntaxTree syntaxTree )
	{
		// Intercept result that could be variables that need to be transformed
		// into a selection.
		Object result = super.evaluate( syntaxTree );
		if ( null != result && Tokens.isVariable( result ) )
			result = getVariableValue( ( Variable ) result );

		return result;
	}

	@Override
	public Object execute( final Operator op, final Deque< Object > stack )
	{
		errorMessage = null;

		// Pop the arguments.
		final int arity = op.getArity();
		final Object[] args = new Object[ arity ];
		for ( int i = args.length - 1; i >= 0; i-- )
		{
			args[ i ] = stack.pop();
		}
		final Object arg0 = args.length > 0 ? args[ 0 ] : null;
		final Object arg1 = args.length > 1 ? args[ 1 ] : null;

		// Intercept variables that can come from selection.
		final Object a;
		final Object b;
		if ( null != arg0 && Tokens.isVariable( arg0 ) )
			a = getVariableValue( ( Variable ) arg0 );
		else
			a = arg0;

		if ( null != arg1 && Tokens.isVariable( arg1 ) )
			b = getVariableValue( ( Variable ) arg1 );
		else
			b = arg1;

		// Let the case logic begin!
		if ( op instanceof Function )
			return function( a, b );
		if ( Tokens.isMatchingGroup( op, Operators.PARENS ) )
			return parens( args );
		if ( op == Operators.POS )
			return pos( a );
		if ( op == Operators.NEG )
			return neg( a );
		if ( op == Operators.NOT )
			return not( a );
		if ( op == Operators.COMPLEMENT )
			return complement( a );
		if ( op == Operators.ADD || op == Operators.BITWISE_OR )
			return add( a, b );
		if ( op == Operators.SUB )
			return sub( a, b );
		if ( op == Operators.LESS_THAN )
			return lessThan( a, b );
		if ( op == Operators.GREATER_THAN )
			return greaterThan( a, b );
		if ( op == Operators.LESS_THAN_OR_EQUAL )
			return lessThanOrEqual( a, b );
		if ( op == Operators.GREATER_THAN_OR_EQUAL )
			return greaterThanOrEqual( a, b );
		if ( op == Operators.EQUAL )
			return equal( a, b );
		if ( op == Operators.NOT_EQUAL )
			return notEqual( a, b );
		if ( op == Operators.BITWISE_AND )
			return and( a, b );

		// Unknown operator.
		errorMessage = "Unknown operator: " + op;
		return null;
	}

	private Object getVariableValue( final Variable variable )
	{
		switch ( variable.getToken().toLowerCase() )
		{
		case "selection":
			return SelectionVariable.fromSelectionModel( selectionModel, idmap );
		case "vertexselection":
		{
			final SelectionVariable sv = SelectionVariable.fromSelectionModel( selectionModel, idmap );
			sv.clearEdges();
			return sv;
		}
		case "edgeselection":
		{
			final SelectionVariable sv = SelectionVariable.fromSelectionModel( selectionModel, idmap );
			sv.clearVertices();
			return sv;
		}
		default:
			return variable;
		}
	}

	private Object and( final Object a, final Object b )
	{
		if ( a instanceof SelectionVariable && b instanceof SelectionVariable )
		{
			( ( SelectionVariable ) a ).inPlaceAnd( ( SelectionVariable ) b );
			return a;
		}

		errorMessage = "Cannot apply the 'and' operator to " + a.getClass().getSimpleName() + " and " + b.getClass().getSimpleName() + ".";
		return null;
	}

	private Object notEqual( final Object a, final Object b )
	{
		if ( a instanceof FeatureVariable && b instanceof Number )
			return ( (org.mastodon.revised.ui.selection.creator.parser.evaluation.FeatureVariable< ? > ) a ).notEqual( ( ( Number ) b ).doubleValue() );
		else if ( a instanceof Number && b instanceof FeatureVariable )
			return ( (org.mastodon.revised.ui.selection.creator.parser.evaluation.FeatureVariable< ? > ) b ).notEqual( ( ( Number ) a ).doubleValue() );
		else if ( a instanceof TagSetVariable || b instanceof TagSetVariable )
		{

			final TagSetVariable tsv;
			final Object param;
			if ( a instanceof TagSetVariable )
			{
				tsv = ( TagSetVariable ) a;
				param = b;
			}
			else
			{
				tsv = ( TagSetVariable ) b;
				param = a;
			}
			return notEqualTag( tsv, param );
		}

		errorMessage = "Cannot apply the 'not equal to' operator to " + a.getClass().getSimpleName() + " and " + b.getClass().getSimpleName() + ".";
		return null;
	}

	private Object equal( final Object a, final Object b )
	{
		if ( a instanceof FeatureVariable && b instanceof Number )
			return ( (org.mastodon.revised.ui.selection.creator.parser.evaluation.FeatureVariable< ? > ) a ).equal( ( ( Number ) b ).doubleValue() );
		else if ( a instanceof Number && b instanceof FeatureVariable )
			return ( (org.mastodon.revised.ui.selection.creator.parser.evaluation.FeatureVariable< ? > ) b ).equal( ( ( Number ) a ).doubleValue() );
		else if ( a instanceof TagSetVariable || b instanceof TagSetVariable )
		{

			final TagSetVariable tsv;
			final Object param;
			if ( a instanceof TagSetVariable )
			{
				tsv = ( TagSetVariable ) a;
				param = b;
			}
			else
			{
				tsv = ( TagSetVariable ) b;
				param = a;
			}
			return equalTag( tsv, param );
		}

		errorMessage = "Cannot apply the 'equal to' operator to " + a.getClass().getSimpleName() + " and " + b.getClass().getSimpleName() + ".";
		return null;
	}

	private Object greaterThanOrEqual( final Object a, final Object b )
	{
		if ( a instanceof FeatureVariable && b instanceof Number )
			return ( (org.mastodon.revised.ui.selection.creator.parser.evaluation.FeatureVariable< ? > ) a ).greaterThanOrEqual( ( ( Number ) b ).doubleValue() );
		else if ( a instanceof Number && b instanceof FeatureVariable )
			return ( (org.mastodon.revised.ui.selection.creator.parser.evaluation.FeatureVariable< ? > ) b ).lessThanOrEqual( ( ( Number ) a ).doubleValue() );

		errorMessage = "Cannot apply the 'greater than or equal to' operator to " + a.getClass().getSimpleName() + " and " + b.getClass().getSimpleName() + ".";
		return null;
	}

	private Object lessThanOrEqual( final Object a, final Object b )
	{
		if ( a instanceof FeatureVariable && b instanceof Number )
			return ( (org.mastodon.revised.ui.selection.creator.parser.evaluation.FeatureVariable< ? > ) a ).lessThanOrEqual( ( ( Number ) b ).doubleValue() );
		else if ( a instanceof Number && b instanceof FeatureVariable )
			return ( (org.mastodon.revised.ui.selection.creator.parser.evaluation.FeatureVariable< ? > ) b ).greaterThanOrEqual( ( ( Number ) a ).doubleValue() );

		errorMessage = "Cannot apply the 'less than or equal to' operator to " + a.getClass().getSimpleName() + " and " + b.getClass().getSimpleName() + ".";
		return null;
	}

	private Object greaterThan( final Object a, final Object b )
	{
		if ( a instanceof FeatureVariable && b instanceof Number )
			return ( (org.mastodon.revised.ui.selection.creator.parser.evaluation.FeatureVariable< ? > ) a ).greaterThan( ( ( Number ) b ).doubleValue() );
		else if ( a instanceof Number && b instanceof FeatureVariable )
			return ( (org.mastodon.revised.ui.selection.creator.parser.evaluation.FeatureVariable< ? > ) b ).lessThan( ( ( Number ) a ).doubleValue() );

		errorMessage = "Cannot apply the 'greater than' operator to " + a.getClass().getSimpleName() + " and " + b.getClass().getSimpleName() + ".";
		return null;
	}

	private Object lessThan( final Object a, final Object b )
	{
		if ( a instanceof FeatureVariable && b instanceof Number )
			return ( (org.mastodon.revised.ui.selection.creator.parser.evaluation.FeatureVariable< ? > ) a ).lessThan( ( ( Number ) b ).doubleValue() );
		else if ( a instanceof Number && b instanceof FeatureVariable )
			return ( (org.mastodon.revised.ui.selection.creator.parser.evaluation.FeatureVariable< ? > ) b ).greaterThan( ( ( Number ) a ).doubleValue() );

		errorMessage = "Cannot apply the 'less than' operator to " + a.getClass().getSimpleName() + " and " + b.getClass().getSimpleName() + ".";
		return null;
	}

	private Object not( final Object a )
	{
		if ( a instanceof TagSetVariable )
			return ( ( TagSetVariable ) a ).unset();

		errorMessage = "Cannot apply the 'not' ('!') operator to " + a.getClass() + ".";
		return null;
	}

	private Object complement( final Object a )
	{
		if ( a instanceof TagSetVariable )
			return ( ( TagSetVariable ) a ).set();

		errorMessage = "Cannot apply the 'complement' ('~') operator to " + a.getClass() + ".";
		return null;
	}

	private Object sub( final Object a, final Object b )
	{
		if ( a instanceof SelectionVariable && b instanceof SelectionVariable )
		{
			( ( SelectionVariable ) a ).inPlaceSub( ( SelectionVariable ) b );
			return a;
		}

		final Object val = defaultEvaluator.add( a, b );
		if ( val == null )
		{
			errorMessage = "Improper use of the 'sub' operator, not defined for "
					+ a.getClass().getSimpleName() + " and " + b.getClass().getSimpleName() + ". "
					+ "Use brackets to clarify operator priority.";
			return null;
		}
		return val;
	}

	private Object add( final Object a, final Object b )
	{
		if ( a instanceof SelectionVariable && b instanceof SelectionVariable )
		{
			( ( SelectionVariable ) a ).inPlaceAdd( ( SelectionVariable ) b );
			return a;
		}
		final Object val = defaultEvaluator.add( a, b );
		if ( val == null )
		{
			errorMessage = "Improper use of the 'add' operator, not defined for "
					+ a.getClass().getSimpleName() + " and " + b.getClass().getSimpleName() + ". "
					+ "Use brackets to clarify operator priority.";
			return null;
		}
		return val;
	}

	private Object neg( final Object a )
	{
		return defaultEvaluator.neg( a );
	}

	private Object pos( final Object a )
	{
		return defaultEvaluator.pos( a );
	}

	private Object parens( final Object[] args )
	{
		if ( args.length == 1 )
			return args[ 0 ];
		return Arrays.asList( args );
	}

	private Object function( final Object a, final Object b )
	{
		if ( Tokens.isVariable( a ) )
		{
			final String name = ( ( Variable ) a ).getToken();
			final Object result = callFunction( name, b );
			if ( result != null )
				return result;
		}

		// NB: Unknown function type.
		errorMessage = ( null == errorMessage )
				? "Do not know how to handle " + a + " and " + b + "."
				: errorMessage;
		return null;
	}

	@SuppressWarnings( "rawtypes" )
	private Object callFunction( final String name, final Object b )
	{
		switch ( name.toLowerCase().trim() )
		{
		case "vertexfeature":
		case "edgefeature":
		{
			return getFromFeature( name, b );
		}
		case "tagset":
		case "vertextagset":
		case "edgetagset":
		{
			if ( b instanceof String )
			{
				final String tagSetName = ( String ) b;
				return getFromTagSet( tagSetName, name );
			}
			errorMessage = "Incorrect syntax for " + name + ". Specify the tag-set name "
					+ "between single quotation marks (e.g. \"tagSet('Reviewed by')\").";
			return null;
		}
		case "morph":
		{
			if ( b instanceof List && ( ( List ) b ).size() > 1 )
			{
				final Object arg0 = ( ( List ) b ).get( 0 );
				final Object arg1 = ( ( List ) b ).get( 1 );
				return getFromMorph( arg0, arg1 );
			}
			errorMessage = "Incorrect syntax for morph. Specify a selection variable and a list of morphings "
					+ "(e.g. \"morph( vertexFeature('Spot N links') == 3, ('toVertex', 'outgoingEdges') )\".";
			return null;
		}
		}
		errorMessage = "Unkown function name: " + name + ".";
		return null;
	}

	private List< String > processMorphingTokens( final Object arg )
	{
		final List< String > switches = new ArrayList<>();
		if ( arg instanceof List )
		{
			@SuppressWarnings( "rawtypes" )
			final List tokens = ( List ) arg;
			if ( tokens.isEmpty() )
			{
				errorMessage = "Calling morph: The list of morphings is empty.";
				return null;
			}
			for ( final Object tk : tokens )
			{

				if ( !( tk instanceof String ) )
				{
					errorMessage = "Calling morph: Please specify morphings between single quotation marks "
							+ "(e.g. 'toVertex', 'incomingEdges')";
					return null;
				}
				switches.add( ( String ) tk );
			}
		}
		else if ( arg instanceof String )
			switches.add( ( String ) arg );
		else
		{
			errorMessage = "Calling morph: Please specify morphings between single quotation marks "
					+ "(e.g. 'toVertex', 'incomingEdges')";
			return null;
		}
		return switches;
	}

	private FeatureVariable< ? > getFromFeature( final String name, Object parameters )
	{
		if ( parameters instanceof String )
		{
			// Single key? Scalar feature?
			parameters = Arrays.asList( new String[] { ( String ) parameters, ( String ) parameters } );
		}

		if ( parameters instanceof List )
		{
			@SuppressWarnings( "unchecked" )
			final List< Object > list = ( List< Object > ) parameters;
			// Did we received a list of variables or strings?
			final boolean haveVariable = list.stream().anyMatch( Variable.class::isInstance );
			if ( haveVariable )
			{
				errorMessage = "Calling " + name + ": Please specify feature and projection keys between single quotation marks "
						+ "(e.g. 'Spot position', 'X')";
				return null;
			}
			if ( list.size() < 2 )
			{
				errorMessage = "Calling " + name + ": Please specify feature and projection keys as two parameters "
						+ "(e.g. \"vertexFeature('Spotposition', 'X')\" )";
				return null;
			}

			final String featureKey = ( String ) list.get( 0 );
			final String projectionKey = ( String ) list.get( 1 );

			final Feature< ?, ? > feature = featureModel.getFeature( featureKey );
			if ( null == feature )
			{
				errorMessage = "Calling " + name + ": The feature '" + featureKey + "' is unknown to the feature model.";
				return null;
			}
			if ( null == feature.getProjections().get( projectionKey ) )
			{
				errorMessage = "Calling " + name + ": The projection key '" + projectionKey + "' is unknown to the feature '" + featureKey + "'.";
				return null;
			}

			final FeatureVariable< ? > fv;
			if ( name.toLowerCase().trim().equals( "vertexfeature" ) )
			{
				if ( !featureModel.getFeatureSet( graph.vertexRef().getClass() ).contains( feature ) )
				{
					errorMessage = "Calling " + name + ": The feature '" + featureKey + "' is not defined for vertices.";
					return null;
				}
				fv = FeatureVariable.vertexFeature( graph, idmap, featureModel, featureKey, projectionKey );
			}
			else
			{
				if ( !featureModel.getFeatureSet( graph.edgeRef().getClass() ).contains( feature ) )
				{
					errorMessage = "Calling " + name + ": The feature '" + featureKey + "' is not defined for edges.";
					return null;
				}
				fv = FeatureVariable.edgeFeature( graph, idmap, featureModel, featureKey, projectionKey );
			}
			return fv;
		}
		errorMessage = "Calling " + name + ": Specify feature and projection keys as a list of two strings. "
				+ "Got: " + parameters.getClass().getSimpleName() + ".";
		return null;
	}

	private SelectionVariable getFromMorph( Object arg0, Object arg1 )
	{
		if (Tokens.isVariable( arg0 ))
			arg0 = getVariableValue( ( Variable ) arg0 );
		if (Tokens.isVariable( arg1 ))
			arg1 = getVariableValue( ( Variable ) arg1 );

		final SelectionVariable selectionVariable;
		final List< String > switches;
		if ( arg0 instanceof SelectionVariable )
		{
			selectionVariable = ( SelectionVariable ) arg0;
			switches = processMorphingTokens( arg1 );
		}
		else if ( arg1 instanceof SelectionVariable )
		{
			selectionVariable = ( SelectionVariable ) arg1;
			switches = processMorphingTokens( arg0 );
		}
		else
		{
			errorMessage = "Incorrect syntax for morph. Specify a selection variable and a list of morphings "
					+ "(e.g. \"morph( vertexFeature('Spot N links') == 3, ('toVertex', 'outgoingEdges') )\".";
			return null;
		}

		final SelectionMorpher< V, E > morpher = new SelectionMorpher<>( graph, idmap );
		final List< Morpher > morphers = new ArrayList<>();
		for ( final String sw : switches )
		{
			final Morpher mp = morpherMap.get( sw );
			if (null == mp )
			{
				errorMessage = "Incorrect syntax for morph. Unknown morpher '" + sw + "'.";
				return null;
			}
			morphers.add( mp );
		}
		final SelectionVariable morphed = morpher.morph( selectionVariable, morphers );
		return morphed;
	}

	private TagSetVariable getFromTagSet( final String tagSetName, final String functionName )
	{
		TagSet tagSet = null;
		// Find tagset from name.
		for ( final TagSet ts : tagSetModel.getTagSetStructure().getTagSets() )
			if ( ts.getName().equals( tagSetName ) )
			{
				tagSet = ts;
				break;
			}
		if ( null == tagSet )
		{
			errorMessage = "The tag-set '" + tagSetName + "' is unknown to the tag-set model.";
			return null;
		}

		final ObjTagMap< V, Tag > vertexTags = tagSetModel.getVertexTags().tags( tagSet );
		final ObjTagMap< E, Tag > edgeTags = tagSetModel.getEdgeTags().tags( tagSet );
		switch ( functionName.toLowerCase().trim() )
		{
		case "tagset":
			return new GraphTagSetVariable<>( tagSet, vertexTags, edgeTags, graph.vertices(), graph.edges(), idmap );
		case "vertextagset":
			return new VertexTagSetVariable<>( tagSet, vertexTags, graph.vertices(), idmap.vertexIdBimap() );
		case "edgetagset":
			return new EdgeTagSetVariable<>( tagSet, edgeTags, graph.edges(), idmap.edgeIdBimap() );
		}
		errorMessage = "Unkown function name: " + functionName + ".";
		return null;
	}

	private SelectionVariable equalTag( final TagSetVariable tsv, final Object param )
	{
		final Tag tag = checkTagAsParam( tsv, param );
		if (null == tag)
			return null;
		return tsv.equal( tag );
	}

	private SelectionVariable notEqualTag( final TagSetVariable tsv, final Object param )
	{
		final Tag tag = checkTagAsParam( tsv, param );
		if (null == tag)
			return null;
		return tsv.notEqual( tag );
	}

	private Tag checkTagAsParam(final TagSetVariable tsv, final Object param)
	{
		if ( param instanceof Variable )
		{
			errorMessage = "When using a comparison operator with tag-sets, specify tag-set and tag between "
					+ "single quotation marks (e.g. 'Reviewed by' == 'someone').";
			return null;
		}

		final TagSet tagSet = tsv.getTagSet();
		final List< Tag > tags = tagSet.getTags();
		Tag tag = null;
		for ( final Tag t : tags )
			if ( t.label().equals( param ) )
			{
				tag = t;
				break;
			}
		if ( null == tag )
		{
			errorMessage = "The tag '" + param + "' is unknown to the tag-set '" + tagSet.getName() + "'.";
			return null;
		}
		return tag;
	}

	public String getErrorMessage()
	{
		return errorMessage;
	}
}
