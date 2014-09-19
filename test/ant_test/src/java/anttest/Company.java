package anttest;

import java.util.*;


public class Company
	{
	private Map<Integer, Employee> m_employees;
	
	public Company()
		{
		m_employees = new HashMap<Integer, Employee>();
		}
		
	public void addEmployee(Employee emp)
		{
		m_employees.put(emp.getId(), emp);
		}
		
	public void printEmployees( )
		{
		Iterator<Employee> it = m_employees.values().iterator();
		Employee emp;
		
		while (it.hasNext())
			{
			emp = it.next();
			System.out.println(emp.getName());
			}
		
		}
	}
