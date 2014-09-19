package tablesaw.definitions;

import java.util.*;
import tablesaw.TablesawException;

import static tablesaw.util.Validation.*;

/*package*/ class Group extends PriorityOption
	{
	public static final String NAME = "name";
	private String m_name;
	private List<Option> m_options;
	
	public Group(String name)
		{
		super(name);
		m_options = new ArrayList<Option>();
		}
		
	//---------------------------------------------------------------------------
	public Object clone()
		{
		Group g = null;
		
		try
			{
			g = (Group)super.clone();
			}
		catch (CloneNotSupportedException cnse)
			{
			}
			
		g.m_options = new ArrayList<Option>();
		try
			{
			for (Option o : m_options)
				g.m_options.add((Option)o.clone());
			}
		catch (CloneNotSupportedException e)
			{}
			
		return (g);
		}
		
	//---------------------------------------------------------------------------
	public void addOption(Option option)
		{
		m_options.add(option);
		}
		
	//---------------------------------------------------------------------------
	public void setParams(Iterable<Object> params)
			throws TablesawException
		{
		Iterator<Object> pIt = params.iterator();
		Iterator<Option> oIt = m_options.iterator();
		
		while (pIt.hasNext())
			{
			if (!oIt.hasNext())
				throw new TablesawException("Incorrect number of parameters for group option '"+m_name+"', "+m_options.size()+" are required.", -1);
				
			String param = objectToString(pIt.next());
			oIt.next().setParam(param);
			}
			
		if (oIt.hasNext())
			throw new TablesawException("Incorrect number of parameters for group option '"+m_name+"', "+m_options.size()+" are required.", -1);
		}
		
	//---------------------------------------------------------------------------
	public String getOptionValue()
		{
		StringBuilder sb = new StringBuilder();
		for (Option o : m_options)
			sb.append(o.getOptionValue()).append(" ");
			
		return (sb.toString().trim());
		}
	}
