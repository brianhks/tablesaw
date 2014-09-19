package tablesaw.parsers;

import java.util.*;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
	This file is a slickxml generated SAX parser.
	
	The following is the configuration used to create this file
	
<parser name="CSProjectParser">
		<object tag="Project" name="Project">
			<property name="AssemblyName">
				<element name="AssemblyName"/>
			</property>
			
			<property name="OutputType">
				<element name="OutputType"/>
			</property>
			
			<property name="Platform">
				<element name="Platform"/>
			</property>
			
			<object tag="Compile" name="Compile">
				<property name="Include">
					<attribute>Include</attribute>
				</property>
			</object>
			
			<object tag="Reference" name="Reference">
				<property name="Include">
					<attribute>Include</attribute>
				</property>
				<property name="HintPath">
					<element name="HintPath"/>
				</property>
			</object>
			
		</object>
	</parser>
*/
public class CSProjectParser extends DefaultHandler
	{
	private SlickHandler _parserHandler;
	private StringBuilder _characterGrabber;
	
	private Project m_Project;
	private int m_ProjectRef = 0;

	
	/**
		Main constructor
		
		@param handler Handler class provided by you to receive the data objects
			created as the XML is parsed.
	*/
	public CSProjectParser(SlickHandler handler)
		{
		_parserHandler = handler;
		}
		
	/**
		This is the interface implemented by you.  As the SAX parser processes the 
		XML data objects are created by the slickXML parser and passed to this 
		interface.
	*/
	public static interface SlickHandler
		{
		public void parsedProject(Project entry) throws Exception;

		}
		

	//========================================================================
	public class Compile
		{
		private boolean _firstCall = true;
		
		private String m_Include;

		public String getInclude() { return (m_Include); }



		//------------------------------------------------------------------------
		protected void startElement(String uri, String localName, String qName, Attributes attrs)
			{
			if (_firstCall)
				{
				m_Include = attrs.getValue("Include");

				_firstCall = false;
				return;
				}
				

				
			}

		//------------------------------------------------------------------------
		protected void endElement(String uri, String localName, String qName)
				throws SAXException
			{

			}
		}

	//========================================================================
	public class Project
		{
		private boolean _firstCall = true;
		
		private StringBuilder m_AssemblyName;
		private StringBuilder m_OutputType;
		private StringBuilder m_Platform;

		private List<String> m_AssemblyNameList = new ArrayList<String>();
		private List<String> m_OutputTypeList = new ArrayList<String>();
		private List<String> m_PlatformList = new ArrayList<String>();

		private List<Compile> m_CompileList = new ArrayList<Compile>();
		private Compile m_Compile;
		private int m_CompileRef = 0;
		private List<Reference> m_ReferenceList = new ArrayList<Reference>();
		private Reference m_Reference;
		private int m_ReferenceRef = 0;


		public String getAssemblyName()
			{
			return (m_AssemblyNameList.size() == 0 ? null : m_AssemblyNameList.get(0));
			}
			
		public List<String> getAssemblyNameList()
			{
			return (m_AssemblyNameList);
			}

		public String getOutputType()
			{
			return (m_OutputTypeList.size() == 0 ? null : m_OutputTypeList.get(0));
			}
			
		public List<String> getOutputTypeList()
			{
			return (m_OutputTypeList);
			}

		public String getPlatform()
			{
			return (m_PlatformList.size() == 0 ? null : m_PlatformList.get(0));
			}
			
		public List<String> getPlatformList()
			{
			return (m_PlatformList);
			}


		public List<Compile> getCompileList() { return (m_CompileList); }
		/**
			Convenience function for getting a single value
		*/
		public Compile getCompile() 
			{
			if (m_CompileList.size() == 0)
				return (null);
			else
				return (m_CompileList.get(0));
			}
			
		public List<Reference> getReferenceList() { return (m_ReferenceList); }
		/**
			Convenience function for getting a single value
		*/
		public Reference getReference() 
			{
			if (m_ReferenceList.size() == 0)
				return (null);
			else
				return (m_ReferenceList.get(0));
			}
			


		//------------------------------------------------------------------------
		protected void startElement(String uri, String localName, String qName, Attributes attrs)
			{
			if (_firstCall)
				{
				_firstCall = false;
				return;
				}
				
			if (m_Compile != null) 
				{
				if (localName.equals("Compile"))
					m_CompileRef ++;
					
				m_Compile.startElement(uri, localName, qName, attrs);
				return;
				}
				
			if (m_Reference != null) 
				{
				if (localName.equals("Reference"))
					m_ReferenceRef ++;
					
				m_Reference.startElement(uri, localName, qName, attrs);
				return;
				}
				


			if (localName.equals("AssemblyName") )
				{
				m_AssemblyName = new StringBuilder();
				_characterGrabber = m_AssemblyName;
				}
			if (localName.equals("OutputType") )
				{
				m_OutputType = new StringBuilder();
				_characterGrabber = m_OutputType;
				}
			if (localName.equals("Platform") )
				{
				m_Platform = new StringBuilder();
				_characterGrabber = m_Platform;
				}

				
			if (localName.equals("Compile"))
				{
				m_Compile = new Compile();
				m_CompileList.add(m_Compile);
				m_CompileRef = 1;
				
				m_Compile.startElement(uri, localName, qName, attrs);
				}

			if (localName.equals("Reference"))
				{
				m_Reference = new Reference();
				m_ReferenceList.add(m_Reference);
				m_ReferenceRef = 1;
				
				m_Reference.startElement(uri, localName, qName, attrs);
				}


			}

		//------------------------------------------------------------------------
		protected void endElement(String uri, String localName, String qName)
				throws SAXException
			{
			if (m_AssemblyName != null)
				{
				m_AssemblyNameList.add(m_AssemblyName.toString());
				m_AssemblyName = null;
				}
				if (m_OutputType != null)
				{
				m_OutputTypeList.add(m_OutputType.toString());
				m_OutputType = null;
				}
				if (m_Platform != null)
				{
				m_PlatformList.add(m_Platform.toString());
				m_Platform = null;
				}
				

			if ((localName.equals("Compile")) && ((--m_CompileRef) == 0))
				{
				m_Compile = null;
				}
					
			if (m_Compile != null)
				m_Compile.endElement(uri, localName, qName);

			if ((localName.equals("Reference")) && ((--m_ReferenceRef) == 0))
				{
				m_Reference = null;
				}
					
			if (m_Reference != null)
				m_Reference.endElement(uri, localName, qName);


			}
		}

	//========================================================================
	public class Reference
		{
		private boolean _firstCall = true;
		
		private String m_Include;
		private StringBuilder m_HintPath;

		private List<String> m_HintPathList = new ArrayList<String>();

		public String getInclude() { return (m_Include); }

		public String getHintPath()
			{
			return (m_HintPathList.size() == 0 ? null : m_HintPathList.get(0));
			}
			
		public List<String> getHintPathList()
			{
			return (m_HintPathList);
			}



		//------------------------------------------------------------------------
		protected void startElement(String uri, String localName, String qName, Attributes attrs)
			{
			if (_firstCall)
				{
				m_Include = attrs.getValue("Include");

				_firstCall = false;
				return;
				}
				

			if (localName.equals("HintPath") )
				{
				m_HintPath = new StringBuilder();
				_characterGrabber = m_HintPath;
				}

				
			}

		//------------------------------------------------------------------------
		protected void endElement(String uri, String localName, String qName)
				throws SAXException
			{
			if (m_HintPath != null)
				{
				m_HintPathList.add(m_HintPath.toString());
				m_HintPath = null;
				}
				

			}
		}

		
	//========================================================================
	@Override
	public void characters(char[] ch, int start, int length)
		{
		if (_characterGrabber != null)
			_characterGrabber.append(ch, start, length);
		}
		
	//------------------------------------------------------------------------
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attrs)
			throws SAXException
		{
		if (localName.equals("Project"))
			{
			//This handles recursive nodes
			if (m_Project != null)
				m_ProjectRef ++;
			else
				{
				m_Project = new Project();
				m_ProjectRef = 1;
				}
			}

		if (m_Project != null)
			{
			m_Project.startElement(uri, localName, qName, attrs);
			}


		}
	
	//------------------------------------------------------------------------
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException
		{
		//Stop grabbing characters for any node.
		_characterGrabber = null;
		
		if ((localName.equals("Project")) && ((--m_ProjectRef) == 0))
			{
			try
				{
				_parserHandler.parsedProject(m_Project);
				}
			catch (Exception e)
				{
				throw new SAXException(e);
				}
				
			m_Project = null;
			}
			
		if (m_Project != null)
			{
			m_Project.endElement(uri, localName, qName);
			}

		} 
	}
	