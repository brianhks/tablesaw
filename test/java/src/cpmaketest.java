import java.io.*;







public class cpmaketest
	{
	/**
	Javadoc comment
	*/
	public static void main(String args[])
		{
		Company comp = new Company();
		
		comp.addEmployee(new Employee("John Doe", new Integer(123)));
		comp.addEmployee(new Employee("Abraham Lincon", new Integer(231)));
		
		System.out.print("Enter name: ");
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		try
			{
			comp.addEmployee(new Employee(reader.readLine(), new Integer(111)));
			}
		catch (IOException ioe) {}
		
		comp.printEmployees();
		}
	}
