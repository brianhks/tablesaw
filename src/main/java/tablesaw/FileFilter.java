package tablesaw;

/**
	Interface for providing custom filters to AbstractFileSet
*/
public interface FileFilter
	{
	/**
		@return Return true or false depending on whether the file should or 
			should not be included in the file set
	*/
	public boolean filter(String baseDir, String file);
	}
