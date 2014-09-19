package tablesaw.addons.ivy;

import tablesaw.TablesawException;
import tablesaw.MakeAction;
import tablesaw.rules.AbstractRule;
import tablesaw.rules.Rule;
import org.apache.ivy.Ivy;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.retrieve.RetrieveOptions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 Created with IntelliJ IDEA.
 User: bhawkins
 Date: 8/11/12
 Time: 10:14 PM
 To change this template use File | Settings | File Templates.
 */
public class RetrieveRule extends AbstractRule<RetrieveRule>
		implements MakeAction
	{
	private File m_ivyFile;
	private ResolveRule m_resolveRule;
	private RetrieveOptions m_retreiveOptions;
	private String m_retrievePattern;
	private boolean m_useWorkingDir = false;

	/*package*/ RetrieveRule(File ivyFile)
		{
		super();
		setName("ivy-retrieve");
		m_ivyFile = ivyFile;
		setMakeAction(this);
		m_retreiveOptions = new RetrieveOptions();

		m_retrievePattern = m_make.getProperty("tablesaw.ivy.retrieve.pattern",
				"lib/ivy/[conf]/[artifact]-[revision](-[classifier]).[ext]");
		}


	//---------------------------------------------------------------------------
	public Iterable<String> getTargets()
		{
		return (new ArrayList<String>());
		}

	//---------------------------------------------------------------------------
	public RetrieveRule setUseWorkingDirectory(boolean useWorkingDir)
		{
		m_useWorkingDir = useWorkingDir;
		return (this);
		}

	//---------------------------------------------------------------------------
	public void doMakeAction(Rule rule) throws TablesawException
		{
		System.out.println("Ivy retrieving dependencies.");
		try
			{
			ModuleDescriptor md = m_resolveRule.getReport().getModuleDescriptor();

			Ivy ivy = m_resolveRule.getIvyInstance();
			
			String pattern;
			
			if (m_useWorkingDir && m_make.getWorkingDirectory() != null)
				{
				pattern = m_make.getWorkingDirectory().getPath();
				pattern += "/" + m_retrievePattern;
				}
			else
				pattern = m_retrievePattern;

			if (m_make.isVerbose())
				System.out.println("Ivy retrieve pattern: "+pattern);

			ivy.retrieve(md.getModuleRevisionId(), pattern,
					m_retreiveOptions);
			}
		catch (IOException ioe)
			{
			throw new TablesawException(ioe);
			}
		}

	//---------------------------------------------------------------------------
	public boolean needToRun() { return (true); }

	//---------------------------------------------------------------------------
	/*package*/ void setResolveRule(ResolveRule resolveRule)
		{
		m_resolveRule = resolveRule;
		addDepend(m_resolveRule);
		}
		
	//---------------------------------------------------------------------------
	public RetrieveRule setRetrievePattern(String pattern)
		{
		m_retrievePattern = pattern;
		return (this);
		}
		
	//---------------------------------------------------------------------------
	public RetrieveOptions getRetrieveOptions()
		{
		return (m_retreiveOptions);
		}
	}
