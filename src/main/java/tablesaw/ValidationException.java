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

import tablesaw.rules.Rule;

public abstract class ValidationException extends TablesawException
	{
	protected String getCallingMethod()
		{
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		
		return (stack[4].getMethodName());
		}

	protected String getCallingRule()
		{
		String ret = "unknown";
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();

		for (int I = 1; I < stack.length; I++)
			{
			if (stack[I].getClass().isAssignableFrom(Rule.class))
				{
				ret = stack[I].getClassName();
				break;
				}
			}

		return (ret);
		}
	
	public ValidationException()
		{
		super("", -1);
		}
	}
