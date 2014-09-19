package tablesaw.rules;

import tablesaw.rules.PatternRule;
import tablesaw.rules.SimpleRule;
import org.junit.Test;
import tablesaw.SimpleFileSet;
import tablesaw.RegExFileSet;
import java.util.*;

import static org.junit.Assert.*;

public class PatternRuleTests extends RulesHelper
	{
	
	//---------------------------------------------------------------------------
	/**
		Test a pattern rule that is multi target
	*/
	@Test
	public void namedMultiTargetPatternTest()
			throws Exception
		{
		List<String> sources = new ArrayList<String>();
		sources.add("test/FirstClass.java");
		sources.add("test/SecondClass.java");
		PatternRule r = new PatternRule("testPRule");
		r.multiTarget();
		r.addSources(sources);
		r.setSourcePattern("(.*)\\.java").setTargetPattern(m_baseDirPath+"/$1.class");
	
		SimpleMakeAction sma = new SimpleMakeAction();
		r.setMakeAction(sma);
		
		m_make.buildTarget("testPRule");
		
		assertTrue(sma.didRun());
		r.verify();
		}
		
	//---------------------------------------------------------------------------
	/**
		Test a pattern rule that is not multi target
	*/
	@Test
	public void namedPatternTest()
			throws Exception
		{
		List<String> sources = new ArrayList<String>();
		sources.add("test/FirstClass.java");
		sources.add("test/SecondClass.java");
		PatternRule r = new PatternRule("testPRule");
		
		r.addSources(sources);
		r.setSourcePattern("(.*)\\.java").setTargetPattern(m_baseDirPath+"/$1.class");
	
		SimpleMakeAction sma = new SimpleMakeAction();
		r.setMakeAction(sma);
		
		m_make.buildTarget("testPRule");
		
		assertEquals(2, sma.getRunCount());
		r.verify();
		}
		
	//---------------------------------------------------------------------------
	/**
		
	*/
	@Test
	public void singleTargetPatternTest()
			throws Exception
		{
		PatternRule r = new PatternRule("compile");
		String base = "src/cpp";
		
		SimpleFileSet fileSet = new SimpleFileSet().addFile(base, "main.cpp")
				.addFile(base, "file.cpp")
				.addFile(base, "socket.cpp");
				
		r.addSources(fileSet.getFullFilePaths());
		r.setSourcePattern(".*/([^/]*)\\.cpp").setTargetPattern(m_baseDirPath+"/$1.obj");
		
		SimpleMakeAction sma = new SimpleMakeAction();
		r.setMakeAction(sma);
		
		m_make.buildTarget("compile");
		
		//System.out.println(r.getSourceList());
		//System.out.println(r.getTargets());
		
		assertEquals(3,  sma.getRunCount());
		r.verify();
		}
		
	//---------------------------------------------------------------------------
	/**
	*/
	@Test
	public void patternRuleDepends()
			throws Exception
		{
		m_make.addSearchPath(".*\\.cpp", m_baseDirPath+"/src/cpp");
		
		List<String> srcFiles = new ArrayList<String>(); 
		srcFiles.add("main.cpp");
		srcFiles.add("file.cpp");
		
		SimpleMakeAction compileAction = new SimpleMakeAction();
		PatternRule compileRule = new PatternRule().addSources(srcFiles)
				.setSourcePattern("(.*)\\.cpp")
				.setTargetPattern(m_baseDirPath+"/$1.obj")
				.setMakeAction(compileAction);
		
		SimpleMakeAction zipAction = new SimpleMakeAction();
		SimpleRule r2 = new SimpleRule("doIt")
				.addTarget(m_baseDirPath+"/out.zip")
				.setMakeAction(zipAction)
				.addDepends(compileRule);
				
		m_make.buildTarget("doIt");
		
		assertTrue(compileAction.didRun());
		assertTrue(zipAction.didRun());
		}
		
	//---------------------------------------------------------------------------
	/**
		Test rule when only some files need updating as they are not there
	*/
	@Test
	public void partialRuleTest()
			throws Exception
		{
		m_make.addSearchPath(".*\\.cpp", m_baseDirPath+"/src/cpp");
		createFile(m_baseDirPath+"/file.obj", 51000L);
		SimpleMakeAction compileAction = new SimpleMakeAction();
		RegExFileSet sourceList = new RegExFileSet(m_baseDirPath+"/src/cpp",
				".*\\.cpp");
				
		PatternRule compile = new PatternRule("compile")
				.addSources(sourceList.getFileNames())
				.setSourcePattern("(.*)\\.cpp")
				.setTargetPattern(m_baseDirPath+"/$1.obj")
				.setMakeAction(compileAction);
				
		m_make.buildTarget("compile");
		
		assertEquals(2, compileAction.getRunCount());
		}

	//---------------------------------------------------------------------------
	/**
		Test rule when only some files need updating as they are out of date
	*/
	@Test
	public void partialRebuildCPatternRuleTest()
			throws Exception
		{
		m_make.addSearchPath(".*\\.cpp", m_baseDirPath+"/src/cpp");
		createFile(m_baseDirPath+"/file.obj", 51000L);
		createFile(m_baseDirPath+"/main.obj", 49900L);
		createFile(m_baseDirPath+"/socket.obj", 30000L);
		SimpleMakeAction compileAction = new SimpleMakeAction();
		RegExFileSet sourceList = new RegExFileSet(m_baseDirPath+"/src/cpp",
				".*\\.cpp");
				
		PatternRule compile = new PatternRule("compile")
				.addSources(sourceList.getFileNames())
				.setSourcePattern("(.*)\\.cpp")
				.setTargetPattern(m_baseDirPath+"/$1.obj")
				.setMakeAction(compileAction);
				
		m_make.buildTarget("compile");
		
		assertEquals(2, compileAction.getRunCount());
		}		
	}
