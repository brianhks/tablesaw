package tablesaw.rules;

import tablesaw.*;
import tablesaw.TablesawException;

import java.util.*;
import java.util.regex.*;
import java.io.File;

//This will either create a bunch of simple rules or will place one rule in the
//queue
public class AbstractPatternRule<T extends AbstractPatternRule> extends AbstractSourceRule<T>
		implements BuildEventListener
	{
	//===========================================================================
	private static class SourceTarget
		{
		public String source;
		public String target;
		public List<String> dependencies;
		
		public SourceTarget(String source, String target)
			{
			this.source = source;
			this.target = target;
			dependencies = new ArrayList<String>();
			}
		}
		
	//===========================================================================
	/**
		This action is used for multi target pattern rules.  A MultiTargetAction
		is placed on each sub action that is created.  The MultiTargetAction
		notifies the pattern rule of which targets need to be compiled when the
		pattern rule is ran
	*/
	private class MultiTargetAction implements MakeAction
		{
		public MultiTargetAction()
			{
			}
			
		public void doMakeAction(Rule rule)
			{
			SimpleRule sr = (SimpleRule)rule;
			rebuild(sr.getSource(), sr.getTarget());
			}
		}
		
	//===========================================================================
	private List<SourceTarget> m_sourceTargetList;
	private Pattern m_sourcePattern;
	private String m_targetPattern;
	private List<String> m_dependencyPatterns;
	private Set<String> m_targetList;
	private List<String> m_patternTargets;   //Targets that are a result of the pattern
	private boolean m_multiTarget;
	private boolean m_needToRun;
	private boolean m_deleteTargets;
	private Set<String> m_rebuildTargets;
	private Set<String> m_rebuildSources;
	
	
	//---------------------------------------------------------------------------
	public AbstractPatternRule()
		{
		super();
		m_name = null;
		m_multiTarget = false;
		m_targetList = new LinkedHashSet<String>();
		m_make.addBuildEventListener(this);
		m_patternTargets = new ArrayList<String>();
		m_needToRun = false;
		m_deleteTargets = true;
		m_rebuildSources = new HashSet<String>();
		m_rebuildTargets = new HashSet<String>();
		m_dependencyPatterns = new ArrayList<String>();
		}

	@Override
	public Object clone() throws CloneNotSupportedException
		{
		AbstractPatternRule copy = (AbstractPatternRule)super.clone();
		
		copy.m_targetList = (Set<String>)((LinkedHashSet<String>)m_targetList).clone();
		copy.m_patternTargets = (List<String>)((ArrayList<String>)m_patternTargets).clone();
		copy.m_rebuildSources = (Set<String>)((HashSet<String>)m_rebuildSources).clone();
		copy.m_rebuildTargets = (Set<String>)((HashSet<String>)m_rebuildTargets).clone();
		copy.m_dependencyPatterns = (List<String>)((ArrayList<String>)m_dependencyPatterns).clone();
		
		return (copy);
		}

		//---------------------------------------------------------------------------
	/**
		received notification from MultiTargetAction that a target is being rebuilt
	*/
	private void rebuild(String source, String target)
		{
		m_rebuildTargets.add(target);
		m_rebuildSources.add(source);
		}
		
	//---------------------------------------------------------------------------
	protected SimpleRule getSimpleRule()
	{
		return (new SimpleRule());
	}
		
	//---------------------------------------------------------------------------
	public Set<String> getRebuildTargets()
		{
		return (m_rebuildTargets);
		}
		
	//---------------------------------------------------------------------------
	public Set<String> getRebuildSources()
		{
		return (m_rebuildSources);
		}
		
	//---------------------------------------------------------------------------
	public T dontDeleteTargets()
		{
		m_deleteTargets = false;
		return ((T)this);
		}
		
	//---------------------------------------------------------------------------
	public T setSourcePattern(String pattern)
		{
		m_sourcePattern = Pattern.compile(pattern);
		return ((T)this);
		}
		
	//---------------------------------------------------------------------------
	public String getSourcePattern()
		{
		return (m_sourcePattern.toString());
		}
	
	//---------------------------------------------------------------------------
	public T setTargetPattern(String pattern)
		{
		m_targetPattern = pattern;
		return ((T)this);
		}
		
	//---------------------------------------------------------------------------
	public T addDependencyPattern(String pattern)
		{
		m_dependencyPatterns.add(pattern);
		return ((T)this);
		}
		
	//---------------------------------------------------------------------------
	public String getTargetPattern()
		{
		return (m_targetPattern);
		}
	
	//---------------------------------------------------------------------------
	/**
		This will convert the source list into targets and also 
		it needs to check the targets of all depends to see if they
		match the source pattern
	*/
	public Iterable<String> getTargets()
		{
		return (m_targetList);
		}
		
	//---------------------------------------------------------------------------
	public List<String> getPatternTargets()
		{
		return (m_patternTargets);
		}
		
	//---------------------------------------------------------------------------
	@Override
	public void preBuild(DependencyCache cache, Map<String, Long> modificationCache)
			throws TablesawException
		{
		//I do not think this needs to be here anymore
		if (m_multiTarget)
			{
			for (SourceTarget st : m_sourceTargetList)
				{
				File sourceFile = m_make.locateFile(st.source);
				File targetFile = m_make.locateFile(st.target);
				m_targetList.add(st.target);
				
				if (sourceFile == null)
					{
					addNewerDepend(st.source);
					m_needToRun = true;
					continue;
					}
					//throw new TablesawException("Unable to locate source file '"+st.source+"'");
				
				if (Debug.isDebug())
					Debug.print("Comparing source: "+(sourceFile == null ? "null" : sourceFile.getName())+
							" to "+(targetFile == null ? "null" : targetFile.getName()));
					
				if (targetFile == null || sourceFile.lastModified() > targetFile.lastModified())
					{
					addNewerDepend(st.source);
					m_needToRun = true;
					}
				}
			}
		else
			m_needToRun = false;
		}
		
	//---------------------------------------------------------------------------
	@Override
	public void inBuildQueue() throws TablesawException
		{
		}
		
	//---------------------------------------------------------------------------
	@Override
	public boolean needToRun()
		{
		return (m_needToRun);
		}
		
	//---------------------------------------------------------------------------
	@Override
	public void verify()
			throws TablesawException
		{
		if (m_multiTarget)
			{
			for (String t : m_targetList)
				{
				//System.out.println(t);
				if (!(new File(m_make.getWorkingDirectory(), t)).exists())
					{
					throw new TablesawException("Unable to verify target file "+t);
					}
				}
			}
		}
		
	//---------------------------------------------------------------------------
	/**
		Sets this rule to be multi target.  A multi target rule will run only once
		to create all the target files.  S single target rule will be ran for each
		target file.
	*/
	public T multiTarget()
		{
		m_multiTarget = true;
		
		return ((T)this);
		}
		
	//---------------------------------------------------------------------------
	public boolean isMultiTarget()
		{
		return (m_multiTarget);
		}
		
	//---------------------------------------------------------------------------
	private void generateSourceTargetPairs()
		{
		m_sourceTargetList = new ArrayList<SourceTarget>();
		
		Debug.print("PatternRule source : target pairs");
		for (String source : m_sources)
			{
			//System.out.println("looking at "+source);
			Matcher m = m_sourcePattern.matcher(source);
			if (m.matches())
				{
				String target = m.replaceFirst(m_targetPattern);
				//System.out.println("Target "+target);
				if (Debug.isDebug())
					Debug.print("  "+source+" : "+target);
				
				SourceTarget st = new SourceTarget(source, target);
				m_sourceTargetList.add(st);
				m_patternTargets.add(target);
				//m_sources.add(source);
				
				for (String depPattern : m_dependencyPatterns)
					{
					String dep = m.replaceFirst(depPattern);
					if (Debug.isDebug())
						Debug.print("    dep: "+dep);
						
					st.dependencies.add(dep);
					}
				}
			else
				Debug.print("  Unmached source: "+source);
			}
		}
		
	//---------------------------------------------------------------------------
	/* BuildEventListener */
	public void buildFailed(Tablesaw make, Exception e) {}
		
	//---------------------------------------------------------------------------
	/* BuildEventListener */
	public void buildSuccess(Tablesaw make, String target) {}
		
	//---------------------------------------------------------------------------
	/* BuildEventListener */
	public void setPrimaryTarget(Tablesaw make, String target)
		{
		generateSourceTargetPairs();
		
		//Save off the depends as they will be added to the new rules
		Iterable<Rule> dependRules = m_dependRules;
		Iterable<String> dependNames = m_dependStrings;
		for (String source : m_sources)
			m_dependStrings.remove(source);
		
		//Reset depends
		m_dependRules = new LinkedHashSet<Rule>();
		m_dependStrings = new LinkedHashSet<String>();
	
		for (SourceTarget st : m_sourceTargetList)
			{
			SimpleRule sr = getSimpleRule()
					.addSources(st.source)
					.addTargets(st.target)
					.setMakeAction(m_action)
					.addDepends(dependNames)
					.addDepends(dependRules)
					.setProperties(m_properties);
					
			if (m_multiTarget)
				sr.setMakeAction(new MultiTargetAction()).dontVerify();
			else
				sr.setMakeAction(m_action);
			
			for (String dep : st.dependencies)
				sr.addDepend(dep);
				
			//Add the new sub rule as a depends
			addDepends(sr);
			}
		
		if (!m_multiTarget)
			{
			//Clear make action
			m_action = null;
			}
		}
		
	//---------------------------------------------------------------------------
	@Override
	public boolean equals(Object o)
		{
		boolean ret = false;
		
		if (this == o)
			return (true);
		
		if (o instanceof AbstractPatternRule)
			{
			AbstractPatternRule other = (AbstractPatternRule)o;
			
			if (m_targetList.size() == 0)
				{
				if ((m_name != null) && (m_name.equals(other.m_name)))
					return (true);
				}
			else if (m_targetList.size() == other.m_targetList.size())
				{
				ret = true;
				for (String target : m_targetList)
					if (!other.m_targetList.contains(target))
						{
						ret = false;
						break;
						}
				}
			}
			
		return (ret);
		}
	}
