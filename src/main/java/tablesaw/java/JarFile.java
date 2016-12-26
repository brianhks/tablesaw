package tablesaw.java;

import java.util.*;
import tablesaw.*;
import tablesaw.definitions.Definition;

import tablesaw.rules.Rule;
import tablesaw.rules.AbstractSimpleRule;

@Deprecated
public class JarFile extends AbstractSimpleRule<JarFile>
		implements MakeAction
	{
	private String m_manifest;
	private List<String[]> m_sourceFiles;
	private Definition m_jarDef;
	private List<AbstractFileSet<? extends AbstractFileSet>> m_fileSets;
	
	
	public JarFile(String targetJarFile)
			throws TablesawException
		{
		this(null, targetJarFile);
		}
		
	public JarFile(String name, String targetJarFile)
			throws TablesawException
		{
		super(name);
		m_manifest = null;
		addTarget(targetJarFile);
		m_sourceFiles = new ArrayList<String[]>();
		m_fileSets = new ArrayList<AbstractFileSet<? extends AbstractFileSet>>();
		
		m_jarDef = m_make.getDefinition("sun_jar");
		setMakeAction(this);
		}
		
	@Override
	public Object clone() throws CloneNotSupportedException
		{
		JarFile copy = (JarFile)super.clone();
		
		copy.m_sourceFiles = (List<String[]>)((ArrayList<String[]>)m_sourceFiles).clone();
		copy.m_fileSets = (List<AbstractFileSet<? extends AbstractFileSet>>)((ArrayList<AbstractFileSet<? extends AbstractFileSet>>)m_fileSets).clone();
		copy.m_jarDef = (Definition)m_jarDef.clone();
		
		return (copy);
		}
		
	//---------------------------------------------------------------------------
	public JarFile setManifest(String manifest)
		{
		m_manifest = manifest;
		addDepends(manifest);
		return (this);
		}
		
	//---------------------------------------------------------------------------
	public void prepForBuild(String target)
		{
		//m_make.createExplicitRule(m_targetJarFile, m_prereqs, this, true);
		}
		
	//---------------------------------------------------------------------------
	public JarFile addFiles(String file)
		{
		addFiles(null, file);
		return (this);
		}
		
	//---------------------------------------------------------------------------
	public JarFile addFiles(String... files)
		{
		addFiles(null, Arrays.asList(files));
		return (this);
		}

	//---------------------------------------------------------------------------		
	public JarFile addFiles(Iterable<String> files)
		{
		addFiles(null, files);
		return (this);
		}
		
	//---------------------------------------------------------------------------
	public JarFile addFiles(String dir, String... files)
		{
		addFiles(dir, Arrays.asList(files));
		return (this);
		}
		
	//---------------------------------------------------------------------------
	public JarFile addFiles(String dir, Iterable<String> files)
		{
		for (String file : files)
			{
			if (dir != null)
				addSource(dir+"/"+file);
			else
				addSource(file);
			m_sourceFiles.add(new String[] {dir, file});
			}
			
		return (this);
		}
		
	//---------------------------------------------------------------------------
	public JarFile addFiles(String dir, String file)
		{
		if (!file.equals("."))
			{
			if (dir != null)
				addSource(dir+"/"+file);
				//m_make.createExplicitDependency(m_targetJarFile, dir+"/"+file);
			else
				addSource(file);
				//m_make.createExplicitDependency(m_targetJarFile, file);
			}
		m_sourceFiles.add(new String[] {dir, file});
		
		return (this);
		}
		
	//---------------------------------------------------------------------------
	public JarFile addFileSet(AbstractFileSet<? extends AbstractFileSet> fileSet)
		{
		m_fileSets.add(fileSet);
		return (this);
		}
		
	//---------------------------------------------------------------------------
	public void doMakeAction(Rule cpRule)
			throws TablesawException
		{
		System.out.println("Creating "+getTarget());
		
		if (m_manifest == null)
			m_jarDef.set("create_no_manifest");
		else
			{
			m_jarDef.set("create_with_manifest");
			m_jarDef.set("manifest", m_manifest);
			}
			
		m_jarDef.set("jar_file", getTarget());
		
		for (String[] file : m_sourceFiles)
			{
			if (file[0] == null)
				m_jarDef.add("inc_file", file[1]);
			else
				m_jarDef.addGroup("inc_sub_file", (Object[])file);
			}
			
		for (AbstractFileSet<? extends AbstractFileSet> set : m_fileSets)
			{
			for (AbstractFileSet.File file : set.getFiles())
				m_jarDef.addGroup("inc_sub_file", new Object[] { file.getBaseDir(), file.getFile() });
			}
			
		m_make.exec(m_jarDef.getCommand(), true);
			
		/* System.out.println("Creating "+cpRule.getTarget());
		String jarCommand = "";
		
		if ((m_manifest == null) || (m_manifest.equals("")))
			jarCommand = "jar -cf "+cpRule.getTarget();
		else
			jarCommand = "jar -cfm "+cpRule.getTarget()+" "+m_manifest;
			
		Iterator<String[]> it = m_sourceFiles.iterator();
		while (it.hasNext())
			{
			String[] srcArray = it.next();
			if (srcArray[0] != null)
				jarCommand += " -C "+srcArray[0];
				
			jarCommand += " "+srcArray[1];
			}
		
		make.exec(jarCommand); */
		}
	}
