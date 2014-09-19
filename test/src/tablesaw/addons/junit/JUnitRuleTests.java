package tablesaw.addons.junit;

import tablesaw.addons.junit.JUnitRule;
import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.AfterClass;

import tablesaw.MockMakeAction;
import tablesaw.TablesawException;
import tablesaw.Tablesaw;

public class JUnitRuleTests
{
	public static Tablesaw s_make;
	
	@BeforeClass
	public static void setupMake()
		throws TablesawException
	{
		s_make = new Tablesaw();
		s_make.init();
	}
	
	//---------------------------------------------------------------------------
	@AfterClass
	public static void cleanup()
	{
		s_make.close();
	}
	
	//---------------------------------------------------------------------------
	@Test
	public void addSourceTest()
		throws Exception
	{
		JUnitRule junit = new JUnitRule();
		
		junit.addSource("test/src/tablesaw/addons/junit/JUnitCommandTests.java");
		
		MockMakeAction mma = new MockMakeAction();
		junit.setMakeAction(mma);
		s_make.buildTarget("junit-test");
	}
}
