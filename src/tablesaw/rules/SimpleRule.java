package tablesaw.rules;

/**
	This is as it says for creating simple rules.  You have one or more source 
	files and they create one or more target files and you have one command
	to create all target files.
*/
public class SimpleRule extends AbstractSimpleRule<SimpleRule>
	{
	//---------------------------------------------------------------------------
	/**
		Constructor for creating an anonymous rule.  An anonymous rule is one that
		cannot be specified on the command line.
	*/
	public SimpleRule()
		{
		this(null);
		}
	
	//---------------------------------------------------------------------------
	/**
		Constructor for creating a named rule.  The name can be used to call the 
		rule from the command line.
		
		@param name The name that can be used in other rules' addDepend call or 
			specified on the command line.
	*/
	public SimpleRule(String name)
		{
		super(name);
		}
	}
