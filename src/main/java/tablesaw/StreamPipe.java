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

import java.io.*;

class StreamPipe extends Thread
	{
	private BufferedReader   m_input;
	private BufferedWriter   m_output;
	private BufferedWriter   m_file;
	private boolean          m_activeThread;
	private boolean          m_multiThreaded;
	private boolean          m_stringFound;
	private String           m_waitForString;
	private String           m_banner;
	
	public StreamPipe(InputStream input, OutputStream output, File file, boolean multiThreaded)
			throws IOException
		{
		this(input, output, file, multiThreaded, null);
		}
		
	public StreamPipe(InputStream input, OutputStream output, File file, boolean multiThreaded, 
			String waitMessage)
			throws IOException
		{
		m_stringFound = false;
		m_waitForString = waitMessage;
		m_multiThreaded = multiThreaded;
		if (m_waitForString != null)
			m_multiThreaded = true;
		m_input = new BufferedReader(new InputStreamReader(input));
		m_output = new BufferedWriter(new OutputStreamWriter(output));
		if (file != null)
			m_file = new BufferedWriter(new FileWriter(file, true));
		else
			m_file = null;
		}
		
	public StreamPipe(InputStream input, OutputStream output, boolean multiThreaded)
			throws IOException
		{
		this(input, output, null, multiThreaded, null);
		}
		
	public void setBanner(String banner) { m_banner = banner; }
		
	public void startPipe()
		{
		m_activeThread = true;
		start();
		}
		
	public synchronized void run()
		{
		int data;
		String line;
		int ch;
		
		//System.out.println("Starting pipe");
		try
			{
			if (m_multiThreaded)
				{
				while ((line = m_input.readLine()) != null)
					{
					if (m_banner != null)
						m_output.write(m_banner, 0, m_banner.length());
					m_output.write(line, 0, line.length());
					m_output.newLine();
					m_output.flush();
					if (m_file != null)
						{
						if (m_banner != null)
							m_file.write(m_banner, 0, m_banner.length());
						m_file.write(line, 0, line.length());
						m_file.newLine();
						m_file.flush();
						}
					if ((m_waitForString != null)&&(!m_stringFound))
						{
						if (line.indexOf(m_waitForString) != -1)
							{
							synchronized (m_waitForString)
								{
								m_stringFound = true;
								m_waitForString.notifyAll();
								}
							}
						}
					}
				}
			else
				{
				while ((ch = m_input.read()) != -1)
					{
					m_output.write(ch);
					m_output.flush();
					if (m_file != null)
						{
						m_file.write(ch);
						}
					}
				}
			}
		catch(IOException ioe)
			{
			ioe.printStackTrace();
			}
		
		try
			{
			//Do not close the input and output streams as this can have
			//Bad effects if one of them is System.in or System.out
			if (m_file != null)
				m_file.close();
			}
		catch(IOException ioe){}
		//System.out.println("Pipe dead");
		m_activeThread = false;
		notifyAll();
		}
		
	public synchronized void waitForClose()
		{
		while (m_activeThread)
			{
			try
				{
				wait();
				}
			catch (InterruptedException ie) {}
			}
		}	
		
	public void waitForString()
			throws TablesawException
		{
		if (m_waitForString == null)
			throw (new TablesawException("waitForString called without specifying string to wait for", -1));
			
		synchronized (m_waitForString)
			{
			while (!m_stringFound)
				{
				try
					{
					m_waitForString.wait();
					}
				catch (InterruptedException ie)
					{}
				}
			}
		}		
	}