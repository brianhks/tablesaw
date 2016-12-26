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

public class ScriptException extends TablesawException
	{
	private int m_line;
	private String m_file;
	private String m_message;
	
	public ScriptException(int line, String file, String message)
		{
		super("", -1);
		m_line = line;
		m_file = file;
		m_message = message;
		}

	public ScriptException(String message)
		{
		super("", -1);
		m_line = -1;
		m_file = null;
		m_message = message;
		}
		
	public String getDescription()
		{
		return (toString());
		}
		
	public String toString()
		{
		StringBuffer sb = new StringBuffer();

		if (m_file != null)
			sb.append("Script error in file "+m_file+" at line "+m_line+"\n");

		sb.append(m_message);
		return (sb.toString());
		}
	}
