package tablesaw;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 Created by bhawkins on 3/13/14.
 */
public class DependencyAnnotations
	{
	private Map<String, Method> m_consumesMethods;
	private Map<String, Method> m_providesMethods;

	public DependencyAnnotations()
		{
		m_consumesMethods = new HashMap<String, Method>();
		m_providesMethods = new HashMap<String, Method>();
		}

	public void addConsumesMethod(String name, Method method)
		{
		m_consumesMethods.put(name, method);
		}

	public List<String> getConsumesTypes()
		{
		return new ArrayList<String>(m_consumesMethods.keySet());
		}

	public Method getConsumesMethod(String type)
		{
		return m_consumesMethods.get(type);
		}

	public void addProvidesMethod(String name, Method method)
		{
		m_providesMethods.put(name, method);
		}

	public List<String> getProvidesTypes()
		{
		return new ArrayList<String>(m_providesMethods.keySet());
		}

	public Method getProvidesMethod(String type)
		{
		return (m_providesMethods.get(type));
		}
	}
