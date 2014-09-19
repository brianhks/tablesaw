package tablesaw.rules;

import java.util.*;
import java.io.*;

import tablesaw.DependencyCache;
import tablesaw.TablesawException;
import tablesaw.Debug;

public abstract class AbstractSimpleRule<T extends AbstractSimpleRule> extends AbstractSourceRule<T>
	{
	protected Set<String> m_targets;
	protected long m_latestSource = 0L;
	protected boolean m_deleteTargets;
	protected boolean m_verify;
	protected boolean m_alwaysRun;
	
	//---------------------------------------------------------------------------
	public AbstractSimpleRule()
		{
		this(null);
		}
	
	//---------------------------------------------------------------------------
	public AbstractSimpleRule(String name)
		{
		super();
		super.setName(name);
		m_targets = new LinkedHashSet<String>();
		m_sources = new LinkedHashSet<String>();
		m_deleteTargets = true;
		m_verify = true;
		m_alwaysRun = false;
		}
		
	//---------------------------------------------------------------------------
	@Override
	public Object clone() throws CloneNotSupportedException
		{
		AbstractSimpleRule copy = (AbstractSimpleRule)super.clone();
		
		copy.m_targets = (Set<String>)((LinkedHashSet<String>)m_targets).clone();
		copy.m_sources = (Set<String>)((LinkedHashSet<String>)m_sources).clone();
		
		return (copy);
		}
	
	
	//---------------------------------------------------------------------------
	/**
		Called to force this Rule to always run regardless of dependencies
	*/
	public T alwaysRun()
		{
		m_alwaysRun = true;
		return ((T)this);
		}
		
	//---------------------------------------------------------------------------
	public T dontVerify()
		{
		m_verify = false;
		return ((T)this);
		}
		
	//---------------------------------------------------------------------------
	public T dontDeleteTargets()
		{
		m_deleteTargets = false;
		return ((T)this);
		}
		
	//---------------------------------------------------------------------------
	public T addTarget(String target)
		{
		m_targets.add(target);
		return ((T)this);
		}
		
	//---------------------------------------------------------------------------
	public T addTargets(String... targets)
		{
		for (String t : targets)
			m_targets.add(t);
		
		return ((T)this);
		}
		
	
	//---------------------------------------------------------------------------
	public Iterable<String> getTargets()
		{
		return (m_targets);
		}
		
	//---------------------------------------------------------------------------
	/**
		Returns the first target
	*/
	public String getTarget()
		{
		return (m_targets.iterator().next());
		}
		
		
	//---------------------------------------------------------------------------
	@Override
	public void preBuild(DependencyCache cache, Map<String, Long> modificationCache)
			throws TablesawException
		{
		//System.out.println("preBuild called on "+getName());
		//System.out.println(m_sources);
		File f;
		long lm;
		
		for (String s : m_sources)
			{
			//System.out.println("source "+s);
			addDepends(s);
			f = m_make.locateFile(s);
			if (f == null)
				continue;
				
			lm = f.lastModified();
			if (lm > m_latestSource)
				m_latestSource = lm;
			}
		}
		
	//---------------------------------------------------------------------------
	@Override
	public void inBuildQueue()
		{
		//Deleting the targets will ensure this rule runs next time if the build fails
		if (m_deleteTargets)
			{
			for (String t : m_targets)
				{
				File f = m_make.locateFile(t);
				if (f == null)
					continue;
					
				f.delete();
				}
			}
		}
		
	//---------------------------------------------------------------------------
	@Override
	public boolean needToRun()
			throws TablesawException
		{
		if (m_alwaysRun)
			return (true);
			
		// if there are no depends or sources for this rule it should always run
		if ((m_sources.size() == 0) && (m_dependRules.size() == 0) &&
				(m_dependStrings.size() == 0))
			return (true);
			
			
		for (String t : m_targets)
			{
			File f = m_make.locateFile(t);
			if (f == null)
				return (true);
				
			if (Debug.isDebug())
					Debug.print("Comparing latest source "+m_latestSource+" to: "+t);
					
			if (m_latestSource > f.lastModified())
				{
				Debug.print("needToRun() returning true");
				return (true);
				}
			}
			
		return (false);
		}
		
	//---------------------------------------------------------------------------
	@Override
	public void verify()
			throws TablesawException
		{
		if (m_verify)
			{
			for (String t : m_targets)
				{
				if (!(new File(m_make.getWorkingDirectory(), t)).exists())
					{
					throw new TablesawException("Unable to verify target file "+t);
					}
				}
			}
		}
		
	//---------------------------------------------------------------------------
	@Override
	public String toString()
		{
		StringBuilder sb = new StringBuilder();
		sb.append("AbstractSimpleRule(").append(m_name).append(") [");
		if (m_targets != null)
			for (String t : m_targets)
				sb.append(t).append(", ");
			
		sb.append("]");
		return (sb.toString());
		}
		
	//---------------------------------------------------------------------------
	@Override
	public boolean equals(Object o)
		{
		boolean ret = false;
		
		if (this == o)
			return (true);
		
		if (o instanceof AbstractSimpleRule)
			{
			AbstractSimpleRule other = (AbstractSimpleRule)o;
			
			if (m_targets.size() == 0)
				{
				if ((m_name != null) && (m_name.equals(other.m_name)))
					return (true);
				}
			else if (m_targets.size() == other.m_targets.size())
				{
				ret = true;
				for (String target : m_targets)
					if (!other.m_targets.contains(target))
						{
						ret = false;
						break;
						}
				}
			}
			
		return (ret);
		}
	}
