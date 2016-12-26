package tablesaw;

import java.util.List;
import java.util.ArrayList;


public abstract class AbstractFileSet<T extends AbstractFileSet>
	{
	public static class File
		{
		private String m_baseDir;
		private String m_file;
		
		public File(String baseDir, String file)
			{
			m_baseDir = baseDir;
			m_file = file;
			}
			
		public String getBaseDir() { return (m_baseDir); }
		public String getFile() { return (m_file); }
		
		@Override
		public String toString()
			{
			StringBuilder sb = new StringBuilder();
			sb.append(m_baseDir).append(" ").append(m_file);
			return (sb.toString());
			}
		}
	
	private List<FileFilter> m_filterList;  //List of filters used to filter files
	private List<AbstractFileSet.File> m_files;
	
	//---------------------------------------------------------------------------
	public AbstractFileSet()
		{
		m_filterList = new ArrayList<FileFilter>();
		m_files = new ArrayList<AbstractFileSet.File>();
		}
		
	//---------------------------------------------------------------------------
	public T addFileFilter(FileFilter fileFilter)
		{
		m_filterList.add(fileFilter);
		return ((T)this);
		}
		
	//---------------------------------------------------------------------------
	public T addFile(String baseDir, String file)
		{
		if (!filterFile(baseDir, file))
			m_files.add(new AbstractFileSet.File(baseDir, file));
			
		return ((T)this);
		}
		
	//---------------------------------------------------------------------------
	/**
		Called by sub classes to see if a file should be filtered before being
		returned from <code>getFiles</code>
	*/
	protected boolean filterFile(String baseDir, String file)
		{
		return (false);
		}
		
	//---------------------------------------------------------------------------
	/**
		Returns a list of just the file names of the files contained in this
		FileSet
	*/
	public List<String> getFileNames()
		{
		ArrayList<String> ret = new ArrayList<String>();
		List<File> files = getFiles();
		
		for (File f : files)
			{
			java.io.File ioFile = new java.io.File(f.getFile());
			ret.add(ioFile.getName());
			}
			
		return (ret);
		}
	
	//---------------------------------------------------------------------------
	/**
		Returns the relative file paths of files contained in this FileSet.
		The file paths start at the base dir that was specified when the FileSet
		was created
	*/
	public List<String> getFilePaths()
		{
		ArrayList<String> ret = new ArrayList<String>();
		List<File> files = getFiles();
		
		for (File f : files)
			{
			ret.add(f.getFile());
			}
			
		return (ret);
		}
		
	//---------------------------------------------------------------------------
	/**
		Returns the full file paths to all files in this FileSet
	*/
	public List<String> getFullFilePaths()
		{
		ArrayList<String> ret = new ArrayList<String>();
		List<File> files = getFiles();
		
		for (File f : files)
			{
			java.io.File ioFile = new java.io.File(f.getBaseDir(), f.getFile());
			ret.add(ioFile.getPath());
			}
			
		return (ret);
		}
		
	//---------------------------------------------------------------------------	
	public List<AbstractFileSet.File> getFiles()
		{
		return (m_files);
		}
	}
