package tablesaw;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;

import tablesaw.annotation.Consumes;
import tablesaw.annotation.Provides;
import tablesaw.rules.Rule;
import tablesaw.rules.Overridable;
import tablesaw.rules.SourceFileSet;


public class MakeEngine
	{
	public static final String DEFAULT_CACHE_FILE = ".cpmakecache";
	public static final String NAMED_RULE_PREFIX = "MakeEngine-NamedRule-";


	/*private class CompoundNameRule implements Rule
		{
		private String m_name;
		private Set<Rule> m_rules;
		
		public CompoundNameRule(String name)
			{
			m_name = name;
			m_rules = new LinkedHashSet<Rule>();
			}
			
		@Override
		public Object clone()
				throws CloneNotSupportedException
			{
			return (super.clone());
			}
		//public void addNewerDepend(String d) {}
			
		public String getName() { return (m_name); }
		public CompoundNameRule setName(String name) { m_name = name; return (this); }
		
		public String getDescription() { return (null); }
		
		public boolean getOverride() { return (true); }
		public MakeAction getMakeAction() { return (null); }
		
		public void addDepends(Rule r) { m_rules.add(r); }
		public Set<Rule> getDependRules() { return (m_rules); }
		public Set<String> getDependNames() { return (new LinkedHashSet<String>()); }
		
		public Set<String> getTargets() { return (new LinkedHashSet<String>()); }
		public boolean needToRun() { return (false); }
		
		public void preBuild(DependencyCache cache) {}
		public void inBuildQueue() {}
		public void buildComplete() {}
		
		public void verify() {}
		}*/
		
	private class ModifiedFileRule implements Rule
		{
		private String m_name;
		
		public ModifiedFileRule(String name)
			{
			m_name = name;
			}
			
		@Override
		public Object clone()
				throws CloneNotSupportedException
			{
			return (super.clone());
			}
			
		public String getName() { return (m_name); }
		public ModifiedFileRule setName(String name) { m_name = name; return (this); }

		public boolean isBinding() { return (true); }
		
		public String getDescription() { return (null); }
		
		public boolean getOverride() { return (true); }
		public MakeAction getMakeAction() { return (null); }
		
		public void addDepends(Rule r) { }
		public Set<Rule> getDependRules() { return (new HashSet<Rule>()); }
		public Set<String> getDependNames() { return (new HashSet<String>()); }
		
		public Set<String> getTargets() { return (new HashSet<String>()); }
		public boolean needToRun() { return (false); }
		
		public void preBuild(DependencyCache cache, Map<String, Long> modificationCache) {}
		public void inBuildQueue() {}
		public void buildComplete() {}
		
		public void verify() {}

		public String toString() { return (m_name); }
		}
	
	private String                    m_cacheFile = DEFAULT_CACHE_FILE;
	private int                       m_ruleNameCounter;  //Counter used when naming rules with no name
	private boolean                   m_printDebug = true;
	private boolean                   m_writeCache = true;
	private Map<String, CachedFile>   m_locatedFiles;
	private Set<String>               m_noRulesList;		//Targets that we do not have a rule for.  Prevents double lookup
	private Map<String, Rule>         m_locatedRules;
	private LinkedList<BuildAction>   m_buildQueue;
	private List<Thread>              m_threadList;   //List of thread that are working on the build queue
	
	private List<Rule>                m_ruleList;    //Initial list of rules
	private Map<String, Rule>         m_targetRuleMap;  //Map of rules where the keys are the target files of the rule
	private Map<String, Rule>         m_nameRuleMap;  //Map of rules based on the name
	private Map<Class, DependencyAnnotations> m_classAnnotations;
	
	private Set<String>               m_noBuildCache;     //Files that do not need to be built or do not have a rule to build them
	private Map<BuildAction, BuildAction> m_buildQueueCache;  //Files that are aldready in build queue
	private BuildFileManager          m_fileManager;
	
	private Set<File>                 m_sourceFiles;  //A set of source files used for continuous build checking
	
	private volatile TablesawException m_makeException;
	
	private DependencyCache           m_depCache;
	private Map<String, Long>         m_modificationCache;
	private Map<String, Long>         m_newModificationCache;
	private Set<String>               m_namedRuleCache;
	private boolean                   m_resolved = false;  //Identifies if the rules have been resolved or not
	
	//---------------------------------------------------------------------------
	public MakeEngine(BuildFileManager fileManager, Properties props)
		{
		m_threadList = new ArrayList<Thread>();
		m_fileManager = fileManager;
		m_noRulesList = new HashSet<String>();
		m_locatedRules = new HashMap<String, Rule>();
		m_targetRuleMap = new HashMap<String, Rule>();
		m_nameRuleMap = new HashMap<String, Rule>();
		m_locatedFiles = new HashMap<String, CachedFile>();
		m_ruleList = new ArrayList<Rule>();
		m_noBuildCache = new HashSet<String>();
		m_buildQueueCache = new HashMap<BuildAction, BuildAction>();
		m_ruleNameCounter = 0;
		m_newModificationCache = new HashMap<String, Long>();
		m_namedRuleCache = new HashSet<String>();
		m_classAnnotations = new HashMap<Class, DependencyAnnotations>();
		
		m_cacheFile = props.getProperty(Tablesaw.PROP_CACHE_FILE, m_cacheFile);
		
		readCache();
		}
		
	//---------------------------------------------------------------------------
	public void close()
		{
		if (m_writeCache)
			writeCache();
		else
			new File(m_cacheFile).delete();
		}
		
	//---------------------------------------------------------------------------
	public void clearCacheFile()
		{
		m_writeCache = false;
		}
		
	//---------------------------------------------------------------------------
	private void readCache()
		{
		Debug.print("Cache file %s", m_cacheFile);
		File cacheFile = new File(m_cacheFile);
		m_depCache = null;
		m_modificationCache = null;
		
		if (cacheFile.exists())
			{
			try
				{
				Debug.print("Reading cache");
				FileInputStream cacheIs = new FileInputStream(cacheFile);
				ObjectInputStream ois = new ObjectInputStream(cacheIs);
				
				m_depCache = (DependencyCache)ois.readObject();
				
				Map<String, Long> modCache = new HashMap<String, Long>();
				int modCacheSize = ois.readInt();
				for (int I = 0; I < modCacheSize; I++)
					{
					String file = ois.readUTF();
					Debug.print("  Read %s", file);
					long ts = ois.readLong();
					
					modCache.put(file, ts);
					}
					
				m_modificationCache = modCache;
				ois.close();
				}
			catch (Exception e)
				{
				Debug.print(e.getMessage());
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				Debug.print(sw.toString());
				}
			}
			
			
			
		if ((m_depCache == null) || (m_modificationCache == null))
			{
			m_depCache = new DependencyCache();
			m_modificationCache = new HashMap<String, Long>();
			}
		}
		
	//---------------------------------------------------------------------------
	private void writeCache()
		{
		try
			{
			Debug.print("Writing cache");
			FileOutputStream fos = new FileOutputStream(m_cacheFile);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			
			oos.writeObject(m_depCache);
			
			oos.writeInt(m_newModificationCache.size());
			
			Iterator<String> it = m_newModificationCache.keySet().iterator();
			
			while (it.hasNext())
				{
				String key = it.next();
				
				oos.writeUTF(key);
				oos.writeLong(m_newModificationCache.get(key));
				}
				
			oos.close();
			}
		catch (Exception e)
			{
			Debug.print(e.getMessage());
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			Debug.print(sw.toString());
			}
		}
	
	//---------------------------------------------------------------------------
	public void setCaptureSource(boolean capture)
		{
		if (capture)
			m_sourceFiles = new HashSet<File>();
		else
			m_sourceFiles = null;
		}

	//--------------------------------------------------------------------
	public Set<File> getSourceFiles()
		{
		return (m_sourceFiles);
		}
	
	//---------------------------------------------------------------------------
	private void addRule(String key, Rule rule)
			throws TablesawException
		{
		Debug.print("Adding rule for "+key);
		Rule old = m_targetRuleMap.put(key, rule);
		if ((old != null) && (!rule.getOverride()) && (!(old instanceof Overridable)))
			throw new TablesawException("Duplicate rules for "+key+" rule1: "+old+" rule2: "+rule, 1);
		}
		
	//---------------------------------------------------------------------------
	/**
		This fills in the m_targetRuleMap.  The keys are the names of the rules and
		the targets they produce
	*/
	private void resolveRules()
			throws TablesawException
		{
		for (Rule rule : m_ruleList)
			{
			//Add rule by name if it has one
			String name = rule.getName();
			if (name != null)
				{
				Debug.print("Adding rule name: "+name);
				Rule namedRule = m_nameRuleMap.get(name);
				if (namedRule == null)
					{
					m_nameRuleMap.put(name, rule);
					}
				/*else if (namedRule instanceof CompoundNameRule)
					{
					((CompoundNameRule)namedRule).addDepends(rule);
					}*/
				else if ((namedRule instanceof Overridable) || (rule.getOverride()))
					{
					m_nameRuleMap.put(name, rule);
					}
				else
					{
					throw new TablesawException("Multiple rules exist for '"+name+
							"'.  Call Rule.override() if you wish to override an existing rule."); 
					/*System.out.println("GGGGGGGGGGGGGGGGGGGAAAAAAAAAAAAAHHHHHHHHHHH");
					CompoundNameRule cnr = new CompoundNameRule(name);
					cnr.addDepends(namedRule);
					cnr.addDepends(rule);
					m_nameRuleMap.put(name, cnr);*/
					}
				}
				
			//Add rule by each target it declares
			Iterable<String> targets = rule.getTargets();
			for (String t : targets)
				{
				Debug.print("Add target "+t);
				addRule(t, rule);
				}
			}
			
		m_resolved = true;
		}
		
	
	Set<String> m_printedDependencies = null;
	
	//---------------------------------------------------------------------------
	private void printDependencies(String target, PrintWriter pw, int spacing)
			throws TablesawException
		{
		Rule tr = findTargetRule(target);
		String[] pre;
		
		for (int I = 0; I < spacing; I++)
			pw.print("\t");
		
			
		List<String> targetList = new ArrayList<String>();
		if (tr != null)
			{
			for (String name : tr.getDependNames())
				{
				targetList.add(name);
				}
				
			for (Rule r : tr.getDependRules())
				{
				if ((r.getName() != null) && (!r.getName().startsWith(NAMED_RULE_PREFIX)))
					{
					targetList.add(r.getName());
					}
				else
					{
					for (String t : r.getTargets())
						{
						targetList.add(t);
						}
					}
				}
			}
			
		if (!m_printedDependencies.add(target) && (targetList.size() != 0))
			{
			pw.println(target+"*");
			return;
			}
			
		pw.println(target);
		
		for (String t : targetList)
			printDependencies(t, pw, spacing+1);
		} 
		
	//---------------------------------------------------------------------------
	 private void printDependencies()
			throws TablesawException
		{
		m_printedDependencies = new HashSet<String>();
		
		try
			{
			PrintWriter pw = new PrintWriter(new FileWriter("dependency.txt"));
			
			pw.println("Targets marked with a * have already been printed");
			//Create a reduced set of stuff to print
			Set<String> ruleNames = new HashSet<String>();
			
			for (String name : m_nameRuleMap.keySet())
				ruleNames.add(name);
				
			for (String name : m_nameRuleMap.keySet())
				{
				Rule rule = m_nameRuleMap.get(name);
				for (String dep : rule.getDependNames())
					ruleNames.remove(dep);
					
				for (Rule dep : rule.getDependRules())
					{
					if (dep.getName() != null)
						ruleNames.remove(dep.getName());
					}
				}
				
			for (String name : ruleNames)
				{
				if (!name.startsWith(NAMED_RULE_PREFIX))
					printDependencies(name, pw, 0);
				}
				
			pw.close();
			}
		catch (IOException ioe)
			{
			throw new TablesawException("Cannot write to file dependency.txt", -1);
			}
		} 
		
	//---------------------------------------------------------------------------
	private BuildAction addToBuildQueue(String target, boolean primaryTarget, 
			int insertPosition)
			throws TablesawException
		{
		//The target was already checked and does not need to be built
		if (m_noBuildCache.contains(target))
			return (null);
			
		Debug.print("addToBuildQueue("+target+", "+primaryTarget+", "+insertPosition+")");
		Debug.indent();
		
		Rule trule = findTargetRule(target);
		
		CachedFile targetFile = null;
		BuildAction[] buildDep = null;
		BuildAction targetBA = null;
		BuildAction depBA = null;
		BuildAction ret = null;
		
		if (trule == null)
			{
			targetFile = m_fileManager.locateFile(target);
			
			// TODO: Add the rule that required this target to the error message
			if (targetFile == null)
				throw new TablesawException("Unable to locate rule for '"+target+"'");
				
			if (m_sourceFiles != null)
				m_sourceFiles.add(new File(targetFile.getPath()));  //Doing this because locateFile will return a CachedFile
				
			// TODO: check file against cache file stamps and if changed return dummy rule
			Debug.print("Cache lookup for "+targetFile.getPath());
			
			//Add file to cache for next run
			m_newModificationCache.put(targetFile.getPath(), targetFile.lastModified());
			
			Long cacheModTime = m_modificationCache.get(targetFile.getPath());
			if (cacheModTime != null)
				{
				Debug.print("Cache hit "+cacheModTime+":"+targetFile.lastModified());
				if (cacheModTime != targetFile.lastModified())
					{
					Debug.print("returning obj for "+targetFile);
					Debug.popIndent();
					targetBA = new BuildAction(m_fileManager, 
							new ModifiedFileRule(targetFile.getPath()), m_classAnnotations);
					m_buildQueue.add(insertPosition, targetBA);
					return (targetBA);
					}
				}
			else
				Debug.print("Cache miss");
			
				
			//System.out.println("File "+targetFile);
			m_noBuildCache.add(target);
			Debug.print("Target "+target+" is a file with no rule");
			Debug.popIndent();
			return (null);
			}
			
		if ((m_sourceFiles != null) && (trule instanceof SourceFileSet))
			{
			m_sourceFiles.addAll(((SourceFileSet)trule).getSourceFiles());
			}
			
		long targetTime = 0;
		boolean tExists = true;
		boolean tDir = false;
		boolean tPhony = true;
		
		boolean rebuild = false;
		
		targetBA = new BuildAction(m_fileManager, trule, m_classAnnotations);
		int index;
		

		if (m_buildQueueCache.containsKey(targetBA))
			{
			//Get the build action from the queue
			targetBA = m_buildQueueCache.get(targetBA);
			
			Debug.print("target: "+trule+" already in build queue.");
			//Target has already been added to build queue
			Debug.popIndent();
			return (targetBA);
			}
		
		
		File f;
		
					
		trule.preBuild(m_depCache, m_modificationCache);
		
		// NOTE: need to add in dependencies that are individually declared
		// NOTE: getPrerequisites is also where dependency parsing happens to include C headers and other java classes
		//String[] prereqs = getPrerequisites(trule, true);
		List<String> prereqs = new ArrayList<String>();
		
		for (String dn : trule.getDependNames())
			{
			Debug.print("adding depend name "+dn);
			prereqs.add(dn);
			}
		
		for (Rule r : trule.getDependRules())
			{
			Debug.print("adding depend rule "+r);
			if (r.getName() == null)
				{
				//Generate name for rule
				String genRuleName = NAMED_RULE_PREFIX+(m_ruleNameCounter++);
				r.setName(genRuleName);
				m_nameRuleMap.put(genRuleName, r);
				}
				
			prereqs.add(r.getName());
			}

		Debug.print("rule dependents " + prereqs.size());
			
		if (prereqs.size() > 0)
			{			
			ListIterator<String> it = prereqs.listIterator();
			
			while (it.hasNext())
				{
				String prereq = it.next();
				
				if (prereq.equals(target))
					throw new TablesawException("Target "+target+" depends on itself");
				
				/* //See if the prereq is the name of a rule first
				Rule nameRule = m_nameRuleMap.get(prereq);
				
				//Add the new rule targets to the prereqs list
				// TODO: some kind of check so we dont add the same named rule again and again.
				if (nameRule != null)
					{
					Iterable<String> ruleTargets = nameRule.getTargets();
					boolean hasTargets = false;
					for (String t : ruleTargets)
						{
						hasTargets = true;
						it.add(t);
						it.previous();
						}
					
					if (hasTargets)
						continue;
					} */
				
				
				//Add dependencies to build queue first.
				//f = m_fileManager.getFile(prereq);
				if ((depBA = addToBuildQueue(prereq, false, insertPosition)) != null)
					{
					targetBA.addDependency(depBA);
					//trule.addNewerDepend(prereq);
					if (depBA.isBinding())
						{
						Debug.print("Rebuild: " + trule + " because rebuild of " + depBA.getTargetRule());
						rebuild = true;
						}
					}						

				}
			}	

		
		if (!rebuild)
			{
			rebuild = trule.needToRun();
			Debug.print("Rule needToRun() returned "+rebuild);
			}			
			
		// TODO: change this to get depends from above and check if no depends
		if ((!rebuild) && (primaryTarget && (!trule.getTargets().iterator().hasNext())))
			{   //If target is the primary target and it has no targets
			Debug.print("Adding primary target: " + trule + " to build queue.");
			rebuild = true;

			}

		if (rebuild)
			{   //Add target to build queue
			m_buildQueue.add(insertPosition, targetBA);
			m_buildQueueCache.put(targetBA, targetBA);
			Debug.print("Adding " + targetBA + " to build queue at pos " + insertPosition);
			//System.out.println("Adding "+targetBA+" to build queue at pos "+insertPosition);

			ret = targetBA;
			}
			
			
		//Add target to cache if it does not need to be built
		//This is to speed up incremental builds
		if (ret == null)
			m_noBuildCache.add(target);
			
		Debug.popIndent();
		return (ret);
		}
		
	//---------------------------------------------------------------------------
	public Rule findTargetRule(String target)
			throws TablesawException
		{
		Rule rule = null;
		
		if (m_resolved)
			{
			ArrayList<String> posTargets;
			String posTarget = target;
			
			if (m_noRulesList.contains(target))
				return (null);
			
			if ((rule = m_locatedRules.get(target)) != null)
				return (rule);
				
			//First look in name map
			rule = m_nameRuleMap.get(target);
			
			if (rule == null)
				{ //Now look for targets
				rule = m_targetRuleMap.get(posTarget);
				if (rule == null)
					{
					posTargets = m_fileManager.getPossibleFiles(posTarget);
					for (String t : posTargets)
						{
						rule = m_targetRuleMap.get(t);
						if (rule != null)
							break;
						}
					}
				}
	
			Debug.print("Rule for "+target+" is "+rule);
			if (rule != null)
				{
				m_locatedRules.put(target, rule);
				}
			else
				m_noRulesList.add(target);
			}
			
		return (rule);
		}
		
	
		
	//===========================================================================
	
	//---------------------------------------------------------------------------
	public CachedFile locateFileUsingRules(String fileName)
		{
		List<String> paths;
		CachedFile ret = null;
		CachedFile f;
		
		f = new CachedFile(fileName);
		if (f.exists())
			ret = f;
		else if ((ret = m_locatedFiles.get(fileName)) == null)
			{
			paths = m_fileManager.getPossibleFiles(fileName);
			
			for (String path : paths)
				{
				f = new CachedFile(path);
				
				if (f.exists())
					{
					ret = f;
					m_locatedFiles.put(fileName, ret);
					break;
					}
				else if (m_targetRuleMap.containsKey(path))
					{
					ret = f;
					break;
					}
				}
			}

		return (ret);
		}
		
	//---------------------------------------------------------------------------
	public void setProperty(String key, String value)
		{
		}
		
	public String getProperty(String key)
		{
		
		return (null);
		}
		
	public void setBanner(String banner)
		{
		}
		
	public void setVerbose(boolean verbose)
		{
		}
		
	
		
	//---------------------------------------------------------------------------
	public List<Rule> getRuleList()
		{
		return (m_ruleList);
		}
		
	//---------------------------------------------------------------------------
	public void addRule(Rule rule)
		{
		Debug.print("AddRule() "+rule.getName()+" "+rule);
		//do not get the rule name as it may not be set yet
		//rules are added often in the constructor when other values
		//have not been set yet.

		//todo: Get a list of provides and consumes annotations
		m_ruleList.add(rule);

		Class ruleClass = rule.getClass();
		DependencyAnnotations da = m_classAnnotations.get(ruleClass);
		if (da == null)
			m_classAnnotations.put(ruleClass, createDependencyAnnotations(ruleClass));
		}

	private DependencyAnnotations createDependencyAnnotations(Class ruleClass)
		{
		DependencyAnnotations da = new DependencyAnnotations();

		for (Method m : ruleClass.getMethods())
			{
			Consumes consumes = m.getAnnotation(Consumes.class);
			if (consumes != null)
				{
				//System.out.println("Adding consumes "+m.getName());
				da.addConsumesMethod(consumes.value(), m);
				}
			else
				{
				Provides provides = m.getAnnotation(Provides.class);
				if (provides != null)
					{
					//System.out.println("Adding provides "+m.getName());
					da.addProvidesMethod(provides.value(), m);
					}
				}
			}

		return da;
		}

	//---------------------------------------------------------------------------
	/**
		Called once to set the primary target
		All rules must be set by this point
	*/
	public boolean setPrimaryTarget(String target)
			throws TablesawException
		{
		m_buildQueue = new LinkedList<BuildAction>();
		resolveRules();
		
		boolean ret = addToBuildQueue(target, true, 0) != null;
		
		if (Debug.isDebug())
			printDependencies();
		
		return (ret);
		}
		
	//---------------------------------------------------------------------------
	/**
		Method to process the build queue, can be called by more than one thread
	*/
	public void processBuildQueue()
			throws TablesawException
		{
		BuildAction ba;
		Rule rule;
		String target;
		
		synchronized (m_threadList)
			{
			m_threadList.add(Thread.currentThread());
			}

		try
			{
			doloop: do
				{
				synchronized (this)
					{
					try
						{
						ba = (BuildAction)m_buildQueue.removeLast();
						}
					catch (NoSuchElementException nsee)
						{
						break doloop;
						//ba = new BuildAction(m_fileManager, null);
						}
						
					if (m_printDebug)
						Debug.print("Processing: "+ba);
					
					if (m_makeException != null)
						break doloop;
						
					ba.waitForDependencies();
					
					if (m_makeException != null)
						break doloop;
					}
			
				rule = ba.getTargetRule();
				MakeAction action = rule.getMakeAction();
				
				//target = ba.getTarget();
				if (action != null)
					{
					action.doMakeAction(rule);
					
					rule.verify();
					}
					
				Debug.print("COMPLETE - "+ba);
				ba.complete();  //Complete the BuildAction
				} 
			while (m_makeException == null);
			}
		catch (Exception e)
			{
			TablesawException cpe;
			
			if (e instanceof TablesawException)
				cpe = (TablesawException)e;
			else 
				cpe = new TablesawException(e);
				
			synchronized (this)
				{
				m_makeException = cpe;
				
				for (Thread t : m_threadList)
					t.interrupt();
				}
				
			throw cpe;
			}
			
		//This causes worker threads to die off and the exception to
		//pass to the main thread
		if (m_makeException != null)
			throw m_makeException;
		}
		
	//---------------------------------------------------------------------------
	public Set<String> getNamedRules()
		{
		return (m_nameRuleMap.keySet());
		}
	}
