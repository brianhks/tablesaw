package tablesaw.util;

import java.io.File;

import tablesaw.*;


public class Validation
	{
	public static String objectToString(Object o)
		{
		if (o == null)
			return ("null");
		else
			return (o.toString());
		}
		
	//---------------------------------------------------------------------------
	public static void fileMustExist(File file)
			throws TablesawException
		{
		if (!file.exists())
			{
			throw new MissingFileException(file);
			}
		}
		
	//---------------------------------------------------------------------------
	public static File locateFileMustExist(Tablesaw make, String file)
			throws TablesawException
		{
		if (file == null)
			throw new NullParameterException();

		File f = make.locateFile(file);
		if (f == null)
			throw new MissingFileException(file);
			
		if (!f.exists())
			throw new MissingFileException(f);

		return (f);
		}

	//---------------------------------------------------------------------------
	public static File fileMustExist(String file)
			throws TablesawException
		{
		if (file == null)
			throw new NullParameterException();

		File f = new File(file);
		if (!f.exists())
			throw new MissingFileException(f);

		return (f);
		}
		
	//---------------------------------------------------------------------------
	public static void notNull(Object obj)
			throws TablesawException
		{
		if (obj == null)
			throw new NullParameterException();
		}

	//---------------------------------------------------------------------------
	public static String getRequiredProperty(Tablesaw saw, String propertyName) throws MissingPropertyException
		{
		String value = saw.getProperty(propertyName);
		if ((value == null) || (value.length() == 0))
			{
			throw new MissingPropertyException(propertyName);
			}

		return (value);
		}
	}
