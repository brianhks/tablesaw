package tablesaw;

import tablesaw.rules.Rule;

/**
	Inteface implemented by all classes that provide callbacks to perform a 
	make action.  Typically a make action is a method in the build script in
	which case the script method is wrapped in a ScriptCallAction class automatically
*/
public interface MakeAction
	{
	public void doMakeAction(Rule rule)
			throws TablesawException;
	}
