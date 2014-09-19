package tablesaw.addons.junit;

import tablesaw.annotation.Consumes;
import tablesaw.rules.AbstractSimpleRule;
import tablesaw.rules.Rule;

import tablesaw.TablesawException;
import tablesaw.MakeAction;

import tablesaw.addons.java.Classpath;

import java.util.ArrayList;
import java.util.List;

/**
Checks for -Djunit=class or file

Look to see if can specify single test to run.
*/
public class JUnitRule extends AbstractSimpleRule<JUnitRule> implements MakeAction
{
	/**
		Name of the junit environment variable to set to run a single class
	*/
	public static final String JUNIT_CLASS_PROPERTY = "junit-class";
	
	/**
	*/
	public static final String JUNIT_FILE_PROPERTY = "junit-file";
	
	/**
	*/
	public static final String JUNIT_DEBUG_PORT = "junit-debug";
	
	private Classpath m_classpath = null;
	private List<String> m_jvmArguments = new ArrayList<String>(); 
	
	
	//---------------------------------------------------------------------------
	private void init()
	{
		setMakeAction(this);
		setDescription("Runs JUnit tests.  "+
			"\n      Use '-D junit-class=<java_class>' to run single test class"+
			"\n      Use '-D junit-file=<java_file>' to run single test file"+
			"\n      Use '-D junit-debug=<port>' to start with debugger on specified port");
			
	}
	
	//---------------------------------------------------------------------------
	/**
		Creates a JUnitRule named 'junit-test'
	*/
	public JUnitRule()
	{
		super("junit-test");
		init();
	}
	
	//---------------------------------------------------------------------------
	/**
		Creates a JUnitRule with the name specified
		@param name Name of the junit rule
	*/
	public JUnitRule(String name)
	{
		super(name);
		init();
	}
	
	//---------------------------------------------------------------------------
	@Consumes("java.classpath")
	public JUnitRule addClasspath(Classpath classpath)
		{
		if (m_classpath == null)
			m_classpath = new Classpath();

		m_classpath.addPaths(classpath);

		return this;
		}

	public JUnitRule setClasspath(Classpath classpath)
	{
		m_classpath = classpath;
		return (this);
	}
	
	//---------------------------------------------------------------------------
	/**
		Adds source files to be ran as part of the unit tests
		This rule does not build the files, that must be done as a separate rule
	*/
	@Override
	public JUnitRule addSource(String source)
	{
		return (super.addSource(source));
	}
	//---------------------------------------------------------------------------
	/**
		Adds source files to be ran as part of the unit tests
		This rule does not build the files, that must be done as a separate rule
	*/
	@Override
	public JUnitRule addSources(Object... sources)
	{
		return (super.addSources(sources));
	}
	
	//---------------------------------------------------------------------------
	/**
		Adds source files to be ran as part of the unit tests
		This rule does not build the files, that must be done as a separate rule
	*/
	@Override
	public JUnitRule addSources(Iterable<Object> sources)
	{
		return (super.addSources(sources));
	}
	
	//---------------------------------------------------------------------------
	public JUnitRule addJvmArgument(String argument)
		{
		m_jvmArguments.add(argument);
		return (this);
		}
	
	//---------------------------------------------------------------------------
	public void doMakeAction(Rule rule)
			throws TablesawException
		{
		JUnitCommand command = new JUnitCommand();
		if (m_classpath != null)
			command.setClasspath(m_classpath);
		
		//If the env var is set we only run the specified class
		String junitClass = m_make.getProperty(JUNIT_CLASS_PROPERTY);
		String junitFile = m_make.getProperty(JUNIT_FILE_PROPERTY);
		String junitDebug = m_make.getProperty(JUNIT_DEBUG_PORT);
		if (junitClass != null)
			{
			command.addClass(junitClass);
			}
		else if (junitFile != null)
			{
			command.addSource(junitFile);
			}
		else
			{
			command.addSources(m_sources);
			}
			
		if (junitDebug != null)
			command.setDebugPort(Integer.parseInt(junitDebug));
			
		if (m_jvmArguments.size() != 0)
			command.setJvmArguments(m_jvmArguments);
		
		command.run();
		}
	}
