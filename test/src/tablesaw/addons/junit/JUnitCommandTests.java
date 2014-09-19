package tablesaw.addons.junit;

import tablesaw.addons.junit.JUnitCommand;
import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import static org.junit.Assert.*;

import tablesaw.TablesawException;
import tablesaw.Tablesaw;

public class JUnitCommandTests
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
		JUnitCommand junit = new JUnitCommand();
		
		junit.addSource("test/src/tablesaw/addons/junit/JUnitCommandTests.java");
		
		assertTrue(junit.getTestClasses().contains(
			"tablesaw.addons.junit.JUnitCommandTests"));
	}
	
	//---------------------------------------------------------------------------
	@Test(expected = TablesawException.class)
	public void addSourceTest_doesntExist()
		throws Exception
	{
		JUnitCommand junit = new JUnitCommand();
		
		junit.addSource("test/src/tablesaw/addons/junit/DoesNotExist.java");
	}
	
	//---------------------------------------------------------------------------
	@Test(expected = TablesawException.class)
	public void addSourceTest_noPackageInfo()
		throws Exception
	{
		JUnitCommand junit = new JUnitCommand();
		
		junit.addSource("src/make.java");
	}
}
