/*
 * Copyright (c) 2005, Brian Hawkins
 * brianhks@activeclickweb.com
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free 
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along 
 * with this program; if not, write to the 
 * Free Software Foundation, Inc., 
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
 
package tablesaw;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import java.io.*;
import java.util.*;
import static org.junit.Assert.*;

public class TablesawTest// extends TestCase
	{
	private static String s_testCompSrcDir = "test/testcompile/src";
	private static String s_testCompOutDir = "test/testcompile/build";
	private static String s_maincpp = s_testCompSrcDir+"/main.cpp";
	private static String s_header1 = s_testCompSrcDir+"/header1.h";
	private static String s_header2 = s_testCompSrcDir+"/header2.h";
	private static String s_header3 = s_testCompSrcDir+"/header3.h";
	private static String s_maincpp_content = "#include <header1.h>";
	private static String s_header1_content = "#include <header2.h>";
	private static String s_header1_content2 = "#include <header3.h>";
	private Tablesaw m_make;
	
	private void makeFile(String file)
		{
		makeFile(file, "");
		}
		
	private void makeFile(String file, String content)
		{
		try
			{
			File f = new File(file);
			//System.out.println("making file "+file);
			f.getParentFile().mkdirs();
			PrintWriter pw = new PrintWriter(new FileWriter(f));
			pw.print(content);
			pw.close();
			}
		catch (IOException ioe)
			{
			assertTrue(false);
			}
		}
		
	private boolean equals(String[] array1, String[] array2)
		{
		boolean ret = false;
		
		if (array1.length == array2.length)
			{
			ret = true;
			Arrays.sort(array1);
			Arrays.sort(array2);
			for (int I = 0; I < array1.length; I++)
				if (!array1[I].equals(array2[I]))
					{
					System.out.println(array1[I]+" != "+array2[I]);
					ret = false;
					break;
					}
			}
		else
			{
			System.out.println(array1.length +" != "+array2.length);
			}
			
		return (ret);
		}
		
		
	@Before
	public void setUp()
			throws TablesawException
		{
		m_make = new Tablesaw();
		m_make.init();
		
		makeFile("testfiles/rootfile");
		makeFile("testfiles/cvs/cvsfile"); //the makeFile command is creating parrent dirs all lower case
		makeFile("testfiles/subdir/file.java");
		makeFile("testfiles/subdir/file.class");
		makeFile("testfiles/subdir/file.c");
		makeFile("testfiles/subdir/file.cpp");
		makeFile("testfiles/subdir/file.h");
		makeFile("testfiles/subdir/file.hpp");
		
		makeFile(s_maincpp, s_maincpp_content);
		makeFile(s_header1, s_header1_content);
		makeFile(s_header2);
		}
		
	@After
	public void tearDown()
		{
		m_make.deltree("testfiles");
		m_make.deltree(s_testCompSrcDir);
		m_make.deltree(s_testCompOutDir);
		}
		
	public TablesawTest()
		{
		//super("Tablesaw test suite");
		}
		
	@Test
	public void testArrayToString()
		{
		String[] array = new String[] {"one", "two", "three"};
		
		assertTrue(Tablesaw.arrayToString(array).equals("one two three "));
		}
		
//------------------------------------------------------------------------------
	@Test
	public void testCreateFileList()
		{
		/* List<String> filelist = m_make.createFileList("testfiles", "(.*)", "cvs", 
				(Tablesaw.INCLUDE_PATH | Tablesaw.RECURSE | Tablesaw.RELATIVE_PATH)); */
		List<String> filelist = new RegExFileSet("testfiles", "(.*)").recurse().
				addExcludeDirs("cvs").getFilePaths();
		
		assertTrue(equals(new String[] {
				"rootfile",
				"subdir/file.java",
				"subdir/file.class",
				"subdir/file.c",
				"subdir/file.cpp",
				"subdir/file.h",
				"subdir/file.hpp"}, filelist.toArray(new String[0])));
		
		/* filelist = m_make.createFileList("testfiles", "(.*)", "(.*cvs.*)|(.*\\.java)", 
				(Tablesaw.INCLUDE_PATH | Tablesaw.RECURSE | Tablesaw.RELATIVE_PATH)); */
		filelist = new RegExFileSet("testfiles", "(.*)").recurse().setExcludeDirPattern(".*cvs.*").
			setExcludePattern(".*\\.java").getFilePaths();
		
		assertTrue(equals(new String[] {
				"rootfile",
				"subdir/file.class",
				"subdir/file.c",
				"subdir/file.cpp",
				"subdir/file.h",
				"subdir/file.hpp"}, filelist.toArray(new String[0])));
				
		/* filelist = m_make.createFileList("testfiles", "(subdir/.*)", 
				(Tablesaw.INCLUDE_PATH | Tablesaw.RECURSE | Tablesaw.RELATIVE_PATH)); */
		filelist = new RegExFileSet("testfiles", ".*").recurse().setDirectoryPattern("subdir").
			getFilePaths();
		
		assertTrue(equals(new String[] {
				"subdir/file.java",
				"subdir/file.class",
				"subdir/file.c",
				"subdir/file.cpp",
				"subdir/file.h",
				"subdir/file.hpp"}, filelist.toArray(new String[0])));
				
		String[] excludeList = new String[]
			{
			"file.cpp",
			"file.h",
			"rootfile",
			"file.hpp"
			};
			
		/* filelist = m_make.createFileList("testfiles", "(.*)", excludeList,
				(Tablesaw.INCLUDE_PATH | Tablesaw.RECURSE | Tablesaw.RELATIVE_PATH)); */
		filelist = new RegExFileSet("testfiles", ".*").recurse().addExcludeFiles(
				(Object[])excludeList).getFilePaths();
				
		assertTrue(equals(new String[] {
				"cvs/cvsfile",
				"subdir/file.java",
				"subdir/file.class",
				"subdir/file.c"
				}, filelist.toArray(new String[0])));
		}

