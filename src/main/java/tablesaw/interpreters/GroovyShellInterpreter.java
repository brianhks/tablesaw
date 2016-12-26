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

import groovy.lang.GroovyShell;
import groovy.lang.Script;
import groovy.lang.Closure;
import groovy.lang.GroovyRuntimeException;
import org.codehaus.groovy.control.CompilationFailedException;
import java.io.*;

import tablesaw.BuildCallback;
import tablesaw.MakeAction;
import tablesaw.ScriptException;
import tablesaw.TablesawException;
import tablesaw.rules.Rule;

public class GroovyShellInterpreter
		implements ScriptInterpreter
	{
	GroovyShell m_interpreter;
	Script m_script;
	
	public GroovyShellInterpreter()
		{
		m_interpreter = new GroovyShell();
		}
		
	public Class<?> getInterpreterClass()
		{
		return (m_interpreter.getClass());
		}
		
	public void set(String var, Object value)
			throws TablesawException
		{
		m_interpreter.setVariable(var, value);
		}
		
	public void call(String method, Object param1)
			throws TablesawException
		{
		//Create the script variable names that use the current
		//thread name so as to be unique
		/* String paramName1 = "param1t" + Thread.currentThread().getName();
		String paramName2 = "param2t" + Thread.currentThread().getName();
		String cmd;
		if (param2 == null)
			{
			set(paramName1, param1);
			cmd = method + "(" + paramName1 + ")";
			}
		else 
			{
			set(paramName1, param1);
			set(paramName2, param2);
			cmd = method + "(" + paramName1 + ", " + paramName2 + ")";
			} */
			
		try
			{
			//m_interpreter.evaluate(cmd);
			m_script.invokeMethod(method, param1);
			}
		catch (CompilationFailedException cfe)
			{
			throw new ScriptException(cfe.getMessage());
			}
		/* catch (IOException ioe)
			{
			throw new TablesawException("Unable to call '"+method+"'", -1);
			} */
		catch(Exception e)
			{
			throw new TablesawException(e.toString(), -1);
			}
		}
		
	public void source(String file)
			throws TablesawException, FileNotFoundException, IOException
		{
		try
			{
			m_script = m_interpreter.parse(new File(file));
			m_script.run();
			}
		catch (CompilationFailedException cfe)
			{
			//cfe.printStackTrace();
			throw new ScriptException(cfe.getMessage());
			/*throw new TablesawException(cfe.toString(), -1);*/
			}
		catch (GroovyRuntimeException gre)
			{
			gre.printStackTrace();
			//throw new ScriptException(gre.getNode().getLineNumber(), file, gre.getMessage());
			throw new TablesawException(gre.getMessage(), -1);
			}
		catch(Exception e)
			{
			e.printStackTrace();
			throw new TablesawException(e.toString(), -1);
			}
		}
		
	public void eval(String statements)
			throws TablesawException
		{
		//todo
		//Not implemented
		}
		
	//---------------------------------------------------------------------------
	public MakeAction getMakeAction(Object o)
		{
		if (o instanceof Closure)
			{
			final Closure clos = (Closure)o;
			return (new MakeAction()
				{
				public void doMakeAction(Rule rule)
					{
					clos.call(rule);
					}
				});
			}
		else
			return (null);
		}

	public BuildCallback getBuildCallback(Object o)
		{
		if (o instanceof Closure)
			{
			final Closure clos = (Closure)o;
			return (new BuildCallback()
			{
			public void doCallback(Object data) throws TablesawException
				{
				clos.call(data);
				}
			});
			}
		else
			return (null);
		}

	public void cleanup()
		{
		}
	}
