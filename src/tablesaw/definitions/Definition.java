/*
 * Copyright (c) 2006, Brian Hawkins
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
 
package tablesaw.definitions;

import tablesaw.*;
import java.util.*;

import static tablesaw.util.Validation.*;

/**
Deliciously abstracted and infinitely configurable
*/
public class Definition
		implements Cloneable
	{
		
//==============================================================================
//Instance data

	private String                               m_name;            //Name of the definition
	//private Map<String, PriorityQueue<Option>>   m_commandOptionMap;   //This maps each command to a priority queue of options
	private Set<String>                          m_supportedModes;     //List of supported modes from the definition file
	private Map<String, String>                  m_properties;         //Available properties from the definition file
	//private Map<String, Option>                  m_optionsMap;         //Map of all options by name
	
	private Set<String>                          m_curModes;           //Currently sellected modes set by user
	
	
	
	//---------------------------------------------------------------------------
	//new ones
	private int m_optionCounter;
	private Map<String, List<PriorityOption>> m_optionMap;
	private PriorityQueue m_commandQueue;
	private String m_command;
	
	//---------------------------------------------------------------------------
	/*package*/ void addMode(String mode)
		{
		m_supportedModes.add(mode);
		}
		
	//---------------------------------------------------------------------------
	/*package*/ void addProperty(String key, String value)
		{
		m_properties.put(key, value);
		}
		
	//---------------------------------------------------------------------------
	/*package*/ void addOption(PriorityOption option)
			throws TablesawException
		{
		option.setPriority(m_optionCounter++);
		
		if (option.getName() == null)
			m_commandQueue.insert(option);
		else
			{
			List<PriorityOption> list = m_optionMap.get(option.getName());
			if (list == null)
				{
				list = new ArrayList<PriorityOption>();
				m_optionMap.put(option.getName(), list);
				}
			
			list.add(option);
			/* PriorityOption oldPo = m_optionMap.put(option.getName(), option);
			if (oldPo != null)
				{
				Debug.print(oldPo.toString());
				Debug.print(option.toString());
				throw new TablesawException("Duplicate options defined for "+option.getName()+" in definition "+m_name, -1);
				} */
			}
		}
		
	//---------------------------------------------------------------------------
	/*package*/ Definition(String name, String command)
		{
		m_optionCounter = 0;
		
		m_name = name;
		m_command = command;
		m_supportedModes = new HashSet<String>();
		m_properties = new HashMap<String, String>();
		m_optionMap = new HashMap<String, List<PriorityOption>>();
		m_commandQueue = new PriorityQueue();
		
		m_curModes = new HashSet<String>();
		}
		
	//---------------------------------------------------------------------------
	/**
		Copy constructor
	*/
	public Object clone()
		{
		Definition def = null;
		
		try
			{
			def = (Definition)super.clone();
			}
		catch (CloneNotSupportedException cnse)
			{
			}
		
		//These are the only two members that are modified in the build
		//script so each definition class needs its own copies
		def.m_curModes = new HashSet<String>();
		def.m_curModes.addAll(m_curModes);
		
		def.m_commandQueue = new PriorityQueue();
		Iterator<PriorityOption> it = m_commandQueue.iterator();
		while (it.hasNext())
			def.m_commandQueue.insert(it.next());
		
		return (def);
		}
		
	//---------------------------------------------------------------------------
	/**
		This clears the set modes and sets the current mode to the value given.
		The mode defines what options are activated in the compile command.
	*/
	public Definition setMode(String mode)
			throws TablesawException
		{
		m_curModes.clear();
		if (!m_supportedModes.contains(mode))
			throw new TablesawException(mode+" mode is not supported by the "+m_name+" definition.", -1);
			
		m_curModes.add(mode);
		
		return (this);
		}
		
	public boolean isModeSet()
		{
		return (m_curModes.size() != 0);
		}
	
	//---------------------------------------------------------------------------
	public Definition setMode(Object... modes)
			throws TablesawException
		{
		if ((modes.length == 1) && (modes[0] instanceof Iterable))
			return (setMode((Iterable<Object>)modes[0]));
		else
			return (setMode(Arrays.asList(modes)));
		}

	//---------------------------------------------------------------------------
	/**
		This clears the set modes and sets the current mode to the values given.
		The mode defines what options are activated in the compile command.
	*/		
	public Definition setMode(Iterable<Object> modes)
			throws TablesawException
		{
		m_curModes.clear();
		for (Object mode : modes)
			{
			String m = objectToString(mode);
			if (!m_supportedModes.contains(m))
				throw new TablesawException(m+" mode is not supported by the "+m_name+" definition.", -1);
				
			m_curModes.add(m);
			}
			
		return (this);
		}
	
	//---------------------------------------------------------------------------
	/**
		Clears a set option
		@param name Name of the option to clear.
	*/
	public Definition clear(String name)
		{
		m_commandQueue.remove(name);
		return (this);
		}
		
	//---------------------------------------------------------------------------
	/**
		Check if an option is set
	*/
	public boolean isSet(String name)
		{
		return (m_commandQueue.contains(name));
		}
		
	//---------------------------------------------------------------------------
	/**
		This clears any option of the same name and then sets the option spaecified
		@param name Name of the option to add.
		@param value Value to give to the option
	*/
	public Definition set(String name, String value)
			throws TablesawException
		{
		clear(name);
		add(name, value);
		return (this);
		}
		
	//---------------------------------------------------------------------------
	/**
		Activates the option and adds it to the list of options.  Calling this multiple
		times will result in multiple options showing up in the command.
		@param name Name of the option to add.
		@param value Value to give to the option
	*/
	public Definition add(String name, String value)
			throws TablesawException
		{
		List<PriorityOption> listPo = m_optionMap.get(name);
		if (listPo == null)
			throw new TablesawException("Option '"+name+"' cannot be found in definition '"+m_name+"'");
		
		for (PriorityOption po : listPo)
			{
			if (!(po instanceof Option))
				throw new TablesawException("'"+name+"' is not a single option", -1);
			
			try
				{
				//Get a clean copy
				po = (PriorityOption)po.clone();
				}
			catch (CloneNotSupportedException e)
				{
				}
			
			((Option)po).setParam(value);
			
			m_commandQueue.insert(po);
			}
		return (this);
		}
		
	//---------------------------------------------------------------------------
	/**
		This clears any option of the same name and then sets the option spaecified
		@param name Name of the option to add.
		@param values Value to give to the option
	*/
	public Definition set(String name, Object... values)
			throws TablesawException
		{
		clear(name);
		add(name, values);
		return (this);
		}
		
	//---------------------------------------------------------------------------
	public Definition add(String name, Object... values)
			throws TablesawException
		{
		if ((values.length == 1) && (values[0] instanceof Iterable))
			return (add(name, (Iterable<Object>)values[0]));
		else
			return (add(name, Arrays.asList(values)));
		}
		
	//---------------------------------------------------------------------------
	/**
		This clears any option of the same name and then sets the option spaecified
		@param name Name of the option to add.
		@param values Value to give to the option
	*/
	public Definition set(String name, Iterable<Object> values)
			throws TablesawException
		{
		clear(name);
		add(name, values);
		return (this);
		}
		
	//---------------------------------------------------------------------------
	/**
		This method is equivilant to calling <code>addOption</code> for each 
		value in values
		@param name Name of the option to activate
		@param values Values to set on the option
	*/
	public Definition add(String name, Iterable<Object> values)
			throws TablesawException
		{
		for (Object s : values)
			add(name, objectToString(s));
			
		return (this);
		}
		
	//---------------------------------------------------------------------------
	/**
		Activates the option by the name given.
		@param name Name of the option to activate
	*/
	public Definition set(String name)
			throws TablesawException
		{
		return (add(name, (String)null));
		}
		
		
	//---------------------------------------------------------------------------
	/**
		@param name name of the group option
		@param values list of values one for each option in the group
	*/
	public Definition addGroup(String name, Object... values)
			throws TablesawException
		{
		if ((values.length == 1) && values[0] instanceof Iterable)
			return (addGroup(name, (Iterable<Object>)values[0]));
		else
			return (addGroup(name, Arrays.asList(values)));
		}
		
	//---------------------------------------------------------------------------
	public Definition addGroup(String name, Iterable<Object> values)
			throws TablesawException
		{
		List<PriorityOption> listPo = m_optionMap.get(name);
		if (listPo == null)
			throw new TablesawException("Option '"+name+"' cannot be found in definition '"+m_name+"'");
			
		for (PriorityOption po : listPo)
			{
			if (!(po instanceof Group))
				throw new TablesawException("'"+name+"' is not a group option", -1);
			
			if (po == null)
				throw new TablesawException("Cannot find option '"+name+"'", -1);
				
			try
				{
				//Get a clean copy
				po = (PriorityOption)po.clone();
				}
			catch (CloneNotSupportedException e)
				{
				}
			
			((Group)po).setParams(values);
			
			m_commandQueue.insert(po);
			}
		return (this);
		}
		
	//---------------------------------------------------------------------------	
	/**
		Returns a property that was specified in the property tag in the definition file.
	*/
	public String getProperty(String name)
		{
		return (m_properties.get(name));
		}
	
	//---------------------------------------------------------------------------
	/**
		Returns a command string that consists of all activated options for the command.
	*/
	public String getCommand()
		{
		StringBuffer sb = new StringBuffer();
		
		sb.append(m_command);
		sb.append(" ");
		
		Iterator<PriorityOption> li = m_commandQueue.iterator();
		while (li.hasNext())
			{
			PriorityOption opt = li.next();
			//System.out.println(opt);
			
			if ((opt.getMode() == null)||(m_curModes.contains(opt.getMode()))) 
				{
				sb.append(opt.getOptionValue());
				sb.append(" ");
				}
			}
			
		return (sb.toString());
		}
		
	//---------------------------------------------------------------------------
	/**
		Returns the name of the definition
	*/
	public String getName()
		{
		return (m_name);
		}
		
	//---------------------------------------------------------------------------
	public String toString()
		{
		StringBuilder sb = new StringBuilder();
		sb.append(m_name).append(": ").append(m_command).append("\n");

		sb.append("  Modes:");

		for (String supportedMode : m_supportedModes)
			sb.append(" ").append(supportedMode);

		sb.append("\n  Properties:\n");

		for (String key : m_properties.keySet())
			sb.append("    ").append(key).append("=").append(m_properties.get(key)).append("\n");

		sb.append("  Options:\n");

		for (String option : m_optionMap.keySet())
			{
			sb.append("    ").append(option).append("\n");
			}

		/*Collection<List<PriorityOption>> values = m_optionMap.values();
		
		for (List<PriorityOption> list : values)
			{
			for (PriorityOption po : list)
				{
				sb.append("  ").append(po.toString());
				sb.append("\n");
				}
			}*/
		
		return (sb.toString());
		}
	}
	
