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
 * Command line flag definition. This defines a simple command line flag that
 * sets a boolean parameter value. Both <code>true</code> and <code>false</code>
 * settings are supported.
 *
 * @author Dennis M. Sosnoski
 * @version 1.0
 */

public class BoolDef extends ParameterDef
{
	/** Value set when flag is seen. */
	protected boolean m_value;

	/**
	 * Constructor with flag sense specified and description.
	 *
	 * @param chr parameter flag character
	 * @param name field name for parameter
	 * @param desc discription text for parameter (may be <code>null</code>)
	 * @param sense value set when flag is seen
	 */
	
	public BoolDef(char chr, String name, String desc, boolean sense) {
		super(chr, name, desc);
		m_value = sense;
	}

	/**
	 * Constructor with flag sense specified.
	 *
	 * @param chr parameter flag character
	 * @param name field name for parameter
	 * @param sense value set when flag is seen
	 */
	
	public BoolDef(char chr, String name, boolean sense) {
		this(chr, name, null, sense);
	}

	/**
	 * Constructor defaulting to flag <code>true</code> with description.
	 *
	 * @param chr parameter flag character
	 * @param name field name for parameter
	 * @param desc discription text for parameter
	 */
	
	public BoolDef(char chr, String name, String desc) {
		this(chr, name, desc, true);
	}

	/**
	 * Constructor defaulting to flag <code>true</code>.
	 *
	 * @param chr parameter flag character
	 * @param name field name for parameter
	 */
	
	public BoolDef(char chr, String name) {
		this(chr, name, null, true);
	}

	/**
	 * Bind parameter to target class field.
	 *
	 * @param clas target class for saving parameter values
	 * @throws IllegalArgumentException if the field is not a boolean
	 */

	protected void bindToClass(Class clas) {
		super.bindToClass(clas);
		Class type = m_field.getType();
		if (type != Boolean.class && type != Boolean.TYPE) {
			throw new IllegalArgumentException("Field '" + m_name + "'in " +
				clas.getName() + " is not of type boolean");
		}
	}

	/**
	 * Handle argument. This implementation of the abstract base class method
	 * just sets the parameter value as appropriate for the flag.
	 *
	 * @param proc argument processor making call to handler
	 * @throws IllegalArgumentException on error in processing
	 */

	public void handle(ArgumentProcessor proc) {
		proc.setValue(m_value ? Boolean.TRUE : Boolean.FALSE, m_field);
	}
}
