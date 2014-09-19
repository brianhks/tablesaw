//
// GZipRuleTests.java
//
// Copyright 2013, NextPage Inc. All rights reserved.
//

package tablesaw.addons;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import tablesaw.Tablesaw;
import tablesaw.TablesawException;

public class GZipRuleTests
	{
	private Tablesaw m_make;

	@Before
	public void setupMake()
			throws TablesawException
		{
		m_make = new Tablesaw();
		m_make.setProperty(Tablesaw.PROP_CACHE_FILE, "build/.ziprulecache");
		m_make.init();
		}

	@After
	public void cleanup()
		{
		m_make.close();
		}

	@Test
	public void firstTest() throws Exception
		{
		GZipRule gr = new GZipRule("gzip");
		gr.setSource("test/testheader.h");
		gr.setTarget("build/testheader.h.gz");
		gr.alwaysRun();

		//Debug.setDebug(true);
		m_make.buildTarget("gzip");
		//Debug.setDebug(false);
		}

	}
