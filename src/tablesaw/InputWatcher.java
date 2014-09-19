
package tablesaw;

import java.util.*;
import java.io.*;



class InputWatcher 
		implements Runnable
	{
	private static InputWatcher s_inputWatcher = null;
	
	private Set m_runningProcesses;
	
	private InputWatcher()
		{
		m_runningProcesses = new HashSet();
		Thread t = new Thread(this);
		t.start();
		}
	
//-------------------------------------------------------------------
	public static InputWatcher getInputWatcher()
		{
		if (s_inputWatcher == null)
			s_inputWatcher = new InputWatcher();
			
		return (s_inputWatcher);
		}
		
//-------------------------------------------------------------------
	public void addProcess(Process proc)
		{		
		synchronized(m_runningProcesses)
			{
			m_runningProcesses.add(proc);
			}
		}
		
//-------------------------------------------------------------------
	public void removeProcess(Process proc)
		{
		synchronized(m_runningProcesses)
			{
			m_runningProcesses.remove(proc);
			}
		}
		
//-------------------------------------------------------------------
	private Process getProcess()
		{
		Process proc = null;
		Iterator it;
		
		synchronized(m_runningProcesses)
			{
			it = m_runningProcesses.iterator();
			if (it.hasNext())
				proc = (Process)it.next();
			}
		
		return (proc);
		}
		
//-------------------------------------------------------------------
	public void killProcesses()
		{
		synchronized(m_runningProcesses)
			{
			Iterator it = m_runningProcesses.iterator();
			Process proc;
			
			while (it.hasNext())
				{
				proc = (Process)it.next();
				proc.destroy();
				}
			}
		}
		
//-------------------------------------------------------------------
	public void run()
		{
		InputStreamReader input = new InputStreamReader(System.in);
		int ch = 0;
		OutputStreamWriter output;
		Process proc;
		
		try
			{
			while ((ch = input.read()) != -1)
				{
				Thread.yield();
				proc = getProcess();
				if (proc != null)
					{
					output = new OutputStreamWriter(proc.getOutputStream());
					output.write(ch);
					output.flush();
					}
				}
			}
		catch(IOException ioe)
			{
			}
		}
	}
