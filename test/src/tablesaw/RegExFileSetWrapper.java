package tablesaw;

import tablesaw.RegExFileSet;

import java.io.File;

public class RegExFileSetWrapper extends RegExFileSet
	{
	public RegExFileSetWrapper(String baseDir, String filePattern)
		{
		super(baseDir, filePattern);
		}
		
	public String getFileAction(java.io.File file, java.io.File dir)
		{
		return (getAction(file, dir).toString());
		}
	}
	
