package tablesaw.definitions;


/*package*/ abstract class PriorityOption
		implements Cloneable
	{
	protected String m_name;
	protected String m_mode;
	private int m_priority;
	
	
	public PriorityOption(String name)
		{
		m_name = name;
		m_mode = null;
		m_priority = 0;
		}
		
	public String getName() { return (m_name); }
	public void setMode(String mode) { m_mode = mode; }
	public String getMode() { return (m_mode); }
	public void setPriority(int priority) { m_priority = priority; }
	public int getPriority() { return (m_priority); }
	
	//---------------------------------------------------------------------------
	public Object clone() throws CloneNotSupportedException
		{
		return (super.clone());
		}
		
	//---------------------------------------------------------------------------
	public abstract String getOptionValue();
	}
