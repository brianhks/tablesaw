package tablesaw.addons.java;

import tablesaw.*;

/**
 Extension to the JarRule that allows you to build a war file.
 */
public class WarRule extends JarRule
		implements MakeAction
	{
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
		
	}
