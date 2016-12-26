package tablesaw.addons.ivy;

import tablesaw.*;
import tablesaw.annotation.Provides;
import tablesaw.rules.AbstractRule;
import tablesaw.rules.Rule;
import tablesaw.addons.java.Classpath;

import java.util.*;
import java.io.File;

import org.apache.ivy.Ivy;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.apache.ivy.util.DefaultMessageLogger;
import org.apache.ivy.util.Message;

public class ResolveRule extends AbstractRule<ResolveRule>
		implements MakeAction
	{
	private File m_ivyFile;
	private List<File> m_settingsFiles;
	private Set<String> m_configurations = new HashSet<String>();
	private ResolveReport m_report = null;
	private Ivy m_ivy;
	private boolean m_binding = false;


	//---------------------------------------------------------------------------
	/*package*/ ResolveRule(File ivyFile, List<File> settingsFiles, Set<String> configurations)
		{
		super();
		//setName("ivy-resolve");
		m_ivyFile = ivyFile;
		setMakeAction(this);
		m_ivy = Ivy.newInstance();

		m_settingsFiles = settingsFiles;
		m_configurations = configurations;

		this.addDepend(m_ivyFile.getAbsolutePath());

		for (File settingsFile : settingsFiles)
			{
			this.addDepend(settingsFile.getAbsolutePath());
			}
		}
		
	//---------------------------------------------------------------------------
	public Iterable<String> getTargets()
		{
		return (new ArrayList<String>());
		}
		
	//---------------------------------------------------------------------------
	public boolean needToRun() { return (true); }

	public boolean isBinding() { return (m_binding); }

	private void checkFile(Map<String, Long> modificationCache, File file)
		{
		Long modTime = modificationCache.get(file.getAbsolutePath());

		if ((modTime == null) || (modTime != file.lastModified()))
			m_binding = true;
		}
	
	//---------------------------------------------------------------------------
	public void preBuild(DependencyCache cache, Map<String, Long> modificationCache)
		{
		checkFile(modificationCache, m_ivyFile);

		for (File settingsFile : m_settingsFiles)
			{
			checkFile(modificationCache, settingsFile);
			}
		}
		
	//---------------------------------------------------------------------------
	/**
	 This report is available after the rule has ran
	 @return
	 */
	public ResolveReport getReport() throws TablesawException
		{
		if (m_report == null)
			throw new TablesawException("You cannot call ResolveRule.getReport until after the rule as been ran", -1);

		return (m_report);
		}

	//---------------------------------------------------------------------------
	/**
	 Returns the ivy instance used for performing this resolve
	 @return
	 */
	public Ivy getIvyInstance() { return (m_ivy); }

	//---------------------------------------------------------------------------
	@Provides("java.classpath")
	public Classpath getClasspath() throws TablesawException
		{
		return getClasspath(m_configurations.iterator().next());
		}

	//---------------------------------------------------------------------------
	public Classpath getClasspath(String configuration)
			throws TablesawException
		{
		if (m_report == null)
			throw new TablesawException("You cannot call ResolveRule.getClasspath until after the rule as been ran", -1);
			
		Classpath cp = new Classpath();

		/*for (String s : m_report.getConfigurations())
			System.out.println(s);*/
		Debug.print("ResolveRule.getClasspath(\"%s\")", configuration);
		Debug.indent();
		ArtifactDownloadReport[] deps = m_report.getConfigurationReport(configuration)
				.getAllArtifactsReports();
				
		for (ArtifactDownloadReport rep : deps)
			{
			Debug.print("adding to classpath: %s", rep.getLocalFile().getPath());
			if (rep.getExt().equals("jar"))
				cp.addPath(rep.getLocalFile().getPath());
			}

		Debug.popIndent();
		
		return (cp);
		}
	
	//---------------------------------------------------------------------------
	public void doMakeAction(Rule rule)
			throws TablesawException
		{
		try
			{
			System.out.println("Ivy resolving dependencies.");

			if (!m_make.getProperty(Tablesaw.PROP_VERBOSE, "false").equals("true"))
				m_ivy.getLoggerEngine().pushLogger(new DefaultMessageLogger(Message.MSG_WARN));

			if (Debug.isDebug())
				m_ivy.getLoggerEngine().pushLogger(new DefaultMessageLogger(Message.MSG_DEBUG));

			for (File settingsFile : m_settingsFiles)
				{
				m_ivy.configure(settingsFile);
				}

			if (m_settingsFiles.size() == 0)
				m_ivy.configureDefault();
				
			ResolveOptions ro = new ResolveOptions();
			//ro.setValidate(false);
			//ro.setUseCacheOnly(false);
			//ro.setCheckIfChanged(false);

			if (m_configurations.size() != 0)
				ro.setConfs(m_configurations.toArray(new String[0]));

			//ro.setUseCacheOnly(false);
			//ro.setRefresh(true);
			
			m_report = m_ivy.resolve(m_ivyFile.toURI().toURL(), ro);
			
			if (m_report.hasError())
				throw new TablesawException("Unable to resolve dependencies", -1);
			}
		catch (java.net.MalformedURLException e)
			{
			throw new TablesawException(e);
			}
		catch (java.text.ParseException e)
			{
			throw new TablesawException(e);
			}
		catch (java.io.IOException e)
			{
			throw new TablesawException(e);
			}
		}
	}
