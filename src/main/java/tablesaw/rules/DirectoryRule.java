package tablesaw.rules;

import java.util.*;
import tablesaw.MakeAction;
import tablesaw.TablesawException;

import java.io.File;

/**
	Rule used to create directories for other rules.
*/
public class DirectoryRule extends AbstractRule<DirectoryRule>
		implements MakeAction
	{
	private File m_directory;
	private String m_strDirectory;

	/**

	 @param directory The relative path to the directory you want to create
	 */
	public DirectoryRule(String directory)
		{
		super();
		super.setMakeAction(this);
		m_directory = new File(m_make.getWorkingDirectory(), directory);
		m_strDirectory = directory;
		}
		
	public boolean needToRun()
		{
		return (!m_directory.exists());
		}
		
	public String getDirectory()
		{
		return (m_strDirectory);
		}
		
	public Set<String> getTargets()
		{
		Set<String> ret = new LinkedHashSet<String>();
		ret.add(m_strDirectory);
		return (ret);
		}
		
	public void doMakeAction(Rule cpRule)
			throws TablesawException
		{
		m_directory.mkdirs();
		}
		
	@Override
	public boolean equals(Object o)
		{
		boolean ret = false;
		
		if (o instanceof DirectoryRule)
			{
			DirectoryRule other = (DirectoryRule)o;
			
			if (m_directory.equals(other.m_directory))
				ret = true;
			}
			
		return (ret);
		}
	}
