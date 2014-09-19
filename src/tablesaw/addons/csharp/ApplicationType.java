package tablesaw.addons.csharp;


public enum ApplicationType
	{
	/**
		Console executable
	*/
	EXE("exe", "exe"),
	
	/**
		Windows executable
	*/
	WINEXE("winexe", "exe"),
	
	/**
		Library
	*/
	LIBRARY("library", "dll"),
	
	/**
		Module that can be added to another assembly
	*/
	MODULE("module", "mod");
	
	private String m_value;
	private String m_extension;
	
	ApplicationType(String value, String extension)
		{
		m_value = value;
		m_extension = extension;
		}
		
	public String toString()
		{
		return (m_value);
		}
		
	public String getExtension()
		{
		return (m_extension);
		}
	}
