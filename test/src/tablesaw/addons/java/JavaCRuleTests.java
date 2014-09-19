package tablesaw.addons.java;

import tablesaw.addons.java.JavaCRule;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import tablesaw.TablesawException;
import tablesaw.Tablesaw;

import java.io.File;

public class JavaCRuleTests
	{
	public static final String BUILD_PATH = "build/unitests";
	private Tablesaw m_make;

	@BeforeClass
	public static void classSetup()
		{
		File f = new File("build/.javacrulecache");
		if (f.exists())
			f.delete();
		}

	@Before
	public void setupMake()
			throws TablesawException
		{
		m_make = new Tablesaw();
		m_make.setProperty(Tablesaw.PROP_CACHE_FILE, "build/.javacrulecache");
		m_make.init();
		}
	
	@After
	public void cleanup()
		{
		m_make.close();
		}

	@Test
	public void compileTest()
			throws TablesawException
		{
		m_make.deltree(BUILD_PATH);
		
		JavaCRule rule = new JavaCRule("build", BUILD_PATH);
		rule.addSourceDir("test/java/src");
		m_make.buildTarget("build");
		
		assertEquals(3, rule.getRebuildSources().size());
		}
		
	//---------------------------------------------------------------------------
	@Test
	public void incrementalBuildTest()
			throws Exception
		{
		m_make.touch("test/java/src/Company.java");
		//Debug.setDebug(true);
		JavaCRule rule = new JavaCRule("build", BUILD_PATH);
		rule.addSourceDir("test/java/src");
		m_make.buildTarget("build");
		
		//Only two of the sources should rebuild
		assertEquals(2, rule.getRebuildSources().size());
		}
		
	//---------------------------------------------------------------------------
	@Test
	public void noBuildTest()
			throws Exception
		{
		//Debug.setDebug(true);
		m_make.setVerbose(true);
		JavaCRule rule = new JavaCRule("build", BUILD_PATH);
		rule.addSourceDir("test/java/src");
		m_make.buildTarget("build");
		
		//No files should be built
		assertEquals(0, rule.getRebuildSources().size());
		
		m_make.deltree(BUILD_PATH);
		}
	}
