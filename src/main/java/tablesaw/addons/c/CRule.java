package tablesaw.addons.c;

import tablesaw.TablesawException;
import tablesaw.rules.AbstractSimpleRule;
import tablesaw.rules.Rule;
import tablesaw.DependencyCache;
import tablesaw.MakeAction;

import java.util.*;
import java.io.File;

public class CRule extends AbstractSimpleRule<CRule> implements MakeAction
	{
	private String m_source;
	private boolean m_parseDepends;
	
	public CRule()
		{
		super();
		m_parseDepends = true;
		}
		
	public CRule setSource(String source)
		{
		m_source = source;
		return (this);
		}
		
	//---------------------------------------------------------------------------
	public CRule dontParseDepends()
		{
		m_parseDepends = false;
		return (this);
		}
		
	//---------------------------------------------------------------------------
	public void doMakeAction(Rule rule)
	{
	}
	
	//---------------------------------------------------------------------------
	@Override
	public void preBuild(DependencyCache cache, Map<String, Long> modificationCache)
		throws TablesawException
	{
		super.preBuild(cache, modificationCache);
		
		File f;
		long lm = m_latestSource;
		
		for (String s : m_sources)
		{
			if (m_parseDepends)
			{
				Set<String> dependencies = cache.getDependencies(m_make, s);
				//System.out.println("depend for "+s);
				for (String d : dependencies)
				{
					//System.out.println("dependency "+d);
					f = m_make.locateFile(d);
					if (f == null)
						continue;
						
					lm = f.lastModified();
					if (lm > m_latestSource)
						m_latestSource = lm;
						
					addDepend(d);
				}
			}
		}
	}
	
	//---------------------------------------------------------------------------
	@Override
	public boolean needToRun()
		{
		// TODO: parce the c source for any includes we need to consider
		//when trying to find header files use cpmake findfile. ho whining if the file cannot be found
		
		return (true);
		}
	}
