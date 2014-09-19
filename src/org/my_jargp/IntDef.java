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
 * Command line integer parameter definition. This defines a command line flag
 * with an associated integer value. The optionally signed value must 
 * immediately follow the flag character within the same argument string.
 *
 * @author Dennis M. Sosnoski
 * @version 1.0
 */

public class IntDef extends ParameterDef
{
	/** Minimum allowed argument value. */
	private final int m_min;
	
	/** Maximum allowed argument value. */
	private final int m_max;

	/**
	 * Constructor with range and description.
	 *
	 * @param chr parameter flag character
	 * @param name field name for parameter
	 * @param desc discription text for parameter
	 * @param min minimum allowed value
	 * @param max maximum allowed value
	 */
	
	public IntDef(char chr, String name, String desc, int min, int max) {
		super(chr, name, desc);
		m_min = min;
		m_max = max;
	}

	/**
	 * Constructor with range but no description.
	 *
	 * @param chr parameter flag character
	 * @param name field name for parameter
	 * @param min minimum allowed value
	 * @param max maximum allowed value
	 */
	
	public IntDef(char chr, String name, int min, int max) {
		this(chr, name, null, min, max);
	}

	/**
	 * Constructor with no range defined.
	 *
	 * @param chr parameter flag character
	 * @param name field name for parameter
	 * @param desc discription text for parameter
	 */
	
	public IntDef(char chr, String name, String desc) {
		this(chr, name, desc, Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	/**
	 * Constructor with no range or description.
	 *
	 * @param chr parameter flag character
	 * @param name field name for parameter
	 */
	
	public IntDef(char chr, String name) {
		this(chr, name, null);
	}

	/**
	 * Get text abbreviation for parameter. This override of the base class
	 * method returns "-cNN", where 'c' is the flag character for the parameter.
	 *
	 * @return text abbreviation for showing parameter
	 */

	public String getAbbreviation() {
		return "-" + m_char + "NN";
	}

	/**
	 * Bind parameter to target class field.
	 *
	 * @param clas target class for saving parameter values
	 * @throws IllegalArgumentException if the field is not an int
	 */

	protected void bindToClass(Class clas) {
		super.bindToClass(clas);
		Class type = m_field.getType();
		if (type != Integer.class && type != Integer.TYPE) {
			throw new IllegalArgumentException("Field '" + m_name + "'in " +
				clas.getName() + " is not of type int");
		}
	}

	/**
	 * Handle argument. This implementation of the abstract base class method
	 * interprets the characters following the flag character as an optionally
	 * signed decimal value. If the value is within the allowed range the
	 * parameter is set to that value. Other flag characters may follow the
	 * numeric value within the argument.
	 *
	 * @param proc argument processor making call to handler
	 * @throws ArgumentErrorException if decimal value missing or out of range
	 * @throws IllegalArgumentException on error in processing
	 */

	public void handle(ArgumentProcessor proc) {
		
		// set up for validating
		boolean minus = false;
		boolean digits = false;
		long value = 0;
		CharTracker track = proc.getChars();
		StringBuffer text = new StringBuffer();
		StringTracker args = proc.getArgs();
		if (track.hasNext()) 
			{
			// check for leading sign flag
			char chr = track.peek();
			if (chr == '-' || chr == '+') 
				{
				text.append(chr);
				track.next();
				}
			
			// accumulate all digits in value
			while (track.hasNext()) 
				{
				chr = track.peek();
				if (chr >= '0' && chr <= '9') 
					{
					text.append(chr);
					track.next();
					} 
				else 
					break;
				}
			}
		else if (args.hasNext())
			{
			String arg = args.next();
			text.append(arg);
			}
		
		// make sure we have a valid value
		try 
			{
			value = Integer.parseInt(text.toString());
			} 
		catch (NumberFormatException ex) 
			{
			proc.reportArgumentError(m_char, ex.getMessage());
			}
		
		if (value < m_min || value > m_max) {
			proc.reportArgumentError(m_char, "Value out of range");
		} else {
			proc.setValue(new Integer((int)value), m_field);
		}
	}
}
