package tablesaw.ant;

import tablesaw.TablesawException;
import java.lang.reflect.*;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.IntrospectionHelper;

/**
	This class wraps a ant build object
*/
public class AbstractAntObject<T extends AbstractAntObject>
	{
	private String m_elementName;
	private Object m_antObject;
	private Project m_project;
	private IntrospectionHelper m_intHelper;
	
	public AbstractAntObject(String name)
		{
		m_elementName = name;
		}
		
	//---------------------------------------------------------------------------
	protected void setAntObject(Object obj)
		{
		m_antObject = obj;
		}
		
	//---------------------------------------------------------------------------
	protected void setAntProject(Project proj)
		{
		m_project = proj;
		}
		
	//---------------------------------------------------------------------------
	protected void init()
		{
		m_intHelper = IntrospectionHelper.getHelper(m_project, m_antObject.getClass());
		}
		
	//---------------------------------------------------------------------------
	/**
		Sets attribute on ant object
	*/
	public T set(String name, Object value)
			throws TablesawException
		{
		try
			{
			if (value instanceof String)
				{
				m_intHelper.setAttribute(m_project, m_antObject, name, (String)value);
				}
			else
				{
				Method m = m_intHelper.getAttributeMethod(name);
				m.invoke(m_antObject, value);
				}
			}
		catch (IllegalArgumentException iae)
			{
			// TODO: add better error
			throw new TablesawException(iae);
			}
		catch (Exception e)
			{
			throw new TablesawException("Error setting attribute '"+name+"' on '"+m_elementName+"'", e);
			}
		
		return ((T)this);
		}
		
	//---------------------------------------------------------------------------
	/**
		Calls an ant objects add... method, passing value.
	*/
	public AntObject add(String name, Object value)
			throws TablesawException
		{
		AntObject ret = new AntObject(name);
		ret.setAntProject(m_project);
		ret.setAntObject(value);
		ret.init();
		try
			{
			Method m = m_intHelper.getElementMethod(name);
			//System.out.println(m.toString());
			m.invoke(m_antObject, value);
			}
		catch (IllegalArgumentException iae)
			{
			throw new TablesawException(iae);
			}
		catch (Exception e)
			{
			throw new TablesawException("Error setting element '"+name+"' on '"+m_elementName+"'", e);
			}
			
		return (ret);
		}
		
	//---------------------------------------------------------------------------
	/**
		This looks for the appropriate add... method and then instantiates the 
		parameter object using the default constructor.
	*/
	public AntObject add(String name)
			throws TablesawException
		{
		AntObject ret = new AntObject(name);
		ret.setAntProject(m_project);
		
		try
			{
			IntrospectionHelper.Creator creator = m_intHelper.getElementCreator(
					m_project, "", m_antObject, name, null);
					
			ret.setAntObject(creator.create());
			creator.store();
			ret.init();
			}
		catch (IllegalArgumentException iae)
			{
			throw new TablesawException(iae);
			}
		catch (Exception e)
			{
			throw new TablesawException("Error setting element '"+name+"' on '"+m_elementName+"'", e);
			}
			
		return (ret);
		}
	}
