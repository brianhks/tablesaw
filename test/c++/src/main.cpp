
#include <stdio.h>
#include "employee.hpp"
#include "company.hpp"


int main()
	{
	setvbuf(stdout, 0, _IONBF, 0);
	Company comp;
	char name[128];
		
	comp.addEmployee(Employee("John Doe", 123));
	comp.addEmployee(Employee("Abraham Lincon", 231));
	
	printf("Enter name: ");
	scanf("%s", name);
	
	comp.addEmployee(Employee(name, 111));
	
	comp.printEmployees();
	
	return (0);
	}

	

