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
 * Character tracker for processing text. This is effectively a specialized
 * iterator for processing characters in a string one at a time.
 *
 * @author Dennis M. Sosnoski
 * @version 1.0
 */

public class CharTracker
{
	/** String supplying character data. */
	private final String m_source;
	
	/** Current position within string. */
	private int m_position;

	/**
	 * Constructor
	 *
	 * @param source string supplying character data
	 * @param offset initial character position within source string
	 */
	
	public CharTracker(String source, int offset) {
		m_source = source;
		m_position = offset;
	}

	/**
	 * Get next character from string, advancing past that character.
	 *
	 * @return next character from string
	 * @exception ArrayIndexOutOfBoundsException if past end of text
	 */

	public char next() {
		if (m_position < m_source.length()) {
			return m_source.charAt(m_position++);
		} else {
			throw new ArrayIndexOutOfBoundsException(m_position);
		}
	}

	/**
	 * Peek next character from string. Gets the next character without
	 * advancing the current character position.
	 *
	 * @return next character from string
	 * @exception ArrayIndexOutOfBoundsException if past end of text
	 */

	public char peek() {
		if (m_position < m_source.length()) {
			return m_source.charAt(m_position);
		} else {
			throw new ArrayIndexOutOfBoundsException(m_position);
		}
	}

	/**
	 * Check if another character is available.
	 *
	 * @return <code>true</code> if a character is available, 
	 * <code>false</code> if at end
	 */

	public boolean hasNext() {
		return m_position < m_source.length();
	}
}
