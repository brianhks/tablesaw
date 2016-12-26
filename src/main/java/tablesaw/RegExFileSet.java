package tablesaw;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.*;
import java.util.Arrays;

import static tablesaw.util.Validation.*;

public class RegExFileSet extends AbstractFileSet<RegExFileSet>
	{
	protected enum Action
		{
		SKIP,
		RECURSE,
		ADD
		}
		
	private String m_workingBaseDir;
	private String m_baseDir;
	private Pattern m_pattern;
	private Pattern m_dirPattern;
	private Pattern m_excPattern;
	private Pattern m_excDirPattern;
	private Set<String> m_excludeFiles;
	private Set<String> m_excludeDirs;
	private String m_startDir;           //Directory that is relative to the m_baseDir
	
	private boolean m_recurse;
	
	public RegExFileSet(String baseDir, String filePattern)
		{
		Tablesaw make = Tablesaw.getCurrentTablesaw();
		m_recurse = false;
		m_baseDir = new java.io.File(baseDir).getPath().replace('\\', '/') + "/";
		m_workingBaseDir = new java.io.File(make.getWorkingDirectory(), m_baseDir).getPath().replace('\\', '/') + "/";
		m_dirPattern = null;
		m_excDirPattern = null;
		m_excPattern = null;
		m_pattern = Pattern.compile(filePattern);
		m_excludeFiles = new HashSet<String>();
		m_excludeDirs = new HashSet<String>();
		}
		
	//---------------------------------------------------------------------------
	/**
		Sets the search for files to recurse through sub directories
	*/
	public RegExFileSet recurse()
		{
		m_recurse = true;
		return (this);
		}
		
	//---------------------------------------------------------------------------
	/**
	Sets the start directory for searching.
	This directory is relative to the base directory.  Effectively lets you 
	search a certain sub tree under the base directory.
	@since 2.2.0
	*/
	public RegExFileSet setStartDir(String startDir)
		{
		m_startDir = startDir;
		
		return (this);
		}
		
	//---------------------------------------------------------------------------
	/**
		Files are excluded from list if they match this pattern
	*/
	public RegExFileSet setExcludePattern(String pattern)
		{
		m_excPattern = Pattern.compile(pattern);
		return (this);
		}
		
	//---------------------------------------------------------------------------
	/**
		Directories are excluded if they match this pattern
	*/
	public RegExFileSet setExcludeDirPattern(String pattern)
		{
		m_excDirPattern = Pattern.compile(pattern);
		return (this);
		}
		
	//---------------------------------------------------------------------------
	/**
		Only files in the directories that match the pattern will be included in 
		the file set.  This pattern does not effect what directories are recursed 
		into.
	*/
	public RegExFileSet setDirectoryPattern(String pattern)
		{
		m_dirPattern = Pattern.compile(pattern);
		return (this);
		}
		
	//---------------------------------------------------------------------------
	public RegExFileSet addExcludeFile(String file)
	{
		m_excludeFiles.add(file);
		return (this);
	}
	
	//---------------------------------------------------------------------------
	/**
		Specify file names to exclucde from the file set
	*/
	public RegExFileSet addExcludeFiles(Object... files)
		{
		if ((files.length == 1) && (files[0] instanceof Iterable))
			return (addExcludeFiles((Iterable<Object>)files[0]));
		else
			return (addExcludeFiles(Arrays.asList(files)));
		}
		
	//---------------------------------------------------------------------------
	public RegExFileSet addExcludeFiles(Iterable<Object> files)
	{
		for (Object f : files)
			addExcludeFile(objectToString(f));
		
		return (this);
	}
		
	//---------------------------------------------------------------------------
	public RegExFileSet addExcludeDir(String dir)
		{
		m_excludeDirs.add(dir);
		
		return (this);
		}
		
	//---------------------------------------------------------------------------
	public RegExFileSet addExcludeDirs(String... dirs)
		{
		for (String d : dirs)
			m_excludeDirs.add(d);
		return (this);
		}
		
	//---------------------------------------------------------------------------
	public RegExFileSet addExcludeDirs(Iterable<String> dirs)
		{
		for (String d : dirs)
			m_excludeDirs.add(d);
			
		return (this);
		}
		
	//---------------------------------------------------------------------------
	protected Action getAction(java.io.File file, java.io.File dir)
		{
		String fName = file.getName();
		if (fName.equals(".") || fName.equals(".."))
			return (Action.SKIP);
		
		if (m_recurse && file.isDirectory() && 
				!m_excludeDirs.contains(fName) &&
				(m_excDirPattern == null || !m_excDirPattern.matcher(fName).matches()))
			{
			return (Action.RECURSE);
			}
		else if (file.isDirectory())
			return (Action.SKIP);
			
		if (!m_excludeFiles.contains(fName) && 
				m_pattern.matcher(fName).matches() &&
				(m_dirPattern == null || m_dirPattern.matcher(dir.getName()).matches()) &&
				(m_excPattern == null || !m_excPattern.matcher(fName).matches()))
			{
			return (Action.ADD);
			}
			
		return (Action.SKIP);
		}
		
	//---------------------------------------------------------------------------
	private void doGetFiles(List<AbstractFileSet.File> files, java.io.File dir)
		{
		java.io.File[] list = dir.listFiles();
		
		if (list != null)
			{
			for (int I = 0; I < list.length; I++)
				{
				switch (getAction(list[I], dir))
					{
					case SKIP:
						break;
					case RECURSE:
						doGetFiles(files, list[I]);
						break;
					case ADD:
						//System.out.println("raw path '"+m_baseDir+"'");
						String fPath = list[I].getPath().replace('\\', '/');
						//System.out.println("Path: "+fPath);
						//System.out.println("Base: "+m_baseDir);
						fPath = fPath.substring(m_workingBaseDir.length()); //add one to get rid of separator
						//System.out.println("adding base: "+m_baseDir+" file: "+fPath);
						files.add(new AbstractFileSet.File(m_baseDir, fPath));
						break;
					}
				}
			}
		}
	
	//---------------------------------------------------------------------------
	@Override
	public List<AbstractFileSet.File> getFiles()
		{
		java.io.File base = new java.io.File(m_workingBaseDir);
		if (m_startDir != null)
			base = new java.io.File(m_workingBaseDir, m_startDir);
		List<AbstractFileSet.File> files = new ArrayList<AbstractFileSet.File>();
		
		files.addAll(super.getFiles());
		
		doGetFiles(files, base);
		
		return (files);
		}
		
	//---------------------------------------------------------------------------
	@Override
	public String toString()
		{
		StringBuilder sb = new StringBuilder();
		sb.append("RegExFileSet [\n");
		for (AbstractFileSet.File f : getFiles())
			{
			sb.append("  ").append(f.toString()).append(",\n");
			}
		sb.append("]");
		return (sb.toString());
		}
	}
