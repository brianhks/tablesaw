package tablesaw.addons.ivy;

import org.apache.ivy.Ivy;
import org.apache.ivy.core.module.descriptor.AbstractArtifact;
import org.apache.ivy.core.module.id.ArtifactId;
import org.apache.ivy.core.module.id.ArtifactRevisionId;
import org.apache.ivy.core.module.id.ModuleId;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.publish.PublishOptions;
import org.apache.ivy.plugins.resolver.DependencyResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import tablesaw.MakeAction;
import tablesaw.Tablesaw;
import tablesaw.TablesawException;
import tablesaw.addons.java.JavaProgram;
import tablesaw.rules.AbstractSourceRule;
import tablesaw.rules.Rule;
import tablesaw.rules.SimpleRule;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

import static tablesaw.util.Validation.getRequiredProperty;

/**
 Created with IntelliJ IDEA.
 User: bhawkins
 Date: 3/28/14
 Time: 10:52 AM
 To change this template use File | Settings | File Templates.
 */
public class PublishRule  extends AbstractSourceRule<PublishRule>
		implements MakeAction
	{
	private static SimpleDateFormat s_mavenSnapshotDate = new SimpleDateFormat("yyyyMMdd.HHmmss");
	private static SimpleDateFormat s_mavenSnapshotUpdateDate = new SimpleDateFormat("yyyyMMddHHmmss");

	static
		{
		s_mavenSnapshotDate.setTimeZone(TimeZone.getTimeZone("GMT"));
		}


	private final File m_ivyFile;
	private final Ivy m_ivy;
	private final ResolveRule m_resolveRule;
	private final String m_resolverName;

	private boolean m_overwrite = false;

	private ModuleRevisionId m_moduleRevisionId;
	private Date m_publicationDate = new Date();
	private final List<IvyArtifact> m_ivyArtifacts = new ArrayList<IvyArtifact>();

	private String m_groupId;
	private String m_artifactId;
	private String m_version;


	public PublishRule(File ivyFile, String resolverName, ResolveRule resolveRule)
		{
		super();
		m_ivyFile = ivyFile;
		m_resolverName = resolverName;
		m_resolveRule = resolveRule;
		m_ivy = m_resolveRule.getIvyInstance();
		addDepend(resolveRule);
		setMakeAction(this);
		}

	private Element createNode(Document doc, String name, String value)
		{
		Element element = doc.createElement(name);
		element.setTextContent(value);
		return (element);
		}

	public PublishRule publishMavenMetadata()
		{
		return (publishMavenMetadata("maven-metadata.xml"));
		}

	public PublishRule publishMavenMetadata(String metadataFileName)
		{
		String buildFolder = m_make.getProperty(Tablesaw.PROP_BUILD_DIRECTORY);
		final String buildTarget = buildFolder+"/"+metadataFileName;

		SimpleRule mavenMetaRule = new SimpleRule()
				.addTarget(buildTarget)
				.addDepend(m_resolveRule)
				.addDepend(buildFolder)
				.alwaysRun()
				.setMakeAction(new MakeAction()
				{
				@Override
				public void doMakeAction(Rule rule) throws TablesawException
					{
					try
						{
						String versionTimestamp = s_mavenSnapshotDate.format(m_publicationDate);
						String updatedTimestamp = s_mavenSnapshotUpdateDate.format(m_publicationDate);

						String version = m_version != null ? m_version : getRequiredProperty(m_make, JavaProgram.PROGRAM_VERSION_PROPERTY);

						if (version.contains("-SNAPSHOT"))
							version = version.replace("-SNAPSHOT", "");

						DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
						DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

						Document document = dBuilder.newDocument();
						Element metadata = document.createElement("metadata");
						metadata.setAttribute("modelVersion", "1.1.0");
						document.appendChild(metadata);

						metadata.appendChild(createNode(document, "groupId",
								m_groupId != null ? m_groupId : getRequiredProperty(m_make, PomRule.GROUP_ID_PROPERTY)));

						metadata.appendChild(createNode(document, "artifactId",
								m_artifactId != null ? m_artifactId : getRequiredProperty(m_make, JavaProgram.PROGRAM_NAME_PROPERTY)));

						//Using the raw version as we may have removed -SNAPSHOT above and we want it here
						metadata.appendChild(createNode(document, "version",
								m_version != null ? m_version : getRequiredProperty(m_make, JavaProgram.PROGRAM_VERSION_PROPERTY)));

						Element versioning = document.createElement("versioning");
						metadata.appendChild(versioning);

						Element snapshot = document.createElement("snapshot");
						versioning.appendChild(snapshot);

						snapshot.appendChild(createNode(document, "timestamp",
								versionTimestamp));

						//Hard coding build number to one as we are not reading the existing meta file
						snapshot.appendChild(createNode(document, "buildNumber", "1"));

						versioning.appendChild(createNode(document, "lastUpdated", updatedTimestamp));


						Element snapshotVersions = document.createElement("snapshotVersions");
						versioning.appendChild(snapshotVersions);

						for (IvyArtifact artifact : m_ivyArtifacts)
							{
							//Skip the ivy type this will skip our maven-metadata.xml file
							if (artifact.getType().equals("ivy"))
								continue;

							Element snapshotVersion = document.createElement("snapshotVersion");
							snapshotVersions.appendChild(snapshotVersion);

							snapshotVersion.appendChild(createNode(document, "extension",
									artifact.getExt()));

							snapshotVersion.appendChild(createNode(document, "value",
									version + "-" + versionTimestamp + "-1"));

							snapshotVersion.appendChild(createNode(document, "updated",
									updatedTimestamp));
							}

						TransformerFactory tf = TransformerFactory.newInstance();
						Transformer transformer = tf.newTransformer();
						transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
						StringWriter writer = new StringWriter();
						transformer.transform(new DOMSource(document), new StreamResult(writer));
						String output = writer.getBuffer().toString();

						FileWriter fw = new FileWriter(buildTarget);
						fw.write(output);
						fw.flush();
						fw.close();
						}
					catch (ParserConfigurationException e)
						{
						e.printStackTrace();
						}
					catch (TransformerConfigurationException e)
						{
						e.printStackTrace();
						}
					catch (TransformerException e)
						{
						e.printStackTrace();
						}
					catch (IOException e)
						{
						e.printStackTrace();
						}
					}
				});


		addArtifact(buildTarget)
				.setName("maven-metadata")
				.setType("ivy")
				.setExt("xml");

		return (this);
		}

	@Override
	public void doMakeAction(Rule rule) throws TablesawException
		{
		PublishOptions po = new PublishOptions();

		DependencyResolver resolver = m_ivy.getSettings().getResolver(m_resolverName);
		if (resolver == null)
			throw new TablesawException("Unable to locate resolver '"+m_resolverName+"'");

		String org = m_groupId != null ? m_groupId : getRequiredProperty(m_make, PomRule.GROUP_ID_PROPERTY);
		String name = m_artifactId != null ? m_artifactId : getRequiredProperty(m_make, JavaProgram.PROGRAM_NAME_PROPERTY);
		String rev = m_version != null ? m_version : getRequiredProperty(m_make, JavaProgram.PROGRAM_VERSION_PROPERTY);

		String classifier = null;
		if (rev.contains("-SNAPSHOT"))
			{
			classifier = s_mavenSnapshotDate.format(m_publicationDate)+ "-1";// hard coding the build to always be one
			rev = rev.replace("-SNAPSHOT", "");
			}

		m_moduleRevisionId = ModuleRevisionId.newInstance(org, name, rev);

		try
			{
			resolver.beginPublishTransaction(m_moduleRevisionId, m_overwrite);

			for (IvyArtifact artifact : m_ivyArtifacts)
				{
				if (classifier != null)
					artifact.addAttribute("classifier", classifier);

				resolver.publish(artifact, m_make.file(artifact.getSourceFile()), m_overwrite);
				}

			resolver.commitPublishTransaction();
			}
		catch (IOException e)
			{
			try
				{
				resolver.abortPublishTransaction();
				}
			catch (IOException e1)
				{
				throw new TablesawException("Ivy publish failed", e1);
				}

			if (m_overwrite || !e.getMessage().contains("overwrite"))
				throw new TablesawException("Ivy publish failed", e);

			if (m_make.isVerbose())
				System.out.println("Ivy skipped publish of "+name+", artifact exists, overwrite == false");
			}

		}

	@Override
	public Iterable<String> getTargets()
		{
		return Collections.EMPTY_LIST;
		}


	public PublishArtifact addArtifact(String artifactFile)
		{
		addSource(artifactFile);
		IvyArtifact ia = new IvyArtifact(artifactFile);
		m_ivyArtifacts.add(ia);
		return (new PublishArtifact(ia));
		}


	public PublishRule setOverwrite(boolean overwrite)
		{
		m_overwrite = overwrite;
		return (this);
		}

	public PublishRule setGroupId(String groupId)
		{
		m_groupId = groupId;
		return this;
		}

	public PublishRule setArtifactId(String artifactId)
		{
		m_artifactId = artifactId;
		return this;
		}

	public PublishRule setVersion(String version)
		{
		m_version = version;
		return this;
		}

	//===========================================================================
	protected class IvyArtifact extends AbstractArtifact
		{
		private final String m_sourceFile;

		private String m_name;
		private String m_type;
		private String m_ext;
		private boolean m_isMetadata;

		private Map<String, String> m_attributes = new HashMap<String, String>();

		public IvyArtifact(String sourceFile)
			{
			super();

			m_sourceFile = sourceFile;
			}

		public String getSourceFile()
			{
			return (m_sourceFile);
			}

		public void setName(String name)
			{
			m_name = name;
			}

		public void setType(String type)
			{
			m_type = type;
			}

		public void setExt(String ext)
			{
			m_ext = ext;
			}

		public void setMetadata(boolean isMetadata)
			{
			m_isMetadata = isMetadata;
			}

		public void addAttribute(String key, String value)
			{
			m_attributes.put(key, value);
			}

		@Override
		public ModuleRevisionId getModuleRevisionId()
			{
			return m_moduleRevisionId;
			}

		@Override
		public Date getPublicationDate()
			{
			return m_publicationDate;
			}

		@Override
		public String getName()
			{
			if (m_name != null)
				return m_name;
			else
				return m_moduleRevisionId.getName();
			}

		@Override
		public String getType()
			{
			return m_type;
			}

		@Override
		public String getExt()
			{
			//TODO: if m_ext is null look at file extension
			return m_ext;
			}

		@Override
		public URL getUrl()
			{
			return null;
			}

		@Override
		public String[] getConfigurations()
			{
			return new String[0];
			}

		@Override
		public ArtifactRevisionId getId()
			{
			ModuleId mid = ModuleId.newInstance(m_moduleRevisionId.getOrganisation(), m_moduleRevisionId.getName());
			ArtifactId ai = new ArtifactId(mid, getName(), getType(), getExt());
			ArtifactRevisionId arid = new ArtifactRevisionId(ai, m_moduleRevisionId, m_attributes);
			return arid;
			}

		@Override
		public boolean isMetadata()
			{
			return m_isMetadata;
			}
		}
	}
