package anttest;

import org.junit.Test;

import java.io.*;

public class TestClass
	{
		
	@Test
	public void testFirst()
			throws Exception
		{
		System.out.println("First Test");
		File results = new File("/home/bhawkins/testclass.txt");
		PrintWriter pw = new PrintWriter(new FileWriter(results));
		pw.println("YES");
		pw.close();
		}
	}
