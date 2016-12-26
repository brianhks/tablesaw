package tablesaw;

import java.util.*;
import tablesaw.rules.Rule;
import java.io.File;

public class BuildFileManager
	{
	private Map<String, CachedFile>   m_locatedFiles;     //Files that have been located via search paths or by rules
	private Map<String, CachedFile>   m_fileCache;        //Cache used by getFile()
	private File                      m_workingDir;
	private ArrayList<SearchPath>     m_searchPaths;
	
	public BuildFileManager(File workingDir)
		{
		m_workingDir = workingDir;
		m_searchPaths = new ArrayList<SearchPath>();
		m_locatedFiles = new HashMap<String, CachedFile>();
		m_fileCache = new HashMap<String, CachedFile>();
		}
		
	//---------------------------------------------------------------------------
	public void clearFileCache()
		{
		m_fileCache = new HashMap<String, CachedFile>();
		}
		
	//---------------------------------------------------------------------------
	public void addSearchPath(SearchPath path)
		{
		m_searchPaths.add(path);
		}
		
	//---------------------------------------------------------------------------
	/**
		Tries to locate a file by looking in the search paths
		
		@param fileName Name of the file to look for
		@return The File object or null if no file is found
	*/
	public CachedFile locateFile(String fileName)
		{
		List<String> paths;
		CachedFile ret = null;
		CachedFile f;
		
		//System.out.println("Checking for "+fileName);
		//System.out.println(m_workingDir);
		f = new CachedFile(m_workingDir, fileName);
		//System.out.println(f.getAbsolutePath() +" "+f.exists());
		if (f.exists())
			ret = f;
		else if ((ret = m_locatedFiles.get(fileName)) == null)
			{
			paths = getPossibleFiles(fileName);
			
			for (String path : paths)
				{
				f = new CachedFile(m_workingDir, path);
				if (f.exists())
					{
					ret = f;
					m_locatedFiles.put(fileName, ret);
					break;
					}
				}
			}

		f = new CachedFile(fileName);
		if (f.exists())
			ret = f;

		return (ret);
		}
		
	//---------------------------------------------------------------------------
	/**
		Returns a list of where a file could be based on the search paths
	*/
	public ArrayList<String> getPossibleFiles(String file)
		{
		Iterator<SearchPath> it = m_searchPaths.iterator();
		ArrayList<String> paths = new ArrayList<String>();
		SearchPath sp;
		
		while (it.hasNext())
			{
			sp = it.next();
			
			if (sp.matches(file))
				paths.add(sp.getPath()+"/"+file);
			}
		return (paths);
		}
		
	//---------------------------------------------------------------------------
	/**
		This returns a file handle for the file name specified wheather or not
		the file exists.  It first tries to locate the file but if that fails
		a File object is returned pointing to a non existing file
	*/
	private CachedFile getFile(String fileName)
		{
		boolean add = false;
		//System.out.println(file);
		
		CachedFile f = m_fileCache.get(fileName);
		if (f == null)
			{
			add = true;
			f = locateFile(fileName);
			}
			
		if (f == null)
			f = new CachedFile(m_workingDir, fileName);
			
		if (add)
			m_fileCache.put(fileName, f);
			
		return (f);
		}
	}
