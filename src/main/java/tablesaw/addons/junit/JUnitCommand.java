package tablesaw.addons.junit;

import tablesaw.TablesawException;
import tablesaw.definitions.Definition;
import tablesaw.addons.java.Classpath;
import tablesaw.util.Validation;
import tablesaw.Tablesaw;
import java.util.Arrays;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.IOException;

import static tablesaw.util.Validation.*;

/**
Takes java file path or class file name
takes . or / notation
*/
public class JUnitCommand
{
	private static final Pattern s_packagePattern = Pattern.compile(".*package\\s*(\\S+);.*");
	
	private Tablesaw m_make;
	private Classpath m_classpath;
	private Set<String> m_classes;
	private String m_encoding;
	private int m_debugPort = -1;
	private List<String> m_jvmArguments = null;
	
	//---------------------------------------------------------------------------
	public JUnitCommand()
		throws TablesawException
	{
		m_make = Tablesaw.getCurrentTablesaw();
		
		m_classes = new HashSet<String>();
		m_encoding = "UTF-8";
	}
	
	//---------------------------------------------------------------------------
	public JUnitCommand setDebugPort(int port)
		{
		m_debugPort = port;
		return (this);
		}
		
	//---------------------------------------------------------------------------
	public JUnitCommand setJvmArguments(List<String> arguments)
		{
		m_jvmArguments = arguments;
		return (this);
		}
	
	//---------------------------------------------------------------------------
	public JUnitCommand setClasspath(Classpath cp)
	{
		m_classpath = cp;
		return (this);
	}
	
	//---------------------------------------------------------------------------
	/**
	*/
	public JUnitCommand addClasses(Object... classes)
	{
		if ((classes.length == 1) && (classes[0] instanceof Iterable))
			return (addClasses((Iterable<Object>)classes[0]));
		else
			return (addClasses(Arrays.asList(classes)));
	}
	
	
	public JUnitCommand addClasses(Iterable<Object> classes)
	{
		for (Object c : classes)
			addClass(objectToString(c));
		
		return (this);
	}
	
	
	//---------------------------------------------------------------------------
	/**
		Add a JUnit class to be ran.
		@param file Can either be a path to a java file or a class name ex 
	*/
	public JUnitCommand addClass(String clazz)
	{
		m_classes.add(clazz);
		return (this);
	}
	
	//---------------------------------------------------------------------------
	public Set<String> getTestClasses()
	{
		return (m_classes);
	}
	
	//---------------------------------------------------------------------------
	/**
	*/
	public JUnitCommand setSourceEncoding(String encoding)
	{
		m_encoding = encoding;
		return (this);
	}
	
	//---------------------------------------------------------------------------
	/**
	*/
	public JUnitCommand addSources(Object... sources)
		throws TablesawException
	{
		if ((sources.length == 1) && (sources[0] instanceof Iterable))
			return (addSources((Iterable<Object>)sources[0]));
		else
			return (addSources(Arrays.asList(sources)));
	}
	
	//---------------------------------------------------------------------------
	/**
	*/
	public JUnitCommand addSources(Iterable<Object> sources)
		throws TablesawException
	{
		for (Object s : sources)
			addSource(objectToString(s));
		
		return (this);
	}
	
	//---------------------------------------------------------------------------
	/**
		Path to source file to include.
		Because it may be easier to get the source path instead of the full
		class name this method allows you to enter the path to the java file.
		The file is then parsed for the <code>package</code> declaration in order
		to build the full class name.
		
		@param source path to java source file.
	*/
	public JUnitCommand addSource(String source)
		throws TablesawException
	{
		File sourceFile = Validation.locateFileMustExist(m_make, source);
		
		String packageStr = null;
		try
		{
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(sourceFile), m_encoding));
			
			String line;
			while ((line = br.readLine()) != null)
			{
				Matcher match = s_packagePattern.matcher(line);
				if (match.matches())
				{
					packageStr = match.group(1);
					break;
				}
			}
			
			br.close();
		}
		catch (IOException ioe)
		{
			throw new TablesawException(ioe);
		}
		
		if (packageStr == null)
			throw new TablesawException("Unable to locate package information in "+source, 1);
		
		String fileName = sourceFile.getName();
		String className = packageStr+"."+fileName.substring(0, fileName.lastIndexOf('.'));
		
		addClass(className);
		
		return (this);
	}
	
	//---------------------------------------------------------------------------
	/**
	*/
	public Definition getDefinition()
		throws TablesawException
	{
		Definition def = m_make.getDefinition("junit4");
		def.set("classpath", m_classpath);
		def.set("test_class", m_classes);
		
		if (m_debugPort != -1)
			def.set("debug", m_debugPort);
			
		if (m_jvmArguments != null)
			def.set("jvm_arg", m_jvmArguments);
		
		return (def);
	}
	//---------------------------------------------------------------------------
	/**
		Call this to execute the junit tests
	*/
	public void run()
		throws TablesawException
	{
		//Load the definition
		Definition def = getDefinition();
		
		m_make.exec(def.getCommand(), true);
	}
}
