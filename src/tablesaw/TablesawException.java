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

public class TablesawException extends Exception
	{
	protected String m_description;
	private int m_retError;
	
	public TablesawException(String description)
		{
		this(description, -1);
		}
	
	public TablesawException(String description, int retError)
		{
		super(description);
		m_description = description;
		m_retError = retError;
		}
		
	public TablesawException(String description, Throwable t)
		{
		super(t);
		m_description = description+"\n"+t.getMessage();
		m_retError = -1;
		}
		
	public TablesawException(Throwable t)
		{
		super(t);
		m_description = t.getMessage();
		m_retError = -1;
		}
		
	public String getDescription()
		{
		return (m_description);
		}
		
	@Override
	public String getMessage()
		{
		return (m_description);
		}
		
	public int getReturnError()
		{
		return (m_retError);
		}
	}
