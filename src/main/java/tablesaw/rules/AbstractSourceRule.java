package tablesaw.rules;

import java.util.*;
import java.io.File;
import tablesaw.Debug;
import tablesaw.CachedFile;
import tablesaw.AbstractFileSet;

import static tablesaw.util.Validation.*;

/**
	Class that provides setters for source files
*/
public abstract class AbstractSourceRule<T extends AbstractSourceRule> extends AbstractRule<T>
		implements SourceFileSet
	{
	protected Set<String> m_sources;
	protected Set<String> m_newerSources;
	
	public AbstractSourceRule()
		{
		super();
		
		m_sources = new LinkedHashSet<String>();
		m_newerSources = new HashSet<String>();
		}
	
	//---------------------------------------------------------------------------
	@Override
	public Object clone() throws CloneNotSupportedException
		{
		AbstractSourceRule copy = (AbstractSourceRule)super.clone();
		
		copy.m_sources = (Set<String>)((LinkedHashSet<String>)m_sources).clone();
		copy.m_newerSources = (Set<String>)((HashSet<String>)m_newerSources).clone();
		
		return (copy);
		}
		
	//---------------------------------------------------------------------------
	@Override 
	public void addNewerDepend(String depend)
		{
		if (Debug.isDebug())
			Debug.print("addNewerDepend("+depend+")");
			
		if (m_sources.contains(depend))
			m_newerSources.add(depend);
		}
	
	//---------------------------------------------------------------------------
	/**
		Returns a list of newer sources that require this rule to run
	*/
	public List<String> getNewerSources()
		{
		List<String> ret = new ArrayList<String>();
		for (String s : m_newerSources)
			{
			CachedFile f = m_make.locateFile(s);
			ret.add(f.getRelativePath());
			}
			
		return (ret);
		}
		
	//---------------------------------------------------------------------------
	public T addSource(String source)
		{
		m_sources.add(source);
		addDepend(source);
		return ((T)this);
		}
		
	//---------------------------------------------------------------------------
	public T addSources(Object... sources)
		{
		if ((sources.length == 1) && (sources[0] instanceof Iterable))
			return (addSources((Iterable<Object>)sources[0]));
		else if ((sources.length == 1) && (sources[0] instanceof AbstractFileSet))
			return (addSources(((AbstractFileSet)sources[0]).getFullFilePaths()));
		else
			return (addSources(Arrays.asList(sources)));
		}
		
	//---------------------------------------------------------------------------
	public T addSources(Iterable<Object> sources)
		{
		for (Object s : sources)
			{
			addSource(objectToString(s));
			}
			
		return ((T)this);
		}
		
	//---------------------------------------------------------------------------
	/**
		Returns the first source
	*/
	public String getSource()
		{
		String source = m_sources.iterator().next();
		CachedFile f = m_make.locateFile(source);
		return (f.getRelativePath());
		}
		
	//---------------------------------------------------------------------------
	/**
		Returns a resolved list of sources
	*/
	public List<String> getSources()
		{
		List<String> ret = new ArrayList<String>();
		for (String s : m_sources)
			{
			CachedFile f = m_make.locateFile(s);
			ret.add(f.getRelativePath());
			}
			
		return (ret);
		}
		
	public List<File> getSourceFiles()
		{
		List<File> ret = new ArrayList<File>();
		for (String s : m_sources)
			{
			CachedFile f = m_make.locateFile(s);
			ret.add(new File(f.getPath()));  //doing this because locateFile returns a CachedFile
			}
			
		return (ret);
		}
	}
