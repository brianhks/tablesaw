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

import org.mozilla.javascript.*;
import tablesaw.*;
import tablesaw.rules.Rule;

import java.io.*;

public class RhinoInterpreter
		implements ScriptInterpreter
	{
	private Context m_context;
	private Scriptable m_scope;
	
	private int getLineNumber(String message)
		{
		return (-1);
		}
	
	public RhinoInterpreter()
			throws TablesawException
		{

		m_context = Context.enter();
		ScriptableObject scope = new ImporterTopLevel(m_context);
		m_scope = m_context.initStandardObjects(scope);
		String script = "function print(a) { java.lang.System.out.println(a); }";
		try
			{
			m_context.evaluateString(m_scope, script, "PrintFunction", 1, null);
			}
		catch(RhinoException re)
			{
			throw new ScriptException(re.lineNumber(), re.sourceName(), re.getMessage());
			}
		}
		
	public Class<?> getInterpreterClass()
		{
		return (m_scope.getClass());
		}
		
	public void set(String var, Object value)
			throws TablesawException
		{
		Object jsVar = Context.javaToJS(value, m_scope);
		ScriptableObject.putProperty(m_scope, var, jsVar);
		}
		
	public void call(String method, Object param1)
			throws TablesawException
		{
		Object[] params = { param1 };
		call(method, params);
		}
		
	private void call(String method, Object[] params)
			throws TablesawException
		{
		Object scriptMethod = m_scope.get(method, m_scope);
		if (!(scriptMethod instanceof Function))
			throw new MissingMethodException(method);
			
		try
			{
			((Function)scriptMethod).call(m_context, m_scope, m_scope, params);
			}
		catch(RhinoException re)
			{
			throw new ScriptException(re.lineNumber(), re.sourceName(), re.getMessage());
			}
		}
		
	public void source(String file)
			throws TablesawException, FileNotFoundException, IOException
		{
		try
			{
			m_context.evaluateReader(m_scope, new FileReader(file), file, 1, null);
			}
		catch(RhinoException re)
			{
			throw new ScriptException(re.lineNumber(), re.sourceName(), re.getMessage());
			}
		}
		
	public void eval(String statements)
			throws TablesawException
		{
		//todo
		//not implemented
		}
		
	//---------------------------------------------------------------------------
	private void printList(Object[] list)
	{
		for (Object o : list)
			System.out.println("   "+o);
	}

	public MakeAction getMakeAction(Object o)
		{
		if (o instanceof Function)
			{
			final Function f = (Function)o;
			return (new MakeAction()
				{
				public void doMakeAction(Rule rule)
					{
					f.call(m_context, m_scope, null, new Object[] {rule});
					}
				});	
			}
		else
			return (null);
		}

	public BuildCallback getBuildCallback(Object o)
		{
		if (o instanceof Function)
			{
			final Function f = (Function)o;
			return (new BuildCallback()
			{
			public void doCallback(Object data) throws TablesawException
				{
				f.call(m_context, m_scope, null, new Object[] {data});
				}
			});
			}
		else
			return (null);
		}

	public void cleanup()
		{
		Context.exit();
		}
	}
