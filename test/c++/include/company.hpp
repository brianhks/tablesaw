#ifndef __COMPANY_HPP__
#define __COMPANY_HPP__

#include <vector>
#include "employee.hpp"

class Company
	{
	private:
		std::vector<Employee> m_employees;
		
	public:
		Company();
		~Company();
		void addEmployee(Employee emp);
		void printEmployees();
	};

	
#endif //__COMPANY_HPP__




