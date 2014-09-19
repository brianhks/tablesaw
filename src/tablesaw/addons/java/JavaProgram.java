package tablesaw.addons.java;

import tablesaw.MakeAction;
import tablesaw.rules.Rule;
import tablesaw.definitions.Definition;
import tablesaw.TablesawException;
import tablesaw.rules.DirectoryRule;
import tablesaw.rules.SimpleRule;
import tablesaw.RegExFileSet;
import tablesaw.Tablesaw;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.TimeZone;

/**
 The JavaProgram class simplifies the creation of rules for building a Java
 program.  By default after calling {@link #setup()} JavaProgram creates two
 {@link JavaCRule}s - one for the source and one for the test code - a {@link JarRule}
 and a {@link SimpleRule} for building javadocs.

 The defaults for the source and build directories can be changed by calling
 {@link tablesaw.Tablesaw#setProperty(String, String)} using the appropriate
 property defined in the static members of this class.  They can also be set
 by adding them to your own custom tablesaw.properties file.

 The defaults can be overridden by calling the appropriate set method before
 calling {@link #setup()}.
*/
public class JavaProgram
	{
	public static final String PROGRAM_NAME_PROPERTY = "tablesaw.java.program.name";

	public static final String PROGRAM_VERSION_PROPERTY = "tablesaw.java.program.version";

	public static final String PROGRAM_DESCRIPTION_PROPERTY = "tablesaw.java.program.description";

	/**
	 Property for setting the default class output directory.
	 Defaults to build/classes
	*/
	public static final String CLASS_DIRECTORY_PROPERTY = "tablesaw.java.program.class_directory";

	/**
	 Property for setting the default jar output directory.
	 Defaults to build/jar
	 */
	public static final String JAR_DIRECTORY_PROPERTY = "tablesaw.java.program.jar_directory";

	/**
	 Property for setting the default java source directory.
	 Defaults to src/main/java
	 */
	public static final String SOURCE_DIRECTORY_PROPERTY = "tablesaw.java.program.source_directory";

	/**
	 Property for setting the default java test source directory.
	 Defaults to src/test/java
	 */
	public static final String TEST_SOURCE_DIRECTORY_PROPERTY = "tablesaw.java.program.test_source_directory";

	/**
	 Property for setting the default test class output directory.
	 Defaults to build/tests/classes
	 */
	public static final String TEST_CLASS_DIRECTORY_PROPERTY = "tablesaw.java.program.test_class_directory";

	/**
	 Property for setting the default javadoc directory.
	 Defaults to build/doc
	 */
	public static final String JAVADOC_DIRECTORY_PROPERTY = "tablesaw.java.program.javadoc_directory";

	/**
	 Property for setting the default external lib directory.
	 Defaults to lib
	 */
	public static final String LIB_DIRECTORY_PROPERTY = "tablesaw.java.program.lib_directory";


	private boolean m_isSetup = false;
	private Tablesaw m_make;
	//private String m_programName;
	private JavaCRule m_javaCRule;
	private JavaCRule m_testJavaCRule;
	private JarRule m_jarRule;
	private List<String> m_libJars = null;
	//private String m_sourceDirectory = null;
	private String m_classDirectory = null;
	private String m_rulePrefix = "";
	private boolean m_includeTests = true;
	private SimpleRule m_javaDocRule;
	private JarRule m_javaDocJarRule;
	private JarRule m_sourceJarRule;

	private void init()
		{
		m_make = Tablesaw.getCurrentTablesaw();
		}

	/**
	 Creates an instance of JavaProgram.  No rules are created until {@link #setup()}
	 is called.  By default JavaProgram uses the name of the parent directory
	 as the program name.
	 @throws TablesawException
	 */
	public JavaProgram()
			throws TablesawException
		{
		init();
		
		try
			{
			if (m_make.getProperty(PROGRAM_NAME_PROPERTY) == null)
				{
				setProgramName(new File(m_make.getWorkingDirectory(), ".")
						.getCanonicalFile().getName());
				}
			}
		catch (java.io.IOException ioe)
			{
			throw new TablesawException(ioe);
			}
			
		
		}
		
	//---------------------------------------------------------------------------

	/**
	 Generates the various rules for building and packaging java code.
	 @return
	 @throws TablesawException
	 */
	public JavaProgram setup()
			throws TablesawException
		{
		String jarAndVersion = m_make.getProperty(PROGRAM_NAME_PROPERTY);
		if (m_make.getProperty(PROGRAM_VERSION_PROPERTY) != null)
			jarAndVersion += "-"+m_make.getProperty(PROGRAM_VERSION_PROPERTY);

		/* String buildDir = "build";
		new DirectoryRule(buildDir);  //This is for auto clean */
		
		//Automatically pull in jars in the lib folder
		if (m_libJars == null)
			{
			String libDir = m_make.getProperty(LIB_DIRECTORY_PROPERTY);
			m_libJars = new RegExFileSet(libDir, ".*\\.jar").recurse().getFullFilePaths();
			}
		Classpath libClasspath = new Classpath(m_libJars);
		
		if (m_classDirectory == null)
			m_classDirectory = m_make.getProperty(CLASS_DIRECTORY_PROPERTY);
		String jarDir = m_make.getProperty(JAR_DIRECTORY_PROPERTY);
		DirectoryRule jarDirRule = new DirectoryRule(jarDir).override();
		
			
		m_javaCRule = new JavaCRule(m_rulePrefix+"compile", m_classDirectory);
		m_javaCRule.addSourceDir(m_make.getProperty(SOURCE_DIRECTORY_PROPERTY));
		m_javaCRule.setDescription("Compile java source code");
		m_javaCRule.addClasspath(libClasspath);
		
		m_jarRule = new JarRule(m_rulePrefix+"jar", jarDir+"/"+jarAndVersion+".jar");
		m_jarRule.addDepend(jarDirRule);
		m_jarRule.addDepend(m_javaCRule);
		m_jarRule.addFileSet(new RegExFileSet(m_classDirectory, ".*\\.class").recurse());
		m_jarRule.setDescription("Create "+jarAndVersion+" jar file");
		
		
		//String testBuildDir = buildDir+"/tests";
		if (m_includeTests)
			{
			String testClassDir = m_make.getProperty(TEST_CLASS_DIRECTORY_PROPERTY);
			m_testJavaCRule = new JavaCRule(m_rulePrefix+"compile-tests", testClassDir);
			m_testJavaCRule.addSourceDir(m_make.getProperty(TEST_SOURCE_DIRECTORY_PROPERTY));
			m_testJavaCRule.addDepend(m_javaCRule);
			m_testJavaCRule.addClasspath(m_javaCRule.getBuildDirectory());
			m_testJavaCRule.setDescription("Compile java test code");
			m_testJavaCRule.addClasspath(libClasspath);
			}
		
		
		final String docDir = m_make.getProperty(JAVADOC_DIRECTORY_PROPERTY);
		m_javaDocRule = new SimpleRule(m_rulePrefix+"javadoc")
				.setDescription("Create source javadocs")
				.addDepend(new DirectoryRule(docDir).override())
				.addDepend(m_javaCRule)
				.setMakeAction(new MakeAction()
					{
					public void doMakeAction(Rule rule)
							throws TablesawException
						{
						Definition df = m_make.getDefinition("sun_javadoc");
						String cp = m_javaCRule.getClasspath().toString();
						df.set("public")
								.set("classpath", cp)
								.set("destination", docDir)
								.set("source", m_javaCRule.getSources());

						m_make.exec(df.getCommand());
						}
					});

		m_javaDocJarRule = (JarRule) new JarRule(m_rulePrefix+"javadoc-jar", jarDir+"/"+jarAndVersion+"-javadoc.jar")
				.setDescription("Create javadoc jar file")
				.addDepend(m_javaDocRule)
				.addDepend(jarDir)
				.addFileSet(new RegExFileSet(docDir, ".*").recurse());

		m_sourceJarRule = (JarRule) new JarRule("source-jar", jarDir+"/"+jarAndVersion+"-sources.jar")
				.setDescription("Create source jar file")
				.addDepend(jarDir)
				.addFileSet(new RegExFileSet(m_make.getProperty(SOURCE_DIRECTORY_PROPERTY), ".*").addExcludeDir(".svn").recurse());
		
		m_isSetup = true;
		return (this);
		}
		
	//---------------------------------------------------------------------------
	/**
	 Returns the program name.
	 @return
	 */
	public String getProgramName() { return (m_make.getProperty(PROGRAM_NAME_PROPERTY)); }
	
	//---------------------------------------------------------------------------
	/**
	 Sets the name of the program.  This name will be used when creating the jar
	 file.  For example if you program is named HackTV then the jar file will be
	 named HackTV.jar.
	 @param programName Name used for the resulting jar file.
	 @return
	 */
	public JavaProgram setProgramName(String programName)
		{

		m_make.setProperty(PROGRAM_NAME_PROPERTY, programName);
		return (this);
		}
		
	//---------------------------------------------------------------------------
	/**
	 Sets the name of the java source directory.
	 @param sourceDir Java source directory such as src/main/java.
	 @return
	 */
	public JavaProgram setSourceDirectory(String sourceDir)
		{
		m_make.setProperty(SOURCE_DIRECTORY_PROPERTY, sourceDir);
		return (this);
		}
		
	//---------------------------------------------------------------------------
	/**
	 Sets the name of the directory to which the class files are built.
	 @param classDir
	 @return
	 */
	public JavaProgram setClassDirectory(String classDir)
		{
		m_classDirectory = classDir;
		return (this);
		}
		
	//---------------------------------------------------------------------------
	/**
	 Sets the prefix for all rules created by JavaProgram.  The rules created are
	 named and can create conflicts if you are building more than one JavaProgram
	 in a single build script.
	 @param prefix
	 @return
	 */
	public JavaProgram setRulePrefix(String prefix)
		{
		m_rulePrefix = prefix;
		return (this);
		}
		
	//---------------------------------------------------------------------------
	/**
		Disables creation of a compile rule for test classes.
		This is used when compiling multiple modules that have a common
		test directory.
	*/
	public JavaProgram noTests()
		{
		m_includeTests = false;
		return (this);
		}
		
	//---------------------------------------------------------------------------
	public List<String> getLibraryJars() { return (m_libJars); }
	
	//---------------------------------------------------------------------------
	public JavaProgram setLibraryJars(List<String> libJars)
		{
		m_libJars = new ArrayList<String>(libJars);
		return (this);
		}
	
	//---------------------------------------------------------------------------
	public JavaCRule getCompileRule() { return (m_javaCRule); }
	
	//---------------------------------------------------------------------------
	public JavaCRule getTestCompileRule() { return (m_testJavaCRule); }
	
	//---------------------------------------------------------------------------
	public JarRule getJarRule() { return (m_jarRule); }

	public SimpleRule getJavaDocRule()
		{
		return m_javaDocRule;
		}

	public JarRule getJavaDocJarRule()
		{
		return m_javaDocJarRule;
		}

	public JarRule getSourceJarRule()
		{
		return m_sourceJarRule;
		}
	}
