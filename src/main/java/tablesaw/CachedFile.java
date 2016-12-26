
package tablesaw;

import java.io.File;


public class CachedFile extends File
	{
	private long m_lastModified;  //Cached modification time
	private String m_path;
	
	/**
		Whenever this is used the parent is a potential working
		directory so we override the getPath to return just the path
	*/
	public CachedFile(File parent, String path)
		{
		super(parent, path);
		m_lastModified = -1;
		m_path = path;
		}
		
	public CachedFile(String path)
		{
		super(path);
		m_lastModified = -1;
		m_path = path;
		}
		
	public long lastModified()
		{
		if (m_lastModified == -1)
			m_lastModified = super.lastModified();
			
		return (m_lastModified);
		}
		
	public long getActualLastModified()
		{
		m_lastModified = super.lastModified();
		return (m_lastModified);
		}
		
	public boolean setLastModified(long time)
		{
		m_lastModified = time;
		return (super.setLastModified(time));
		}
		
	public String getRelativePath()
		{
		return (m_path);
		}
		
	}
