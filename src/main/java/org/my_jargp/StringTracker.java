/*
Copyright (c) 2003, Dennis M. Sosnoski
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.
 * Neither the name of JargP nor the names of its contributors may be used
   to endorse or promote products derived from this software without specific
   prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package org.my_jargp;

/**
 * String tracker for processing an array of strings. This is effectively a
 * specialized iterator for processing an array of strings one at a time.
 *
 * @author Dennis M. Sosnoski
 * @version 1.0
 */

public class StringTracker
{
	/** Array supplying string data. */
	private final String[] m_source;
	
	/** Current position within array. */
	private int m_position;

	/**
	 * Constructor
	 *
	 * @param source array supplying string data
	 * @param offset initial string position within source array
	 */
	
	public StringTracker(String[] source, int offset) {
		m_source = source;
		m_position = offset;
	}

	/**
	 * Get next string from array, advancing past that string.
	 *
	 * @return next string from array
	 * @exception ArrayIndexOutOfBoundsException if past end of array
	 */

	public String next() {
		return m_source[m_position++];
	}

	/**
	 * Peek next string from array. Gets the next string without
	 * advancing the current string position.
	 *
	 * @return next string from array
	 * @exception ArrayIndexOutOfBoundsException if past end of array
	 */

	public String peek() {
		return m_source[m_position];
	}

	/**
	 * Check if another string is available.
	 *
	 * @return <code>true</code> if a string is available, 
	 * <code>false</code> if at end
	 */

	public boolean hasNext() {
		return m_position < m_source.length;
	}

	/**
	 * Get position of next string in array.
	 *
	 * @return offset in array of next string
	 */

	public int nextOffset() {
		return m_position;
	}

	/**
	 * Get length of array.
	 *
	 * @return total number of strings in array
	 */

	public int length() {
		return m_source.length;
	}

    /**
     * Get count of entries remaining.
     *
     * @return remaining number of strings in array
     */

    public int countRemaining() {
        return m_source.length - m_position;
    }
}