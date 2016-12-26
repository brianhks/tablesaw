package tablesaw;


public interface BuildEventListener
	{
	/**
		Called right before the primary target is set.  This is a good place for
		any last minute changes to the build rules that need to take place.
	*/
	public void setPrimaryTarget(Tablesaw make, String target);
	public void buildFailed(Tablesaw make, Exception e);
	public void buildSuccess(Tablesaw make, String target);
	}
