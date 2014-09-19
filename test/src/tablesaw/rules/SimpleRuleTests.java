package tablesaw.rules;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import tablesaw.interpreters.BeanShellInterpreter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import static org.junit.Assert.*;

public class SimpleRuleTests extends RulesHelper
	{
	
	/**
		Test a named primary target
	*/
	@Test
	public void namedSimpleTest()
			throws Exception
		{
		SimpleRule r = new SimpleRule("test1").
				addTargets(m_baseDirPath+"/test/FirstClass.class", m_baseDirPath+"/test/SecondClass.class").
				addSources("test/FirstClass.java", "test/SecondClass.java");
				
		r.setMakeAction(new SimpleMakeAction());
				
		assertTrue(r.needToRun());
		
		m_make.buildTarget("test1");
		
		r.verify();
		}
		
	//---------------------------------------------------------------------------
	/**
		Test primary target of a file
	*/
	@Test
	public void fileSimpleTest()
			throws Exception
		{
		SimpleRule r = new SimpleRule("test1").
				addTargets(m_baseDirPath+"/test/FirstClass.class", m_baseDirPath+"/test/SecondClass.class").
				addSources("test/FirstClass.java", "test/SecondClass.java");
				
		r.setMakeAction(new SimpleMakeAction());
				
		assertTrue(r.needToRun());
		
		m_make.buildTarget(m_baseDirPath+"/test/FirstClass.class");
		
		r.verify();
		}
		
	//---------------------------------------------------------------------------
	/**
		A primary target with no sources or depends should always run
	*/
	@Test
	public void testPhonyRule()
			throws Exception
		{
		SimpleRule r = new SimpleRule("clean").override();
		
		SimpleMakeAction sma = new SimpleMakeAction();
		
		r.setMakeAction(sma);
		
		assertTrue(r.needToRun());
		
		m_make.buildTarget("clean");
		
		r.verify();
		
		assertTrue(sma.didRun());
		}
		
	//---------------------------------------------------------------------------
	/**
		A primary target with depends will return fals to needToRun but will 
		run if a depends runs
	*/
	@Test
	public void testPhonyRule2()
			throws Exception
		{
		SimpleRule r1 = new SimpleRule().
				addTargets(m_baseDirPath+"/test/FirstClass.class").
				addSources("test/FirstClass.java");
				
		r1.setMakeAction(new SimpleMakeAction());
				
		SimpleRule r2 = new SimpleRule("test").addDepends(r1);
		
		SimpleMakeAction phonyAction = new SimpleMakeAction();
		r2.setMakeAction(phonyAction);
		
		assertFalse(r2.needToRun());
		
		m_make.buildTarget("test");
		
		r1.verify();
		
		assertTrue(phonyAction.didRun());
		}
		
	//---------------------------------------------------------------------------
	/**
		Adding a depends via the name
	*/
	@Test
	public void testPhonyRule3()
			throws Exception
		{
		SimpleRule r1 = new SimpleRule("subrule").
				addTargets(m_baseDirPath+"/test/FirstClass.class").
				addSources("test/FirstClass.java");
				
		r1.setMakeAction(new SimpleMakeAction());
				
		SimpleRule r2 = new SimpleRule("test").addDepends("subrule");
		
		SimpleMakeAction phonyAction = new SimpleMakeAction();
		r2.setMakeAction(phonyAction);
		
		assertFalse(r2.needToRun());
		
		m_make.buildTarget("test");
		
		//This will make sure subrule was ran
		r1.verify();
		
		assertTrue(phonyAction.didRun());
		}
		
	//---------------------------------------------------------------------------
	/**
		Testing two target rules with a depends between them
	*/
	@Test
	public void testDepends()
			throws Exception
		{
		m_make.addSearchPath(".*\\.cpp", m_baseDirPath+"/src/cpp");
		
		SimpleMakeAction compileAction = new SimpleMakeAction();
		SimpleRule r1 = new SimpleRule()
				.addTarget(m_baseDirPath+"/main.obj")
				.addSource("main.cpp")
				.setMakeAction(compileAction);
				
		SimpleMakeAction zipAction = new SimpleMakeAction();
		SimpleRule r2 = new SimpleRule("doIt")
				.addTarget(m_baseDirPath+"/out.zip")
				.setMakeAction(zipAction)
				.addDepends(r1);
				
		m_make.buildTarget("doIt");
		
		assertTrue(compileAction.didRun());
		assertTrue(zipAction.didRun());
		}
		
	//---------------------------------------------------------------------------
	/**
		Tests the ant rule
	*/
	@Test
	public void testAntRule()
			throws Exception
		{
		m_make.setScripInterpreter(new BeanShellInterpreter(m_make.getProperties()));
		
		new SimpleRule("compile").setDescription("Compile all the files. Comment that xml doesn't <like> & \"others\"");
		//AntRule ant = new AntRule(m_baseDirPath+"/build.xml");
		AntBuildRule ant = new AntBuildRule("ant_build.xml");
		m_make.buildTarget("ant");
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setNamespaceAware(true);

		DocumentBuilder builder = factory.newDocumentBuilder();
		//Parse the build file to make sure it is well formed
		Document document = builder.parse(new InputSource("ant_build.xml"));
		}

	}
