package tablesaw.addons.java;

import java.util.*;
import java.io.File;

/**

*/
public class Classpath
	{
	private Set<String> m_paths;

	/**
	 Creates an empty Classpath object.
	 */
	public Classpath()
		{
		m_paths = new LinkedHashSet<String>();
		}
		
	/**
		Copy constructor.  Creates a copy of otherPath
	 	@param otherPath Other Classpath object to copy paths from.
	*/
	public Classpath(Classpath otherPath)
		{
		if (otherPath == null)
			m_paths = new LinkedHashSet<String>();
		else
			m_paths = new LinkedHashSet<String>(otherPath.getPaths());
		}

	/**
	 Creates a new Classpath object containing a single path.
	 @param path Path to add to the Classpath.
	 */
	public Classpath(String path)
		{
		this();
		m_paths.add(path);
		}

	/**
	 Creates a new Classpath object containing the list of paths provided.
	 @param paths List of paths to add to the classpath.
	 */
	public Classpath(String[] paths)
		{
		this();
		for (int I = 0; I < paths.length; I++)
			m_paths.add(paths[I]);
		}

	/**
	 Creates a new Classpath object containing the list of paths provided.
	 @param paths List of paths to add to the classpath.
	 */
	public Classpath(Iterable<String> paths)
		{
		this();
		for (String path : paths)
			m_paths.add(path);
		}

	/**
	 Check if the classpath is empty
	 @return Returns true if the classpath is empty.
	 */
	public boolean isEmpty()
		{
		return (m_paths.size() == 0);
		}

	/**
	 Appends a single path to the classpath.
	 @param path Path to add
	 @return
	 */
	public Classpath addPath(String path)
		{
		m_paths.add(path);
		return (this);
		}

	/**
	 Appends a file object as a new path.  The actual path that is added is
	 <code>path.getPath()</code>
	 @param path
	 @return
	 */
	public Classpath addPath(File path)
		{
		m_paths.add(path.getPath());
		return (this);
		}

	/**
	 Append multiple paths to the classpath.
	 @param paths
	 @return
	 */
	public Classpath addPaths(String[] paths)
		{
		for (int I = 0; I < paths.length; I++)
			m_paths.add(paths[I]);
			
		return (this);
		}

	/**
	 Append multiple paths to the classpath.
	 @param paths
	 @return
	 */
	public Classpath addPaths(Iterable<String> paths)
		{
		for (String path : paths)
			m_paths.add(path);
		return (this);
		}

	/**
	 Append paths from another Classpath object to this classpath.
	 @param otherPath
	 @return
	 */
	public Classpath addPaths(Classpath otherPath)
		{
		m_paths.addAll(otherPath.getPaths());
		return (this);
		}

	/**
	 Returns a string representation of this classpath that is appropriate for
	 the platform you are on.  Ex. on Linux it would return path1:path2:...
	 @return
	 */
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

	/**
	 Returns a set of paths this Classpath object contains.
	 @return
	 */
	public Set<String> getPaths()
		{
		return (m_paths);
		}

	/**
	 Get the paths as a String[]
	 @return
	 */
	public String[] getPathsArray()
		{
		return (String[])m_paths.toArray(new String[0]);
		}

	/**
	 Returns the same as {@link #getFormattedPath()}
	 @return
	 */
	public String toString()
		{
		return (getFormattedPath());
		}
	}
