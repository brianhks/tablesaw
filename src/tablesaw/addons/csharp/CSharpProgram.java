package tablesaw.addons.csharp;


import tablesaw.MakeAction;
import tablesaw.TablesawException;
import tablesaw.rules.Rule;
import tablesaw.definitions.Definition;
import tablesaw.rules.AbstractSourceRule;

import static tablesaw.util.Validation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import tablesaw.parsers.CSProjectParser;

import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.SAXException;


/**
*/
public class CSharpProgram extends AbstractSourceRule<CSharpProgram> implements MakeAction
	{
	private Definition m_definition;
	private String m_programName;
	private boolean m_isSetup = false;
	private ApplicationType m_appType;
	private PlatformType m_platformType;
	
	//---------------------------------------------------------------------------
	private class Handler implements CSProjectParser.SlickHandler
		{
		private CSProjectParser.Project m_project;
		
		public void parsedProject(CSProjectParser.Project proj)
			{
			m_project = proj;
			}
			
		public CSProjectParser.Project getProject()
			{
			return (m_project);
			}
		}
		
	//---------------------------------------------------------------------------
	
	private void ensureSetup()
			throws TablesawException
		{
		if (!m_isSetup)
			throw new TablesawException("You must call setup first", -1);
		}
	
		
	//---------------------------------------------------------------------------
	public CSharpProgram()
			throws TablesawException
		{
		super();
		
		setName("compile");
		setDescription("Compile C# code");

		m_definition = m_make.getDefinition("microsoft_cs_compiler");
		
		try
			{
			m_programName = new File(m_make.getWorkingDirectory(), ".")
					.getCanonicalFile().getName();
			}
		catch (java.io.IOException ioe)
			{
			throw new TablesawException(ioe);
			}
		}
		
	//---------------------------------------------------------------------------
	public Definition getDefinition()
		{
		return (m_definition);
		}
	
	//---------------------------------------------------------------------------
	/**
		This method will cause the addon to read settings from a visual studio
		csproj file.
	*/
	public CSharpProgram setProjectFile(String csprojFile)
			throws TablesawException
		{
		notNull(csprojFile);
		fileMustExist(new File(csprojFile));

		Handler handler = new Handler();
		try
			{
			XMLReader xmlParser = XMLReaderFactory.createXMLReader();
			CSProjectParser genHandler = new CSProjectParser(handler);
			xmlParser.setContentHandler(genHandler);
			
			InputSource source = new InputSource(new FileInputStream(csprojFile));
			xmlParser.parse(source);
			}
		catch (IOException ioe)
			{
			throw new TablesawException(ioe);
			}
		catch (SAXException sax)
			{
			throw new TablesawException(sax);
			}

		CSProjectParser.Project csProject = handler.getProject();

		m_programName = csProject.getAssemblyName();
		m_appType = ApplicationType.valueOf(handler.getProject().getOutputType().toUpperCase());
		List<CSProjectParser.Compile> sourceList = csProject.getCompileList();

		for (CSProjectParser.Compile source : sourceList)
			{
			addSource(source.getInclude().replace("\\", "/"));
			}

		m_platformType = PlatformType.valueOf(handler.getProject().getPlatform().toUpperCase());

		
		return (this);
		}
		
	//---------------------------------------------------------------------------
	public CSharpProgram setup()
			throws TablesawException
		{
		String appName = m_programName + "." + m_appType.getExtension();

		m_definition.set("out", "build/debug/"+appName);
		m_definition.set("target", m_appType);
		m_definition.set("platform", m_platformType);
		m_definition.set("sourcefile", getSources());
		
		m_isSetup = true;
		return (this);
		}
		
	//---------------------------------------------------------------------------
	public CSharpProgram setApplicationType(ApplicationType type)
		{
		m_appType = type;
		return (this);
		}
	
	//---------------------------------------------------------------------------
	public ApplicationType getApplicationType()
		{
		return (m_appType);
		}
		
	//---------------------------------------------------------------------------
	public CSharpProgram setPlatformType(PlatformType type)
		{
		m_platformType = type;
		return (this);
		}
		
	//---------------------------------------------------------------------------
	public PlatformType getPlatformType()
		{
		return (m_platformType);
		}
		
	//---------------------------------------------------------------------------
	public CSharpProgram setProgramName(String name)
		{
		m_programName = name;
		return (this);
		}
		
	//---------------------------------------------------------------------------
	public String getProgramName()
		{
		return (m_programName);
		}
		
	//---------------------------------------------------------------------------
	/**
		Set the name of the documentation file to generate
		@param file XML Documentation file to generate
	*/
	public CSharpProgram setDocumentationFile(String file)
		{
		
		return (this);
		}
		
	//---------------------------------------------------------------------------
	public Iterable<String> getTargets()
		{
		
		return (null);
		}
		
	//---------------------------------------------------------------------------
	/**
	 
	*/
	public void doMakeAction(Rule rule)
		{
		
		}
	}
