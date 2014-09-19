package anttest;


public class Employee
	{
	private String m_name;
	private Integer m_id;
	
	
	public Employee(String name, Integer id)
		{
		m_name = name;
		m_id = id;
		}
		
	public String getName()
		{
		return (m_name);
		}
		
	public Integer getId()
		{
		return (m_id);
		}
	}
