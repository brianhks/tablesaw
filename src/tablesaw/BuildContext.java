package tablesaw;

import java.util.*;

/**
 Created by bhawkins on 3/13/14.
 */
public class BuildContext
	{
	private final Map<String, List<RuleMethod>> m_providesMap;

	public BuildContext()
		{
		m_providesMap = new HashMap<String, List<RuleMethod>>();
		}

	public List<RuleMethod> getProvides(String name)
		{
		List<RuleMethod> ret = m_providesMap.get(name);
		if (ret == null)
			return Collections.EMPTY_LIST;
		else
			return ret;
		}

	private void addProvides(String name, RuleMethod method)
		{
		List<RuleMethod> list = m_providesMap.get(name);
		if (list == null)
			{
			list = new ArrayList<RuleMethod>();
			m_providesMap.put(name, list);
			}

		list.add(method);
		}

	public void setProvides(String name, RuleMethod method)
		{
		List<RuleMethod> list = new ArrayList<RuleMethod>();
		list.add(method);
		m_providesMap.put(name, list);
		}

	public void combine(BuildContext other)
		{
		for (String provideName : other.m_providesMap.keySet())
			{
			List<RuleMethod> methods = other.m_providesMap.get(provideName);
			for (RuleMethod method : methods)
				{
				addProvides(provideName, method);
				}
			}
		}

	public BuildContext clone()
		{
		BuildContext ret = new BuildContext();

		ret.combine(this);

		return ret;
		}

	@Override
	public String toString()
		{
		return "BuildContext{" +
				"m_providesMap=" + m_providesMap +
				'}';
		}
	}
