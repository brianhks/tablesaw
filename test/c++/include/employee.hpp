#ifndef __EMPLOYEE_HPP__
#define __EMPLOYEE_HPP__

#include <string>
#include <utility>

class Employee
	{
	private:
		std::string m_name;
		int m_id;
		
	public:
		Employee(const char* name, int id);
		~Employee();
		std::string getName();
		int getId();
	};


#endif //__EMPLOYEE_HPP__




