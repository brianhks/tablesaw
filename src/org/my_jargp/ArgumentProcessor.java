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

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.List;

/**
 * Command line parameter processing handler. Organizes all the parameter
 * information, including the data object to which parameter values defined
 * by the command line are stored. Provides specialized processing for the
 * argument strings, including recognizing the '-' character at the start of
 * an argument as indicating that the argument provides control information 
 * (flags and possibly embedded values) as opposed to data.
 *
 * @author Dennis M. Sosnoski
 * @version 1.0
 */

public class ArgumentProcessor
{
	/** Head of parameter set chain. */
	private final ParameterSet m_parameterSet;
	
	/** Character tracker for current argument. */
	private CharTracker m_currentArg;
	
	/** Current argument position in list. */
	private int m_currentIndex;

	/** String tracker for full set of arguments. */
	private StringTracker m_remainingArgs;
	
	/** Argument data object. */
	private Object m_targetObject;

	/**
	 * Constructor from parameter set definition.
	 *
	 * @param set head parameter set in possible chain of sets defined
	 */
	
	public ArgumentProcessor(ParameterSet set) {
		m_parameterSet = set;
	}

	/**
	 * Constructor from array of parameter definitions.
	 *
	 * @param set head parameter set in possible chain of sets defined
	 */
	
	public ArgumentProcessor(ParameterDef[] defs) {
		this(new ParameterSet(defs, null));
	}

	/**
	 * Bind parameter definitions to target object class. This goes through the
	 * set of defined parameters, binding each to the class of the supplied
	 * target object and finding the associated field information. Rather than
	 * doing this binding of all parameter definitions prior to processing the
	 * command line information, it'd also be possible (and even more efficient)
	 * to just lookup the field information for each parameter actually present
	 * in the list. The only advantage of doing this lookup in advance is that
	 * it insures that configuration errors are reported whether the parameter
	 * in error is used or not.
	 *
	 * @param parm data object for parameter values
	 * @throws ArgumentErrorException on error in data
	 * @throws IllegalArgumentException on error in processing
	 */
	
	private void bindDefinitions(Object parm) {
		int index = 0;
		Class clas = parm.getClass();
		ParameterDef def;
		while ((def = m_parameterSet.indexDef(index++)) != null) {
			def.bindToClass(clas);
		}
	}

	/**
	 * Process argument list control information. Processes control flags
	 * present in the supplied argument list, setting the associated parameter
	 * values. Arguments not consumed in the control flag processing are
	 * available for access using other methods after the return from this
	 * call.
	 *
	 * @param args command line argument string array
	 * @param target application object defining parameter fields
	 * @throws ArgumentErrorException on error in data
	 * @throws IllegalArgumentException on error in processing
	 */
	
	public Object processArgs(String[] args, Object target) {
		
		// verify field definitions for all parameters
		bindDefinitions(target);
		
		// clean up argument text (may have CR-LF line ends, confusing Linux)
		String[] trims = new String[args.length];
		for (int i = 0; i < args.length; i++) {
			trims[i] = args[i].trim();
		}
		
		// initialize argument list information
		m_currentArg = new CharTracker("", 0);
		m_remainingArgs = new StringTracker(trims, 0);
		m_targetObject = target;
		
		// loop for processing all argument values present
		while (true) {
			if (m_currentArg.hasNext()) {
				
				// find the parameter definition for current flag character
				char flag = m_currentArg.next();
				ParameterDef def = m_parameterSet.findDef(flag);
				if (def != null) {
					
					// process the argument
					def.handle(this);
					
				} else if (flag == ' ') {
					break;  // preserve old functionality
				} else {
					throw new IllegalArgumentException("Control flag '" + 
						flag + "' in argument " + m_currentIndex + 
						" is not defined");
				}
				
			} else if (m_remainingArgs.hasNext()) {
				
				// check if more control flags in next argument
				String next = m_remainingArgs.peek();
				if (next.length() > 0 && next.charAt(0) == '-') {
					m_remainingArgs.next();
					m_currentIndex = m_remainingArgs.nextOffset();
					m_currentArg = new CharTracker(next, 1);
				} else if (next.length() > 0) {
					m_currentArg = new CharTracker("- ", 1);
				} else {
					break;
				}
				
			} else {
				break;
			}
		}
		return m_targetObject;
	}

	/**
	 * Get current control argument character information. The caller can 
	 * consume characters from the current argument as needed.
	 *
	 * @return argument string tracking information
	 */

	/*package*/ CharTracker getChars() {
		return m_currentArg;
	}

	/**
	 * Get current argument position in list.
	 *
	 * @return offset in argument list of current flag argument
	 */

	/*package*/ int getIndex() {
		return m_currentIndex;
	}

	/**
	 * Get argument list information. The caller can comsume arguments
	 * from the list as needed.
	 *
	 * @return argument list information
	 */

