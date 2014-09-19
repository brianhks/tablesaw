package tablesaw.addons.csharp;

import tablesaw.Tablesaw;
import tablesaw.TablesawException;
import tablesaw.addons.csharp.ApplicationType;
import tablesaw.addons.csharp.CSharpProgram;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created with IntelliJ IDEA.
 * User: bhawkins
 * Date: 8/8/12
 * Time: 7:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class CSharpProgramTests
	{
	private Tablesaw m_make;
	
	@Before
	public void setupMake()
			throws TablesawException
		{
		m_make = new Tablesaw("test/SampleApp/ConsoleApp");
		m_make.init();
		}
	
	//---------------------------------------------------------------------------
	@After
	public void cleanup()
		{
		m_make.close();
		}

	@Test
	public void readProjectFileTest()
			throws TablesawException
		{
		CSharpProgram csProgram = new CSharpProgram();
		
		csProgram.setProjectFile("test/SampleApp/ConsoleApp/ConsoleApp.csproj").setup();

		assertEquals("run-me", csProgram.getProgramName());
		Assert.assertEquals(ApplicationType.EXE, csProgram.getApplicationType());
		
		System.out.println(csProgram.getDefinition().getCommand());
		}
	}
