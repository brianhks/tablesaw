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

import java.lang.reflect.Field;

/**
 * Base class for command line parameter definitions. This is used for simple
 * command line parameters of various flavors. Subclasses define the particular
 * types of parameters supported.
 *
 * @author Dennis M. Sosnoski
 * @version 1.0
 */

public abstract class ParameterDef
{
	/** Argument flag character. */
	protected final char m_char;

	/** Name of field holding parameter value. */
	protected final String m_name;
	
	/** Argument description text. */
	protected final String m_description;
	
	/** Information for field linked to parameter. */
	protected Field m_field;

	/**
	 * Constructor
	 *
	 * @param chr parameter flag character
	 * @param name field name for parameter
	 * @param desc discription text for parameter (may be <code>null</code>)
	 * @throws IllegalArgumentException if the field is not accessible
	 */
	
	protected ParameterDef(char chr, String name, String desc) {
		m_char = chr;
		m_name = name;
		m_description = (desc == null) ? "[see code for description]" : desc;
	}

	/**
	 * Get text abbreviation for parameter. The default format is just "-c", 
	 * where 'c' is the flag character for the parameter. If a different format 
	 * is needed by a subclass it should override this method.
	 *
	 * @return text abbreviation for showing parameter
	 */

	public String getAbbreviation() {
		return "-" + m_char;
	}

	/**
	 * Get flag character for parameter.
	 *
	 * @return flag character specifying the parameter
	 */

	public char getFlag() {
		return m_char;
	}

	/**
	 * Get text of parameter description.
	 *
	 * @param file file to be read
	 * @return array of bytes containing all data from file
	 * @throws IOException on file access error
	 */

	public String getDescription() {
		return m_description;
	}

	/**
	 * Bind parameter to target class field. This will generally be overridden
	 * by subclasses to verify the field type found, but should be called during
	 * the subclass processing.
	 *
	 * @param clas target class for saving parameter values
	 */

	protected void bindToClass(Class clas) {
		try {
			m_field = clas.getDeclaredField(m_name);
			m_field.setAccessible(true);
		} catch (NoSuchFieldException ex) {
			throw new IllegalArgumentException("Field '" + m_name +
				"' not found in " + clas.getName());
		}
	}

	/**
	 * Handle argument. This abstract method must be overridden in each
	 * subclass to perform the appropriate processing, if necessary using
	 * additional characters from the current argument or the next argument
	 * in the list.
	 *
	 * @param proc argument processor making call to handler
	 */

	public abstract void handle(ArgumentProcessor proc);
}
