package tablesaw;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildLogger;
import org.apache.tools.ant.BuildListener;
import java.util.Vector;

/**
 * An ant task used to set the banner of the ant logger.  Some editors key off
 * the banner in front of the log message such as <code>[javac]</code> to identify
 * build errors.  You can use this task to change the banner
 */
public class AntPlugin extends Task 
	{
	private boolean m_banner;
	
	public AntPlugin() 
		{
		super();
		}
		
	public void setBanner(boolean banner) { m_banner = banner; }

	public void execute() throws BuildException 
		{
		Vector<BuildListener> listeners = (Vector<BuildListener>)getProject().getBuildListeners();
		for (BuildListener bl : listeners)
			{
			if (bl instanceof BuildLogger)
				{
				((BuildLogger)bl).setEmacsMode(!m_banner);
				}
			}
		}
	}

