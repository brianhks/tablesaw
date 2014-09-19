package tablesaw.rules;

import java.util.*;
import tablesaw.MakeAction;
import tablesaw.TablesawException;

public class HelpRule extends SimpleRule
		implements MakeAction, Overridable
	{
	public HelpRule()
		{
		super("help");
		super.setMakeAction(this);
		super.setDescription("Prints named targets in build script");
		super.alwaysRun();
		}
		
	//---------------------------------------------------------------------------
	public void doMakeAction(Rule cpRule)
			throws TablesawException
		{
		System.out.println("Build targets for script: "+m_make.getBuildFile());
		TreeSet<String> sortedSet = new TreeSet<String>(m_make.getNamedRules());
		for (String r : sortedSet)
			{
			Rule namedRule = m_make.findRule(r);
			String description = namedRule.getDescription() == null ? "" : namedRule.getDescription();
			
			System.out.println("  "+r+": "+description);
			}
		}
		
	}
