package org.mastodon.revised.ui.selection.creator.parser.evaluation;

public class EmptyFeatureVariable< O > implements FeatureVariable< O >
{

	@Override
	public SelectionVariable notEqual( final double value )
	{
		return new SelectionVariable();
	}

	@Override
	public SelectionVariable equal( final double value )
	{
		return new SelectionVariable();
	}

	@Override
	public SelectionVariable greaterThanOrEqual( final double threshold )
	{
		return new SelectionVariable();
	}

	@Override
	public SelectionVariable lessThanOrEqual( final double threshold )
	{
		return new SelectionVariable();
	}

	@Override
	public SelectionVariable greaterThan( final double threshold )
	{
		return new SelectionVariable();
	}

	@Override
	public SelectionVariable lessThan( final double threshold )
	{
		return new SelectionVariable();
	}

	@Override
	public String toString()
	{
		return "EmptyFeature()";
	}
}
