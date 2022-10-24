/*-
 * #%L
 * mastodon-selection-creator
 * %%
 * Copyright (C) 2018 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.mamut.selectioncreator.evaluation;

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
