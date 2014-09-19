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

import java.util.*;

/**
	Weak priority queue implimentation
	Needs work
*/
/*package*/ class PriorityQueue
	{
	private LinkedList<PriorityOption> m_queue;
	
	//---------------------------------------------------------------------------
	public PriorityQueue()
		{
		m_queue = new LinkedList<PriorityOption>();
		}
		
	//---------------------------------------------------------------------------
	public boolean contains(String name)
		{
		ListIterator<PriorityOption> li = m_queue.listIterator();
		
		while (li.hasNext())
			{
			PriorityOption opt = li.next();
			if (name.equals(opt.getName()))
				return (true);
			}
			
		return (false);
		}
		
	//---------------------------------------------------------------------------
	/**
		Removes all PriorityOptions that have the specified name
	*/
	public void remove(String name)
		{
		ListIterator<PriorityOption> li = m_queue.listIterator();
		
		while (li.hasNext())
			{
			PriorityOption opt = li.next();
			if (name.equals(opt.getName()))
				li.remove();
			}
		}
		
	//---------------------------------------------------------------------------
	public void insert(PriorityOption insertMe)
		{
		ListIterator<PriorityOption> li = m_queue.listIterator();
		PriorityOption comp;
		boolean inserted = false;
		
		while (li.hasNext())
			{
			comp = li.next();
			if (insertMe.getPriority() < comp.getPriority())
				{
				if (li.hasPrevious())
					{
					li.previous();
					li.add(insertMe);
					inserted = true;
					break;
					}
				else
					{
					m_queue.addFirst(insertMe);
					inserted = true;
					break;
					}
				}
			}
		
		//if not inserted before a node add to the end
		if (!inserted)
			m_queue.add(insertMe);
		}
		
	//---------------------------------------------------------------------------
	public Iterator<PriorityOption> iterator()
		{
		return (m_queue.iterator());
		}
	}
