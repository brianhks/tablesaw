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
 * Command line parameter collection definition. Each collection consists of
 * some number of parameter definitions. Multiple collections may be linked
 * to function as a single collection.
 *
 * @author Dennis M. Sosnoski
 * @version 1.0
 */

public class ParameterSet
{
	/** Arguments known by this handler. */
	private final ParameterDef[] m_knownArguments;

	/** Next parameter set for control flags not included in this one. */
	private final ParameterSet m_nextSet;

	/**
	 * Constructor
	 *
	 * @param defs parameter definitions for this handler
	 * @param next parameter set used for parameters not defined in this set
	 */
	
	public ParameterSet(ParameterDef[] defs, ParameterSet next) {
		m_knownArguments = defs;
		m_nextSet = next;
	}

	/**
	 * Find the parameter definition for a particular control flag. If the 
	 * control flag is not defined in the set this will pass the call on
	 * to the next set until we reach the end of the chain.
	 *
	 * @param flag control flag for parameter
	 * @param parameter definition, or <code>null</code> if not defined
	 */

	/*package*/ ParameterDef findDef(char flag) {
		for (int i = 0; i < m_knownArguments.length; i++) {
			if (flag == m_knownArguments[i].getFlag()) {
				return m_knownArguments[i];
			}
		}
		if (m_nextSet == null) {
			return null;
		} else {
			return m_nextSet.findDef(flag);
		}
	}

	/**
	 * Get the parameter definition at a particular position in the list. If
	 * the index value supplied is not defined in the set this will pass the
	 * call on to the next set until we reach the end of the chain. The caller
	 * can index through all defined values by starting at zero and
	 * incrementing until a <code>null</code> is returned.
	 *
	 * @param index position for parameter definition to be returned
	 * @param parameter definition, or <code>null</code> if not defined
	 */

	/*package*/ ParameterDef indexDef(int index) {
		if (index < m_knownArguments.length) {
			return m_knownArguments[index];
		} else if (m_nextSet == null) {
			return null;
		} else {
			return m_nextSet.indexDef(index);
		}
	}
}
