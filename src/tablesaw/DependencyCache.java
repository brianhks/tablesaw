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
 
import java.io.*;
import java.util.*;

public class DependencyCache
		implements Serializable
	{
	private Map<String, ParsedFile> m_parsedFileMap;
	private Map<String, Map<String, Long>> m_modifiedTimeMap;  //Contains the modified time of files used to see if a file has been modifed.
	private Map<String, String> m_properties;
	
	
//===================================================================
//==-- ParsedFile inner class --==
	private class ParsedFile
			implements Serializable
		{
		private long m_lastParsed;  //this is the modification time of when the file was parsed
		private CachedFile m_sourceFile;
		private Set<ParsedFile> m_dependencyList;
		private DependencyParser m_parser;
		private String m_includeName; //path used to include the file.  This is what is passed to cpmake
		
		private boolean isUpToDate()
			{
			if (Debug.isDebug())
				{
				Debug.print("  isUpToDate("+m_lastParsed+" == "+m_sourceFile.lastModified()+")");
				}
			return (m_lastParsed == m_sourceFile.lastModified());
			}
			
		private void parseFile(tablesaw.Tablesaw make)
				throws IOException
			{
			Debug.print("  Parsing file "+m_sourceFile);
			String[] list;
			CachedFile file;
			ParsedFile parsedFile;
			
			m_dependencyList.clear();
			
			if (m_parser == null)
				return;
			
			if (!m_sourceFile.exists()) //Source file was possibly moved
				{
				CachedFile newFile = make.locateFile(m_includeName);
				if (newFile != null)
					m_sourceFile = newFile;
				}
				
			m_lastParsed = m_sourceFile.lastModified();
			list = m_parser.parseFile(m_sourceFile);
			for (int I = 0; I < list.length; I++)
				{
				Debug.print("  parsed out "+list[I]);
				file = make.locateFileUsingRules(list[I]);
				Debug.print("    found "+file);
				
				if (file != null)
					{
					parsedFile = getParsedFile(list[I], file);
					
					m_dependencyList.add(parsedFile);
					}
				//else we are going to igonore files we cannot find
				}
			}
		
		//=====================================================
		public ParsedFile(String includeName, CachedFile source, DependencyParser parser)
			{
			m_sourceFile = source;
			m_parser = parser;
			m_dependencyList = new HashSet<ParsedFile>();
			m_lastParsed = 0;
			m_includeName = includeName;
			}
			
		public CachedFile getSourceFile()
			{
			return (m_sourceFile);
			}
		
		public String getIncludeName()
			{
			return (m_includeName);
			}
			
		public Set<ParsedFile> getDependencyList(Tablesaw make)
				throws IOException
			{
			if (!isUpToDate())
				parseFile(make);
				
			return (m_dependencyList);
			}
			
		public boolean canRecurse()
			{
			return (m_parser.canRecurse());
			}
			
		public boolean equals(Object obj)
			{
			return (m_sourceFile.getAbsolutePath().equals(obj));
			}
			
		public int hashCode()
			{
			int ret = 0;
			
			//When loading the object form file the m_sourceFile can be null
			if (m_sourceFile != null)
				ret = m_sourceFile.getAbsolutePath().hashCode();
				
			return (ret);
			}
			
		public long getParsedTime()
			{
			return (m_lastParsed);
			}
		}
//==-- End of ParsedFile inner class --==
//===================================================================
		
	private ParsedFile getParsedFile(String includeName, CachedFile source)
		{
		ParsedFile parsedFile = m_parsedFileMap.get(source.getAbsolutePath());
		
		if (parsedFile == null)
			{
			parsedFile = new ParsedFile(includeName, source, getDependencyParser(source.getName()));
			m_parsedFileMap.put(parsedFile.getSourceFile().getAbsolutePath(), parsedFile);
			}
			
		return (parsedFile);
		}
		
	private DependencyParser getDependencyParser(String file)
		{
		if (file.matches(".*\\.c") || file.matches(".*\\.cpp") ||
					file.matches(".*\\.h") || file.matches(".*\\.hpp"))
			return (new CDependencyParser());
		else if (file.matches(".*\\.class"))
			{
			return (new JavaDependencyParser());
			}
		else
			return (null);
		}
		
	private void getDependencies(Set<String> retSet, Tablesaw make, ParsedFile parsedFile)
			throws IOException
		{
		Set<ParsedFile> dependencies = parsedFile.getDependencyList(make);
		Iterator<ParsedFile> it = dependencies.iterator();
		ParsedFile pfile;
		
		while (it.hasNext())
			{
			pfile = it.next();
			
			//System.out.println(pfile.getIncludeName());
			if ((retSet.add(pfile.getIncludeName())) && (parsedFile.canRecurse()))
				getDependencies(retSet, make, pfile);
			}
		}
	
//===================================================================
	public DependencyCache()
		{
		m_parsedFileMap = new HashMap<String, ParsedFile>();
		m_modifiedTimeMap = new HashMap<String, Map<String, Long>>();
		}
		
	public void clearCache()
		{
		m_modifiedTimeMap = new HashMap<String, Map<String, Long>>();
		}
		
	public boolean canParse(String file)
		{
		boolean ret = getDependencyParser(file) != null;
		return (ret);
		}
		
	public Set<String> getDependencies(Tablesaw make, String file)
			throws TablesawException
		{
		CachedFile srcFile = make.locateFile(file);
		ParsedFile parsedFile;
		DependencyParser parser = null;
		Set<String> dependencies = new HashSet<String>();

		Debug.print("Dependency lookup for "+file);		
		
		if ((srcFile != null) && (canParse(file)))
			{
			Debug.print("  Can parse");
			parsedFile = getParsedFile(file, srcFile);
			
			try
				{
				getDependencies(dependencies, make, parsedFile);
				}
			catch (IOException ioe)
				{
				throw (new TablesawException("Dependency error\n"+ioe.getMessage(), -1));
				}	
			}
		
		Debug.print("Lookup done "+dependencies.size()+" dependencies found.");
		return (dependencies);
		}
		
	public void cacheDependencyMap(String file, Map<String, Long> map)
		{
		m_modifiedTimeMap.put(file, map);
		}
		
	public Map<String, Long> getDependencyCacheMap(String file)
		{
		return (m_modifiedTimeMap.get(file));		
		}
		
	public String getProperty(String key)
		{
		return (m_properties.get(key));
		}
		
	/*public void setProperty(String key, String value)
		{
		m_properties.put(key, value);
		}*/
		
//===================================================================
	/* public static void writeDependencyCache()
			throws IOException
		{
		FileOutputStream fos = new FileOutputStream(s_cacheFile);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		
		oos.writeObject(s_depCache);
		
		oos.close();
		}
		
	public static DependencyCache getDependencyCache(String cacheFile)
		{
		try
			{
			if (s_depCache == null)
				{
				s_cacheFile = cacheFile;
				readDependencyCache(cacheFile);
				}
			}
		catch (Exception ioe)
			{
			Debug.print(ioe.getMessage());
			StringWriter sw = new StringWriter();
			ioe.printStackTrace(new PrintWriter(sw));
			Debug.print(sw.toString());
			s_depCache = new DependencyCache();
			}
			
		return (s_depCache);
		} */
		
	/* public static String getCacheFile()
		{
		return (s_cacheFile);
		} */
		
	/* private static void readDependencyCache(String cacheFile)
			throws IOException, ClassNotFoundException
		{
		FileInputStream fis = new FileInputStream(cacheFile);
		ObjectInputStream ois = new ObjectInputStream(fis);
		
		s_depCache = (DependencyCache)ois.readObject();
		
		ois.close();
		} */
	}
