package tablesaw.definitions;

import java.util.*;
import tablesaw.*;
import java.io.*;

import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

public class DefinitionManager
	{
	public static final String NAME_SPACE = "http://www.cpmake.org";
	
	public static final String TAG_OPTION = "option";
	public static final String TAG_GROUP = "group";
	
	public static final String ORDER_DECLARED = "declared";
	public static final String ORDER_SET = "set";
	
	private Map<String, Definition> m_definitions;
	
	
	private static String getValue(Element n)
		{
		CharacterData cd = (CharacterData)n.getFirstChild();
		if (cd == null)
			return ("");
		else
			return (cd.getData().replaceAll("\\s+", " "));
		}
	
	public DefinitionManager()
		{
		m_definitions = new HashMap<String, Definition>();
		}
		
	//---------------------------------------------------------------------------
	private Option readOption(Element optElm)
		{
		String name = null;
		if (optElm.hasAttribute(Option.NAME))
			name = optElm.getAttribute(Option.NAME);
			
		Option op = new Option(name, getValue(optElm));
				
		String value = optElm.getAttribute("mode");
		if (value != null && !value.equals(""))
			op.setMode(value);
			
		value = optElm.getAttribute("fix_slash");
		if ((value != null)&&(value.equals("true")))
			op.setFixSlash(true);
			
		value = optElm.getAttribute("pattern");
		if (value != null && !value.equals(""))
			op.setPattern(value);
			
		return (op);
		}
		
	//---------------------------------------------------------------------------
	public void includeDefinitionFile(String file)
			throws TablesawException
		{
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		InputStream is = cl.getResourceAsStream(file);
		if (is == null)
			{
			File defFile = new File(Tablesaw.getCurrentTablesaw().getWorkingDirectory(), file);
			if (!defFile.exists())
				defFile = new File(Tablesaw.getTablesawPath(), file);

			try
				{
				is = new FileInputStream(defFile);
				}
			catch (FileNotFoundException fnfe)
				{
				throw new TablesawException("Cannot find file "+file, -1);
				}
			}
			
		try
			{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			dbf.setIgnoringElementContentWhitespace(true);
			dbf.setIgnoringComments(true);
			dbf.setValidating(false);
			
			DocumentBuilder db = dbf.newDocumentBuilder();
	
			Document xmldoc = db.parse(new InputSource(is));
			
			NodeList defList = xmldoc.getElementsByTagNameNS(NAME_SPACE, "definition");
			
			for (int I = 0; I < defList.getLength(); I++)
				{
				Element defElm = (Element)defList.item(I);
				
				Definition def = new Definition(defElm.getAttribute("name"),
						defElm.getAttribute("command"));
						
				NodeList children = defElm.getChildNodes();
				for (int J = 0; J < children.getLength(); J++)
					{
					Node n = children.item(J);
					if (!(n instanceof Element))
						continue;
						
					Element e = (Element)n;
					String tagName = e.getTagName();
					if (tagName.equals("mode"))
						def.addMode(getValue(e));
					
					else if (tagName.equals("property"))
						def.addProperty(e.getAttribute("name"), getValue(e));
						
					else if (tagName.equals(TAG_OPTION))
						{
						Option op = readOption(e);
							
						//System.out.println(op);
						def.addOption(op);
						}
						
					else if (tagName.equals(TAG_GROUP))
						{
						Group group = new Group(e.getAttribute(Group.NAME));
						
						NodeList grpOpList = e.getElementsByTagNameNS(NAME_SPACE, TAG_OPTION);
						for (int opCnt = 0; opCnt < grpOpList.getLength(); opCnt ++)
							{
							Option op = readOption((Element)grpOpList.item(opCnt));
							
							group.addOption(op);
							}
						//Get name
						//Iterator over children
						def.addOption(group);
						}
					else
						{
						throw new TablesawException("Error in definition file "+file+": Unknown definition tag '"+tagName+"'", -1);
						}
						
					}
					
				m_definitions.put(def.getName(), def);
				}
			}
		catch (TablesawException cpe)
			{
			throw cpe;
			}
		catch (Exception e)
			{
			Debug.print(e);
			throw new TablesawException(e.toString(), -1);
			}
			
		}
		
	/**
	Returns the definition by the name given.  Definition file must already
	be loaded.
	*/
	public Definition getDefinition(String name)
		{
		Definition def = m_definitions.get(name);
		
		if (def != null)
			def = (Definition)def.clone();
			
		return (def);
		}

	public Collection<Definition> getDefinitions()
		{
		return (m_definitions.values());
		}
	}
