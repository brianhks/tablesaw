package tablesaw.addons;

import tablesaw.addons.ZipRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import tablesaw.TablesawException;
import tablesaw.Tablesaw;

public class ZipRuleTests
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
		ZipRule zr = new ZipRule("zip", "build/ziptest1.zip");
		
		zr.addFile("test/test_scripts/test_method_signature.bsh");
		zr.addFile("build.bsh");
		zr.addFile("test", "test_scripts/test_method_signature.bsh");
		zr.addFile("test/testheader.h");
		zr.addFile("test", "test_scripts/test_method_signature.groovy");
		zr.addFileTo("zip_only_dir", "build.bsh");
		zr.addFileAs("test/testheader.h", "zip_only_dir/renamed.h");
		
		zr.alwaysRun();

		//Debug.setDebug(true);
		m_make.buildTarget("zip");
		//Debug.setDebug(false);
		}
	}
