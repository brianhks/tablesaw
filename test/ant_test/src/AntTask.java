
import org.apache.tools.ant.*;

public class AntTask extends Task
	{
	private String m_param;
	private int m_integer;

	public void setValue(String val)
		{
		m_param = val;
		}
		
	public void setInteger(int val)
		{
		m_integer = val;
		}
	
	@Override
	public void execute()
		{
		System.out.println("execute");
		System.out.println("I'm executing "+m_param+" "+m_integer);
		}
	}
