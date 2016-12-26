package tablesaw;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.*;
import java.util.Arrays;

import static tablesaw.util.Validation.*;

public class GlobFileSet extends AbstractFileSet<GlobFileSet>
	{
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
	
	public GlobFileSet(String baseDir, String globPattern)
		{
		Tablesaw make = Tablesaw.getCurrentTablesaw();
		m_recurse = false;
		m_baseDir = new java.io.File(baseDir).getPath().replace('\\', '/') + "/";
		m_workingBaseDir = new java.io.File(make.getWorkingDirectory(), m_baseDir).getPath().replace('\\', '/') + "/";
		m_dirPattern = null;
		m_excDirPattern = null;
		m_excPattern = null;
		m_pattern = Pattern.compile(globPattern);
		m_excludeFiles = new HashSet<String>();
		m_excludeDirs = new HashSet<String>();
		}
		
		
	private String convertGlobToRegEx(String line)
    {
    //LOG.info("got line [" + line + "]");
    line = line.trim();
    int strLen = line.length();
    StringBuilder sb = new StringBuilder(strLen);
    // Remove beginning and ending * globs because they're useless
    if (line.startsWith("*"))
    {
        line = line.substring(1);
        strLen--;
    }
    if (line.endsWith("*"))
    {
        line = line.substring(0, strLen-1);
        strLen--;
    }
    boolean escaping = false;
    int inCurlies = 0;
    for (char currentChar : line.toCharArray())
    {
        switch (currentChar)
        {
        case '*':
            if (escaping)
                sb.append("\\*");
            else
                sb.append(".*");
            escaping = false;
            break;
        case '?':
            if (escaping)
                sb.append("\\?");
            else
                sb.append('.');
            escaping = false;
            break;
        case '.':
        case '(':
        case ')':
        case '+':
        case '|':
        case '^':
        case '$':
        case '@':
        case '%':
            sb.append('\\');
            sb.append(currentChar);
            escaping = false;
            break;
        case '\\':
            if (escaping)
            {
                sb.append("\\\\");
                escaping = false;
            }
            else
                escaping = true;
            break;
        case '{':
            if (escaping)
            {
                sb.append("\\{");
            }
            else
            {
                sb.append('(');
                inCurlies++;
            }
            escaping = false;
            break;
        case '}':
            if (inCurlies > 0 && !escaping)
            {
                sb.append(')');
                inCurlies--;
            }
            else if (escaping)
                sb.append("\\}");
            else
                sb.append("}");
            escaping = false;
            break;
        case ',':
            if (inCurlies > 0 && !escaping)
            {
                sb.append('|');
            }
            else if (escaping)
                sb.append("\\,");
            else
                sb.append(",");
            break;
        default:
            escaping = false;
            sb.append(currentChar);
        }
    }
    return sb.toString();
}

		
	//---------------------------------------------------------------------------
	/**
		Sets the search for files to recurse through sub directories
	*/
	public GlobFileSet recurse()
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
	public GlobFileSet setStartDir(String startDir)
		{
		m_startDir = startDir;
		
		return (this);
		}
		
	//---------------------------------------------------------------------------
	/**
		Files are excluded from list if they match this pattern
	*/
	public GlobFileSet setExcludePattern(String pattern)
		{
		m_excPattern = Pattern.compile(pattern);
		return (this);
		}
		
	//---------------------------------------------------------------------------
	/**
		Directories are excluded if they match this pattern
	*/
	public GlobFileSet setExcludeDirPattern(String pattern)
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
	public GlobFileSet setDirectoryPattern(String pattern)
		{
		m_dirPattern = Pattern.compile(pattern);
		return (this);
		}
		
	//---------------------------------------------------------------------------
	public GlobFileSet addExcludeFile(String file)
	{
		m_excludeFiles.add(file);
		return (this);
	}
	
	//---------------------------------------------------------------------------
	/**
		Specify file names to exclucde from the file set
	*/
	public GlobFileSet addExcludeFiles(Object... files)
		{
		if ((files.length == 1) && (files[0] instanceof Iterable))
			return (addExcludeFiles((Iterable<Object>)files[0]));
		else
			return (addExcludeFiles(Arrays.asList(files)));
		}
		
	//---------------------------------------------------------------------------
	public GlobFileSet addExcludeFiles(Iterable<Object> files)
	{
		for (Object f : files)
			addExcludeFile(objectToString(f));
		
		return (this);
	}
		
	//---------------------------------------------------------------------------
	public GlobFileSet addExcludeDir(String dir)
		{
		m_excludeDirs.add(dir);
		
		return (this);
		}
		
	//---------------------------------------------------------------------------
	public GlobFileSet addExcludeDirs(String... dirs)
		{
		for (String d : dirs)
			m_excludeDirs.add(d);
		return (this);
		}
		
	//---------------------------------------------------------------------------
	public GlobFileSet addExcludeDirs(Iterable<String> dirs)
		{
		for (String d : dirs)
			m_excludeDirs.add(d);
			
		return (this);
		}
		
	//---------------------------------------------------------------------------
	private void doGetFiles(List<AbstractFileSet.File> files, java.io.File dir)
		{
		java.io.File[] list = dir.listFiles();
		
		if (list != null)
			for (int I = 0; I < list.length; I++)
				{
				if (list[I].getName().equals(".") || list[I].getName().equals(".."))
					continue;
				
				String fName = list[I].getName();
				if (m_recurse && list[I].isDirectory() && 
						!m_excludeDirs.contains(fName) &&
						(m_excDirPattern == null || !m_excDirPattern.matcher(fName).matches()))
					{
					doGetFiles(files, list[I]);
					continue;
					}
				else if (list[I].isDirectory())
					continue;
					
				if (!m_excludeFiles.contains(fName) && 
						m_pattern.matcher(fName).matches() &&
						(m_dirPattern == null || m_dirPattern.matcher(dir.getName()).matches()) &&
						(m_excPattern == null || !m_excPattern.matcher(fName).matches()))
					{
					//System.out.println("raw path '"+m_baseDir+"'");
					String fPath = list[I].getPath().replace('\\', '/');
					//System.out.println("Path: "+fPath);
					//System.out.println("Base: "+m_baseDir);
					fPath = fPath.substring(m_workingBaseDir.length()); //add one to get rid of separator
					//System.out.println("adding base: "+m_baseDir+" file: "+fPath);
					files.add(new AbstractFileSet.File(m_baseDir, fPath));
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
		
		doGetFiles(files, base);
		
		return (files);
		}
		
	//---------------------------------------------------------------------------
	@Override
	public String toString()
		{
		StringBuilder sb = new StringBuilder();
		sb.append("GlobFileSet [\n");
		for (AbstractFileSet.File f : getFiles())
			{
			sb.append("  ").append(f.toString()).append(",\n");
			}
		sb.append("]");
		return (sb.toString());
		}
	}
