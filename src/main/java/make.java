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

import tablesaw.util.TablesawClassLoader;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;

import static java.lang.ClassLoader.getSystemClassLoader;

public class make
	{
	public static void main(String[] args)
		{
		try
			{
			ClassLoader sysLoader = getSystemClassLoader();

			String tablesawPath = sysLoader.getResource("tablesaw/Tablesaw.class").getPath();

			int bangIndex = tablesawPath.indexOf('!');

			if (bangIndex != -1)
				{
				//This is expecting to find the file within a jar
				tablesawPath = tablesawPath.substring(5, bangIndex);
				}

			TablesawClassLoader loader = new TablesawClassLoader(new URL[] { new File(tablesawPath).toURL() }, sysLoader);
			Class[] params = new Class[] { String[].class };
			Class make = loader.loadClass("tablesaw.Tablesaw");
			Method m = make.getMethod("main", params);

			Thread.currentThread().setContextClassLoader(loader);

			m.invoke(null, new Object[] { args });
			}
		catch(Exception e)
			{
			e.printStackTrace();
			}

		//tablesaw.Tablesaw.main(args);
		}
	}
