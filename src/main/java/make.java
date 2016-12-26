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

public class make
	{
	public static void main(String[] args)
		{
		/*try
			{
			bhutils.ClassLoaderWrapper loader = bhutils.ClassLoaderWrapper.getClassLoader();
			Class[] params = new Class[] { String[].class };
			Class make = loader.loadClass("Tablesaw");
			Method m = make.getMethod("main", params);
			
			m.invoke(null, new Object[] { args });
			}
		catch(Exception e) {}*/
		tablesaw.Tablesaw.main(args);
		}
	}
