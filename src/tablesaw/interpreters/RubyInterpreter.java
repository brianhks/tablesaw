//
// RubyInterpreter.java
//
// Copyright 2013, NextPage Inc. All rights reserved.
//

package tablesaw.interpreters;

import org.jruby.Ruby;
import org.jruby.RubyRuntimeAdapter;
import org.jruby.embed.InvokeFailedException;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.ScriptingContainer;
import tablesaw.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class RubyInterpreter implements ScriptInterpreter
	{
	private Ruby m_runtime;
	private RubyRuntimeAdapter m_runtimeAdapter;
	private ScriptingContainer m_scriptingContainer;

	public RubyInterpreter()
		{
		/*m_runtime = JavaEmbedUtils.initialize(new ArrayList());
		m_runtimeAdapter = JavaEmbedUtils.newRuntimeAdapter();*/
		m_scriptingContainer = new ScriptingContainer(LocalContextScope.SINGLETON);
		URLClassLoader cl = (URLClassLoader)ClassLoader.getSystemClassLoader();

		List<String> paths = new ArrayList<String>();
		URL[] urls = cl.getURLs();
		for (URL url : urls)
			paths.add(url.getPath());

		m_scriptingContainer.setLoadPaths(paths);
		}


	public void set(String var, Object value) throws TablesawException
		{
		m_scriptingContainer.put('$'+var, value);
		}

	public void call(String method, Object param1) throws TablesawException
		{
		if (!m_scriptingContainer.getProvider().getRuntime().getTopSelf().respondsTo(method))
			{
			throw new MissingMethodException(method);
			}

		try
			{
			m_scriptingContainer.callMethod(null, method, param1);
			}
		catch (InvokeFailedException e)
			{
			if (Debug.isDebug())
				e.printStackTrace();

			throw new MissingMethodException(method);
			}
		}


	public void source(String buildFile) throws TablesawException, FileNotFoundException, IOException
		{
		FileInputStream buildInputStream = new FileInputStream(buildFile);
		m_scriptingContainer.runScriptlet(buildInputStream, "build.rb");
		buildInputStream.close();
		}

	public Class<?> getInterpreterClass()
		{
		return (m_scriptingContainer.getClass());
		}

	public void eval(String statements) throws TablesawException
		{
		m_scriptingContainer.runScriptlet(statements);
		}

	public void cleanup()
		{
		m_scriptingContainer.terminate();
		}

	public MakeAction getMakeAction(Object o)
		{
		System.out.println(o.getClass());
		return null;  //To change body of implemented methods use File | Settings | File Templates.
		}

	public BuildCallback getBuildCallback(Object o)
		{
		return null;  //To change body of implemented methods use File | Settings | File Templates.
		}
	}
