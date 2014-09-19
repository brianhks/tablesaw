#include <employee.hpp>

Employee::Employee(const char* name, int id)
	{
	m_name = name;
	m_id = id;
	}
	
Employee::~Employee()
	{
	}
	
std::string Employee::getName()
	{
	return (m_name);
	}
	
int Employee::getId()
	{
	return (m_id);
	}
	
