package tablesaw.addons.java;

import tablesaw.*;
import tablesaw.annotation.Consumes;
import tablesaw.annotation.Provides;
import tablesaw.rules.Rule;
import tablesaw.rules.AbstractPatternRule;
import tablesaw.rules.DirectoryRule;
import tablesaw.rules.SimpleRule;
import tablesaw.definitions.*;
import java.util.*;

import static tablesaw.util.Validation.*;

/**
 Rule for compiling java code into class files.
 */
public class JavaCRule extends AbstractPatternRule<JavaCRule> implements MakeAction
	{
	private class JavaCSimpleRule extends SimpleRule
		{
		@Override
		public void preBuild(DependencyCache cache, Map<String, Long> modificationCache)
				throws TablesawException
			{
			super.preBuild(cache, modificationCache);
			
			if (m_parseDepends)
				{
				for (String t : m_targets)
					{
					addDepends(cache.getDependencies(m_make, t));
					}
				}
			}
		}
	
	//==========================================================================
	private boolean m_parseDepends;
	private Classpath m_classpath;
	private String m_buildDir;
	private List<String> m_sourceDirs;          //Used temporarily before filling m_sourceFiles
	private Definition m_compilerDef;
	private String m_banner;
	private List<ClasspathProvider> m_providers;

	/**
	 Creates a JavaCRule with the specified output directory for the .class files.
	 @param buildDir Directory where .class files are placed.
	 @throws TablesawException
	 */
	public JavaCRule(String buildDir)
			throws TablesawException
		{
		this(null, buildDir);
		}
	
	//---------------------------------------------------------------------------
	/**
	 Creates a named JavaCRule that can be called from the command line.
	 @param name Name of the rule such as 'compile'
	 @param buildDir Directory where .class files are placed.
	 @throws TablesawException
	 */
	public JavaCRule(String name, String buildDir)
			throws TablesawException
		{
		super();
		
		notNull(buildDir);
		
		setName(name);
		m_buildDir = buildDir;
		Debug.print("JavaCRule: buildDir=%s", m_buildDir);
		addDepends(new DirectoryRule(buildDir).override());
		
		m_sourceDirs = new ArrayList<String>();
		m_classpath = new Classpath();
		m_classpath.addPath(buildDir); //We want to add this to the front
		
		m_compilerDef = m_make.getDefinition("sun_javac");
		
		//Set PatternRule options
		multiTarget();
		setSourcePattern("(.*)\\.java");
		setTargetPattern(m_buildDir+"/$1.class");
		setMakeAction(this);
		
		m_parseDepends = true;
		m_providers = new ArrayList<ClasspathProvider>();
		}
		
	//---------------------------------------------------------------------------
	@Override
	public Object clone() throws CloneNotSupportedException
		{
		JavaCRule copy = (JavaCRule)super.clone();
		
		copy.m_sourceDirs = (List<String>)((ArrayList<String>)m_sourceDirs).clone();
		copy.m_compilerDef = (Definition)m_compilerDef.clone();
		copy.m_providers = (List<ClasspathProvider>)((ArrayList<ClasspathProvider>)m_providers).clone();
		
		return (copy);
		}
	
	//---------------------------------------------------------------------------
	/**
	 Adds a directory to be searched for java source.  The directory should be the
	 base of the package directory structure.  The subdirectories will be searched
	 recursively for .java files.
	 @param srcDir
	 @return
	 */
	public JavaCRule addSourceDir(String srcDir)
		{
		Debug.print("JavaCRule: srcDir=%s", srcDir);
		m_sourceDirs.add(srcDir);
		m_make.addSearchPath(".*\\.java", srcDir);
		return (this);
		}
		
	
	//---------------------------------------------------------------------------
	/**
	 Same as {@link #addSource(String)} but lets you pass a list of directories
	 to add.
	 @param srcDirs
	 @return
	 */
	public JavaCRule addSourceDir(Iterable<Object> srcDirs)
		{
		for (Object dir : srcDirs)
			addSourceDir(dir.toString());
		
		return (this);
		}
	
	//---------------------------------------------------------------------------
	@Provides("java.classpath")
	public Classpath getClasspath()
		{
		return (m_classpath);
		}
		
	//---------------------------------------------------------------------------
	@Consumes("java.classpath")
	public JavaCRule addClasspath(Classpath classpath)
		{
		m_classpath.addPaths(classpath);
		return (this);
		}
		
	//---------------------------------------------------------------------------
	public JavaCRule addClasspath(String path)
		{
		m_classpath.addPath(path);
		return (this);
		}
		
	//---------------------------------------------------------------------------
	public String getBuildDirectory()
		{
		return (m_buildDir);
		}
		

	//---------------------------------------------------------------------------
	/**
	 Turns off class dependency parsing.
	 By default the JavaCRule does smart incremental compiles, meaning if you modify
	 a single java class it will figure out what other classes need to be
	 recompiled.  It does this by parsing the the .class files from the previous
	 build and determining what class references are.  This method turns off that
	 feature.
	 @return
	 */
	public JavaCRule dontParseDepends()
		{
		m_parseDepends = false;
		return (this);
		}
	
	//---------------------------------------------------------------------------
	@Override
	protected SimpleRule getSimpleRule()
		{
		return (new JavaCSimpleRule());
		}
	
	//---------------------------------------------------------------------------
	@Override
	public void inBuildQueue()
			throws TablesawException
		{
		m_compilerDef.set("builddir", m_buildDir);
		/*if (!m_classpath.isEmpty())
			m_compilerDef.set("classpath", m_classpath.getFormattedPath());*/
		}
	
	//---------------------------------------------------------------------------
	@Override
	public void setPrimaryTarget(Tablesaw make, String target)
		{
		for (String dir : m_sourceDirs)
			{
			addSources(new RegExFileSet(dir, ".*\\.java")
					.addExcludeFiles("package-info.java").recurse().getFilePaths());
			}
		
		//Not sure why this is here
		m_make.setProperty("java.classpath", m_classpath.getFormattedPath());
		
		//Load definition
		super.setPrimaryTarget(make, target);
		}
	
	//---------------------------------------------------------------------------
	/**
	 Sets the name of the compiler definition to use.  Make sure the definition
	 supports the same options as the sun_javac definition does.
	 @param definition
	 @return
	 @throws TablesawException
	 */
	public JavaCRule setDefinition(String definition) throws TablesawException
		{
		m_compilerDef = m_make.getDefinition(definition);
		
		return (this);
		}
		
	//---------------------------------------------------------------------------
	/**
	 Returns the javac definition that will be used by this rule.
	 @return
	 */
	public Definition getDefinition()
		{
		return (m_compilerDef);
		}
		
	//---------------------------------------------------------------------------
	/**
	 Sets a banner to be printed out in front of each line of compiler output.
	 Used to mimic output similar to ant, if you have and application that
	 needs to parse that output.
	 @param banner
	 */
	public void setBanner(String banner) { m_banner = banner; }
		
	//---------------------------------------------------------------------------
	public void doMakeAction(Rule rule)
			throws TablesawException
		{
		for (String t : getRebuildTargets())
			m_make.delete(t);
		
		Set<String> sources = getRebuildSources();
		
		if (m_make.getProperty(m_make.PROP_LONG_FILE_NAMES, m_make.PROP_VALUE_OFF).equals(m_make.PROP_VALUE_ON))
			{
			Set<String> fullPaths = new HashSet<String>();
			
			for (String s : sources)
				{
				//System.out.println(m_make.locateFile(s).getRelativePath());
				fullPaths.add(m_make.locateFile(s).getRelativePath());
				}
			
			//Add source files to definition
			m_compilerDef.set("sourcefile", fullPaths);
			}
		else
			{
			//Add source files to definition
			m_compilerDef.set("sourcefile", sources);
			}
			
		//Adding in delayed classpaths
		/*for (ClasspathProvider provider : m_providers)
			m_classpath.addPaths(provider.getClasspath());*/
		
		m_compilerDef.set("class_dir", m_buildDir);
		m_compilerDef.set("classpath", m_classpath.toString());
		if (!m_compilerDef.isModeSet())
			m_compilerDef.setMode("debug");
		
		System.out.println("Compiling "+sources.size()+" source files.");
		String cmd = m_compilerDef.getCommand();
		//make.setBanner(m_banner);
		m_make.exec(cmd, true);
		//make.setBanner(null);


		}
	}
