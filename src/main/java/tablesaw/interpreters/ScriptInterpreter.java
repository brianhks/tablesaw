/*
 * Copyright (c) 2004, Brian Hawkins
 * brianhks@activeclickweb.com
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free 
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along 
 * with this program; if not, write to the 
 * Free Software Foundation, Inc., 
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
 
package tablesaw.interpreters;

import tablesaw.BuildCallback;
import tablesaw.MakeAction;
import tablesaw.TablesawException;

import java.io.*;

public interface ScriptInterpreter
	{
	public void set(String var, Object value)
		throws TablesawException;
	public void call(String method, Object param1)
		throws TablesawException;
	public void source(String buildFile)
		throws TablesawException, FileNotFoundException, IOException;
		
	public Class<?> getInterpreterClass();
		
/**
	Evaluates a string of statements
*/
	public void eval(String statements)
		throws TablesawException;
	public void cleanup();
	
	/**
		Returns a MakeAction object for the given generic object.
		In the case of groovy the object o maybe a closure.
	*/
	public MakeAction getMakeAction(Object o);

	/**
	 Returns a BuildCallback object for the given generic object.
	 In the case of groovy the object o maybe a closure.
	 */
	public BuildCallback getBuildCallback(Object o);
	}
