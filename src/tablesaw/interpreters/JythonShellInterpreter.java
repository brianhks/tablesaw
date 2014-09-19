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

import org.python.util.PythonInterpreter;
import tablesaw.BuildCallback;
import tablesaw.MakeAction;
import tablesaw.Tablesaw;
import tablesaw.TablesawException;

import java.io.*;

public class JythonShellInterpreter
		implements ScriptInterpreter
	{
	private PythonInterpreter m_interpreter;
	
	public JythonShellInterpreter(Tablesaw make)
		{
		PythonInterpreter.initialize(make.getProperties(), make.getProperties(), null);
		
		m_interpreter = new PythonInterpreter();
		
		}
		
	public Class<?> getInterpreterClass()
		{
		return (m_interpreter.getClass());
		}
		
	public void set(String var, Object value)
			throws TablesawException
		{
		m_interpreter.set(var, value);
		}
		
	public void call(String method, Object param1)
			throws TablesawException
		{
		try
			{
			//Create the script variable names that use the current
			//thread name so as to be unique
			String paramName1 = "param1t" + Thread.currentThread().getName();
			String paramName2 = "param2t" + Thread.currentThread().getName();
			String cmd;
			set(paramName1, param1);
			cmd = method + "(" + paramName1 + ")";

			m_interpreter.exec(cmd);
			}
		catch (Exception e)
			{
			throw new TablesawException(e.toString(), -1);
			}
		}
		

	public void source(String file)
			throws TablesawException, FileNotFoundException, IOException
		{
		try
			{
			m_interpreter.execfile(file);
			}
		catch (Exception e)
			{
			throw new TablesawException(e.toString(), -1);
			}
		}

	public void eval(String statements)
			throws TablesawException
		{
		//todo
		//not implemented
		}
		
	//---------------------------------------------------------------------------
	public MakeAction getMakeAction(Object o)
		{
		return (null);
		}

	public BuildCallback getBuildCallback(Object o)
		{
		return null;  //To change body of implemented methods use File | Settings | File Templates.
		}

	public void cleanup()
		{
		m_interpreter.cleanup();
		}
	}
