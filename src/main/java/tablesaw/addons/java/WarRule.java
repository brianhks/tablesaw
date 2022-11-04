package tablesaw.addons.java;

import tablesaw.*;
import tablesaw.annotation.Consumes;

import java.io.File;

/**
 Extension to the JarRule that allows you to build a war file.
 */
public class WarRule extends JarRule
		implements MakeAction
	{
	public static final String LIB_FOLDER = "WEB-INF/lib";

	/**
	 Create a WarRule specifying the target war file to create.
	 @param targetWarFile
	 @throws TablesawException
	 */
	public WarRule(String targetWarFile)
			throws TablesawException
		{
		super(targetWarFile);
		init();
		}

	/**
	 Create a named WarRule specifying the target war file to create.
	 @param name
	 @param targetWarFile
	 @throws TablesawException
	 */
	public WarRule(String name, String targetWarFile)
			throws TablesawException
		{
		super(name, targetWarFile);
		init();
		}
		
	private void init()
		{
		setMakeAction(this);
		}
		
	//---------------------------------------------------------------------------
	/**
		Sets the web.xml file to use in your war file.  This method will set the
		file as WEB-INF/web.xml in the war file despite the path or name provided.
	*/
	public WarRule setWebXmlFile(String webxmlFile)
		{
		addFileAs(webxmlFile, "WEB-INF/web.xml");
		
		return (this);
		}
		
	//---------------------------------------------------------------------------
	/**
		Sets the context.xml file to use in your war file.  This method will set
		the file as META-INF/context.xml in the war file despite the path or name
		provided.
	*/
	public WarRule setContextXmlFile(String contextFile)
		{
		addFileAs(contextFile, "META-INF/context.xml");
		
		return (this);
		}

	public WarRule addWebAppDirectory(String webAppFolder)
	{
		RegExFileSet fileSet = new RegExFileSet(webAppFolder, ".*").recurse();

		addFileSet(fileSet);

		return (this);
	}

	public WarRule addJavaProgram(JavaProgram jp)
	{
		JarRule jarRule = jp.getJarRule();
		addDepend(jarRule);
		File targetFile = new File(jarRule.getTarget());
		addFileTo(LIB_FOLDER, targetFile.getParent(), targetFile.getName());

		return this;
	}
	

	@Consumes("java.classpath")
	public WarRule addClasspath(Classpath classpath)
		{
		for (String path : classpath.getPaths())
		{
			if (path.endsWith(".jar"))
			{
				File pathFile = new File(path);
				this.addFileTo(LIB_FOLDER, pathFile.getParent(), pathFile.getName());
			}
		}

		return (this);
		}
	}