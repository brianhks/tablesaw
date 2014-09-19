package tablesaw;

import tablesaw.TablesawException;
import tablesaw.Tablesaw;
import tablesaw.rules.SimpleRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MakeEngineTests
	{
	private Tablesaw m_make;
	
	@Before
	public void setupMake()
			throws TablesawException
		{
		m_make = new Tablesaw();
		m_make.setProperty(Tablesaw.PROP_CACHE_FILE, "build/.makeenginetests");
		m_make.init();
		}
	
	@After
	public void cleanup()
		{
		m_make.close();
		}
	
	@Test(expected = TablesawException.class)
	public void testMultipleRuleException()
			throws Exception
		{
		SimpleRule rule1 = new SimpleRule("javadoc");
		SimpleRule rule2 = new SimpleRule("javadoc");
		
		SimpleRule rule3 = new SimpleRule("run");
		rule3.addDepend(rule1);
		
		m_make.buildTarget("run");
		}
		
	//---------------------------------------------------------------------------
	@Test
	public void testMultipleRuleOverride()
			throws Exception
		{
		SimpleRule rule1 = new SimpleRule("javadoc");
		SimpleRule rule2 = new SimpleRule("javadoc").override();
		
		SimpleRule rule3 = new SimpleRule("run");
		rule3.addDepend("javadoc");
		
		m_make.buildTarget("run");
		}
		
	//---------------------------------------------------------------------------
	/**
		Should throw an error because two rules define the same target build/testfile
	*/
	@Test(expected = TablesawException.class)
	public void testMultipleRulesSameTarget()
			throws Exception
		{
		SimpleRule rule1 = new SimpleRule();
		rule1.addTarget("build/testfile");
		SimpleRule rule2 = new SimpleRule();
		rule2.addTarget("build/testfile");
		
		SimpleRule rule3 = new SimpleRule("run");
		rule3.addDepend(rule1);
		
		m_make.buildTarget("run");
		}
		
	}
