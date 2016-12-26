/*
, Brian Hawkins
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

/**
	AsyncProcess represents an background process that is started using
	Tablesaw.createAsyncProcess() method.
*/
public class AsyncProcess
	{
	private Tablesaw m_make;
	private String m_cwd;
	private String[] m_cmdArr;
	private String m_logFile;
	private String m_redirect;
	private Process m_process;
	private OutputStream m_out;
	private OutputStreamWriter m_writer;
	
	/*package*/ AsyncProcess(Tablesaw make, String cwd, String cmd)
			throws TablesawException
		{
		m_make = make;
		m_cwd = cwd;
		m_cmdArr = m_make.splitString(cmd);
		m_logFile = null;
		m_redirect = null;
		m_process = null;
		}
		
	/**
		Set a log file to copy all output to.  Output is sent to standard out as
		well as to the specified log file.
		@param file Log file to copy output to.
	*/
	public void setLogFile(String file)
		{
		m_logFile = file;
		}
		
	/**
		Redirect standard output to a file.  Useful if the output of the process
		tends to be too verbose for a build script.
		@param file The file name to redirect the output to.
	*/
	public void setRedirect(String file)
		{
		m_redirect = file;
		}
		
	/**
		Begin running the background process
	*/
	public void run()
			throws TablesawException
		{
		runAndWaitFor(null);
		}
		
	/**
		Begin running the process and wait for a message to be printed out before
		returning.  This is good for running services that take a few seconds to 
		start up such as Tomcat.  With this call you can specify some output message
		that needs to be printed out by the process before it returns.
		@param msg Message to wait for from the running process before returning.
	*/
	public void runAndWaitFor(String msg)
			throws TablesawException
		{
		StreamPipe stderr, stdout;

		
		if (m_make.getProperty("Tablesaw.verbose", "false").equals("true"))
			{
			if (m_cwd != null)
				System.out.print(m_cwd+" ");
				
			for (int I = 0; I < m_cmdArr.length; I++)
				{
				if (m_cmdArr[I].indexOf(' ') == -1)
					System.out.print(m_cmdArr[I]+" ");
				else
					System.out.print("\""+m_cmdArr[I]+"\" ");
				}
			System.out.println();
			}
		
		if (m_redirect == null)
			m_out = System.out;
		else
			{
			try
				{
				m_out = new FileOutputStream(m_make.file(m_redirect));
				}
			catch (FileNotFoundException fnfe)
				{
				throw new TablesawException("Cannot open "+m_redirect+" for output", -1);
				}
			}
			
		//verbose message
		
		try
			{
			m_process = Runtime.getRuntime().exec(m_cmdArr, m_make.getEnvArr(), m_make.file(m_cwd));
			
			m_writer = new OutputStreamWriter(m_process.getOutputStream());
			
			if (m_logFile == null)
				{
				stdout = new StreamPipe(m_process.getInputStream(), m_out, null, true, msg);
				stdout.startPipe();
				stderr = new StreamPipe(m_process.getErrorStream(), m_out, true);
				stderr.startPipe();
				//new StreamPipe(System.in, proc.getOutputStream());
				}
			else
				{
				stdout = new StreamPipe(m_process.getInputStream(), m_out, m_make.file(m_logFile), true, msg);
				stdout.startPipe();
				stderr = new StreamPipe(m_process.getErrorStream(), m_out, m_make.file(m_logFile), true);
				stderr.startPipe();
				}
			
			if (msg != null)
				stdout.waitForString();
			
			}
		catch (IOException ioe)
			{
			System.out.println("Tablesaw Error! Unable to run command!");
			System.out.println(ioe);
			Debug.print(ioe);
			System.exit(-1);
			}
		
		}
		
		
	/**
		Send a message to standard in of the running process.
		@param msg Message to send to standard in of the process.
	*/
	public void sendMessage(String msg)
			throws TablesawException
		{
		try
			{
			m_process.exitValue();
			throw (new TablesawException("Process has already exited before calling sendMessage.", -1));
			}
		catch(IllegalThreadStateException itse)
			{
			try
				{
				m_writer.write(msg);
				m_writer.flush();
				}
			catch (IOException ioe)
				{
				throw (new TablesawException("Unable to send \""+msg+"\" to process", -1));
				}
			}
		}
		
	//---------------------------------------------------------------------------
	/**
		Wait for the background process to finish before returning.
	*/
	public void waitForProcess()
			throws TablesawException
		{
		try
			{
			m_process.waitFor();
			
			if (m_redirect != null)
				{
				m_out.flush();
				m_out.close();
				}
			}
		catch (InterruptedException ie)
			{
			throw (new TablesawException(ie.toString(), -1));
			}
		catch (IOException ioe)
			{
			throw (new TablesawException(ioe.toString(), -1));
			}
		}
		
	//---------------------------------------------------------------------------
	/**
		Returns the exit code from the process.
	*/
	public int getExitCode()
		{
		return (m_process.exitValue());
		}
		
	/**
		Kill the background process.
	*/
	public void kill()
		{
		if (m_process == null)
			return;
		
		try
			{
			m_process.destroy();
			
			if (m_redirect != null)
				{
				m_out.flush();
				m_out.close();
				}
			}
		catch (IOException ioe)
			{
			}
		}
	}
