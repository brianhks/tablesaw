package tablesaw;

import tablesaw.rules.Rule;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 Created by bhawkins on 3/13/14.
 */
public class RuleMethod
	{
	private final Rule m_rule;
	private final Method m_method;


	public RuleMethod(Rule rule, Method method)
		{
		m_rule = rule;
		m_method = method;
		}

	public Object invoke() throws InvocationTargetException, IllegalAccessException
		{
		return m_method.invoke(m_rule);
		}

	public void invoke(Object arg) throws InvocationTargetException, IllegalAccessException
		{
		m_method.invoke(m_rule, arg);
		}

	public Method getMethod()
		{
		return m_method;
		}
	}
