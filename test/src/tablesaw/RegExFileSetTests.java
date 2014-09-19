package tablesaw;

import org.junit.Test;

import static org.junit.Assert.*;

import java.io.File;

public class RegExFileSetTests
	{
	@Test
	public void recurseTest()
		{
		RegExFileSetWrapper refs = new RegExFileSetWrapper(".", ".*");
		refs.recurse();
		
		assertEquals("RECURSE", refs.getFileAction(new File("test/src"), new File("test")));
		}
	}
