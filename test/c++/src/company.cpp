#include <company.hpp>
#include <iostream>

Company::Company()
	{
	}
	
Company::~Company()
	{
	}
	
void Company::addEmployee(Employee emp)
	{
	m_employees.push_back(emp);
	}
	
void Company::printEmployees()
	{
	for (unsigned int I = 0; I < m_employees.size(); I++)
		std::cout << m_employees[I].getName() << std::endl;
	}


	
