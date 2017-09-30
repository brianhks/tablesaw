package tablesaw.addons.ivy;

import org.apache.ivy.core.module.id.ModuleId;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import tablesaw.BuildCallback;
import tablesaw.MakeAction;
import tablesaw.ScriptCallback;
import tablesaw.Tablesaw;
import tablesaw.TablesawException;
import tablesaw.addons.java.JavaProgram;
import tablesaw.rules.AbstractRule;
import tablesaw.rules.Rule;
import tablesaw.util.Triple;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 Created with IntelliJ IDEA.
 User: bhawkins
 Date: 3/24/14
 Time: 3:47 PM
 To change this template use File | Settings | File Templates.
 */
public class PomRule extends AbstractRule<PomRule>
		implements MakeAction
	{
	public static final String ARTIFACT_ID_PROPERTY = JavaProgram.PROGRAM_NAME_PROPERTY;

	public static final String GROUP_ID_PROPERTY = "tablesaw.java.ivy.group_id";

	public static final String VERSION_PROPERTY = JavaProgram.PROGRAM_VERSION_PROPERTY;
	
	public static final String PACKAGING_PROPERTY = "tablesaw.java.ivy.packaging";
	
	public static final String DESCRIPTION_PROPERTY = JavaProgram.PROGRAM_DESCRIPTION_PROPERTY;
	
	public static final String URL_PROPERTY = "tablesaw.java.ivy.url";
	
	public static final String SCM_URL_PROPERTY = "tablesaw.java.ivy.scm_url";
	
	public static final String SCM_CONNECTION_PROPERTY = "tablesaw.java.ivy.scm_connection";

	private static final String MAVEN_NS = "http://maven.apache.org/POM/4.0.0";

	private static final String DEFAULT_SCOPE = "default";
	private static final String TEST_SCOPE = "test";
	
	//This static map helps with indenting the pom file so it doesn't look like crap.
	private static final Map<String, Integer> s_indentMap = new HashMap<String, Integer>();
	static
		{
		s_indentMap.put("<project", 1);
		s_indentMap.put("</project", -1);

		s_indentMap.put("<scm", 1);
		s_indentMap.put("</scm", -1);

		s_indentMap.put("<licenses", 1);
		s_indentMap.put("</licenses", -1);

		s_indentMap.put("<license", 1);
		s_indentMap.put("</license", -1);

		s_indentMap.put("<developers", 1);
		s_indentMap.put("</developers", -1);

		s_indentMap.put("<developer", 1);
		s_indentMap.put("</developer", -1);

		s_indentMap.put("<dependencies", 1);
		s_indentMap.put("</dependencies", -1);
		s_indentMap.put("<dependency", 1);
		s_indentMap.put("</dependency", -1);

		s_indentMap.put("<exclusions", 1);
		s_indentMap.put("</exclusions", -1);

		s_indentMap.put("<exclusion", 1);
		s_indentMap.put("</exclusion", -1);

		s_indentMap.put("<build", 1);
		s_indentMap.put( "</build", -1);

		s_indentMap.put("<plugins", 1);
		s_indentMap.put("</plugins", -1);

		s_indentMap.put("<plugin", 1);
		s_indentMap.put("</plugin", -1);

		s_indentMap.put("<configuration", 1);
		s_indentMap.put("</configuration", -1);
		}


	BuildCallback m_domCallback;
	private final File m_ivyFile;
	private final File m_pomFile;
	private final Tablesaw m_tablesaw;
	private final ResolveRule m_resolveRule;
	private final ResolveRule m_testResolveRule;

	private String m_artifactId;
	private String m_groupId;
	private String m_version;
	private String m_packaging;
	private String m_name;
	private String m_description;
	private String m_url;
	private String m_scmUrl;
	private String m_scmConnection;
	private String m_javaVersion;


	private List<Triple<String, String, String>> m_licenses;
	private List<Triple<String, String, String>> m_developers;


	/**
	 Normally PomRule objects are not created directly but, through calling
	 {@link tablesaw.addons.ivy.IvyAddon#createPomRule(String, ResolveRule)}
	 @param ivyFile Ivy file to read from
	 @param pomFile Target pom file to create
	 @param resolveRule Resolve rule to use
	 @param testResolveRule Test resolve rule to use
	 */
	public PomRule(File ivyFile, File pomFile, ResolveRule resolveRule, ResolveRule testResolveRule)
		{
		super();
		this.setName("ivy-pom");

		m_ivyFile = ivyFile;
		m_pomFile = pomFile;
		m_resolveRule = resolveRule;
		m_testResolveRule = testResolveRule;
		addDepend(m_resolveRule);
		if (m_testResolveRule != null)
			addDepend(m_testResolveRule);

		m_licenses = new ArrayList<Triple<String, String, String>>();
		m_developers = new ArrayList<Triple<String, String, String>>();

		m_tablesaw = Tablesaw.getCurrentTablesaw();

		setMakeAction(this);
		}

	/**
	 This lets you set a custom callback that can manipulate the pom as a DOM object
	 before the pom file is rendered.  The object that is passed to the callback
	 is a org.w3c.dom.Document which is the pom.
	 @param callback This can be either the string name of a method, an instance
	                 off BuildCallback, a closure (groovy) or a function (js).
	 @return
	 */
	public PomRule setPomCallback(Object callback)
		{
		if (callback instanceof BuildCallback)
			m_domCallback = (BuildCallback)callback;
		else if (callback instanceof String)
			m_domCallback = new ScriptCallback((String)callback);
		else
			m_domCallback = m_make.getScriptInterpreter().getBuildCallback(callback);

		return (this);
		}

	private String getProperty(String localValue, String propertyName)
		{
		if (localValue != null)
			return localValue;

		return m_tablesaw.getProperty(propertyName);
		}

	private void setLicenses(Document document)
		{
		if (m_licenses.size() != 0)
			{
			Element rootNode = document.getDocumentElement();
			Element licenses = document.createElementNS(MAVEN_NS, "licenses");
			rootNode.appendChild(licenses);

			for (Triple<String, String, String> license : m_licenses)
				{
				Element licenseElement = document.createElementNS(MAVEN_NS, "license");
				licenses.appendChild(licenseElement);

				Element name = document.createElementNS(MAVEN_NS, "name");
				name.setTextContent(license.getFirst());
				licenseElement.appendChild(name);

				Element url = document.createElementNS(MAVEN_NS, "url");
				url.setTextContent(license.getSecond());
				licenseElement.appendChild(url);

				Element distribution = document.createElementNS(MAVEN_NS, "distribution");
				distribution.setTextContent(license.getThird());
				licenseElement.appendChild(distribution);
				}
			}
		}

	private void setDevelopers(Document document)
		{
		if (m_developers.size() != 0)
			{
			Element rootNode = document.getDocumentElement();
			Element developers = document.createElementNS(MAVEN_NS, "developers");
			rootNode.appendChild(developers);

			for (Triple<String, String, String> developer : m_developers)
				{
				Element developerElement = document.createElementNS(MAVEN_NS, "developer");
				developers.appendChild(developerElement);

				Element id = document.createElementNS(MAVEN_NS, "id");
				id.setTextContent(developer.getFirst());
				developerElement.appendChild(id);

				Element name = document.createElementNS(MAVEN_NS, "name");
				name.setTextContent(developer.getSecond());
				developerElement.appendChild(name);

				Element email = document.createElementNS(MAVEN_NS, "email");
				email.setTextContent(developer.getThird());
				developerElement.appendChild(email);
				}
			}
		}

	private void setJavaVersion(Document document)
		{
		if (m_javaVersion != null && m_javaVersion.length() > 0)
			{
			Element rootNode = document.getDocumentElement();
			Element build = document.createElementNS(MAVEN_NS, "build");
			rootNode.appendChild(build);

			Element plugins = document.createElementNS(MAVEN_NS, "plugins");
			build.appendChild(plugins);

			Element plugin = document.createElementNS(MAVEN_NS, "plugin");
			plugins.appendChild(plugin);

			Element groupId = document.createElementNS(MAVEN_NS, "groupId");
			groupId.setTextContent("org.apache.maven.plugins");
			Element artifactId = document.createElementNS(MAVEN_NS, "artifactId");
			artifactId.setTextContent("maven-compiler-plugin");
			Element configuration = document.createElementNS(MAVEN_NS, "configuration");
			plugin.appendChild(groupId);
			plugin.appendChild(artifactId);
			plugin.appendChild(configuration);

			Element source = document.createElementNS(MAVEN_NS, "source");
			source.setTextContent(m_javaVersion);
			Element target = document.createElementNS(MAVEN_NS, "target");
			target.setTextContent(m_javaVersion);
			configuration.appendChild(source);
			configuration.appendChild(target);
			}
		}

		private void setTransParam(Transformer transformer, String property, String value)
		{
		if ((value == null) || (value.equals("")))
			return;

		transformer.setParameter(property, value);
		}

	@Override
	public void doMakeAction(Rule rule) throws TablesawException
		{
		try
			{
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			InputStream xslStream = ClassLoader.getSystemClassLoader().getResourceAsStream("tablesaw/addons/ivy/pom.xsl");
			Transformer transformer = transformerFactory.newTransformer(new StreamSource(xslStream));

			setTransParam(transformer, "artifactId", getProperty(m_artifactId, ARTIFACT_ID_PROPERTY));
			setTransParam(transformer, "groupId", getProperty(m_groupId, GROUP_ID_PROPERTY));
			setTransParam(transformer, "version", getProperty(m_version, VERSION_PROPERTY));
			setTransParam(transformer, "packaging", getProperty(m_packaging, PACKAGING_PROPERTY));
			setTransParam(transformer, "name", getProperty(m_name, ARTIFACT_ID_PROPERTY));
			setTransParam(transformer, "description", getProperty(m_description, DESCRIPTION_PROPERTY));
			setTransParam(transformer, "url", getProperty(m_url, URL_PROPERTY));
			setTransParam(transformer, "scm_url", getProperty(m_scmUrl, SCM_URL_PROPERTY));
			setTransParam(transformer, "scm_connection", getProperty(m_scmConnection, SCM_CONNECTION_PROPERTY));

			//Set of resolved dependencies for the specified configuration
			Map<ModuleRevisionId, String> resolvedDependencies = new HashMap<ModuleRevisionId, String>();
			ArtifactDownloadReport[] allArtifactsReports = m_resolveRule.getReport().getAllArtifactsReports();
			for (ArtifactDownloadReport adreport : allArtifactsReports)
				{
				resolvedDependencies.put(adreport.getArtifact().getModuleRevisionId(), DEFAULT_SCOPE);
				}

			//Add in test dependencies
			if (m_testResolveRule != null)
				{
				allArtifactsReports = m_testResolveRule.getReport().getAllArtifactsReports();
				for (ArtifactDownloadReport adreport : allArtifactsReports)
					{
					ModuleRevisionId moduleRevisionId = adreport.getArtifact().getModuleRevisionId();
					if (!resolvedDependencies.containsKey(moduleRevisionId))
						resolvedDependencies.put(moduleRevisionId, TEST_SCOPE);
					}
				}

			//Read in the ivy.xml file
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document ivyDoc = dBuilder.parse(m_ivyFile);

			NodeList dependencyNodes = ivyDoc.getElementsByTagName("dependency");

			//We are going to iterate through the dependencies and remove those
			//that are not part of this configuration.
			//helps keep the pom clean
			for (int I = 0; I < dependencyNodes.getLength(); I++)
				{
				Element node = (Element)dependencyNodes.item(I);

				ModuleRevisionId moduleRevisionId = new ModuleRevisionId(new ModuleId(node.getAttribute("org"), node.getAttribute("name")), node.getAttribute("rev"));

				if (!resolvedDependencies.containsKey(moduleRevisionId))
					{
					node.getParentNode().removeChild(node);
					}
				else if (resolvedDependencies.get(moduleRevisionId).equals(TEST_SCOPE))
					{
					node.setAttribute("scope", "test");
					}
				}

			DOMResult domResult = new DOMResult();
			transformer.transform(new DOMSource(ivyDoc), domResult);

			Document doc = (Document)domResult.getNode();

			setLicenses(doc);
			setDevelopers(doc);
			setJavaVersion(doc);

			if (m_domCallback != null)
				m_domCallback.doCallback(doc);

			Transformer domTransformer = transformerFactory.newTransformer();

			StringWriter sw = new StringWriter();
			domTransformer.transform(new DOMSource(doc), new StreamResult(sw));
			BufferedReader sr = new BufferedReader(new StringReader(sw.toString().replace("><", ">\n<")));

			BufferedWriter pomWriter = new BufferedWriter(new FileWriter(m_pomFile));

			//The rest of this is to format the pom so it isn't ugly
			String line;
			int indent = 0;
			while ((line = sr.readLine()) != null)
				{
				String tag = line.split("[> ]")[0];
				//System.out.println(tag);
				int indentChange = 0;

				if (s_indentMap.get(tag) != null)
					indentChange = s_indentMap.get(tag);

				if (indentChange == -1)
					indent --;

				for (int i = 0; i < indent; i ++)
					pomWriter.write('\t');

				pomWriter.write(line);
				pomWriter.write('\n');

				//Change the indent for the next tag
				if (indentChange == 1)
					indent ++;
				}

			pomWriter.flush();
			pomWriter.close();
			}
		catch (TransformerConfigurationException e)
			{
			throw new TablesawException(e);
			}
		catch (TransformerException e)
			{
			throw new TablesawException(e);
			}
		catch (IOException e)
			{
			throw new TablesawException(e);
			}
		catch (ParserConfigurationException e)
			{
			e.printStackTrace();
			}
		catch (SAXException e)
			{
			e.printStackTrace();
			}
		}

	@Override
	public Iterable<String> getTargets()
		{
		return Collections.singleton(m_pomFile.getPath());
		}

	public String getTarget()
		{
		return m_pomFile.getPath();
		}

	public PomRule addLicense(String name, String url, String distribution)
		{
		m_licenses.add(new Triple<String, String, String>(name, url, distribution));
		return this;
		}

	public PomRule addDeveloper(String id, String name, String email)
		{
		m_developers.add(new Triple<String, String, String>(id, name, email));
		return this;
		}

	public PomRule setPomArtifactId(String artifactId)
		{
		m_artifactId = artifactId;
		return this;
		}

	public PomRule setPomGroupId(String groupId)
		{
		m_groupId = groupId;
		return this;
		}

	public PomRule setPomVersion(String version)
		{
		m_version = version;
		return this;
		}

	public PomRule setPomPackaging(String packaging)
		{
		m_packaging = packaging;
		return this;
		}

	public PomRule setPomName(String name)
		{
		m_name = name;
		return this;
		}

	public PomRule setPomDescription(String description)
		{
		m_description = description;
		return this;
		}

	public PomRule setPomUrl(String url)
		{
		m_url = url;
		return this;
		}

	public PomRule setPomScmUrl(String scmUrl)
		{
		m_scmUrl = scmUrl;
		return this;
		}

	public PomRule setPomScmConnection(String scmConnection)
		{
		m_scmConnection = scmConnection;
		return this;
		}

	public PomRule setJavaVersion(String javaVersion)
		{
		m_javaVersion = javaVersion;
		return this;
		}
	}
