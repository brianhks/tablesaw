/*
 * Copyright (c) 2005, Brian Hawkins
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

class BuildFile extends File
	{
	String m_relativePath;
	
	public BuildFile(File cwd, String child)
		{
		super(cwd, child);
		m_relativePath = child;
		}
		
	public static BuildFile createBuildFile(File cwd, String parent, String child)
		{
		BuildFile ret;
		
		if (cwd != null)
			ret = new BuildFile(new File(parent), child);
		else if (parent == null)
			ret = new BuildFile(cwd, child);
		else
			{
			String relChild = parent+separator+child;
			ret = new BuildFile(cwd, relChild);
			}
			
		return (ret);
		}
		
	public String getRelativePath()
		{
		return (m_relativePath);
		}
	}
