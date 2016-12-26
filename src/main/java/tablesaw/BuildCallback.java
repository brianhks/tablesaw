package tablesaw;

/**
 Created with IntelliJ IDEA.
 User: bhawkins
 Date: 3/25/14
 Time: 11:12 AM

 Much like MakeAction but more generic.  This is for plugins to easily call
 back into script code to perform some action.
 */
public interface BuildCallback
	{
	public void doCallback(Object data)
			throws TablesawException;
	}
