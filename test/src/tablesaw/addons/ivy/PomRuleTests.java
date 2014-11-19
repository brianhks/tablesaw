package tablesaw.addons.ivy;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import tablesaw.Tablesaw;
import tablesaw.TablesawException;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 Created by bhawkins on 4/16/14.
 */
public class PomRuleTests
	{
	private Tablesaw m_make;

	@Before
	public void setupMake()
			throws TablesawException
		{
		m_make = new Tablesaw("test/ivy");
		//m_make.setVerbose(true);
		m_make.setProperty(Tablesaw.PROP_CACHE_FILE, "build/.ivyrulecache");
		m_make.init();
		}

	@After
	public void cleanup()
		{
		m_make.close();
		}

	@Test
	public void testIvyParser() throws IOException, TablesawException
		{
		IvyAddon ia = new IvyAddon().setup();
		ia.setIvyFile("test/resources/ivy.xml");
		ResolveRule resolveRule = ia.getResolveRule("default");
		resolveRule.doMakeAction(resolveRule);
		File pomFile = File.createTempFile("pom", ".xml");
		PomRule pr = new PomRule(new File("test/resources/ivy.xml"), pomFile, resolveRule, null);
		pr.setPomName("parser_test");
		pr.setPomArtifactId("parser_test");

		pr.doMakeAction(pr);

		String genPomContent = FileUtils.readFileToString(pomFile);
		String pomContent = FileUtils.readFileToString(new File("test/resources/generated_pom.xml"));
		assertEquals(pomContent, genPomContent);
		//System.out.println(pomContent);
		//System.out.println(genPomContent);
		}

	@Test
	public void testIvyParser_withTests() throws IOException, TablesawException
		{
		IvyAddon ia = new IvyAddon().setup();
		ia.setIvyFile("test/resources/ivy.xml");
		ResolveRule resolveRule = ia.getResolveRule("default");
		ResolveRule testResolveRule = ia.getResolveRule("test");
		resolveRule.doMakeAction(resolveRule);
		testResolveRule.doMakeAction(testResolveRule);
		File pomFile = File.createTempFile("pom", ".xml");
		PomRule pr = new PomRule(new File("test/resources/ivy.xml"), pomFile, resolveRule, testResolveRule);
		pr.setPomName("parser_test");
		pr.setPomArtifactId("parser_test");

		pr.doMakeAction(pr);

		String genPomContent = FileUtils.readFileToString(pomFile);
		String pomContent = FileUtils.readFileToString(new File("test/resources/generated_test_pom.xml"));
		assertEquals(pomContent, genPomContent);
		//System.out.println(pomContent);
		//System.out.println(genPomContent);
		}

	/*@Test
	public void testPomRule() throws TablesawException
		{
		IvyAddon ia = new IvyAddon().setup();
		ia.setIvyFile("test/resources/ivy.xml");

		PomRule pr = ia.getPomRule();
		PomRule pr = ia.createPomRule(
		pr.addLicense("Apache", "http://apache.org", "repo");
		pr.addDeveloper("123", "Brian Hawkins", "brianhks1@gmail.com");
		pr.doMakeAction(pr);
		}*/
	}
