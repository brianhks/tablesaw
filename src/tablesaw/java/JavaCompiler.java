package tablesaw.java;

import tablesaw.*;
import tablesaw.definitions.*;

import java.util.*;

import tablesaw.rules.Rule;
import tablesaw.rules.AbstractPatternRule;
import tablesaw.rules.DirectoryRule;


/**
	The JavaCompiler can be instantiated in a script to compile java files
	Use tablesaw.addons.java.JavaCRule instead
*/
@Deprecated
public class JavaCompiler extends AbstractPatternRule<JavaCompiler> implements MakeAction
	{
	private ClassPath m_classpath;
	private String m_buildDir;
	private List<String> m_sourceDirs;          //Used temporarily before filling m_sourceFiles
	private Definition m_compilerDef;
	private String m_banner;
	
	public JavaCompiler(String buildDir)
			throws TablesawException
		{
		this(null, buildDir);
		}
	
	//---------------------------------------------------------------------------
	public JavaCompiler(String name, String buildDir)
			throws TablesawException
		{
		super();
		
		setName(name);
		m_buildDir = buildDir;
		addDepends(new DirectoryRule(buildDir).override());
		
		m_sourceDirs = new ArrayList<String>();
		m_classpath = new ClassPath();
		m_classpath.addPath(buildDir); //We want to add this to the front
		
		m_compilerDef = m_make.getDefinition("sun_javac");
		
		//Set PatternRule options
		multiTarget();
		setSourcePattern("(.*)\\.java");
		setTargetPattern(m_buildDir+"/$1.class");
		setMakeAction(this);
		}
		
	//---------------------------------------------------------------------------
	@Override
	public Object clone() throws CloneNotSupportedException
		{
		JavaCompiler copy = (JavaCompiler)super.clone();
		
		copy.m_sourceDirs = (List<String>)((ArrayList<String>)m_sourceDirs).clone();
		copy.m_compilerDef = (Definition)m_compilerDef.clone();
		
		return (copy);
		}
	
		
	//---------------------------------------------------------------------------
	public JavaCompiler addSourceDir(String srcDir)
		{
		m_sourceDirs.add(srcDir);
		m_make.addSearchPath(".*\\.java", srcDir);
		return (this);
		}
		
	
	//---------------------------------------------------------------------------
	public JavaCompiler addSourceDir(Iterable<Object> srcDirs)
		{
		for (Object dir : srcDirs)
			addSourceDir(dir.toString());
		
		return (this);
		}
	
	//---------------------------------------------------------------------------		
	public ClassPath getClasspath()
		{
		return (m_classpath);
		}
		
	//---------------------------------------------------------------------------
	public JavaCompiler addClasspath(ClassPath classpath)
		{
		m_classpath.addPaths(classpath);
		return (this);
		}
		
	//---------------------------------------------------------------------------
	public JavaCompiler addClasspath(String path)
		{
		m_classpath.addPath(path);
		return (this);
		}
		
		
	//---------------------------------------------------------------------------
	@Override
	public void inBuildQueue()
			throws TablesawException
		{
		m_compilerDef.set("builddir", m_buildDir);
		if (!m_classpath.isEmpty())
			m_compilerDef.set("classpath", m_classpath.getFormattedPath());
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
	// TODO: add method to set the definition
	public Definition getDefinition()
		{
		return (m_compilerDef);
		}
		
	//---------------------------------------------------------------------------
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
				System.out.println(m_make.locateFile(s).getRelativePath());
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
