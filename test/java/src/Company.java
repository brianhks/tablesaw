import java.util.*;


public class Company
	{
	private Map m_employees;
	
	public Company()
		{
		m_employees = new HashMap();
		}
		
	public void addEmployee(Employee emp)
		{
		m_employees.put(emp.getId(), emp);
		}
		
	public void printEmployees( )
		{
		Iterator it = m_employees.values().iterator();
		Employee emp;
		
		while (it.hasNext())
			{
			emp = (Employee)it.next();
			System.out.println(emp.getName());
			}
		
		}
	}
