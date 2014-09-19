package tablesaw;

/**
 Created with IntelliJ IDEA.
 User: bhawkins
 Date: 1/23/13
 Time: 7:03 PM
 To change this template use File | Settings | File Templates.
 */
public class MissingClassException extends TablesawException
	{
	public MissingClassException(ClassNotFoundException classNotFoundException)
		{
		super(classNotFoundException);

		String missingClass = classNotFoundException.getMessage();
		if (missingClass.startsWith("org.apache.ivy"))
			setDescription("Ivy");
		else if (missingClass.startsWith("org.apache.commons.compress.archivers"))
			setDescription("Apache Commons Compress");
		else
			m_description = "Unable to load class "+missingClass;
		}

	private void setDescription(String jar)
		{
		StringBuilder sb = new StringBuilder();

		sb.append("It appears you are using code that is trying to reference the ").append(jar).append(" jar file.\n");
		sb.append("Add this jar to the directory where the Tablesaw jar is located and it will be\n");
		sb.append("automatically added to the classpath.");

		m_description = sb.toString();
		}
	}
