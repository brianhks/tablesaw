package tablesaw.java;

import tablesaw.*;
import tablesaw.rules.AbstractSimpleRule;
import tablesaw.rules.DirectoryRule;


/**
	Tablesaw plugin for creating/compiling a java program.
	This plugin acts as a wrapper for the rules to create a java program from 
	building class files to packaging a jar.
	
	Use tablesaw.addons.java.JavaProgram instead.
*/
@Deprecated
public class JavaProgram extends AbstractSimpleRule<JavaProgram>
	{
	private JavaCompiler m_compileRule;
	private JarFile m_jarRule;
	private String m_jarFile;
	private String m_classOutDir;
	
	public JavaProgram(String sourceDir, String buildDir, String jarFileName)
			throws TablesawException
		{
		super();
		m_classOutDir = buildDir+"/classes";
		String jarDir = buildDir+"/jar";
		
		new DirectoryRule(buildDir); // This is so auto clean gets the whole dir
		DirectoryRule jarDirRule = new DirectoryRule(jarDir);
		
		m_compileRule = new JavaCompiler("compile", m_classOutDir).setDescription("Compile source files");
		m_compileRule.addSourceDir(sourceDir);
		
		m_jarFile = jarDir+"/"+jarFileName;
		m_jarRule = new JarFile("jar", m_jarFile).setDescription("Build jar file");
		m_jarRule.addDepend(m_compileRule);
		m_jarRule.addDepend(jarDirRule);
		
		m_jarRule.addFileSet(new RegExFileSet(m_classOutDir, ".*\\.class").recurse());
		
		addDepend(m_jarRule);
		}
		
	//---------------------------------------------------------------------------
	/**
		Get the directory that the class files will be created in
	*/
	public String getClassOutDir()
		{
		return (m_classOutDir);
		}
		
	//---------------------------------------------------------------------------
	/**
		Sets the jar file as the default target
	*/
	public JavaProgram setDefaultTarget()
		{
		m_make.setDefaultTarget(m_jarFile);
		return (this);
		}
		
	//---------------------------------------------------------------------------
	/**
		Returns the jar file name
	*/
	public String getJarFileTarget()
		{
		return (m_jarFile);
		}
		
	//---------------------------------------------------------------------------
	/**
		Returns the JavaCompiler rule for this java program
	*/
	public JavaCompiler getCompiler()
		{
		return (m_compileRule);
		}
		
	//---------------------------------------------------------------------------
	/**
		Returns the JarFile rule for this java program
	*/
	public JarFile getJar()
		{
		return (m_jarRule);
		}
	}
