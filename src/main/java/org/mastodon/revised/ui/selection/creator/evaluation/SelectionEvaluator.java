package org.mastodon.revised.ui.selection.creator.evaluation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Deque;
import java.util.List;

import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.model.SelectionModel;
import org.mastodon.revised.model.feature.FeatureModel;
import org.mastodon.revised.model.tag.ObjTagMap;
import org.mastodon.revised.model.tag.TagSetModel;
import org.mastodon.revised.model.tag.TagSetStructure.Tag;
import org.mastodon.revised.model.tag.TagSetStructure.TagSet;
import org.scijava.parse.Function;
import org.scijava.parse.Operator;
import org.scijava.parse.Operators;
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

	public SelectionEvaluator( final ReadOnlyGraph< V, E > graph, final GraphIdBimap< V, E > idmap, final TagSetModel< V, E > tagSetModel, final FeatureModel featureModel, final SelectionModel< V, E > selectionModel )
	{
		this.graph = graph;
		this.idmap = idmap;
		this.tagSetModel = tagSetModel;
		this.featureModel = featureModel;
		this.selectionModel = selectionModel;
		this.defaultEvaluator = new DefaultEvaluator();
	}

	@Override
	public Object execute( final Operator op, final Deque< Object > stack )
	{
		System.out.println( "\nGot:" ); // DEBUG
		System.out.println( op ); // DEBUG
		System.out.println( stack ); // DEBUG

		// Pop the arguments.
		final int arity = op.getArity();
		final Object[] args = new Object[ arity ];
		for ( int i = args.length - 1; i >= 0; i-- )
		{
			args[ i ] = stack.pop();
		}
		final Object a = args.length > 0 ? args[ 0 ] : null;
		final Object b = args.length > 1 ? args[ 1 ] : null;

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
		if ( op == Operators.ADD || op == Operators.LOGICAL_OR )
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
		return null;
	}

	private Object and( final Object a, final Object b )
	{
		if ( a instanceof SelectionVariable && b instanceof SelectionVariable )
		{
			( ( SelectionVariable ) a ).inPlaceAnd( ( SelectionVariable ) b );
			return a;
		}
		return null;
	}

	private Object notEqual( final Object a, final Object b )
	{
		if ( a instanceof FeatureVariable && b instanceof Number )
			return ( ( FeatureVariable< ? > ) a ).notEqual( ( ( Number ) b ).doubleValue() );
		else if ( a instanceof Number && b instanceof FeatureVariable )
			return ( ( FeatureVariable< ? > ) b ).notEqual( ( ( Number ) a ).doubleValue() );
		else if ( a instanceof TagSetVariable && b instanceof Variable )
		{
			final TagSetVariable tsv = ( TagSetVariable ) a;
			final Tag tag = getTagFromName( ( ( Variable ) b ).getToken(), tsv.getTagSet() );
			if ( null == tag )
				return new SelectionVariable( new BitSet(), new BitSet() );
			return tsv.notEqual( tag );
		}
		else if ( b instanceof TagSetVariable && a instanceof Variable )
		{
			final TagSetVariable tsv = ( TagSetVariable ) b;
			final Tag tag = getTagFromName( ( ( Variable ) a ).getToken(), tsv.getTagSet() );
			if ( null == tag )
				return new SelectionVariable( new BitSet(), new BitSet() );
			return tsv.notEqual( tag );
		}
		return null;
	}

	private Object equal( final Object a, final Object b )
	{
		if ( a instanceof FeatureVariable && b instanceof Number )
			return ( ( FeatureVariable< ? > ) a ).equal( ( ( Number ) b ).doubleValue() );
		else if ( a instanceof Number && b instanceof FeatureVariable )
			return ( ( FeatureVariable< ? > ) b ).equal( ( ( Number ) a ).doubleValue() );
		else if ( a instanceof TagSetVariable && b instanceof Variable )
		{
			final TagSetVariable tsv = ( TagSetVariable ) a;
			final Tag tag = getTagFromName( ( ( Variable ) b ).getToken(), tsv.getTagSet() );
			if ( null == tag )
				return new SelectionVariable( new BitSet(), new BitSet() );
			return tsv.equal( tag );
		}
		else if ( b instanceof TagSetVariable && a instanceof Variable )
		{
			final TagSetVariable tsv = ( TagSetVariable ) b;
			final Tag tag = getTagFromName( ( ( Variable ) a ).getToken(), tsv.getTagSet() );
			if ( null == tag )
				return new SelectionVariable( new BitSet(), new BitSet() );
			return tsv.equal( tag );
		}
		return null;
	}

	private Object greaterThanOrEqual( final Object a, final Object b )
	{
		if ( a instanceof FeatureVariable && b instanceof Number )
			return ( ( FeatureVariable< ? > ) a ).greaterThanOrEqual( ( ( Number ) b ).doubleValue() );
		else if ( a instanceof Number && b instanceof FeatureVariable )
			return ( ( FeatureVariable< ? > ) b ).lessThanOrEqual( ( ( Number ) a ).doubleValue() );
		return null;
	}

	private Object lessThanOrEqual( final Object a, final Object b )
	{
		if ( a instanceof FeatureVariable && b instanceof Number )
			return ( ( FeatureVariable< ? > ) a ).lessThanOrEqual( ( ( Number ) b ).doubleValue() );
		else if ( a instanceof Number && b instanceof FeatureVariable )
			return ( ( FeatureVariable< ? > ) b ).greaterThanOrEqual( ( ( Number ) a ).doubleValue() );
		return null;
	}

	private Object greaterThan( final Object a, final Object b )
	{
		if ( a instanceof FeatureVariable && b instanceof Number )
			return ( ( FeatureVariable< ? > ) a ).greaterThan( ( ( Number ) b ).doubleValue() );
		else if ( a instanceof Number && b instanceof FeatureVariable )
			return ( ( FeatureVariable< ? > ) b ).lessThan( ( ( Number ) a ).doubleValue() );
		return null;
	}

	private Object lessThan( final Object a, final Object b )
	{
		if ( a instanceof FeatureVariable && b instanceof Number )
			return ( ( FeatureVariable< ? > ) a ).lessThan( ( ( Number ) b ).doubleValue() );
		else if ( a instanceof Number && b instanceof FeatureVariable )
			return ( ( FeatureVariable< ? > ) b ).greaterThan( ( ( Number ) a ).doubleValue() );
		return null;
	}

	private Object not( final Object a )
	{
		if ( a instanceof TagSetVariable )
			return ( ( TagSetVariable ) a ).unset();
		return null;
	}

	private Object sub( final Object a, final Object b )
	{
		if ( a instanceof SelectionVariable && b instanceof SelectionVariable )
		{
			( ( SelectionVariable ) a ).inPlaceSub( ( SelectionVariable ) b );
			return a;
		}
		return defaultEvaluator.sub( a, b );
	}

	private Object add( final Object a, final Object b )
	{
		if ( a instanceof SelectionVariable && b instanceof SelectionVariable )
		{
			( ( SelectionVariable ) a ).inPlaceAdd( ( SelectionVariable ) b );
			return a;
		}
		return defaultEvaluator.add( a, b );
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
		System.out.println( "Function with parameters " + a + " and " + b ); // DEBUG
		if ( Tokens.isVariable( a ) )
		{
			final String name = ( ( Variable ) a ).getToken();
			final Object result = callFunction( name, b );
			if ( result != null )
				return result;
		}

		// NB: Unknown function type.
		return null;
	}

	@SuppressWarnings( "rawtypes" )
	private Object callFunction( final String name, final Object b )
	{
		System.out.println( " - Calling function " + name + " on " + b ); // DEBUG
		switch(name.toLowerCase().trim())
		{
		case "vertexfeature":
		case "edgefeature":
		{
			if ( b instanceof List )
			{
				@SuppressWarnings( "unchecked" )
				final String featureKey = ( ( List< Variable > ) b ).get( 0 ).getToken();
				@SuppressWarnings( "unchecked" )
				final String projectionKey = ( ( List< Variable > ) b ).get( 1 ).getToken();
				final FeatureVariable< ? > fv;
				if ( name.toLowerCase().trim().equals( "vertexfeature" ) )
					fv = FeatureVariable.vertexFeature( graph, idmap, featureModel, featureKey, projectionKey );
				else
					fv = FeatureVariable.edgeFeature( graph, idmap, featureModel, featureKey, projectionKey );
				return fv;
			}
			break;
		}
		case "tagset":
		case "vertextagset":
		case "edgetagset":
		{
			if ( b instanceof String )
			{
				final String tagSetName = ( String ) b;
				TagSet tagSet = null;
				// Find tagset from name.
				for ( final TagSet ts : tagSetModel.getTagSetStructure().getTagSets() )
					if ( ts.getName().equals( tagSetName ) )
					{
						tagSet = ts;
						break;
					}
				if ( null == tagSet )
					return new EmptyTagSetVariable();

				final ObjTagMap< V, Tag > vertexTags = tagSetModel.getVertexTags().tags( tagSet );
				final ObjTagMap< E, Tag > edgeTags = tagSetModel.getEdgeTags().tags( tagSet );
				switch ( name.toLowerCase().trim() )
				{
				case "tagset":
					return new GraphTagSetVariable<>( tagSet, vertexTags, edgeTags, graph.vertices(), graph.edges(), idmap );
				case "vertextagset":
					return new VertexTagSetVariable<>( tagSet, vertexTags, graph.vertices(), idmap.vertexIdBimap() );
				case "edgetagset":
					return new EdgeTagSetVariable<>( tagSet, edgeTags, graph.edges(), idmap.edgeIdBimap() );
				}
			}
			break;
		}
		case "morph":
		{
			if ( b instanceof List && ( ( List ) b ).size() > 1 )
			{
				final Object arg0 = ( ( List ) b ).get( 0 );
				final Object arg1 = ( ( List ) b ).get( 1 );
				final SelectionVariable selectionVariable;
				final List< String > switches = new ArrayList<>();
				if ( arg0 instanceof SelectionVariable )
				{
					selectionVariable = ( SelectionVariable ) arg0;
					if (arg1 instanceof List)
					{
						@SuppressWarnings( "unchecked" )
						final List<Variable> tokens = ( List< Variable > ) arg1;
						for ( final Variable tk : tokens )
							switches.add( tk.getToken() );
					}
					else if (arg1 instanceof Variable)
						switches.add( ( ( Variable ) arg1 ).getToken() );
					else
						return null;
				}
				else if ( arg1 instanceof SelectionVariable )
				{
					selectionVariable = ( SelectionVariable ) arg1;
					if ( arg0 instanceof List )
					{
						@SuppressWarnings( "unchecked" )
						final List< Variable > tokens = ( List< Variable > ) arg0;
						for ( final Variable tk : tokens )
							switches.add( tk.getToken() );
					}
					else if ( arg0 instanceof Variable )
						switches.add( ( ( Variable ) arg0 ).getToken() );
					else
						return null;
				}
				else
					return null;


			}
			break;
		}
		}
		System.out.println( "Trolololo: " + b ); // DEBUG
		return null;
	}

	private Tag getTagFromName( final String b, final TagSet tagSet )
	{
		if ( tagSet == null )
			return null;
		final List< Tag > tags = tagSet.getTags();
		for ( final Tag tag : tags )
			if ( tag.label().equals( b ) )
				return tag;
		return null;
	}

}
