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
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

/**
Information for this parser comes from 
http://java.sun.com/docs/books/vmspec/2nd-edition/html/ClassFile.doc.html

*/
class JavaDependencyParser
		implements DependencyParser, Serializable
	{	
	private short getShort(FileInputStream fis)
			throws IOException
		{
		ByteBuffer buf = ByteBuffer.allocate(2);
		buf.order(ByteOrder.BIG_ENDIAN);
		fis.getChannel().read(buf);
		buf.rewind();
		return (buf.getShort());
		}
		
	private byte[] read(FileInputStream fis, int readSz)
			throws IOException
		{
		byte[] buf = new byte[readSz];
		fis.read(buf);
		return (buf);
		}
		
	public boolean canRecurse()
		{
		return (false);
		}
		
	public String[] parseFile(File file)
			throws IOException
		{
		int magic;
		short majorVer;
		short minVer;
		short tableSz;
		byte tag;
		short strSz;
		String className;
		Map tableStrs = new HashMap();
		Stack classEntries = new Stack();
		ArrayList dependency = new ArrayList();
		ByteBuffer header = ByteBuffer.allocate(10);
		FileInputStream fis = new FileInputStream(file);
		//FileChannel fc = fis.getChannel();

		header.order(ByteOrder.BIG_ENDIAN);
		
		fis.getChannel().read(header);
		header.rewind();
		magic = header.getInt();
		
		if (magic != 0xCAFEBABE)
			return (new String[0]);
			
		majorVer = header.getShort();
		minVer = header.getShort();
		if ((majorVer != 0) || (minVer < 46))
			return (new String[0]);
		
		//If we got here the file is good and we can start reading in the table
		//We are going to read the constant_pool for class names that are used in 
		//this class file.
		tableSz = header.getShort(); //constant_pool_count
		for (int I = 1; I < tableSz; I++)
			{
			//Watch for end of stream -1
			tag = (byte)fis.read();
			switch (tag)
				{
				case 1: //This is a constant string.  Some of the strings are class names
					strSz = getShort(fis);
					tableStrs.put(new Integer(I), new String(read(fis, strSz)));
					break;
				case 7: //Class entries refer to a string within the constant table
					classEntries.push(new Integer(getShort(fis)));
					break;
				case 8:
					fis.skip(2);
					break;
				case 3:
				case 4:
				case 9:
				case 10:
				case 11:
				case 12:
					fis.skip(4);
					break;
				case 5:
				case 6:
					fis.skip(8);
					break;
				default:
					{
					Debug.print("JavaDependencyParser: Unknown tag %s at %d", tag, fis.getChannel().position());
					//Stop reading the header as we don't know the format
					I = tableSz;
					break;
					}
				
				}
			}
		
		fis.close();
		
		while (!classEntries.empty())
			{
			className = (String)tableStrs.get((Integer)classEntries.pop());
			if (className != null)
				{
				//System.out.println(className);
				dependency.add(className+".java");
				}
			}
		
		return ((String[])dependency.toArray(new String[0]));
		}
	}
