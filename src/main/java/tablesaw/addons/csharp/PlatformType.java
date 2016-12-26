package tablesaw.addons.csharp;


public enum PlatformType
	{
	/**
		32 bit platform
	*/
	X86("x86"),
	
	/**
		64 bit platform
	*/
	X64("x64"),
	
	/**
		Itanium (does anyone really use this??
	*/
	ITANIUM("Itanium"),
	
	/**
		Any cpu.  Code will be compiled by JIT compiler
	*/
	ANYCPU("anycpu");
	
	private String m_value;
	PlatformType(String value)
		{
		m_value = value;
		}
		
	public String toString()
		{
		return (m_value);
		}
	}
