package tablesaw.ant;

import tablesaw.MakeAction;
import tablesaw.TablesawException;
import tablesaw.rules.Rule;
import tablesaw.Tablesaw;

import java.util.Properties;
import java.io.IOException;
import java.io.InputStream;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.Location;

/**
	Class for compatibility with ant tasks
*/
public class AntTask extends AbstractAntObject<AntTask>
		implements MakeAction
	{
	public static final String ANT_PROJECT = "AntProject";
	private static final Object s_lock = new Object();
	
	private Tablesaw m_make;
	private Task m_task;
	private Project m_project;
	
	
	//---------------------------------------------------------------------------
	private void getProject()
		{
		synchronized(s_lock)
			{
			m_project = (Project)m_make.getObject(ANT_PROJECT);
			if (m_project == null)
				{
				m_project = new Project();
				m_make.setObject(ANT_PROJECT, m_project);
				
				DefaultLogger logger = new DefaultLogger();
				logger.setOutputPrintStream(System.out);
				logger.setErrorPrintStream(System.err);
				logger.setMessageOutputLevel(Project.MSG_INFO);
				logger.setEmacsMode(true);
				
				m_project.addBuildListener(logger);
				m_project.init();
				}
			}
		}
		
		
	//---------------------------------------------------------------------------
	/**
		Can either be task name or a class name
	*/
	public AntTask(String task)
			throws TablesawException
		{
		super(task);
		m_make = Tablesaw.getCurrentTablesaw();
		
		String className = null;
		if (task.indexOf('.') == -1) //No dots, must be ant task
			{
			className = isAntTask(task);
			if (className == null)
				className = task;
				//throw new TablesawException("Unable to find ant task '"+task+"'");
			}
		else
			className = task;
			
		ClassLoader cl = m_make.getClass().getClassLoader();
		
		try
			{
			Class<Task> taskClass = (Class<Task>)cl.loadClass(className);
			m_task = taskClass.newInstance();
			}
		catch (ClassNotFoundException cnfe)
			{
			throw new TablesawException(cnfe);
			}
		catch (InstantiationException ie)
			{
			throw new TablesawException(ie);
			}
		catch (IllegalAccessException iae)
			{
			throw new TablesawException(iae);
			}
			
		getProject();
		super.setAntObject(m_task);
		super.setAntProject(m_project);
		super.init();
		
		m_task.setProject(m_project);
		m_task.setLocation(Location.UNKNOWN_LOCATION);
		}
		
	//---------------------------------------------------------------------------
	/**
		returns class name if it is ant task otherwise returns null
	*/
	private String isAntTask(String task)
		{
		String className = null;
		
		try
			{
			ClassLoader cl = m_make.getClass().getClassLoader();
			InputStream is = cl.getResourceAsStream("org/apache/tools/ant/taskdefs/defaults.properties");
			
			Properties prop = new Properties();
			prop.load(is);
			
			className = (String)prop.get(task);
			}
		catch (IOException ioe)
			{
			}
			
		return (className);
		}
		
	//---------------------------------------------------------------------------
	public void doMakeAction(Rule rule)
		{
		execute();
		}
		
	//---------------------------------------------------------------------------
	public void execute()
		{
		m_task.execute();
		}
	}
