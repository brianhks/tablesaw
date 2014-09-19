package tablesaw.addons;

import java.util.*;
import tablesaw.*;

import tablesaw.rules.Rule;
import tablesaw.rules.AbstractSimpleRule;
import tablesaw.util.Pair;

import java.io.*;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;

import static tablesaw.util.Validation.*;

public class ZipRule extends AbstractSimpleRule<ZipRule>
		implements MakeAction
	{
	protected class FileEntries
		{
		private SimpleFileSet m_sourceFiles;
		private List<AbstractFileSet<? extends AbstractFileSet>> m_fileSets;
		
		public FileEntries()
			{
			m_sourceFiles = new SimpleFileSet();
			m_fileSets = new ArrayList<AbstractFileSet<? extends AbstractFileSet>>();
			m_fileSets.add(m_sourceFiles);
			}
			
		public SimpleFileSet getDefaultFileSet()
			{
			return (m_sourceFiles);
			}
			
		public List<AbstractFileSet<? extends AbstractFileSet>> getFileSets()
			{
			return (m_fileSets);
			}
		}
	
	
	/*protected SimpleFileSet m_sourceFiles;
	protected List<AbstractFileSet<? extends AbstractFileSet>> m_fileSets;*/
	
	protected Map<String, FileEntries> m_fileEntries;
	protected List<Pair<String, String>> m_renamedFiles;
	
	private ZipOutputStream m_zipOut;  //Used while zipping up files
	protected byte[] m_buffer = new byte[1024]; //Buffer used while writing zip
	private Set<String> m_addedDirectories;
	
	public ZipRule(String targetZipFile)
			throws TablesawException
		{
		this(null, targetZipFile);
		}
		
	//---------------------------------------------------------------------------
	public ZipRule(String name, String targetZipFile)
			throws TablesawException
		{
		super(name);
		addTarget(targetZipFile);
		
		m_fileEntries = new HashMap<String, FileEntries>();
		m_renamedFiles = new ArrayList<Pair<String, String>>();
		
		m_fileEntries.put("", new FileEntries());
		
		/*m_sourceFiles = new SimpleFileSet();
		m_fileSets = new ArrayList<AbstractFileSet<? extends AbstractFileSet>>();*/
		
		/*m_fileSets.add(m_sourceFiles);*/
		
		setMakeAction(this);
		}
		
	@Override
	public Object clone() throws CloneNotSupportedException
		{
		ZipRule copy = (ZipRule)super.clone();
		
		copy.m_fileEntries = (Map<String, FileEntries>)((HashMap<String, FileEntries>)m_fileEntries).clone();
		copy.m_renamedFiles = (List<Pair<String, String>>)((ArrayList<Pair<String, String>>)m_renamedFiles).clone();
		
		return (copy);
		}
	//---------------------------------------------------------------------------
	/**
		Returns the file entries for a zip directory, creates if necessary
	*/
	private FileEntries getFileEntries(String zipDir)
		{
		//Ensure zipDir ends with '/'
		if (!(zipDir.endsWith("/") || zipDir.endsWith("\\")))
			zipDir += '/';

		FileEntries entries = m_fileEntries.get(zipDir);
		if (entries == null)
			{
			entries = new FileEntries();
			m_fileEntries.put(zipDir, entries);
			}
		
		return (entries);
		}
		
	//---------------------------------------------------------------------------
	/**
		This lets you add a file and rename it within the zip.
		ex. addFileAs("conf/my_context.xml", "META-INF/context.xml")  The file
		conf/my_context.xml is added to the zip as META-INF/context.xml
	*/
	public ZipRule addFileAs(String file, String zipFile)
		{
		addSource(file);
		m_renamedFiles.add(new Pair(file, zipFile));
		
		return (this);
		}
		
	//---------------------------------------------------------------------------
	public ZipRule addFile(String file)
		{
		return (addFileTo("", null, file));
		}
	
	//---------------------------------------------------------------------------
	public ZipRule addFile(String dir, String file)
		{
		return (addFileTo("", dir, file));
		}
		
	//---------------------------------------------------------------------------
	public ZipRule addFiles(Object... files)
		{
		return (addFilesTo("", null, Arrays.asList(files)));
		}

	//---------------------------------------------------------------------------		
	public ZipRule addFiles(Iterable<Object> files)
		{
		return (addFilesTo("", null, files));
		}
		
	//---------------------------------------------------------------------------
	public ZipRule addFiles(String dir, Object... files)
		{
		return (addFilesTo("", dir, Arrays.asList(files)));
		}
		
	//---------------------------------------------------------------------------
	public ZipRule addFiles(String dir, Iterable<Object> files)
		{
		return (addFilesTo("", dir, files));
		}
		
	//---------------------------------------------------------------------------
	public ZipRule addFileSet(AbstractFileSet<? extends AbstractFileSet> fileSet)
		{
		return (addFileSetTo("", fileSet));
		}
		
	//---------------------------------------------------------------------------
	/**
		@param zipDir The directory in the zip file in which to place the file
		@param file File to add to zip
	*/
	public ZipRule addFileTo(String zipDir, String file)
		{
		return (addFileTo(zipDir, null, file));
		}
	
	//---------------------------------------------------------------------------
	public ZipRule addFileTo(String zipDir, String dir, String file)
		{
		if (!file.equals("."))
			{
			if (dir != null)
				addSource(dir+"/"+file);
			else
				addSource(file);
			}
			
		getFileEntries(zipDir).getDefaultFileSet().addFile(dir, file);
		
		return (this);
		}
		
	//---------------------------------------------------------------------------
	public ZipRule addFilesTo(String zipDir, Object... files)
		{
		addFilesTo(zipDir, null, Arrays.asList(files));
		return (this);
		}

	//---------------------------------------------------------------------------		
	public ZipRule addFilesTo(String zipDir, Iterable<Object> files)
		{
		addFilesTo(zipDir, null, files);
		return (this);
		}
		
	//---------------------------------------------------------------------------
	public ZipRule addFilesTo(String zipDir, String dir, Object... files)
		{
		if ((files.length == 1) && (files[0] instanceof Iterable))
			return (addFilesTo(zipDir, dir, (Iterable<Object>)files[0]));
		else
			return (addFilesTo(zipDir, dir, Arrays.asList(files)));
		}
		
	//---------------------------------------------------------------------------
	public ZipRule addFilesTo(String zipDir, String dir, Iterable<Object> files)
		{
		for (Object file : files)
			{
			if (dir != null)
				addSource(dir+"/"+objectToString(file));
			else
				addSource(objectToString(file));
				
			getFileEntries(zipDir).getDefaultFileSet().addFile(dir, objectToString(file));
			}
			
		return (this);
		}
		
	//---------------------------------------------------------------------------
	public ZipRule addFileSetTo(String zipDir, AbstractFileSet<? extends AbstractFileSet> fileSet)
		{
		addSources(fileSet.getFullFilePaths());
		getFileEntries(zipDir).getFileSets().add(fileSet);
		return (this);
		}
		
		
	//---------------------------------------------------------------------------
	protected void addEntry(File file, String zipName)
			throws IOException
		{
		if (Debug.isDebug())
			Debug.print("Adding %s to archive as %s", file.getPath(), zipName);
		FileInputStream fin = new FileInputStream(file);
							
		m_zipOut.putNextEntry(new ZipEntry(zipName));
		
		int len;
		while ((len = fin.read(m_buffer)) > 0)
			m_zipOut.write(m_buffer, 0, len);

		m_zipOut.closeEntry();
			
		fin.close();
		}
		
	//---------------------------------------------------------------------------
	protected void addDirectory(String zipDir)
			throws IOException
		{
		if (!zipDir.endsWith("/"))
			zipDir += '/';
			
		m_zipOut.putNextEntry(new ZipEntry(zipDir));
		m_zipOut.closeEntry();
		}
		
	//---------------------------------------------------------------------------
	private void addParents(String zipEntry)
			throws IOException
		{
		File zipFile = new File(zipEntry);
		
		String zipParent = zipFile.getParent();
		
		if (zipParent != null && !zipParent.equals("/") && m_addedDirectories.add(zipParent))
			{
			addParents(zipParent);
			addDirectory(zipParent);
			}
		}
		
	//---------------------------------------------------------------------------
	protected void addFilesToArchive()
			throws IOException
		{
		m_addedDirectories = new HashSet<String>();
		
		for (Map.Entry<String, FileEntries> entry : m_fileEntries.entrySet())
			{
			String zipDir = entry.getKey();

			for (AbstractFileSet<? extends AbstractFileSet> set : entry.getValue().getFileSets())
				{
				for (AbstractFileSet.File file : set.getFiles())
					{
					File baseDir = (file.getBaseDir() == null ? m_make.getWorkingDirectory() :
							m_make.file(file.getBaseDir()));
					
					if (zipDir.equals("/"))
						zipDir = "";
						
					String zipFile = zipDir + file.getFile();
					addParents(zipFile);

					addEntry(new File(baseDir, file.getFile()), zipFile);
					}
				}
			}
		
		for (Pair<String, String> renamed : m_renamedFiles)
			{
			addEntry(new File(renamed.getFirst()), renamed.getSecond());
			}
		}
		
	//---------------------------------------------------------------------------
	public void doMakeAction(Rule cpRule)
			throws TablesawException
		{
		System.out.println("Creating "+getTarget());
		
		try
			{
			m_zipOut = new ZipOutputStream(new FileOutputStream(
					new File(m_make.getWorkingDirectory(), getTarget()), false));

			addFilesToArchive();
				
			m_zipOut.close();
			}
		catch (IOException ioe)
			{
			throw new TablesawException(ioe);
			}
		}
	}
