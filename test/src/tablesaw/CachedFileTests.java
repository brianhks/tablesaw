package tablesaw;

import tablesaw.CachedFile;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class CachedFileTests
	{
	@Test
	public void testOne()
			throws Exception
		{
		String path = "src/cpmake/CachedFileTests.java";
		CachedFile cf = new CachedFile(new File("test"), path);

		assertEquals(path, cf.getRelativePath());
		}
	}
