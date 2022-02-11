package tablesaw.addons.ivy;


import org.apache.ivy.core.report.ResolveReport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import tablesaw.TablesawException;
import tablesaw.Tablesaw;

import org.apache.ivy.core.report.ArtifactDownloadReport;
import tablesaw.addons.java.JavaProgram;

import java.util.Collections;

public class IvyAddonTests
	{
	private Tablesaw m_make;
	
	@Before
	public void setupMake()
			throws TablesawException
		{
		m_make = new Tablesaw("test/ivy");
		m_make.setProperty(Tablesaw.PROP_CACHE_FILE, "build/.ivyrulecache");
		m_make.setProperty(JavaProgram.CLASS_DIRECTORY_PROPERTY, "build/classes");
		m_make.setProperty(JavaProgram.SOURCE_DIRECTORY_PROPERTY, "src/main/java");
		m_make.init();
		}
	
	@After
	public void cleanup()
		{
		m_make.close();
		}



	@Test 
	public void testGetResolveRule()
			throws TablesawException
		{
		IvyAddon ia = new IvyAddon().addSettingsFile("test/ivy/ivysettings.xml").setup();
		
		ResolveRule rr1 = ia.getResolveRule("*");
		ResolveRule rr2 = ia.getResolveRule(Collections.singleton("*"));
		
		assertTrue(rr1 == rr2);
		}
	
	//---------------------------------------------------------------------------
	@Test
	public void testResolve()
			throws TablesawException
		{
		IvyAddon ia = new IvyAddon().addSettingsFile("test/ivy/ivysettings.xml").setup();
		
		m_make.buildTarget("ivy-resolve");

		ResolveRule rr = ia.getResolveRule("*");
		ResolveReport report = rr.getReport();
		assertFalse(report.hasError());
		
		ArtifactDownloadReport[] deps = ia.getResolveRule("*").getReport()
				.getConfigurationReport("default").getAllArtifactsReports();
		for (ArtifactDownloadReport rep : deps)
			{
			if (rep.getType().equals("jar"))
				System.out.println(rep.getLocalFile().getPath());
			}
		/* List<IvyNode> deps = ia.getResolveRule().getReport().getDependencies();
		for (IvyNode in : deps)
			{
			System.out.println(in.);
			} */
		}
		
	//---------------------------------------------------------------------------
	@Test
	public void testRetrieve()
			throws TablesawException
		{
		IvyAddon ia = new IvyAddon().addSettingsFile("test/ivy/ivysettings.xml").setup();

		ia.getRetrieveRule("*").setUseWorkingDirectory(true);

		m_make.buildTarget("ivy-retrieve");
		}
		
	//---------------------------------------------------------------------------
	@Test
	public void buildFileTest()
			throws TablesawException
		{
		//tablesaw.Debug.setDebug(true);
		m_make.processBuildFile("test/ivy/build.groovy");
		//m_make.setVerbose(true);
		m_make.buildTarget("jar");
		}
	}
