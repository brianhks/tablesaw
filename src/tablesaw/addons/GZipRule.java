package tablesaw.addons;


import tablesaw.MakeAction;
import tablesaw.TablesawException;
import tablesaw.rules.AbstractRule;
import tablesaw.rules.Rule;
import tablesaw.util.Validation;

import java.io.*;
import java.util.Collections;
import java.util.zip.GZIPOutputStream;

public class GZipRule extends AbstractRule<GZipRule>
		implements MakeAction
	{
	private String m_sourceFile;
	private String m_targetFile;
	private boolean m_alwaysRun = false;

	public GZipRule()
		{
		this(null);
		}

	public GZipRule(String name)
		{
		super();
		m_name = name;

		setMakeAction(this);
		}

	public GZipRule setSource(String source)
		{
		m_sourceFile = source;
		addDepend(source);

		return (this);
		}

	public GZipRule setTarget(String target)
		{
		m_targetFile = target;

		return (this);
		}

	public void doMakeAction(Rule rule) throws TablesawException
		{
		File source = Validation.locateFileMustExist(m_make, m_sourceFile);

		byte[] buffer = new byte[1024];
		try
			{
			BufferedInputStream is = new BufferedInputStream(new FileInputStream(source));
			GZIPOutputStream zipOut = new GZIPOutputStream(new FileOutputStream(m_targetFile));

			System.out.println("Compressing "+m_sourceFile+" -> "+m_targetFile);

			int length = 0;
			while ((length = is.read(buffer)) != -1)
				{
				zipOut.write(buffer, 0, length);
				}

			is.close();
			zipOut.close();
			}
		catch (IOException e)
			{
			throw new TablesawException("Unable to create gzip file", e);
			}
		}

	public Iterable<String> getTargets()
		{
		return (Collections.singleton(m_targetFile));
		}

	public String getTarget()
		{
		return (m_targetFile);
		}

	public GZipRule alwaysRun()
		{
		m_alwaysRun = true;

		return (this);
		}

	public boolean needToRun() throws TablesawException
		{
		return (m_alwaysRun);
		}

	}
