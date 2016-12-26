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
import tablesaw.rules.CopyRule;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.io.*;

/**
	This class encapsulates a Rule for a specifed target and is placed in the build
	queue.  The BuildAction tracks dependent build actions and provided means for
	notifying downstream actions that they can be built.
*/
class BuildAction extends Observable
		implements Observer
	{
	//private String m_target;
	private Rule m_targetRule;
	private BuildContext m_buildContext;
	private int m_dependencyCount;
	//private String m_absTarget;
	private BuildFileManager m_fileManager;
	private Set<String> m_newerDep;		//Only the dependencies that are newer then the target
	//private boolean m_multiTarget;
	private HashSet<BuildAction> m_dependantBuildActions;
	private Map<Class, DependencyAnnotations> m_annotations;
	
	public BuildAction(BuildFileManager fm, Rule targetRule, Map<Class, DependencyAnnotations> annotations)
		{
		m_buildContext = new BuildContext();
		m_fileManager = fm;
		m_targetRule = targetRule;
		m_dependencyCount = 0;
		//m_absTarget = target;
		m_newerDep = new HashSet<String>();
		
		/* if (targetRule instanceof PatternRule)
			m_multiTarget = ((PatternRule)targetRule).isMultiTarget();
		else
			m_multiTarget = false; */
			
		m_dependantBuildActions = new HashSet<BuildAction>();
		m_annotations = annotations;
		}
		
	/* public void addTarget(String target)
		{
		//System.out.println("Adding target "+target);
		m_targets.addLast(target);
		} */
		
	public Rule getTargetRule()
		{
		return (m_targetRule);
		}
		
	public void addNewerDependency(String dep)
		{
		m_newerDep.add(dep);
		//System.out.println("Adding newer dep "+dep+" to "+getTarget());
		}
		
	public Set<String> getNewerDependencies()
		{
		return (m_newerDep);
		}

	public boolean isBinding() { return (m_targetRule.isBinding()); }
		
	@Override
	public int hashCode()
		{
		// FIXME:
		//return (m_targets.getFirst().hashCode());
		return (0);
		}
		
	@Override
	public boolean equals(Object o)
		{
		boolean ret = false;
		
		if (o == this)
			return (true);
		
		if (o instanceof BuildAction)
			{
			BuildAction ba = (BuildAction)o;
			//If the rule is a TRule it can produce multiple targets
			//Need to compare to each one.
			// FIXME:
			ret = m_targetRule.equals(ba.m_targetRule);
			/* if (m_targetRule instanceof TRule)
				{
				List<String> ruleTargets = ((TRule)m_targetRule).getTargets();
				for (int I = 0; I < ruleTargets.size(); I++)
					if ((ret = Tablesaw.comparePaths(ba.getTarget(), ruleTargets.get(I))))
						break;
				}
			else if ((m_targetRule instanceof PatternRule)&&(m_multiTarget))
				{
				ret = ((PatternRule)m_targetRule).matchTarget(ba.getTarget());
				}
			else
				ret = Tablesaw.comparePaths(ba.getTarget(), getTarget()); */
				
			//System.out.println(((BuildAction)o).getTarget() + " == " + m_target);
			}
		return (ret);
		}
		
	public synchronized void waitForDependencies()
		{
		try
			{
			if (m_dependencyCount != 0)
				wait();
			}
		catch (InterruptedException ie) {}
		}
		
	public void update(Observable o, Object arg)
		{
		synchronized(this)
			{
			m_dependencyCount--;

			BuildAction other = (BuildAction)o;
			m_buildContext.combine(other.m_buildContext);
			
			//wake up thread if one is waiting on this action
			if (m_dependencyCount == 0)
				{
				//Set consumers on rule before processing build
				DependencyAnnotations annotations = m_annotations.get(m_targetRule.getClass());

				for (String type : annotations.getConsumesTypes())
					{
					Method consumeMethod = annotations.getConsumesMethod(type);
					//System.out.println("Found consume method "+consumeMethod.getName());

					//System.out.println(m_buildContext);
					for (RuleMethod ruleMethod : m_buildContext.getProvides(type))
						{
						try
							{
							//System.out.println("Calling consumes "+ruleMethod.getMethod().getName());
							consumeMethod.invoke(m_targetRule, ruleMethod.invoke());
							}
						catch (IllegalAccessException e)
							{
							e.printStackTrace();
							}
						catch (InvocationTargetException e)
							{
							e.printStackTrace();
							}
						}
					}

				notify();
				}
			}
		}
		
	public int getDependencyCount()
		{
		//Returns the number of outstanding dependencies
		return (m_dependencyCount);
		}
		
	/* public void setAbsoluteTarget(String absTarget)
		{
		m_absTarget = absTarget;
		}
		
	public String getAbsoluteTarget()
		{
		return (m_absTarget);
		} */
		
		
	public void addDependency(BuildAction ba)
		{
		if (m_dependantBuildActions.contains(ba))
			return;
			
		m_dependantBuildActions.add(ba);
		//Tablesaw.debugPrint("\t"+ba.getTarget());
		m_dependencyCount++;
		ba.addObserver(this);
		}
		
	public void addDependencies(BuildAction[] ba)
		{
		//this is done in a single thread, no need to synchronize
		//Tablesaw.debugPrint("Adding dependencies for "+getTarget());
		if (ba.length > 0)
			{
			for (int I = 0; I < ba.length; I++)
				{
				addDependency(ba[I]);
				}
			}
		}
		
	//Marks this BuildAction complete and notifies all dependents
	public void complete()
		{
		//Add provides to context before notifying
		DependencyAnnotations annotations = m_annotations.get(m_targetRule.getClass());

		if (annotations != null)
			{
			List<String> providesTypes = annotations.getProvidesTypes();

			for (String providesType : providesTypes)
				{
				//System.out.println("Adding provides "+providesType);
				m_buildContext.setProvides(providesType,
						new RuleMethod(m_targetRule, annotations.getProvidesMethod(providesType)));
				}
			}

		setChanged();
		notifyObservers();
		clearChanged();
		
		m_targetRule.buildComplete();
		
		/* CachedFile f;
		DependencyCache depCache = DependencyCache.getDependencyCache("");
		Map<String, Long> depMap = new HashMap<String, Long>();
		
		for (int I = 0; I < m_depList.length; I++)
			{
			f = m_make.getFile(m_depList[I]);
			depMap.put(f.getAbsolutePath(), new Long(f.getActualLastModified()));
			}
			
		//System.out.println("Writing cache for "+m_absTarget);
		depCache.cacheDependencyMap(m_absTarget, depMap); */
		}
		
	public String toString()
		{
		// FIXME:
		return ("BuildAction("+m_targetRule+")");
		//return ("BuildAction: "+m_targets.getFirst());
		}
		
	}
