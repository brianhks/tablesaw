package tablesaw.addons.java;

import tablesaw.addons.java.JarRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import tablesaw.TablesawException;
import tablesaw.NullParameterException;
import tablesaw.MissingFileException;
import tablesaw.Tablesaw;

public class JarRuleTests
	{
	private Tablesaw m_make;
	
	@Before
	public void setupMake()
			throws TablesawException
		{
		m_make = new Tablesaw();
		m_make.setProperty(Tablesaw.PROP_CACHE_FILE, "build/.ziprulecache");
		m_make.init();
		}
	
	@After
	public void cleanup()
		{
		m_make.close();
		}
	
	@Test
	public void firstTest() throws Exception
		{
		JarRule jr = new JarRule("jar", "build/ziptest1.jar");
		
		jr.addFile("build.bsh");
		jr.addFile("test", "test_scripts/test_method_signature.bsh");
		jr.addFile("test/testheader.h");
		jr.addFile("test", "test_scripts/test_method_signature.groovy");
		jr.alwaysRun();
		
		m_make.buildTarget("jar");
		}
		
	//---------------------------------------------------------------------------
	@Test (expected = NullParameterException.class)
	public void nullCheckTest() throws Exception
		{
		JarRule jr = new JarRule("jar", "build/ziptest2.jar");
		
		jr.setManifest(null);
		}
		
	//---------------------------------------------------------------------------
	@Test (expected = MissingFileException.class)
	public void missingFileCheckTest() throws Exception
		{
		JarRule jr = new JarRule("jar", "build/ziptest2.jar");
		
		jr.setManifest("/file/not/there");
		}

	@Test
	public void metainfTest() throws Exception
		{
		JarRule jr = new JarRule("jar", "build/ziptest3.jar");

		jr.addFile("build.bsh");
		jr.addFile("test", "test_scripts/test_method_signature.bsh");
		jr.addFile("test/testheader.h");
		jr.addFile("test", "test_scripts/test_method_signature.groovy");
		jr.addFileTo("META-INF", "build.bsh");
		jr.alwaysRun();

		m_make.buildTarget("jar");
		}
	}
