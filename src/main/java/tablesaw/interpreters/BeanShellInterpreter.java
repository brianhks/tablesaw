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

import bsh.*;
import tablesaw.*;

import java.io.*;
import java.util.Properties;

public class BeanShellInterpreter
		implements ScriptInterpreter
	{
	private Properties m_properties;
	private Interpreter m_interpreter;
	
	private int getLineNumber(String message)
		{
		return (-1);
		}
		
	private void targetError(TargetError te)
			throws TablesawException
		{
		if (te.getTarget() instanceof TablesawException)
			{
			throw (TablesawException)te.getTarget();
			}
		else
			{
			if (Debug.isDebug())
				te.getTarget().printStackTrace();

			Throwable cause = te.getTarget();
			while (cause.getCause() != null)
				cause = cause.getCause();

			if (cause instanceof ClassNotFoundException)
				throw new MissingClassException((ClassNotFoundException)cause);
			else
				{
				throw new ScriptException(te.getErrorLineNumber(), te.getErrorSourceFile(),
						te.getMessage()+"\n"+te.getTarget().getMessage());
				}
			}
		}
		
	private void evalError(EvalError ee)
			throws TablesawException
		{
		try
			{
			//Checks for a null pointer in getErrorLineNumber.  Bug in Beanshell
			ee.getErrorLineNumber();
			throw new ScriptException(ee.getErrorLineNumber(), ee.getErrorSourceFile(), ee.getMessage());
			}
		catch (NullPointerException npe)
			{
			throw new ScriptException(-1, ee.getErrorSourceFile(), ee.getMessage());
			}
		}
		
	public BeanShellInterpreter(Properties props)
		{
		m_properties = props;
		m_interpreter = new Interpreter();
		m_interpreter.setErr(null);
		m_interpreter.setExitOnEOF(false);
		Interpreter.DEBUG = false;
		Interpreter.TRACE = false;
		}
		
	public Class<?> getInterpreterClass()
		{
		return (m_interpreter.getClass());
		}
		
	public void set(String var, Object value)
			throws TablesawException
		{
		try
			{
			m_interpreter.set(var, value);
			}
		catch (EvalError ee)
			{
			throw new ScriptException(ee.getErrorLineNumber(), ee.getErrorSourceFile(), ee.getMessage());
			}
		}
		
	public void call(String method, Object param1)
			throws TablesawException
		{
		//Create the script variable names that use the current
		//thread name so as to be unique
		String paramName1 = "param1t" + Thread.currentThread().getName();
		String paramName2 = "param2t" + Thread.currentThread().getName();
		String cmd;
		set(paramName1, param1);
		cmd = method + "(" + paramName1 + ")";

		try
			{
			m_interpreter.eval(cmd);
			}
		catch(TargetError te)
			{
			targetError(te);
			}
		catch(EvalError ee)
			{
			if (ee.getErrorSourceFile().startsWith("inline"))
				throw new MissingMethodException(cmd);
			else
				evalError(ee);
			}
		}
		
	public void source(String file)
			throws TablesawException, FileNotFoundException, IOException
		{
		try
			{
			m_interpreter.source(file);
			}
		catch(TargetError te)
			{
			targetError(te);
			}
		catch (EvalError ee)
			{
			evalError(ee);
			/*int lineNumber;
			//ee.printStackTrace();
			//This is a hack for a bug in beanshell
			try
				{
				lineNumber = ee.getErrorLineNumber();
				}
			catch (NullPointerException npe)
				{
				lineNumber = getLineNumber(ee.getMessage());
				}
			throw new ScriptException(lineNumber, ee.getErrorSourceFile(), ee.getMessage());*/
			}
		}
		
	public void eval(String statements)
			throws TablesawException
		{
		try
			{
			m_interpreter.eval(statements);
			}
		catch (TargetError te)
			{
			targetError(te);
			}
		catch (EvalError ee)
			{
			evalError(ee);
			}
		}
		
	//---------------------------------------------------------------------------
	public MakeAction getMakeAction(Object o)
		{
		return (null);
		}


	public BuildCallback getBuildCallback(Object o)
		{
		return null;
		}


	public void cleanup()
		{
		}
	}
