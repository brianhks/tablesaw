package tablesaw.addons;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import tablesaw.Tablesaw;
import tablesaw.TablesawException;

/**
 Created with IntelliJ IDEA.
 User: bhawkins
 Date: 1/23/13
 Time: 8:45 PM
 To change this template use File | Settings | File Templates.
 */
public class TarRuleTests
	{
	private Tablesaw m_make;

	@Before
	public void setupMake()
			throws TablesawException
		{
		m_make = new Tablesaw();
		m_make.setProperty(Tablesaw.PROP_CACHE_FILE, "build/.tarrulecache");
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
		TarRule zr = new TarRule("tar", "build/tartest1.tar");

		zr.addFile("test/test_scripts/test_method_signature.bsh");
		zr.addFile("build.bsh");
		zr.addFile("test", "test_scripts/test_method_signature.bsh");
		zr.addFile("test/testheader.h");
		zr.addFile("test", "test_scripts/test_method_signature.groovy");
		zr.addFileTo("zip_only_dir", "build.bsh");
		zr.addFileAs("test/testheader.h", "zip_only_dir/renamed.h");

		zr.setFilePermission(".*\\.bsh", 0755);

		zr.alwaysRun();

		//Debug.setDebug(true);
		m_make.buildTarget("tar");
		//Debug.setDebug(false);
		}
	}