//------------------------------------------------------------------------------
	//@Test
	public void testFileRename()
		{
		m_make.setProperty("cpmake.cacheDir", "");
		try
			{
			String cmd = "java -classpath ../../build/classes:../../ext/interpreters/beanshell/bsh-1.3.0.jar make -f buildfakecpp.bsh";
			m_make.exec("test/testcompile", cmd, true);
			File rename = new File(s_header2);
			rename.renameTo(new File(s_header3));
			makeFile(s_header1, s_header1_content2);
			m_make.exec("test/testcompile", cmd, true);
			}
		catch (TablesawException cpme)
			{
			cpme.printStackTrace();
			assertTrue(false);
			}
		/*catch (IOException ioe)
			{
			System.out.println(ioe.toString());
			assertTrue(false);
			}*/
		}
		
//------------------------------------------------------------------------------
	@Test
	public void testSplitString()
		{
		try
 			{												//There is an extra space here to mess up the spliter
			String[] arr = m_make.splitString("C:\\tools\\perl-5.8.6\\bin\\perl.exe  C:\\x.pl C:\\gen\\ C:\\gen2\\ ");
			assertTrue(equals(new String[] {
					"C:\\tools\\perl-5.8.6\\bin\\perl.exe",
					"C:\\x.pl",
					"C:\\gen\\",
					"C:\\gen2\\"}, arr));
			}
		catch (TablesawException cpme)
			{
			System.out.println(cpme.toString());
			assertTrue(false);
			}

		try
			{
			String[] arr = m_make.splitString("I Like \\\"apples\\\" \"and bananas\"");
			assertTrue(equals(new String[] {
					"I",
					"Like",
					"\"apples\"",
					"and bananas"}, arr));
			}
		catch (TablesawException cpme)
			{
			System.out.println(cpme.toString());
			assertTrue(false);
			}
			
		try
			{
			String[] arr = m_make.splitString("I Like \\\"apples\" \"and bananas\"");
			assertTrue(false);
			}
		catch (TablesawException cpme)
			{
			//System.out.println(cpme.toString());
			//It is supposed to throw an exception
			}
		}
		
//------------------------------------------------------------------------------
	@Test
	public void testGrep()
		{
		makeFile("testfiles/grep.txt", "red orange\norange blue\nblue green\n");
		GrepResult res = null;
		try
			{
			res = m_make.grep("testfiles/grep.txt", ".*blue.*");
			}
		catch (TablesawException cpme)
			{
			System.out.println(cpme);
			assertTrue(false);
			}
		
		assertTrue(res.isFound());
		assertTrue(res.getResults().length == 2);
		assertTrue(res.getLineNumbers()[0].intValue() == 2);
		assertTrue(res.getLineNumbers()[1].intValue() == 3);
		}
		
//------------------------------------------------------------------------------
	//@Test
	public void testExitOnError()
		{
		try
			{
			m_make.exec("java -cp build FailExec", false);
			}
		catch (TablesawException e)
			{
			assertTrue(false);
			}
		}
		
//------------------------------------------------------------------------------
	@Test
	public void testCDependencyParser()
		{
		CDependencyParser cdp = new CDependencyParser();
		String testheader = "test/testheader.h";
		long startTime, stopTime;
		//Time a bunch of iterations for performance reasons
		
		startTime = System.currentTimeMillis();
		for (int I = 0; I < 1000; I++)
			{
			try
				{
				String[] includes = cdp.parseFile(new File(testheader));
				
				assertEquals(4, includes.length);
				}
			catch (IOException ioe)
				{
				assertTrue(false);
				}
			}
		stopTime = System.currentTimeMillis();
		long totalTime = stopTime - startTime;
		long seconds = totalTime / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;
		seconds -= minutes * 60;
		minutes -= hours * 60;
		Object pargs[] = new Object[] {new Long(hours), new Long(minutes),
				new Long(seconds)};
		System.out.println(Printf.print("Parse time: %02d:%02d:%02d", pargs));
		}

	@Test
	public void testPropertyReplace()
		{
		m_make.setProperty("name", "Brian");
		m_make.setProperty("succeed", "Hello ${name}");
		m_make.setProperty("fail", "Hello ${duh}");

		assertEquals("Hello Brian", m_make.getProperty("succeed"));
		assertEquals("Hello ${duh}", m_make.getProperty("fail"));
		}

	}