	public StringTracker getArgs() {
		return m_remainingArgs;
	}

	/**
	 * Set parameter value. Uses reflection to set a value within the
	 * target data object.
	 *
	 * @param value value to be set for parameter
	 * @param field target field for parameter value
	 * @throws IllegalArgumentException on error in setting parameter value
	 */

	/*package*/ void setValue(Object value, Field field) {
		try {
            field.setAccessible(true);
			field.set(m_targetObject, value);
		} catch (IllegalAccessException ex) {
			throw new IllegalArgumentException("Field " + field.getName() +
				" is not accessible in object of class " + 
				m_targetObject.getClass().getName());
		}
	}

    /**
     * Add parameter value to list. Uses reflection to retrieve the list object
     * and add a value to those present in the list.
     *
     * @param value value to be added to list
     * @param field target list field
     * @throws IllegalArgumentException on error in adding parameter value
     */

    /*package*/ void addValue(Object value, Field field) {
        try {
            field.setAccessible(true);
            List list = (List)field.get(m_targetObject);
            if (list == null) {
                list = (List)field.getType().newInstance();
                field.set(m_targetObject, list);
            }
            list.add(value);
        } catch (IllegalAccessException ex) {
            throw new IllegalArgumentException("Field " + field.getName() +
                " is not accessible in object of class " + 
                m_targetObject.getClass().getName());
        } catch (InstantiationException ex) {
            throw new IllegalArgumentException("Unable to create instance of " +
                field.getType().getName() + " for storing to list field " +
                field.getName() + " in object of class " + 
                m_targetObject.getClass().getName());
        }
    }

	/**
	 * Report argument error. Generates an exception with information about
	 * the argument causing the problem.
	 *
	 * @param flag argument flag character
	 * @param text error message text
	 * @throws ArgumentErrorException reporting the error
	 */

	public void reportArgumentError(char flag, String text) {
		throw new ArgumentErrorException(text + " for parameter '" + 
			flag + "' in argument " + m_currentIndex);
	}

	/**
	 * List known parameter definitions. This lists all known parameter
	 * definitions in fixed maximum width format.
	 *
	 * @param width maximum number of columns in listing
	 * @param print print stream destination for listing definitions
	 */

	public void listParameters(int width, PrintStream print) {
		
		// scan once to find maximum parameter abbreviation length
		int count = 0;
		int maxlen = 0;
		ParameterDef def = null;
		while ((def = m_parameterSet.indexDef(count)) != null) {
			int length = def.getAbbreviation().length();
			if (maxlen < length) {
				maxlen = length;
			}
			count++;
		}
		
		// initialize for handling text generation
		StringBuffer line = new StringBuffer(width);
		int lead = maxlen + 2;
		char[] blanks = new char[lead];
		for (int i = 0; i < lead; i++) {
			blanks[i] = ' ';
		}
		
		// scan again to print text of definitions
		for (int i = 0; i < count; i++) {
			
			// set up lead parameter abbreviation for first line
			line.setLength(0);
			def = m_parameterSet.indexDef(i);
			line.append(' ');
			line.append(def.getAbbreviation());
			line.append(blanks, 0, lead-line.length());
			
			// format description text in as many lines as needed
			String text = def.getDescription();
			while (line.length()+text.length() > width) {
				
				// scan for first line break position (even if beyond limit)
				int limit = width - line.length();
				int mark = text.indexOf(' ');
				if (mark >= 0) {
					
					// find break position closest to limit
					int split = mark;
					while (mark >= 0 && mark <= limit) {
						split = mark;
						mark = text.indexOf(' ', mark+1);
					}
					
					// split the description for printing line
					line.append(text.substring(0, split));
					print.println(line.toString());
					line.setLength(0);
					line.append(blanks);
					text = text.substring(split+1);
					
				} else {
					break;
				}
			}
			
			// print remainder of description in single line
			line.append(text);
			print.println(line.toString());
		}
	}

	/**
	 * Process argument list directly. Creates and initializes an instance of
	 * this class, then processes control flags present in the supplied argument
	 * list, setting the associated parameter values in the target object.
	 * Arguments not consumed in the control flag processing are available for
	 * access using other methods after the return from this call.
	 *
	 * @param args command line argument string array
	 * @param parm data object for parameter values
	 * @param target application object defining parameter fields
	 * @return index of first command line argument not consumed by processing
	 * @throws ArgumentErrorException on error in data
	 * @throws IllegalArgumentException on error in processing
	 */
	
	public static int processArgs(String[] args,  ParameterDef[] parms,
		Object target) {
		ArgumentProcessor inst = new ArgumentProcessor(parms);
		inst.processArgs(args, target);
		return inst.m_remainingArgs.nextOffset();
	}
}