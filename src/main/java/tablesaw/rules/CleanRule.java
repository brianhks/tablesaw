package tablesaw.rules;

import tablesaw.MakeAction;
import tablesaw.MakeEngine;
import tablesaw.TablesawException;

public class CleanRule extends AbstractSimpleRule<CleanRule>
		implements MakeAction, Overridable
	{
	MakeEngine m_engine;
	
	public CleanRule(MakeEngine engine)
		{
		super("clean");
		super.setMakeAction(this);
		m_engine = engine;
		super.setDescription("Removes the contents of any directory rule created in the build script");
		alwaysRun();
		}
		
	//---------------------------------------------------------------------------
	public void doMakeAction(Rule cpRule)
			throws TablesawException
		{
		for (Rule r : m_engine.getRuleList())
			{
			if (r instanceof DirectoryRule)
				m_make.deltree(((DirectoryRule)r).getDirectory());
			}
		}
		
	}
