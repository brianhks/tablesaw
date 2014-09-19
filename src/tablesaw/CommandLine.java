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

class CommandLine
	{
	HashMap m_optionMap;
	String[] m_optionList;
	TreeSet m_boolOptionSet;
	TreeSet m_valueOptionSet;
	TreeSet m_invalidOptionSet;
	ArrayList m_nonOptions;
	boolean m_commandLineProcessed;
	
	private void processCommandLine()
		{
		Character option;
		if (m_commandLineProcessed)
			return;
		
		for (int I = 0;I < m_optionList.length;I++)
			{
			if (m_optionList[I].charAt(0) == '-')
				{
				option = new Character(m_optionList[I].charAt(1));
				if (m_boolOptionSet.contains(option))
					m_optionMap.put(option, "");
				else if (m_valueOptionSet.contains(option))
					{
					m_optionMap.put(option, m_optionList[++I]);
					}
				else
					m_invalidOptionSet.add(option);
				}
			else
				m_nonOptions.add(m_optionList[I]);
			}
		
		m_commandLineProcessed = true;
		}
	
	public CommandLine(String[] args)
		{
		m_optionList = args;
		m_commandLineProcessed = false;
		m_optionMap = new HashMap();
		m_boolOptionSet = new TreeSet();
		m_valueOptionSet = new TreeSet();
		m_invalidOptionSet = new TreeSet();
		m_nonOptions = new ArrayList();
		}
		
	public void setBooleanOptions(String options)
		{
		char[] optArr = options.toCharArray();
		
		for(int I = 0;I < optArr.length;I++)
			m_boolOptionSet.add(new Character(optArr[I]));
		
		m_commandLineProcessed = false;
		}
		
	public void setValueOptions(String options)
		{
		char[] optArr = options.toCharArray();
		
		for(int I = 0;I < optArr.length;I++)
			m_valueOptionSet.add(new Character(optArr[I]));
			
		m_commandLineProcessed = false;
		}
		
	public String[] getNonOptions()
		{
		processCommandLine();
		return ((String[])m_nonOptions.toArray(new String[0]));
		}
		
	public void validateCommandLine()
			//throws InvalidOptionException
		{
		
		}
		
	public boolean isSet(char option)
		{
		processCommandLine();
		return (m_optionMap.containsKey(new Character(option)));
		}
		
	public String getOptionValue(char option)
		{
		processCommandLine();
		return ((String)m_optionMap.get(new Character(option)));
		}
	}