package tablesaw;


/**
	The purpose of this class is to print debug messages
*/
public class Debug
	{
	private Debug() {}
	
	private static boolean s_debug = false;
	private static int s_indent = 0;
	
	public static void indent()
		{
		s_indent ++;
		}
	
	public static void popIndent()
		{
		s_indent --;
		}
	
	//---------------------------------------------------------------------------
	public static boolean isDebug()
		{
		return (s_debug);
		}
	
	//---------------------------------------------------------------------------
	public static void setDebug(boolean debug)
		{
		s_debug = debug;
		}
		
	//---------------------------------------------------------------------------
	public static void print(String msg)
		{
		print(s_indent, msg);
		}
		
	//---------------------------------------------------------------------------
	public static void print(int indent, String msg)
		{
		if (s_debug)
			{
			for (int I = 0; I < indent; I++)
				System.out.print("  ");
				
			System.out.println(msg);
			}
		}
		
	//---------------------------------------------------------------------------
	public static void print(String format, Object... args)
		{
		print(s_indent, format, args);
		/* if (s_debug)
			{
			System.out.format(format, args);
			} */
		}
		
	//---------------------------------------------------------------------------
	public static void print(int indent, String format, Object... args)
		{
		if (s_debug)
			{
			for (int I = 0; I < indent; I++)
				System.out.print("  ");
				
			System.out.format(format, args);
			System.out.println();
			}
		}
		
	//---------------------------------------------------------------------------
	public static void print(Throwable t)
		{
		if (s_debug)
			{
			// TODO: pretty this up a bit
			t.printStackTrace();
			}
		}
	}
