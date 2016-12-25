package tablesaw;

import tablesaw.sync.SyncFile;
import java.io.File;
import java.util.*;

class ContinuousBuild
	{
	private String m_buildFile;
	private String m_buildTarget;
	private ArrayList<SyncFile> m_fileList;
	private int m_timeout;
	private List<String> m_defines;
	private String m_propertiesFile;
	
	//---------------------------------------------------------------------------
	public ContinuousBuild(String buildFile, String target, int timeout)
		{
		m_buildFile = buildFile;
		m_buildTarget = target;
		m_timeout = timeout;
		m_fileList = new ArrayList<SyncFile>();
		}
	
	//---------------------------------------------------------------------------
	public void setDefines(List<String> defines)
		{
		m_defines = defines;
		}
	public void setPropertiesFile(String propertiesFile)
		{
		m_propertiesFile = propertiesFile;
		}
		
	//---------------------------------------------------------------------------
	public void rebuild()
			throws TablesawException
		{
		Tablesaw make = new Tablesaw();
		
		try
			{
			make.loadPropertiesFile(m_propertiesFile);
			make.init();
			Tablesaw.addDefines(make, m_defines);
			make.setCaptureSource(true);
			make.clearFileCache();  //or cpmake will use old timestamps
			make.processBuildFile(m_buildFile);
			if (m_buildTarget == null)
				make.buildDefaultTarget();
			else
				make.buildTarget(m_buildTarget);
			}
		catch (Exception e)
			{
			System.out.println(e.getMessage());
			}
			
		m_fileList.clear();
		//Reset source files
		Iterator<File> it = make.getSourceFiles().iterator();
		while (it.hasNext())
			{
			File f = it.next();
			m_fileList.add(new SyncFile(f));
			}
			
		make.close();
		System.out.println("Rebuild done");
		Thread.interrupted();  //Clear the interrupt in case of a build break
		}
		
	//---------------------------------------------------------------------------
	public void run()
		{
		try
			{
			for (;;)
				{
				try
					{
					outer:
					for(;;)
						{
						Thread.sleep(m_timeout);
						
						try
							{
							for (SyncFile sf : m_fileList)
								{
								File source = sf.getSource();
								long mod = source.lastModified();
								if (mod != sf.getSourceModTime())
									{
									sf.setSourceModTime(mod);
									
									System.out.println("Rebuild triggered by "+source);
									Thread.sleep(500); //Pause in case the file is begin copied
									break outer;
									}
								}
							}
						catch (Exception e)
							{
							e.printStackTrace();
							}
						}
					}
				catch (Exception e)
					{
					e.printStackTrace();
					}
					
				rebuild();
				}
			}
		catch (TablesawException cpe)
			{
			cpe.printStackTrace();
			}
		}
	}
