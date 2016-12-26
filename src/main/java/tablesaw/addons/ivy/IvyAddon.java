package tablesaw.addons.ivy;

//Do not import ivy classes here.
import tablesaw.TablesawException;
import tablesaw.Tablesaw;
import tablesaw.addons.java.JavaProgram;
import tablesaw.util.Validation;

import java.io.File;
import java.util.*;

public class IvyAddon
	{
	private boolean m_isSetup = false;
	
	private File m_ivyFile;
	private ResolveRule m_resolveRule;
	private RetrieveRule m_retrieveRule;
	private List<File> m_settingsFiles = new ArrayList<File>();
	private Map<Set<String>, ResolveRule> m_resolveRules =
			new HashMap<Set<String>, ResolveRule>();

	public IvyAddon()
			throws TablesawException
		{
		Tablesaw make = Tablesaw.getCurrentTablesaw();
		m_ivyFile = make.file("ivy.xml");
		
		ClassLoader cl = getClass().getClassLoader();
		if (cl.getResource("org/apache/ivy/Ivy.class") == null)
			throw new TablesawException("Please place the Ivy jar file in the classpath or in the same directory as the tablesaw jar");
		}
		
	public IvyAddon addSettingsFile(String settingsFile)
			throws TablesawException
		{
		m_settingsFiles.add(Validation.fileMustExist(settingsFile));

		return (this);
		}

	public IvyAddon setIvyFile(String ivyFile) throws TablesawException
		{
		m_ivyFile = Validation.fileMustExist(ivyFile);

		return this;
		}
		
	public IvyAddon setup()
		{
		//check if m_ivyFile exists else throw error
		/* 
		Create rule for 
		Publish
		*/

		//I think I want to remove these
		Set<String> conf = Collections.singleton("*");
		m_resolveRule = new ResolveRule(m_ivyFile, m_settingsFiles, Collections.singleton("*"));
		m_resolveRule.setName("ivy-resolve");
		m_resolveRules.put(conf, m_resolveRule);

		m_retrieveRule = new RetrieveRule(m_ivyFile);
		m_retrieveRule.setResolveRule(m_resolveRule);
		
		m_isSetup = true;
		return (this);
		}
		
	//---------------------------------------------------------------------------
	private void ensureSetup()
			throws TablesawException
		{
		if (!m_isSetup)
			throw new TablesawException("You must call setup first", -1);
		}
		
	//---------------------------------------------------------------------------
	public ResolveRule getResolveRule(String configuration)
			throws TablesawException
		{
		return (getResolveRule(Collections.singleton(configuration)));
		}

	//---------------------------------------------------------------------------
	public ResolveRule getResolveRule(Object... configurations)
			throws TablesawException
		{
		if ((configurations.length == 1) && (configurations[0] instanceof Iterable))
			return (getResolveRule((Iterable<Object>) configurations[0]));
		else
			return (getResolveRule(Arrays.asList(configurations)));
		}

	//---------------------------------------------------------------------------
	public ResolveRule getResolveRule(Iterable<Object> configurations)
			throws TablesawException
		{
		ensureSetup();

		Set<String> configSet = new HashSet<String>();
		for (Object o : configurations)
			configSet.add(o.toString());

		ResolveRule rule = m_resolveRules.get(configSet);
		if (rule == null)
			{
			rule = new ResolveRule(m_ivyFile, m_settingsFiles, configSet);
			m_resolveRules.put(configSet, rule);
			}

		return (rule);
		}

	//---------------------------------------------------------------------------
	public RetrieveRule getRetrieveRule(String configuration)
			throws TablesawException
		{
		ensureSetup();
		return (m_retrieveRule);
		}

	/**
	 Creates a rule to write a pom file
	 @param pomFile Name of the pom file to write
	 @param resolveRule resolve rule to use when adding dependencies to the pom
	 @return
	 @throws TablesawException
	 */
	public PomRule createPomRule(String pomFile, ResolveRule resolveRule) throws TablesawException
		{
		ensureSetup();
		return (new PomRule(m_ivyFile, new File(pomFile), resolveRule, null));
		}

	/**
	 Creates a rule to write a pom file that includes test dependencies
	 @param pomFile Name of the pom file to write
	 @param resolveRule resolve rule to use when adding dependencies to the pom.
	 @param testResolveRule resolve rule to use when adding test dependencies to pom.
	 @return
	 @throws TablesawException
	 */
	public PomRule createPomRule(String pomFile, ResolveRule resolveRule,
			ResolveRule testResolveRule) throws TablesawException
		{
		ensureSetup();
		return (new PomRule(m_ivyFile, new File(pomFile), resolveRule, testResolveRule));
		}

	public PublishRule createPublishRule(String resolverName, ResolveRule resolveRule) throws TablesawException
		{
		ensureSetup();
		return (new PublishRule(m_ivyFile, resolverName, resolveRule));
		}
	}
