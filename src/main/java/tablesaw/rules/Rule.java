package tablesaw.rules;

import tablesaw.*;

import java.util.Map;

public interface Rule extends Cloneable
	{
	/**
		Returns the name of the rule or null if the rule is anonymous.
	*/
	public String getName();
	
	/**
		Sets the name of this rule.  The name can be passed to other rules' 
		addDepend method or specified on the command line to run this rule.
		@param name Name of the rule.
	*/
	public Rule setName(String name);
	
	/**
		Returns a description of this rule - used for displaying help and 
		generating ant build file.
	*/
	public String getDescription();

	/**
	 Determins if rules that depend on this rule should be ran if this one runs.
	 @return true if dependent rule should run if this rule runs (default)
	 */
	public boolean isBinding();
	
	
	/**
		This identifies if this rule can be overridden by other rules in the build
		script
	*/
	public boolean getOverride();
	
	/**
		Returns the make action for this rule.
	*/
	public MakeAction getMakeAction();
	
	/**
		Gets the immediate depends of this rule
	*/
	public Iterable<Rule> getDependRules();
	
	/**
		Gets the immediate depends of this rule.  Each name in the returned list
		is first treated as a named rule, if that lookup fails it is treated as
		the name of an actual file.
	*/
	public Iterable<String> getDependNames();
	
	/**
		Returns a list of targets this rule will create
	*/
	public Iterable<String> getTargets();
	
	/**
		the rule will check to see if it needs to run or not
		This will only be called if none of the depends cause this
		rule to run
	*/
	public boolean needToRun() throws TablesawException;
	
	/**
		Called when the build engine is considering this rule to be added to the 
		build queue.  This is called before <code>needToRun</code>
		This is a good place for the rule to parse source and or target files
		to include any additional depends that it may have
	*/
	public void preBuild(DependencyCache cache, Map<String, Long> modificationCache) throws TablesawException;
	
	/**
		This is to notify the rule that the rule has been placed in the build
		queue for a rebuild.  This gives the rule the oportunity to delete
		any target files before rebuilding, this ensures a complete build.
	*/
	public void inBuildQueue() throws TablesawException;
	
	/**
		Called if the build for this rule completes successfully.  It allows
		the rule to save information to the build cache.
	*/
	public void buildComplete();
	
	/**
		Called after the build so the rule can verify the results
	*/
	public void verify() throws TablesawException;
	
	/**
		Clone the rule
	*/
	public Object clone() throws CloneNotSupportedException;
	}
