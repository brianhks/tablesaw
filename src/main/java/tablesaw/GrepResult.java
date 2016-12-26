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
 
/**
GrepResult is the returned from a call to Tablesaw.grep.  This contains the lines
and line numbers of the file that matches the grep pattern.
*/
public class GrepResult
	{
	private boolean m_found;
	private String[] m_results;
	private Integer[] m_lineNumbers;
	
	/*package*/ GrepResult(String[] results, Integer[] lineNumbers)
		{
		m_results = results;
		m_lineNumbers = lineNumbers;
		if (m_results.length > 0)
			m_found = true;
		else
			m_found = false;
		}
	
//-------------------------------------------------------------------
/**
	Returns true if any matches were found in the file.
*/
	public boolean isFound()
		{
		return (m_found);
		}
		
//-------------------------------------------------------------------
/**
	Returns an array of strings where each element of the array is the line
	in the file that matches the grep pattern.
*/
	public String[] getResults()
		{
		return (m_results);
		}

//-------------------------------------------------------------------
/**
	Returns an array of integers where each element in the array is the line
	number in the file that matches the grep pattern.
*/		
	public Integer[] getLineNumbers()
		{
		return (m_lineNumbers);
		}
	}
