package tablesaw.java;

import java.util.*;
import java.io.File;

@Deprecated
public class ClassPath
	{
	private Set<String> m_paths;
	
	public ClassPath()
		{
		m_paths = new LinkedHashSet<String>();
		}
		
	/**
		Copy constructor
	*/
	public ClassPath(ClassPath otherPath)
		{
		if (otherPath == null)
			m_paths = new LinkedHashSet<String>();
		else
			m_paths = new LinkedHashSet<String>(otherPath.getPaths());
		}
		
	public ClassPath(String path)
		{
		this();
		m_paths.add(path);
		}
	
	public ClassPath(String[] paths)
		{
		this();
		for (int I = 0; I < paths.length; I++)
			m_paths.add(paths[I]);
		}
		
	public ClassPath(Iterable<String> paths)
		{
		this();
		for (String path : paths)
			m_paths.add(path);
		}
		
	public boolean isEmpty()
		{
		return (m_paths.size() == 0);
		}
		
	public ClassPath addPath(String path)
		{
		m_paths.add(path);
		return (this);
		}
		
	public ClassPath addPath(File path)
		{
		m_paths.add(path.getPath());
		return (this);
		}
		
	public ClassPath addPaths(String[] paths)
		{
		for (int I = 0; I < paths.length; I++)
			m_paths.add(paths[I]);
			
		return (this);
		}
		
	public ClassPath addPaths(Iterable<String> paths)
		{
		for (String path : paths)
			m_paths.add(path);
		return (this);
		}
		
	public ClassPath addPaths(ClassPath otherPath)
		{
		m_paths.addAll(otherPath.getPaths());
		return (this);
		}
	
	public String getFormattedPath()
		{
		StringBuffer sb = new StringBuffer();
		Iterator it = m_paths.iterator();
		if (it.hasNext())
			sb.append(it.next());
			
		while (it.hasNext())
			{
			sb.append(File.pathSeparator);
			sb.append(it.next());
			}
			
		return (sb.toString());
		}
		
	public Set<String> getPaths()
		{
		return (m_paths);
		}
		
	public String[] getPathsArray()
		{
		return (String[])m_paths.toArray(new String[0]);
		}
		
	public String toString()
		{
		return (getFormattedPath());
		}
	}
