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
import java.util.regex.*;
import java.util.*;

class CDependencyParser
		implements DependencyParser, Serializable
	{		
	public boolean canRecurse()
		{
		return (true);
		}
		
	public String[] parseFile(File file)
			throws IOException
		{
		BufferedReader srcFile = new BufferedReader(new FileReader(file));
		String line;
		boolean inComment = false;
		int index;
		Pattern pattern = Pattern.compile("#\\s*include.+[<\"](.+)[>\"]");
		Matcher matcher;
		ArrayList dependency = new ArrayList();
		int startComment, endComment;
		
		while ((line = srcFile.readLine()) != null)
			{
			startComment = line.lastIndexOf("/*");
			endComment = line.lastIndexOf("*/");
			if (endComment > startComment)
				{
				if (inComment)
					inComment = false;
				
				line = line.substring(endComment + 2, line.length());
				}
	
			if (inComment)
				continue;
				
			line = line.trim();
			
			if (line.startsWith("#"))
				{
				//System.out.println(line);
				matcher = pattern.matcher(line);
				if (matcher.find())
					dependency.add(matcher.group(1));
				}
				
			if (startComment > endComment)
				inComment = true;
			}
			
		return ((String[])dependency.toArray(new String[0]));
		}
	}
