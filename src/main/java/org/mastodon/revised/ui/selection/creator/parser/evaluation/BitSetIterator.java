package org.mastodon.revised.ui.selection.creator.parser.evaluation;

import java.util.BitSet;
import java.util.NoSuchElementException;

/**
 * Inspired from
 * https://github.com/google/guava/blob/master/guava/src/com/google/common/collect/AbstractIterator.java
 *
 * @author Jean-Yves Tinevez
 *
 */
public class BitSetIterator
{

	private static final int NO_VALUE = Integer.MIN_VALUE;

	private final BitSet set;

	private int next;

	private State state;

	private int index;

	public BitSetIterator( final BitSet set )
	{
		this.set = set;
		this.index = 0;
		this.state = State.NOT_READY;
	}

	private enum State
	{
		/** We have computed the next element and haven't returned it yet. */
		READY,

		/** We haven't yet computed or have already returned the element. */
		NOT_READY,

		/** We have reached the end of the data and are finished. */
		DONE,
	}

	private int computeNext()
	{
		final int val = set.nextSetBit( index );
		state = ( val < 0 ) ? State.DONE : State.READY;
		index = val + 1;
		return val;
	}

	private boolean tryToComputeNext()
	{
		next = computeNext();
		if ( state == State.READY && next < Integer.MAX_VALUE )
			return true;

		return false;
	}

	public final boolean hasNext()
	{
		switch ( state )
		{
		case DONE:
			return false;
		case READY:
			return true;
		default:
		}
		return tryToComputeNext();
	}

	public final int next()
	{
		if ( !hasNext() ) { throw new NoSuchElementException(); }
		state = State.NOT_READY;
		final int result = next;
		next = NO_VALUE;
		return result;
	}
}
