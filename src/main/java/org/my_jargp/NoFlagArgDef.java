package org.my_jargp;

public class NoFlagArgDef extends StringListDef
	{
	public NoFlagArgDef(String name, String desc)
		{
		super(' ', name, desc);
		}
		
	public NoFlagArgDef(String name)
		{
		super(' ', name, null);
		}
	}
