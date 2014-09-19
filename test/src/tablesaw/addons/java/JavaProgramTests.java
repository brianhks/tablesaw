package tablesaw.addons.java;

import tablesaw.addons.java.JavaProgram;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import tablesaw.TablesawException;
import tablesaw.Tablesaw;

public class JavaProgramTests
	{
	private Tablesaw m_make;
	
	@Before
	public void setupMake()
			throws TablesawException
		{
		m_make = new Tablesaw("test");
		m_make.setProperty(Tablesaw.PROP_CACHE_FILE, "build/.javaprogram");
		m_make.init();
		}
	
	@After
	public void cleanup()
		{
		m_make.close();
		}

	@Test
	public void noArgConstructorTest()
			throws TablesawException
		{
		JavaProgram jp = new JavaProgram();
		
		// TODO: change this to different directory
		assertEquals("test", jp.getProgramName());
		}
		
	}
