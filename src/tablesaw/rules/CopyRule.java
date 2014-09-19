package tablesaw.rules;

import java.util.*;

import tablesaw.*;
import tablesaw.TablesawException;

import java.io.File;

/**
	The CopyRule can copy one or more files to a destination directory.
 */
public class CopyRule extends AbstractRule<CopyRule>
		implements MakeAction, SourceFileSet
	{
	private static class MutableBool
		{
		private boolean m_value = false;
		
		public void setValue(boolean value) { m_value = value; }
		public boolean getValue() { return (m_value); }
		}
		
	private static class Counter
		{
		private int m_value = 0;
		
		public void increment() { m_value ++; }
		public int getValue() { return (m_value); }
		}
		
	//---------------------------------------------------------------------------
	private static interface FileCallBack
		{
		/**
			The file iteration continues as along a true is returned
		*/
		public boolean file(AbstractFileSet.File file) throws TablesawException;
		}
		
	//---------------------------------------------------------------------------
	private String m_targetDir;
	private List<AbstractFileSet> m_fileSets;
	private boolean m_setTargets = false;
	private boolean m_verbose = false;
	private boolean m_copyPermissions = false;
	
	
	public CopyRule()
		{
		this(null);
		}
	
	public CopyRule(String name)
		{
		super();
		super.setMakeAction(this);
		super.setName(name);
		m_fileSets = new ArrayList<AbstractFileSet>();
		}
		
	//---------------------------------------------------------------------------
	@Override
	public Object clone() throws CloneNotSupportedException
		{
		CopyRule copy = (CopyRule)super.clone();
		
		copy.m_fileSets = (List<AbstractFileSet>)((ArrayList<AbstractFileSet>)m_fileSets).clone();
		
		return (copy);
		}
		
	//---------------------------------------------------------------------------
	private void iterateFiles(FileCallBack cb)
			throws TablesawException
		{
		top:
		for (AbstractFileSet fs : m_fileSets)
			{
			List<AbstractFileSet.File> files = fs.getFiles();
			for (AbstractFileSet.File f : files)
				{
				if (!cb.file(f))
					break top;
				}
			}
		}
		
	//---------------------------------------------------------------------------

	/**
	 Called by Tablesaw to perform the copy action
	 @param cpRule
	 @throws TablesawException
	 */
	public void doMakeAction(Rule cpRule)
			throws TablesawException
		{
		final Tablesaw make = Tablesaw.getCurrentTablesaw();
		final Counter counter = new Counter();
		
		iterateFiles(new FileCallBack()
				{
				public boolean file(AbstractFileSet.File file)
						throws TablesawException
					{
					File src = new File(file.getBaseDir(), file.getFile());
					File trg = new File(m_targetDir, file.getFile());
					
					if (make.copyIfNewer(src.getPath(), trg.getPath()))
						{
						counter.increment();
						
						if (m_copyPermissions)
							make.duplicateProperties(src, trg);
						}
						
					return (true);
					}
				});
		
		System.out.println("Copied "+counter.getValue()+" files to "+m_targetDir);
		}
		
		
	//---------------------------------------------------------------------------

	/**
	 Returns a list of source files that will be copied
	 @return
	 */
	public List<File> getSourceFiles()
		{
		final List<File> ret = new ArrayList<File>();
		
		try
			{
			iterateFiles(new FileCallBack()
					{
					public boolean file(AbstractFileSet.File file)
						{
						ret.add(new File(file.getBaseDir(), file.getFile()));
						return (true);
						}
					});
			}
		catch (TablesawException e) {} //Not thrown in this case
		
		return (ret);
		}
		
	//---------------------------------------------------------------------------
	public boolean needToRun()
		{
		final MutableBool ret = new MutableBool();
		
		try
			{
			iterateFiles(new FileCallBack()
					{
					public boolean file(AbstractFileSet.File file)
						{
						// TODO: sources need to be set and depends
						File src = new File(file.getBaseDir(), file.getFile());
						File trg = new File(m_targetDir, file.getFile());
						
						if (src.lastModified() != trg.lastModified())
							{
							Debug.print("Need to copy because of %s != %s", src.getPath(), trg.getPath());
							Debug.print("Source: %d", src.lastModified());
							Debug.print("Target: %d", trg.lastModified());
							ret.setValue(true);
							return (false);
							}
						else
							return (true);
						}
					});
			}
		catch (TablesawException e) {} //Not thrown in this case
		
		//System.out.println("Need to run "+ret.getValue());
		return (ret.getValue());
		}
		
	//---------------------------------------------------------------------------
	public Set<String> getTargets()
		{
		final Set<String> targets = new LinkedHashSet<String>();
		
		if (m_setTargets)
			{
			try
				{
				iterateFiles(new FileCallBack()
						{
						public boolean file(AbstractFileSet.File file)
							{
							File f = new File(m_targetDir, file.getFile());
							targets.add(f.getPath());
							return (true);
							}
						});
				}
			catch (TablesawException e) {} //Not thrown in this case */
			}
		
		return (targets);
		}
	//need methodso add groups of files to the copy
	
	//---------------------------------------------------------------------------
	/**
	 When called puts this rule in verbose mode.  When in verbose mode it will
	 print out to standard out each file that is copied
	 @return
	 */
	public CopyRule verbose()
		{
		m_verbose = true;
		return (this);
		}
	//---------------------------------------------------------------------------
	/**
		When called the copy rule will set the destination files as targets of this
		rule.  The targets can then be used for resolving dependencies but may slow
		down the build if the rule contains a lot of files.
	*/
	public CopyRule setTargets()
		{
		m_setTargets = true;
		return (this);
		}
		
	public CopyRule addFile(String file)
		{
		File f = new File(file);
		m_fileSets.add(new SimpleFileSet().addFile(f.getParent(), f.getName()));
		return (this);
		}
		
	//---------------------------------------------------------------------------
	public CopyRule addFiles(Iterable<String> files)
		{
		SimpleFileSet fileSet = new SimpleFileSet();
		
		for (String file : files)
			{
			File f = new File(file);
			fileSet.addFile(f.getParent(), f.getName());
			}
			
		m_fileSets.add(fileSet);
		return (this);
		}
		
	//---------------------------------------------------------------------------
	public CopyRule addFiles(String... files)
		{
		SimpleFileSet fileSet = new SimpleFileSet();
		
		for (String file : files)
			{
			File f = new File(file);
			fileSet.addFile(f.getParent(), f.getName());
			}
		
		m_fileSets.add(fileSet);
		return (this);
		}
		
	//---------------------------------------------------------------------------
	public CopyRule addFile(String dir, String file)
		{
		m_fileSets.add(new SimpleFileSet().addFile(dir, file));
		return (this);
		}
		
	//---------------------------------------------------------------------------
	/**
		Adds and a file set as the source files.  The file set is not evaluated
		until it is time to cope the files.  Changes made to the file set after
		this call has been made will be reflected at time of copy.
	*/
	public CopyRule addFileSet(AbstractFileSet fileSet)
		{
		//System.out.println("addFileSet");
		m_fileSets.add(fileSet);
		return (this);
		}
		
	//---------------------------------------------------------------------------
	/**
	 Sets the destination directory to which all added source files will be copied
	 @param dir
	 @return
	 */
	public CopyRule setDestination(String dir)
		{
		m_targetDir = dir;
		return (this);
		}
		
	//---------------------------------------------------------------------------
	/**
		For Linux this will copy the executable permissions on the file.
		This does slow down the copy when turned on, it is a good idea to 
		limit this feature to only a few files that need it.
	*/
	public CopyRule copyPermissions()
		{
		m_copyPermissions = true;
		return (this);
		}
		
	//---------------------------------------------------------------------------
	/**
		The Converter would allow the file name to be changed in some way
	*/
	/* public CopyRule addConverter(); */
		
	}
