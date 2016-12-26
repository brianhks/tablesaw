package tablesaw.addons.ivy;

import org.apache.ivy.core.module.descriptor.AbstractArtifact;
import org.apache.ivy.core.module.id.ArtifactRevisionId;
import org.apache.ivy.core.module.id.ModuleRevisionId;

import java.net.URL;
import java.util.Date;

/**
 Created by bhawkins on 5/5/14.
 */
public class PublishArtifact
	{
	private final PublishRule.IvyArtifact m_ivyArtifact;

	public PublishArtifact(PublishRule.IvyArtifact ivyArtifact)
		{
		m_ivyArtifact = ivyArtifact;
		}

	public PublishArtifact setName(String name)
		{
		m_ivyArtifact.setName(name);
		return (this);
		}

	public PublishArtifact setType(String type)
		{
		m_ivyArtifact.setType(type);
		return (this);
		}

	public PublishArtifact setExt(String ext)
		{
		m_ivyArtifact.setExt(ext);
		return (this);
		}

	public PublishArtifact setIsMetadata()
		{
		m_ivyArtifact.setMetadata(true);
		return (this);
		}

	public PublishArtifact addAttribute(String key, String value)
		{
		m_ivyArtifact.addAttribute(key, value);
		return (this);
		}
	}
