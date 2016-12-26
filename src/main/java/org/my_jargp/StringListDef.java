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

import java.util.List;

/**
 * Command line string list parameter definition. This defines a command line
 * flag with an associated string value which may be used repeatedly. The
 * argument giving the parameter value must be the next unused argument from the
 * command line following each occurrance of the flag, and must not begin with
 * the '-' character used to indicate control argument flags.
 *
 * @author Dennis M. Sosnoski
 * @version 1.0
 */

public class StringListDef extends ParameterDef
{
	/**
	 * Constructor with description.
	 *
	 * @param chr parameter flag character
	 * @param name field name for parameter
	 * @param desc discription text for parameter
	 */
	
	public StringListDef(char chr, String name, String desc) {
		super(chr, name, desc);
	}

	/**
	 * Constructor without description.
	 *
	 * @param chr parameter flag character
	 * @param name field name for parameter
	 */
	
	public StringListDef(char chr, String name) {
		this(chr, name, null);
	}

	/**
	 * Bind parameter to target class field.
	 *
	 * @param clas target class for saving parameter values
	 * @throws IllegalArgumentException if the field is not a List
	 */

	protected void bindToClass(Class clas) {
		super.bindToClass(clas);
		Class type = m_field.getType();
		if (!List.class.isAssignableFrom(type)) {
			throw new IllegalArgumentException("Field '" + m_name + "'in " +
				clas.getName() + " is not a List type");
		}
	}

	/**
	 * Handle argument. This implementation of the abstract base class method
	 * makes sure that we have another command line argument available, and
	 * checks that the argument does not begin with the '-' character used to
	 * indicate control argument flags. If these conditions are met the
	 * string value of the argument is added to the field list.
	 *
	 * @param proc argument processor making call to handler
	 * @throws ArgumentErrorException if argument value missing or malformed
	 * @throws IllegalArgumentException on error in processing
	 */

	public void handle(ArgumentProcessor proc) {
		StringTracker args = proc.getArgs();
		if (args.hasNext()) {
			String arg = args.next();
			if (arg.length() > 0 && arg.charAt(0) == '-') {
				proc.reportArgumentError(m_char, 
					"Argument value starts with '-'");
			} else {
				proc.addValue(arg, m_field);
			}
		} else {
			proc.reportArgumentError(m_char, "Argument value missing");
		}
	}
}
