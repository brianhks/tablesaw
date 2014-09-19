package tablesaw.sync;

import java.io.*;

/**
	Represents a single file to synchronize
*/
public class SyncFile
	{
	private File m_source;
	private long m_sourceModTime;
	
	public SyncFile(File source)
		{
		m_source = source;
		m_sourceModTime = source.lastModified();
		}
		
	public void setSourceModTime(long sourceModTime) { m_sourceModTime = sourceModTime; }
	public long getSourceModTime() { return (m_sourceModTime); }
	
	public File getSource()
		{
		return (m_source);
		}
	}
