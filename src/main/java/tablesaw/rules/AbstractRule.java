package tablesaw.rules;

import tablesaw.DependencyCache;
import tablesaw.ScriptCallAction;
import tablesaw.MakeAction;
import java.util.*;
import tablesaw.TablesawException;
import tablesaw.Tablesaw;

public abstract class AbstractRule<T extends AbstractRule> implements Rule
	{
	protected String m_name;    //name of the rule
	protected String m_description;
	protected MakeAction m_action;
	
	protected Set<Rule> m_dependRules;
	protected Set<String> m_dependStrings;  //These are either names of rules or files
	
	protected Map<String, Object> m_properties;
	protected boolean m_override;
	
	protected Tablesaw m_make;
	
	public AbstractRule()
		{
		m_name = null;
		m_description = null;
		m_dependStrings = new LinkedHashSet<String>();
		m_dependRules = new LinkedHashSet<Rule>();
		m_properties = new HashMap<String, Object>();
		
		m_make = Tablesaw.getCurrentTablesaw();
		m_make.addRule(this);
		}
		
	//---------------------------------------------------------------------------
	@Override
	public Object clone()
			throws CloneNotSupportedException
		{
		AbstractRule copy = (AbstractRule)super.clone();
		
		copy.m_dependStrings = (Set<String>)((LinkedHashSet<String>)m_dependStrings).clone();
		copy.m_dependRules = (Set<Rule>)((LinkedHashSet<Rule>)m_dependRules).clone();
		copy.m_properties = (Map<String, Object>)((HashMap<String, Object>)m_properties).clone();
		
		return (copy);
		}

	public boolean isBinding() { return (true); }
		
	//---------------------------------------------------------------------------
	public String getName() { return (m_name); }
	public T setName(String name) { m_name = name; return ((T)this); }
	
	//---------------------------------------------------------------------------
	public String getDescription() { return (m_description); }
	public T setDescription(String desc) { m_description = desc; return ((T)this); }
	
	//---------------------------------------------------------------------------
	public T override() { m_override = true; return ((T)this);}
	public boolean getOverride() { return (m_override); }
	
	//---------------------------------------------------------------------------
	public T setProperty(String key, Object value) { m_properties.put(key, value); return ((T)this); }
	public Object getProperty(String key) { return (m_properties.get(key)); }
	public T setProperties(Map<String, Object> props) { m_properties = props; return ((T)this); }
	
	//---------------------------------------------------------------------------
	public T setMakeAction(Object action) 
		{
		if (action instanceof MakeAction)
			m_action = (MakeAction)action;
		else if (action instanceof String)
			m_action = new ScriptCallAction((String)action);
		else
			m_action = m_make.getScriptInterpreter().getMakeAction(action); 
		return ((T)this); 
		}
		
	public MakeAction getMakeAction() { return (m_action); }
	
	
	//---------------------------------------------------------------------------
	public T addDepends(Iterable<? extends Object> rules)
		{
		for (Object r : rules)
			{
			if (r instanceof Rule)
				m_dependRules.add((Rule)r);
			else
				m_dependStrings.add((String)r);
			}
		return ((T)this);
		}
		
	//---------------------------------------------------------------------------
	public T addDepend(Rule rule)
		{
		m_dependRules.add(rule);
		return ((T)this);
		}
		
	//---------------------------------------------------------------------------
	public T addDepends(Rule... rules)
		{
		for (Rule r : rules)
			m_dependRules.add(r);
		return ((T)this);
		}
		
	//---------------------------------------------------------------------------
	public T addDepend(String depName)
		{
		m_dependStrings.add(depName);
		return ((T)this);
		}
		
	//---------------------------------------------------------------------------
	/**
		Looks for a rule by the name given.  If no rule is found then looks for a file
		by the name given and wrapps it in a simple rule wrapper.
	*/
	public T addDepends(String... ruleNames)
		{
		for (String s : ruleNames)
			m_dependStrings.add(s);
		return ((T)this);
		}
		
	//---------------------------------------------------------------------------
	public Iterable<Rule> getDependRules()
		{
		return (m_dependRules);
		}
		
	//---------------------------------------------------------------------------
	public Iterable<String> getDependNames()
		{
		return (m_dependStrings);
		}
		
	//---------------------------------------------------------------------------
	public boolean needToRun() throws TablesawException
		{
		return (false);
		}
		
	//---------------------------------------------------------------------------
	public void preBuild(DependencyCache cache, Map<String, Long> modificationCache) throws TablesawException
	{}
	public void buildComplete() {}
	public void verify() throws TablesawException
	{}
	public void addNewerDepend(String depend) {}
	public void inBuildQueue() throws TablesawException
	{}
	
	//---------------------------------------------------------------------------
	/* public abstract List<String> getTargets();
	
	//---------------------------------------------------------------------------
	public abstract boolean needToRun();
	
	//---------------------------------------------------------------------------
	public abstract boolean isMultiTarget(); */
	
	
	}
