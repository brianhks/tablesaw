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
import tablesaw.Tablesaw;

import java.util.regex.*;
import java.io.*;

/*package*/ class Option extends PriorityOption
	{
	public static final String NAME = "name";
	public static final String PATTERN = "pattern";
	public static final String MODE = "mode";
	public static final String FIX_SLASH = "fix_slash";
	
	private boolean m_fixSlash;
	
	private Pattern m_pattern;
	private String m_value;
	private String m_param;

	
	
//==============================================================================
	public Option(String name, String value)
		{
		super(name);
		m_value = value;
		m_fixSlash = false;
		m_pattern = null;
		}
		
	//---------------------------------------------------------------------------
	public void setFixSlash(boolean fixSlash)
		{
		m_fixSlash = fixSlash;
		}
		
	//---------------------------------------------------------------------------
	public void setPattern(String pattern)
		{
		m_pattern = Pattern.compile(pattern);
		}
		
	//---------------------------------------------------------------------------
	public void setParam(String param)
		{
		m_param = param;
		}
		
	//---------------------------------------------------------------------------
	public String getOptionValue()
		{
		if (m_fixSlash)
			m_param = Tablesaw.fixPath(m_param);
		if (m_pattern == null)
			return (m_value);
		else
			{
			Matcher m = m_pattern.matcher(m_param);
			return (m.replaceAll(m_value));
			}
		}
		
	//---------------------------------------------------------------------------
	public Object clone()
			throws CloneNotSupportedException
		{
		return (super.clone());
		}

	//---------------------------------------------------------------------------		
	public String toString()
		{
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		
		pw.println("name: "+m_name);
		pw.println("fix_slash: "+m_fixSlash);
		pw.println("pattern: "+(m_pattern == null ? "null" : m_pattern.pattern()));
		pw.println("mode: "+m_mode);
		pw.println("value: "+m_value);
		pw.println("param: "+m_param);
		pw.close();
		
		return (sw.toString());
		}
	}
