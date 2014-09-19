package tablesaw.rules;

import java.io.*;

import tablesaw.rules.Rule;
import org.junit.Before;
import org.junit.After;

import tablesaw.MakeAction;
import tablesaw.TablesawException;
import tablesaw.Tablesaw;

public class RulesHelper
	{
	public static class SimpleMakeAction implements MakeAction
		{
		private int m_ranCount = 0;
		
		public boolean didRun() { return (m_ranCount != 0); }
		public int getRunCount() { return (m_ranCount); }
		
		public void doMakeAction(Rule rule)
				throws TablesawException
			{
			m_ranCount ++;
			try
				{
				Iterable<String> targets = rule.getTargets();
				for (String t : targets)
					{
					File f = new File(t);
					if (f.exists())
						f.setLastModified(System.currentTimeMillis());
					else
						{
						f.getParentFile().mkdirs();
						f.createNewFile();
						}
					}
				}
			catch (Exception e)
				{
				throw new TablesawException(e);
				}
			}
		}
	
	public static String m_baseDirPath = "build/testdata";
	public static File m_baseDir = new File(m_baseDirPath);
	
	protected Tablesaw m_make;
	
	//---------------------------------------------------------------------------
	protected void createFile(String path, long mt)
		{
		createFile(path, mt, null);
		}
		
	//---------------------------------------------------------------------------
	protected void createFile(String path, long mt, String content)
		{
		File f = new File(m_baseDir, path);
		try
			{
			f.getParentFile().mkdirs();
			if (content != null)
				{
				PrintWriter pw = new PrintWriter(f);
				pw.println(content);
				pw.close();
				}
			else
				f.createNewFile();
				
			f.setLastModified(mt);
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}
		
	//---------------------------------------------------------------------------
	private void deltree(File directory)
		{
		if (!directory.exists())
			return;
		File[] list = directory.listFiles();
		
		if (list.length > 0)
			{
			for (int I = 0; I < list.length; I++)
				{
				if (list[I].isDirectory())
					deltree(list[I]);
				
				list[I].delete();
				}
			}
		
		directory.delete();
		}
		
	//---------------------------------------------------------------------------
	@Before
	public void setupFiles()
			throws TablesawException
		{
		m_baseDir.mkdirs();
		
		createFile("src/java/test/FirstClass.java", 10000L);
		createFile("src/java/test/SecondClass.java", 20000L);
		
		createFile("src/cpp/main.cpp", 50000L);
		createFile("src/cpp/file.cpp", 50100L);
		createFile("src/cpp/socket.cpp", 50000L);
		
		m_make = new Tablesaw();
		m_make.init();
		m_make.addSearchPath(".*\\.java", m_baseDirPath+"/src/java");
		m_make.addSearchPath(".*", m_baseDirPath);
		}
		
	//---------------------------------------------------------------------------
	@After
	public void cleanupFiles()
		{
		deltree(m_baseDir);
		}
	}
