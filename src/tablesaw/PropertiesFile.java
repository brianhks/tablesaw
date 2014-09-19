/*
 * Copyright (c) 2004, Brian Hawkins
 * brianhks@activeclickweb.com
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free 
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details.
 *
 * You should have received a copy of the GNU General Public License along 
 * with this program; if not, write to the 
 * Free Software Foundation, Inc., 
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
 


package tablesaw;
 
import java.util.*;
import java.io.*;

class PropertiesFile extends Properties
	{
	String m_FileName;
	boolean m_nonFile;

	public PropertiesFile(String fileName)
		{
		m_nonFile = false;
		FileInputStream fReader;
		m_FileName = fileName;

		try
			{
			fReader = new FileInputStream(fileName);
			if (fReader != null)
				{
				this.load(fReader);
				fReader.close();
				}
			}
		catch(Exception e)
			{
			m_nonFile = true;
			//e.printStackTrace();
			}
			
		if (m_nonFile)
			{
			try
				{
				InputStream in = this.getClass().getResourceAsStream(fileName);
				if (in != null)
					{
					this.load(in);
					in.close();
					}
				}
			catch (IOException ioe)
				{
				System.out.println("Resource "+fileName+" failed");
				ioe.printStackTrace();
				}
			}
		}

//-------------------------------------------------------------------
	public void close()
			throws IOException
		{
		FileOutputStream fWriter;

		try
			{
			fWriter = new FileOutputStream(m_FileName);
			this.store(fWriter, "properties file");
			fWriter.close();
			}
		catch(Exception e)
			{
			throw new IOException();
			}

		}
	}

