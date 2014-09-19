package tablesaw.rules;

import tablesaw.MakeAction;
import tablesaw.MakeEngine;
import tablesaw.TablesawException;
import tablesaw.definitions.Definition;
import tablesaw.definitions.DefinitionManager;

public class DefinitionsRule extends AbstractSimpleRule<CleanRule>
		implements MakeAction, Overridable
	{
	DefinitionManager m_dm;
	
	public DefinitionsRule(DefinitionManager dm)
		{
		super("definitions");
		super.setMakeAction(this);
		m_dm = dm;
		super.setDescription("Dumps out all command line definitions.");
		alwaysRun();
		}
		
	//---------------------------------------------------------------------------
	public void doMakeAction(Rule cpRule)
			throws TablesawException
		{
		for (Definition definition : m_dm.getDefinitions())
			{
			System.out.println(definition.toString());
			}
		}
		
	}
