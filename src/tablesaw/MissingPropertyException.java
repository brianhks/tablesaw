package tablesaw;

/**
 Created by bhawkins on 5/5/14.
 */
public class MissingPropertyException extends ValidationException
	{
	public MissingPropertyException(String property)
		{
		super();

		m_description = "The property '"+property+"' must be set when using '"+getCallingRule()+"'";
		}
	}
