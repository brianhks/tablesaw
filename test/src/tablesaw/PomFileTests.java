package tablesaw;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.File;

public class PomFileTests
	{

	@Test
	public void testVersion() throws TablesawException
		{
		File testPom = new File("test/resources/pom.xml");

		PomFile pomFile = new PomFile(testPom);

		assertEquals("2.0.2", pomFile.getVersion());
		}
	}
