package tablesaw.addons.java;

import tablesaw.*;
import tablesaw.addons.ZipRule;

import tablesaw.rules.Rule;

import java.io.*;

import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.jar.Attributes;

import static tablesaw.util.Validation.*;

/**
 Rule for creating a jar files.
 */
public class JarRule extends ZipRule
		implements MakeAction
	{
	protected Manifest m_manifest;
	
	private JarOutputStream m_jarOut;
	
	/**
		Create a JarRule specifying the path to the target jar file to create.
		@param targetJarFile Path to jar file to create
	*/
	public JarRule(String targetJarFile)
			throws TablesawException
		{
		super(targetJarFile);
		init();
		}
		
	/**
		@param name Name of this rule that you can call from the command line.
		@param targetJarFile Path to jar file to create
	*/
	public JarRule(String name, String targetJarFile)
			throws TablesawException
		{
		super(name, targetJarFile);
		init();
		}
		
	private void init()
		{
		setMakeAction(this);
		m_manifest = new Manifest();
		m_manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
		}
		
	//---------------------------------------------------------------------------
	/**
		Set the manifest file to include in the jar file
		@param manifestFile Path to the manifest file to include
	*/
	public JarRule setManifest(String manifestFile)
			throws TablesawException
		{
		notNull(manifestFile);
		File mf = new File(m_make.getWorkingDirectory(), manifestFile);
		fileMustExist(mf);
		
		addSource(manifestFile);
		try
			{
			m_manifest = new Manifest(new FileInputStream(mf));
			}
		catch (IOException ioe) 
			{
			throw new TablesawException(ioe);
			}
		
		return (this);
		}
		
	//---------------------------------------------------------------------------
	/**
		Returns the Manifest object used for creating this jar file
	*/
	public Manifest getManifest()
		{
		if (m_manifest == null)
			m_manifest = new Manifest();
			
		return (m_manifest);
		}
	
		
	//---------------------------------------------------------------------------
	@Override
	protected void addEntry(File file, String zipName)
			throws IOException
		{
		FileInputStream fin = new FileInputStream(file);
		
		JarEntry entry = new JarEntry(zipName);
		entry.setTime(file.lastModified());
		m_jarOut.putNextEntry(entry);
		
		int len;
		while ((len = fin.read(m_buffer)) > 0)
			m_jarOut.write(m_buffer, 0, len);
			
		m_jarOut.closeEntry();
			
		fin.close();
		}
		
	//---------------------------------------------------------------------------
	@Override
	protected void addDirectory(String jarDir)
			throws IOException
		{
		if (!jarDir.endsWith("/"))
			jarDir += '/';
			
		m_jarOut.putNextEntry(new JarEntry(jarDir));
		m_jarOut.closeEntry();
		}
		
	//---------------------------------------------------------------------------
	@Override
	public void doMakeAction(Rule cpRule)
			throws TablesawException
		{
		System.out.println("Creating "+getTarget());
		
		try
			{
			m_jarOut = new JarOutputStream(new FileOutputStream(
					new File(m_make.getWorkingDirectory(), getTarget()), false),
					m_manifest);
			
			//addDirectory("META-INF");
			addFilesToArchive();
				
			m_jarOut.close();
			}
		catch (IOException ioe)
			{
			throw new TablesawException(ioe);
			}
		}
	}
