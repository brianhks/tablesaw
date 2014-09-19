package tablesaw.addons.java;

import tablesaw.TablesawException;

/**
	Interface to be used by objects that will compute a classpath at some future
	point in time.  A good example is a rule that needs to run before providing
	the classpath to the <code>JavaCRule</code>
*/
public interface ClasspathProvider
	{
	/**
		Returns the computed classpath.
	*/
	public Classpath getClasspath() throws TablesawException;
	}
