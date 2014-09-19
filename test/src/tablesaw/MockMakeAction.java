package tablesaw;

import tablesaw.MakeAction;
import tablesaw.rules.Rule;

public class MockMakeAction implements MakeAction
{
	private Rule m_rule;
	
	public void doMakeAction(Rule rule)
	{
		m_rule = rule;
	}
	
	public Rule getRule() 
	{
		return (m_rule);
	}
}
