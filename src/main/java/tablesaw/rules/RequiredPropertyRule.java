package tablesaw.rules;

import java.util.HashSet;
import java.util.Set;

import tablesaw.TablesawException;
import tablesaw.MakeAction;

public class RequiredPropertyRule extends AbstractRule<RequiredPropertyRule>
		implements MakeAction
	{
	private String m_propName;
	
	public RequiredPropertyRule(String propertyName)
		{
		super();
		m_propName = propertyName;
		setMakeAction(this);
		}
	
	@Override
	public boolean needToRun() 
		{
		String property = m_make.getProperty(m_propName);
		if (property == null)
			return (true);
		else
			return (false);
		}
	
	public void doMakeAction(Rule rule)
			throws TablesawException
		{
		throw new TablesawException("You must set the environment variable \""+m_propName+"\" before running this target");
		}
		
	public Set<String> getTargets() { return (new HashSet<String>()); }
	}
