package org.mastodon.revised.ui.selection.creator.evaluation;

import java.util.BitSet;

public class EmptyFeatureVariable< O > implements FeatureVariable< O >
{

	@Override
	public SelectionVariable notEqual( final double value )
	{
		return new SelectionVariable( new BitSet(), new BitSet() );
	}

	@Override
	public SelectionVariable equal( final double value )
	{
		return new SelectionVariable( new BitSet(), new BitSet() );
	}

	@Override
	public SelectionVariable greaterThanOrEqual( final double threshold )
	{
		return new SelectionVariable( new BitSet(), new BitSet() );
	}

	@Override
	public SelectionVariable lessThanOrEqual( final double threshold )
	{
		return new SelectionVariable( new BitSet(), new BitSet() );
	}

	@Override
	public SelectionVariable greaterThan( final double threshold )
	{
		return new SelectionVariable( new BitSet(), new BitSet() );
	}

	@Override
	public SelectionVariable lessThan( final double threshold )
	{
		return new SelectionVariable( new BitSet(), new BitSet() );
	}

	@Override
	public String toString()
	{
		return "EmptyFeature()";
	}
}
