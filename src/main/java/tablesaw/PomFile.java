package tablesaw;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;

import static tablesaw.definitions.DefinitionManager.getValue;

public class PomFile
	{
	private final String m_version;

	public PomFile(File pomFile) throws TablesawException
		{
		if (!pomFile.exists())
			throw new TablesawException("Unable to locate pom file "+pomFile.getName());

		try
			{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			dbf.setIgnoringElementContentWhitespace(true);
			dbf.setIgnoringComments(true);
			dbf.setValidating(false);

			DocumentBuilder db = dbf.newDocumentBuilder();

			InputStream is = new FileInputStream(pomFile);

			Document xmldoc = db.parse(new InputSource(is));
			Element rootElement = xmldoc.getDocumentElement();
			Node version = rootElement.getElementsByTagName("version").item(0);

			m_version = getValue((Element)version);
			}
		catch (FileNotFoundException e)
			{
			throw new RuntimeException(e);
			}
		catch (ParserConfigurationException e)
			{
			throw new RuntimeException(e);
			}
		catch (IOException e)
			{
			throw new RuntimeException(e);
			}
		catch (SAXException e)
			{
			throw new RuntimeException(e);
			}

		}

	public String getVersion()
		{
		return m_version;
		}
	}
