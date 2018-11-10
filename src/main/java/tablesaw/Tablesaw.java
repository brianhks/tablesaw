/*
 * Copyright (c) 2006, Brian Hawkins
 * brianhks@activeclickweb.com
 * 
    Copyright [yyyy] [name of copyright owner]

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
 
package tablesaw;
 
import java.util.regex.*;
import java.util.*;
import java.io.*;
import java.net.*;
import java.lang.reflect.*;
import org.my_jargp.*;

import java.awt.Toolkit;
import tablesaw.definitions.*;

import tablesaw.interpreters.*;
import tablesaw.rules.CleanRule;
import tablesaw.rules.DefinitionsRule;
import tablesaw.rules.HelpRule;
import tablesaw.rules.AntBuildRule;
import tablesaw.addons.java.Classpath;
import tablesaw.util.TablesawClassLoader;


/**
	General Comments
*/

public class Tablesaw
	{
	/**
	Name of the tablesaw properties file.
	*/
	public static final String TABLESAW_PROPERTIES = "tablesaw.properties";
	
	/**
	Property controls the processing of pattern rules.  When set to on dependencies
	created by a pattern rule must exist or have a rule to create them.
	Default (off)
	*/
	/* public static final String PROP_PATTERN_RULE_STRICT = "tablesaw.patternRuleStrict"; */
	
	/**
	Property controls debug output, set to either "on" or "off"<br/>
	Default (off)
	*/
	public static final String PROP_DEBUG = "tablesaw.debug";
	
	/**
	Property turns on the beep notify when build is done, set to "on" or "off"<br/>
	Default (off)
	*/
	public static final String PROP_FINISH_NOTIFY = "tablesaw.finishNotify";
	
	/**
	Property controls if dependencies are looked for in known file types, set to "on" or "off"<br/>
	Default (on)
	*/
	public static final String PROP_DEPENDENCY_CHECK = "tablesaw.dependencyCheck";
	
	/**
	Property controls the number of threads to use when building a target<br/>
	Default (1)
	*/
	public static final String PROP_THREAD_COUNT = "tablesaw.threadCount";
	
	/**
	Property controls if the input watcher is on or off.  The input watcher looks for
	user input while building.  Good for unit test where user input is required.<br/>
	Default (on)
	*/
	public static final String PROP_INPUT_WATCHER = "tablesaw.inputWatcher";
	
	/**
	Property sets the location of the cache file.<br/>
	Default (.)
	*/
	public static final String PROP_CACHE_FILE = "tablesaw.cacheFile";
	
	/**
	Property controls verbose output of Tablesaw.<br/>
	Default (off)
	*/
	public static final String PROP_VERBOSE = "tablesaw.verbose";
	
	/**
	Property controls multi thread output when running with one thread.  
	Multi thread output prints output from builds one line at a time so messages are
	not badly interleaved<br/>
	Default (off)
	*/
	public static final String PROP_MULTI_THREAD_OUTPUT = "tablesaw.multiThreadOutput";
	
	/*Defaults to 'on' for non windows OS's.  When set to on and the console
	supports ansi color escaping then the output from builds show up in pretty
	colors.
	public static final String PROP_ANSI_CONSOLE = "tablesaw.ansiConsole";
	*/

	/**
	Property controls the copying of file permissions when copying files on Linux. 
	File copy is much faster if this is off.<br/>
	Default (on)
	*/
	//public static final String PROP_COPY_PERMISSIONS = "tablesaw.copyPermissions";
	
	/**
	This property if set to "on" will cause the java compiler to use absolute
	paths.  This is more ant like so some IDE's can parse the output and link
	to the files when errors occure.
	*/
	public static final String PROP_LONG_FILE_NAMES = "tablesaw.longFileNames";
	
	public static final String PROP_VALUE_ON = "on";
	public static final String PROP_VALUE_OFF = "off";
	
	/**
	This property specifies the directory to load additional support jar files
	from.  If the value is not set the directory where tablesaw.jar resides is used.
	*/
	public static final String PROP_SUPPORT_LIB_DIR = "tablesaw.supportLibDir";


	/**
	 Directory into which all build atrifacts should be placed.
	 */
	public static final String PROP_BUILD_DIRECTORY = "tablesaw.build_directory";
	

	private ArrayList                 m_rules;
	private MakeEngine                m_engine;
	private BuildFileManager          m_fileManager;
	private ScriptInterpreter         m_interpreter;
	private String                    m_defaultTarget;
	private List<BuildEventListener>  m_eventListeners;
	
	private Set                       m_buildCache;	//targets that have already been checked and do not need to be built
	private int                       m_threadCount;			//Number of threads to use for building
	private LinkedList                m_threadQueue;	//Threads waiting for dependencies
	private int                       m_waitingThreadCnt;		//Number of threads waiting in build queue for a dependency
	private int                       m_activeThreadCnt;		//Number of threads working on the build queue.  This may be less then m_threadCount as the queue empties
	private Properties                m_properties;
	private Map<String, Object>       m_objects;         //Generic store of objects stored on this instance of tablesaw.
	private TablesawException m_makeException;
	private boolean                   m_verbose;
	private boolean                   m_buildInProgress;
	private String                    m_currentTarget;
	private String                    m_primaryTarget;
	private String                    m_buildFile;
	private InputWatcher              m_inputWatcher;
	private ArrayList                 m_autoCleanDirs;		//Directories set with directory rule, used by autocleans
	private boolean                   m_modifiedEnv;
	//private DependencyCache           m_depCache;
	private File                      m_workingDir;		//This is the directory in which the build file is located
	private Map                       m_scriptObjects;	//Map of objects to be added to the script namespace
	private LinkedList                m_multiTargetBuildActions;	//list of multi target build actions in the build queue
	private LinkedList                m_asyncProcesses;   //List of async processes that need to be shut down.
	private String                    m_banner;  //Banner that is added to exec process output (like ant)
	private Set<File>                 m_sourceFiles;  //A set of source files used for continuous build checking
	private DefinitionManager         m_definitionManager = null;
	private Classpath                 m_antClasspath;
	private Set<File>                 m_includedScripts;   //Set of scripts that have been included,  used primarily by includeOnce
	
	private static class Counter
		{
		private int m_count;
		public Counter()
			{
			m_count = 1;
			}
			
		public void increment() { m_count ++; }
		public int getCount() { return (m_count); }
		}
	
	private static class CommandLine
		{
		public boolean verbose;
		public boolean help;
		public boolean notify;
		public boolean debug;
		public String buildFile;
		public int threadCount;
		public List defines;
		public List targets;
		public Float continuous;
		public String banner;
		public String properties;
		
		public CommandLine()
			{
			//Set default options
			verbose = false;
			help = false;
			notify = false;
			debug = false;
			buildFile = null;
			threadCount = 1;
			defines = new ArrayList();
			targets = new ArrayList();
			continuous = null;
			banner = null;
			properties = null;
			}
		}
		
	private static final ParameterDef[] PARAMETERS =
		{
		new BoolDef('v', "verbose"),
		new BoolDef('?', "help"),
		new BoolDef('a', "notify"),
		new BoolDef('d', "debug"),
		new StringDef('f', "buildFile"),
		new IntDef('t', "threadCount"),
		new StringListDef('D', "defines"),
		new NoFlagArgDef("targets"),
		new FloatDef('c', "continuous"),
		new StringDef('b', "banner"),
		new StringDef('p', "properties")
		};
	
	private static final int DO_NOT_BUILD = 0;
	private static final int UNBUILT_DEPENDENCIES = 1;
	private static final int OUT_OF_DATE = 2;
	
	
	//===========================================================================
	/**
		The thread local data is designed like a stack
	*/
	private static MyThreadLocal s_tlMakeList = new MyThreadLocal();
			
	private static class MyThreadLocal extends ThreadLocal<LinkedList<Tablesaw>>
		{
		@Override
		protected synchronized LinkedList<Tablesaw> initialValue()
			{
			return (new LinkedList<Tablesaw>());
			}
			
		public void push(Tablesaw make)
			{
			get().addFirst(make);
			}
			
		public Tablesaw peek()
			{
			return (get().getFirst());
			}
			
		public void pop()
			{
			get().removeFirst();
			}
		}

	private static File findBuildFile(String directory)
		{
		File f = new File(directory, "build.bsh");
		if (!f.exists())
			f = new File(directory, "build.py");

		if (!f.exists())
			f = new File(directory, "build.gvy");

		if (!f.exists())
			f = new File(directory, "build.groovy");

		if (!f.exists())
			f = new File(directory, "build.js");

		if (!f.exists())
			f = new File(directory, "build.rb");

		return (f);
		}

	//---------------------------------------------------------------------------
	/**
		Returns the current instance of <code>Tablesaw</code> from off of the thread
		local data.  This is provided as an easy mechanism for accessing the current
		make instance from within the build script
	*/
	public static Tablesaw getCurrentTablesaw()
		{
		return (s_tlMakeList.peek());
		}
		
	//---------------------------------------------------------------------------
	/**
	*/
	public static void printErr(String msg)
		{
		if (System.getProperty("os.name").contains("Linux"))
			{
			System.out.print((char)27 + "[31;1m");
			System.out.println(msg);
			System.out.print((char)27 + "[m");
			}
		else
			{
			System.out.println(msg);
			}
		}
		
		
	//===========================================================================
	
/**
	Creates an instance of Tablesaw and process the targets on the command line.
	The options main takes are:<br/>
	-v Verbose mode.  This will echo every command passed to exec and every file
	copy<br/>
	-f <file> Build file. If no file is specified "build.bsh" is the default.<br/>
	-t <num> Number of threads to use.  This is the number of threads to use
	when building a target.<br/>
	After the options are the list of targets to build.
	<p>
	Property files.  Tablesaw loads properties from three locations and makes them
	available to the script via the getProperty methods.  First the system
	properties are read.  Second a tablesaw.properties file is read if it exists.
	Third an env.properties file is read if it exists.  In order to gain access
	to the environment variables the last properties file is used.  To use this
	file you will need to echo the environment to this file before calling Tablesaw.<br/>
	An example of this would be to write a script as follows<br/>
	<code>(windows)<br/>
	set > env.properties<br/>
	java -jar tablesaw.jar<br/>
	</code>
	or<br/>
	<code>(linux)<br/>
	env > env.properties<br/>
	java -jar tablesaw.jar<br/>
	</code>
	Then for neetness tablesaw will delete the env.properties file when it is done.<p>
	
	Properties that effect tablesaw<br/>
	(note the underscore versions are for linux envoronment variable compatibility)<br/>
	tablesaw.threadCount or tablesaw_threadCount - Setting this property will tell
	tablesaw the number of threads to use while building a target.
	
*/
	public static void main(String[] args)
		{
		long startTime;
		long stopTime;
		CommandLine cl = new CommandLine();
		Tablesaw make = null;
		int err = 0;
		
		ArgumentProcessor proc = new ArgumentProcessor(PARAMETERS);
		
		proc.processArgs(args, cl);
		
		if (cl.help)
			{
			printHelp();
			System.exit(0);
			}
		
		try
			{			
			if (cl.buildFile == null)
				{
				File f = findBuildFile(null);

				if (!f.exists())
					f = findBuildFile(getTablesawPath());

				if (!f.exists())
					{
					printErr("No build file specified");
					System.exit(1);
					}
				else
					cl.buildFile = f.getPath();
				}
				
			if (cl.debug)
				Debug.setDebug(true);
			
			if (cl.continuous != null)
				{
				if (cl.targets.size() > 1)
					{
					printErr("Continuous build only works with one target");
					System.exit(1);
					}
					
				boolean defaultTarget = true;
				if (cl.targets.size() == 1)
					defaultTarget = false;
				
				ContinuousBuild cb = new ContinuousBuild(cl.buildFile, 
						(String)(cl.targets.size() == 1 ? cl.targets.get(0) : null), 
						(int)(cl.continuous.floatValue() * 1000.0));
				cb.setDefines(cl.defines);
				cb.rebuild();
				
				cb.run();
				}
			else
				{
				startTime = System.currentTimeMillis();
				//targets = cl.getNonOptions();
				if (cl.targets.size() > 0)
					{
					Iterator it = cl.targets.iterator();
					while (it.hasNext())
						{
						String target = (String)it.next();
						make = new Tablesaw();
						make.getTablesawPath();
						if (cl.properties != null)
							make.loadPropertiesFile(cl.properties);
						addDefines(make, cl.defines);
						make.init();
						if (cl.threadCount != 1)
							make.setThreadCount(cl.threadCount);
						make.setBanner(cl.banner);
						make.setVerbose(cl.verbose);
						make.processBuildFile(cl.buildFile);
						make.buildTarget(target);
						
						//make.close();
						}
					}
				else
					{
					make = new Tablesaw();
					if (cl.properties != null)
						make.loadPropertiesFile(cl.properties);
					addDefines(make, cl.defines);
					make.init();
					if (cl.threadCount != 1)
						make.setThreadCount(cl.threadCount);
					make.setBanner(cl.banner);
					make.setVerbose(cl.verbose);
					make.processBuildFile(cl.buildFile);
					make.buildDefaultTarget();
					//make.close();
					}
					
				stopTime = System.currentTimeMillis();
				long totalTime = stopTime - startTime;
				long seconds = totalTime / 1000;
				long minutes = seconds / 60;
				long hours = minutes / 60;
				seconds -= minutes * 60;
				minutes -= hours * 60;
				Object pargs[] = new Object[] {new Long(hours), new Long(minutes),
						new Long(seconds)};
				System.out.println(Printf.print("Build time: %02d:%02d:%02d", pargs));
				//System.out.println("Build time: "+hours+":"+minutes+":"+seconds);
				}
			}
		catch (TablesawException cpme)
			{
			printErr(cpme.getDescription());
			err = cpme.getReturnError();
			if (cl.debug)
				cpme.printStackTrace();
			//System.exit(cpme.getReturnError());
			}
		catch (Exception e)
			{
			System.out.println(e.getMessage());
			if (cl.debug)
				e.printStackTrace();
			}
			
		if (make != null)
			make.close();
			
		//Clean up the environment properties file
		File env = new File("env.properties");
		if (env.exists())
			env.delete();
			
		
			
		if (make != null && (cl.notify||(make.getProperty(PROP_FINISH_NOTIFY, "off").equals("on"))))
			{
			Toolkit.getDefaultToolkit().beep();
			/* System.out.write(7);
			System.out.flush(); */
			}
					
		//System.exit is required to kill the InputWatcher thread
		System.exit(err);
		}

//-------------------------------------------------------------------
	/*package*/ static void addDefines(Tablesaw make, List defines)
		{
		String[] keyValue;
		Iterator it = defines.iterator();
		while (it.hasNext())
			{
			keyValue = ((String)it.next()).split("=");
			if (keyValue.length == 1)
				{
				make.setProperty(keyValue[0], "");
				Debug.print("Set property "+keyValue[0]+"=");
				}
			else
				{
				make.setProperty(keyValue[0], keyValue[1]);
				Debug.print("Set property "+keyValue[0]+"="+keyValue[1]);
				}
			}
		}
		
//-------------------------------------------------------------------
	private static void printHelp()
		{
		System.out.println("Cross Platform Make Utility (version = " + Version.getVersion() +" build = "+Version.getBuild()+")");
		System.out.println("Copyright (C) 2015, Brian Hawkins");
		System.out.println("Licensed under GNU General Public License\n");
		System.out.println("Tablesaw Command line help.");
		System.out.println("Usage: java -jar tablesaw.jar [-v][-d] [-D <key=value>] [-f <build file>] [-t <thread count>] [-c <seconds>] [<targets>]");
		System.out.println("   -v : Verbose output");
		System.out.println("   -f : Build file to process");
		System.out.println("   -t : Number of threads to use");
		System.out.println("   -d : Print debug messages");
		System.out.println("   -a : System beep when build is done");
		System.out.println("   -D : Add a define to the Tablesaw properties");
		System.out.println("   -c : Continuous rebuild (parameter is decimal number of seconds to wait between rebuild ie 1.5)");
		System.out.println("   -p : Properties file to include");
		
		}
		
	//---------------------------------------------------------------------------
	public static String getVersion()
		{
		return (Version.getVersion()+" b"+Version.getBuild());
		}
		
//===================================================================
//-------------------------------------------------------------------
	public ScriptInterpreter getScriptInterpreter()
		{
		return (m_interpreter);
		}
		
	//---------------------------------------------------------------------------
	public void setScripInterpreter(ScriptInterpreter si)
		{
		m_interpreter = si;
		}
		
	//---------------------------------------------------------------------------
	public void addBuildEventListener(BuildEventListener listener)
		{
		m_eventListeners.add(listener);
		}
		
	//---------------------------------------------------------------------------
	/**
		None of the targets or sources are processed until the script is finishd
		processing in its entirety
	*/
	public void addRule(tablesaw.rules.Rule rule)
		{
		m_engine.addRule(rule);
		}
		
	//---------------------------------------------------------------------------
	/**
		Locates the rule for the specified target.
		@param target Can either be the name of a rule or a target created from
		the rele
	*/
	public tablesaw.rules.Rule findRule(String target)
			throws TablesawException
		{
		return (m_engine.findTargetRule(target));
		}
		
//------------------------------------------------------------------------------
	private int getActiveThreadCnt()
		{
		return (m_activeThreadCnt);
		}
		
//-------------------------------------------------------------------
	/*package*/ String[] getEnvArr()
		{
		Enumeration e = m_properties.propertyNames();
		String key;
		ArrayList env = new ArrayList();
		
		if (!m_modifiedEnv)
			return (null);
		
		while (e.hasMoreElements())
			{
			key = (String)e.nextElement();
			env.add(key+"="+m_properties.getProperty(key));
			}
			
		return ((String[])env.toArray(new String[0]));
		}
		
//-------------------------------------------------------------------
	private void loadArrayList(ArrayList v, String[] s)
		{
		if (s.length > 0)
			{
			for (int I = 0; I < s.length; I++)
				{
				if (s[I].length() != 0)					
					v.add(s[I]);
				//System.out.print(s[I] + " ");
				}
			}
		}
		
//-------------------------------------------------------------------
	/*private boolean targetOutOfDate(String target, long targetTime, File dependency)
		{
		Long mod;
		boolean ret;
		//System.out.print("targetOutOfDate "+target+" with "+dependency.getPath());
		Map<String, Long> cacheMap = m_depCache.getDependencyCacheMap(target);
		
		//Compares cached modified time if there is one otherwise is the dependency newer then the target
		if (((cacheMap != null) && ((mod = cacheMap.get(dependency.getAbsolutePath())) != null) &&
				(mod.longValue() != dependency.lastModified())) || (dependency.lastModified() > targetTime))
			ret = true;
		else
			ret = false;
			
		//System.out.println(" " + ret + " " + (dependency.lastModified() > targetTime));
		return (ret);
		}*/

	public void loadPropertiesFile(String propertiesFile) throws TablesawException
		{
		File propFile = new File(propertiesFile);
		if (propFile.exists())
			{
			PropertiesFile pf = new PropertiesFile(propFile.getAbsolutePath());
			loadProperties(pf);
			}
		else
			{
			throw new TablesawException("Unable to locate "+propertiesFile);
			}
		}

//-------------------------------------------------------------------
	private void loadPropertiesFile()
			throws TablesawException
		{
		//First load internal tablesaw.properties file
		try
			{
			Properties prop = new Properties();
			prop.load(this.getClass().getClassLoader()
					.getResourceAsStream("tablesaw/"+ TABLESAW_PROPERTIES));

			loadProperties(prop);
			}
		catch (IOException ioe)
			{
			throw new TablesawException(ioe);
			}

		//Now load tablesaw.properties from the same directory as the Tablesaw.jar file
		File loadPropFile = new File(getTablesawPath(), TABLESAW_PROPERTIES);
		if (loadPropFile.exists())
			{
			PropertiesFile pf = new PropertiesFile(loadPropFile.getAbsolutePath());
			loadProperties(pf);
			}

		
		//Finally load from the working dir if it exists
		File propFile = new File(m_workingDir, TABLESAW_PROPERTIES);
		if (propFile.exists())
			{
			PropertiesFile pf = new PropertiesFile(propFile.getAbsolutePath());
			loadProperties(pf);
			}
		}
		
//---------------------------------------------------------------------
	private void loadProperties(Properties prop)
		{
		Enumeration en = prop.propertyNames();
		String name;
		
		while (en.hasMoreElements())
			{
			name = (String)en.nextElement();
			
			setProperty(name, prop.getProperty(name));
			}
		}

//---------------------------------------------------------------------
	private void loadProperties(Reader input)
		{
		BufferedReader br;
		String line;
		String[] split;
		
		try
			{
			br = new BufferedReader(input);
			while ((line = br.readLine()) != null)
				{
				split = line.split("=", 2);
				setProperty(split[0], split[1]);
				}
			br.close();
			}				
		catch (IOException e) 
			{
			Debug.print(e);
			}
		}
		
//---------------------------------------------------------------------
	private void loadEnvProperties()
		{
		File f = new File("env.properties");
		Process proc;		
		String cmd;
		
		if (f.exists())
			{
			m_modifiedEnv = true;
			try
				{
				loadProperties(new FileReader(f));
				}
			catch (FileNotFoundException fnfe) {}
			}
		else
			{
			Iterator<Map.Entry<String, String>> env = System.getenv().entrySet().iterator();
			
			while (env.hasNext())
				{
				Map.Entry<String, String> entry = env.next();
				setProperty(entry.getKey(), entry.getValue());
				}
				
			/* if (System.getProperty("os.name").startsWith("Windows"))
				cmd = "cmd /D /C set";
			else
				cmd = "printenv";

			try
				{
				proc = Runtime.getRuntime().exec(cmd);
			
				loadProperties(new InputStreamReader(proc.getInputStream()));
			
				proc.waitFor();
				}
			catch (Exception e)
				{
				Debug.print(e.getMessage());
				} */
			}
		}
		
//---------------------------------------------------------------------
	private int readUnixPermissions(File file)
		{
		Process proc;
		BufferedReader read;
		String fileInfo;
		char[] chars;
		int perm = 0;
		int user = 0, group = 0, world = 0;
		
		try
			{
			String[] cmd = new String[] {"bash", "-c", "ls -l "+file.getPath()};
			proc = Runtime.getRuntime().exec(cmd);
			read = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			fileInfo = read.readLine();
			}
		catch (IOException ioe)
			{
			return (-1);
			}
		
		if (fileInfo != null)
			{
			//This ensures we can alwasy read and write to files we copy
			user |= 0x4;
			user |= 0x2;
			if (fileInfo.charAt(3) == 'x')
				user |= 0x1;
			
			if (fileInfo.charAt(4) == 'r')
				group |= 0x4;
			if (fileInfo.charAt(5) == 'w')
				group |= 0x2;
			if (fileInfo.charAt(6) == 'x')
				group |= 0x1;
				
			if (fileInfo.charAt(7) == 'r')
				world |= 0x4;
			if (fileInfo.charAt(8) == 'w')
				world |= 0x2;
			if (fileInfo.charAt(9) == 'x')
				world |= 0x1;
				
			perm = (user * 100) + (group * 10) + world;

			return (perm);
			}
		else
			return (-1);
		}
		
//---------------------------------------------------------------------
	public void duplicateProperties(File src, File dest)
		{
		Process proc;
		dest.setLastModified(src.lastModified());
		
		if (System.getProperty("os.name").equals("Linux"))
			{
			int perm = readUnixPermissions(src);
			
			if (perm != -1)
				{
				try
					{
					String cmd = "chmod "+perm+" "+dest.getPath();
					if (m_verbose)
						System.out.println(cmd);
					proc = Runtime.getRuntime().exec(cmd);
					proc.waitFor();
					}
				catch (Exception e) {}
				}
			}
		}
		
//---------------------------------------------------------------------
	private void runThread()
		{
		s_tlMakeList.push(this);
		try
			{
			synchronized(this)
				{
				m_activeThreadCnt++;
				}
			m_engine.processBuildQueue();
			synchronized(this)
				{
				m_activeThreadCnt--;
				if (m_activeThreadCnt == 0)
					notify();
				}
			}
		catch (TablesawException cpme)
			{
			m_makeException = cpme;
			synchronized(this)
				{
				notifyAll();
				}
			}
		finally
			{
			s_tlMakeList.pop();
			}
		}
		
	//---------------------------------------------------------------------------
	/**
		Returns the location of the Tablesaw jar file
	*/
	public static String getTablesawPath()
		{
		String ret = null;
		
		try
			{
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			//ClassLoader cl = ClassLoader.getSystemClassLoader();
			String tablesawPath = cl.getResource("tablesaw/Tablesaw.class").getPath();

			int bangIndex = tablesawPath.indexOf('!');

			if (bangIndex != -1)
				{
				//This is expecting to find the file within a jar
				tablesawPath = tablesawPath.substring(5, bangIndex);
				}
		
			ret = URLDecoder.decode(new File(tablesawPath).getParent(), "UTF-8");
			}
		catch (java.io.UnsupportedEncodingException e)
			{
			Debug.print(e);
			}
			
		return (ret);
		}
		
	//---------------------------------------------------------------------------
	private void loadSupportJars()
			throws TablesawException
		{
		String loadPath = getProperty(PROP_SUPPORT_LIB_DIR);
		
		if (loadPath == null)
			{
			loadPath = getTablesawPath();
			}
		
		File libDir = new File(loadPath);
		File[] fileList = libDir.listFiles();
		for (File f : fileList)
			{
			if (f.getName().endsWith(".jar") && !f.getName().startsWith("tablesaw"))
				{
				String path = f.getPath();
				addClasspath(path);
				Debug.print("Adding to classpath: %s", path);
				}
			}
		
		}
		
//-------------------------------------------------------------------
	/*package*/ static boolean comparePaths(String path1, String path2)
		{
		if (System.getProperty("os.name").startsWith("Windows"))
			{
			return (path1.toLowerCase().replace('\\', '/').equals(
					path2.toLowerCase().replace('\\', '/')));
			}
		else
			return (path1.equals(path2));
		}
		
//===================================================================
/**
	Creaes a Tablesaw object.
	
*/

	public Tablesaw()
			throws TablesawException
		{
		this(null);
		}
		
	//---------------------------------------------------------------------------
	public Tablesaw(String workingDirectory)
			throws TablesawException
		{
		m_workingDir = null;
		if (workingDirectory != null)
			m_workingDir = new File(workingDirectory);
			
		m_objects = new HashMap<String, Object>();
		m_properties = new Properties(System.getProperties());
		m_rules = new ArrayList();
		
		m_eventListeners = new ArrayList<BuildEventListener>();
		m_primaryTarget = null;
		m_defaultTarget = null;
		m_buildInProgress = false;
		m_threadQueue = new LinkedList();
		m_waitingThreadCnt = 0;
		m_autoCleanDirs = new ArrayList();
		m_modifiedEnv = false;
		m_scriptObjects = new HashMap();
		m_buildCache = new HashSet();
		m_multiTargetBuildActions = new LinkedList();
		m_asyncProcesses = new LinkedList();
		m_banner = null;
		m_includedScripts = new HashSet<File>();
		m_activeThreadCnt = 0;
		m_makeException = null;


		/*if (System.getProperty("os.name").contains("Linux"))
			setProperty(PROP_ANSI_CONSOLE, PROP_VALUE_ON);*/
		
		
		loadPropertiesFile();
		loadEnvProperties();
		}

//===================================================================
	/**
	*/
	public void init()
			throws TablesawException
		{
		//Add this instance to thread data
		s_tlMakeList.push(this);
		
		loadSupportJars();
		
		
		m_fileManager = new BuildFileManager(m_workingDir);
		m_engine = new MakeEngine(m_fileManager, m_properties);
		
		m_threadCount = Integer.parseInt(getProperty(PROP_THREAD_COUNT, "1"));
		
		if (!getProperty(PROP_INPUT_WATCHER, "on").equals("off"))
			m_inputWatcher = InputWatcher.getInputWatcher();
		else
			m_inputWatcher = null;
		
		//Add Shutdown hook to close sub processes
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
				{
				public void run()
					{
					AsyncProcess proc;
					if (m_inputWatcher != null)
						m_inputWatcher.killProcesses();
					
					Iterator it = m_asyncProcesses.listIterator();
					while (it.hasNext())
						{
						proc = (AsyncProcess)it.next();
						proc.kill();
						}
					}
				}));
				
		//Add some default rules
		//They add them selves
		new CleanRule(m_engine);
		new HelpRule();
		new AntBuildRule();
		}
		
	//---------------------------------------------------------------------------
	/**
		Finishes the build and lets Tablesaw save/close any data it has open
	*/
	public void close()
		{
		if (m_interpreter != null)
			m_interpreter.cleanup();
			
		m_engine.close();
		s_tlMakeList.pop();
		}
		
//---------------------------------------------------------------------------
/**
	Returns the name of the build file
*/
	public String getBuildFile()
		{
		return (m_buildFile);
		}
		
//------------------------------------------------------------------------------
/**
	Processes the build file.
*/
	public void processBuildFile(String buildFile)
			throws TablesawException
		{
		Debug.print("Build file = %s", buildFile);

		if (buildFile == null)
			throw new TablesawException("No build file specified");

		m_buildFile = buildFile;
		
		if (m_buildFile.endsWith(".bsh"))
			{
			try
				{
				m_interpreter = new BeanShellInterpreter(m_properties);
				}
			catch (NoClassDefFoundError ncdfe)
				{
				throw new TablesawException("Unable to find Bean shell interpreter", -1);
				}
			}
		else if (m_buildFile.endsWith(".py"))
			{
			try
				{
				m_interpreter = new JythonShellInterpreter(this);
				}
			catch (NoClassDefFoundError ncdfe)
				{
				throw new TablesawException("Unable to find Jython interpreter", -1);
				}
			}
		else if (m_buildFile.endsWith(".groovy")||(m_buildFile.endsWith(".gvy")))
			{
			try
				{
				m_interpreter = new GroovyShellInterpreter();
				}
			catch (NoClassDefFoundError ncdfe)
				{
				throw new TablesawException("Unable to find Groovy interpreter\n" + ncdfe.toString(), -1);
				}
			}
		else if (m_buildFile.endsWith(".js"))
			{
			try
				{
				m_interpreter = new RhinoInterpreter();
				}
			catch (NoClassDefFoundError ncdfe)
				{
				throw new TablesawException("Unable to find Rhino interrpreter\n" +ncdfe.toString(), -1);
				}
			}
		else if (m_buildFile.endsWith(".rb"))
			{
			try
				{
				m_interpreter = new RubyInterpreter();
				}
			catch (NoClassDefFoundError ncdfe)
				{
				throw new TablesawException("Unable to find JRuby interrpreter\n" +ncdfe.toString(), -1);
				}
			}
		else
			throw new TablesawException("Unknown build script type", -1);
			
		try
			{
			String name;
			Iterator it = m_scriptObjects.keySet().iterator();
			while (it.hasNext())
				{
				name = (String)it.next();
				m_interpreter.set(name, m_scriptObjects.get(name));
				}
			m_interpreter.set("saw", this);
			//m_interpreter.set("cpmake", this); cannot use cpmake as that is the package name
			Debug.print("Processing "+m_buildFile);
			m_includedScripts.add(new File(m_buildFile).getCanonicalFile());
			m_interpreter.source(m_buildFile);
			}
		catch(TablesawException cpme)
			{
			m_makeException = cpme;
			synchronized(this)
				{
				notifyAll();
				}
			throw cpme;
			}
		catch(IOException ioe)
			{
			m_makeException = new TablesawException("Unable to load "+m_buildFile, -1);
			synchronized(this)
				{
				notifyAll();
				}
			throw m_makeException;
			}
		catch (Error error)
			{
			if (Debug.isDebug())
				error.printStackTrace();

			//Find root cause
			Throwable cause = error;
			while (cause.getCause() != null)
				cause = cause.getCause();

			if (cause instanceof ClassNotFoundException)
				throw new MissingClassException((ClassNotFoundException)cause);
			}
		/*finally
			{
			m_interpreter.cleanup();
			}*/
		
		}
		
	public void clearFileCache()
		{
		m_fileManager.clearFileCache();
		}
//-------------------------------------------------------------------
/**
	Sets a banner to be prepended to process output.  If you want to add
	[javac] to a call to the javac compiler, call this before calling exec().
	
	To stop the banner call this with a null parameter.
	
	@param banner Banner to prepend to process output
*/
	public void setBanner(String banner)
		{
		//System.out.println("Banner set "+banner);
		m_banner = banner;
		}
//-------------------------------------------------------------------
/**
	Returns the primary build target.  This may be null if no primary target
	has been given and a default has not yet been set.
	
	@return Primary build target or null.
*/
	public String getPrimaryTarget()
		{
		return (m_primaryTarget);
		}
		
//------------------------------------------------------------------------------
/**
	Starts the input watcher for external commands that need input
*/
	public void startInputWatcher()
		{
		if (m_inputWatcher != null)
			m_inputWatcher = InputWatcher.getInputWatcher();
		}
		
//-------------------------------------------------------------------
/**
	Processes the rule for the given target.
	
	Before the build queue is generated for the specified target an attempt is 
	made to call a script function that matches the following signiture<br/>
	void prepForTarget(String target);<p>
	This allows the script to set up any special varaibles for the target.
	
	@param target Target to be built.
*/
	public void buildTarget(String target)
			throws TablesawException
		{
		buildTarget(target, true);
		}

//-------------------------------------------------------------------
/**
	Builds the default target.  <code>setDefaultTarget</code> Must have been called
	within the build script for this method to work.
	
*/
	public void buildDefaultTarget()
			throws TablesawException
		{
		if (m_primaryTarget == null)
				throw new TablesawException("No target specified", -1);
				
		buildTarget(m_primaryTarget);
		}
		
//-------------------------------------------------------------------
/**
	Processes the rule for the given target.
	
	Before the build queue is generated for the specified target an attempt is 
	made to call a script function that matches the following signiture<br/>
	void prepForTarget(String target);<p>
	This allows the script to set up any special varaibles for the target.
	
	@param target Target to be built.
	@param prep If true prepForTarget is called otherwise it is not
*/
	public void buildTarget(String target, boolean prep)
			throws TablesawException
		{
		File targetFile = null;
		Thread t;
		
		Debug.print("Building target "+target);
		
		//Removing the cache file when calling clean helps prevent the cache
		//file from becomming corrupt.
		if (target.equals("clean"))
			m_engine.clearCacheFile(); //Wipe out cache file when cleaning.

		
		try
			{
			for (BuildEventListener bel : m_eventListeners)
				bel.setPrimaryTarget(this, target);
			
			/* if (m_printDebug)
				printDependencies(); */
			
			if (prep)
				{
				try
					{
					m_interpreter.call("prepForTarget", target);
					}
				catch (Exception e)
					{
					//Need to catch specific exception for when method does not exist
					}
				}
				
			
			if (m_buildInProgress)
				throw new TablesawException("Build already in progress for "+m_currentTarget, -1);
				
			/*for (int I = 0; I < m_dependencies.size(); I++)
				{
				d = (Dependency)m_dependencies.elementAt(I);
				System.out.println(d.toString());
				}*/
				
			m_currentTarget = target;
			m_buildInProgress = true;
			
			if (m_engine.setPrimaryTarget(target))
				{
				/* if (m_printDebug)
					{
					Debug.print("Build Queue Size: "+m_buildQueue.size());
					ListIterator it = m_buildQueue.listIterator(m_buildQueue.size());
					while (it.hasPrevious())
						Debug.print("Build Queue: "+it.previous());
					} */
					
				if (m_verbose)
					System.out.println("Using " + m_threadCount +" threads");
				//Using the thread name as a unique id for that thread
				//Used to set script variables for that thread
				Thread.currentThread().setName("0");
				if (m_threadCount > 1)
					for (int I = 1; I < m_threadCount; I++)
						{
						t = new Thread(new Runnable()
							{
							public void run()
								{
								runThread();
								}
							});
						t.setName("" + I);
						t.start();
						}
						
				m_engine.processBuildQueue();
				}
				
			try
				{
				synchronized(this)
					{
					if (getActiveThreadCnt() > 0)
						wait();
					}
				}
			catch (InterruptedException ie) {}
			
			m_buildInProgress = false;
			try
				{
				m_interpreter.call("buildSuccess", target);
				}
			catch (Exception e1) {}
			}
		catch (TablesawException e)
			{
			try
				{
				m_interpreter.call("buildFailure", e);
				}
			catch (Exception e2) { /* e2.printStackTrace(); */}
			finally
				{
				System.out.println("Exception "+e);
				}
			throw e;
			}
		finally
			{		
			if (!target.contains("clean"))
				{
				/* try
					{
					Debug.print("Saving cache file");
					//Update the depencency cache in case any files changed durring build
					
					//DependencyCache.writeDependencyCache();
					}
				catch (IOException ioe)
					{
					Debug.print(ioe.getMessage());
					StringWriter sw = new StringWriter();
					ioe.printStackTrace(new PrintWriter(sw));
					Debug.print(sw.toString());
					System.out.println("Unable to save cache file "+DependencyCache.getCacheFile());
					} */
				}
				
			//Pop instance back off thread data
			
			}			
		}
		
//-------------------------------------------------------------------
/**
	Converts an array into a string seperated by spaces.
	There is also a space left on the end of the string.<p>
	{"foo", "bar"} would become "foo bar "
	
	@param array Array to be turned into a string.
*/
	public static String arrayToString(String[] array)
		{
		StringBuffer sb = new StringBuffer();
		
		for (int I = 0; I < array.length; I++)
			sb.append(array[I] + " ");
			
		return (sb.toString());
		}
		
//-------------------------------------------------------------------
/**
	Adds a path to search when looking for targets.
	
	@param path Path to search for targets and prerequisites.
*/
	public void addSearchPath(String path)
		{
		addSearchPaths(".*", new String[] {path});
		}
		
//-------------------------------------------------------------------
/**
	Tells Tablesaw to echo out build information
	
	@param verbose Whether to be verbose or not.
*/
	public void setVerbose(boolean verbose)
		{
		m_verbose = verbose;
		setProperty(PROP_VERBOSE, String.valueOf(verbose));
		}
		
	//---------------------------------------------------------------------------
	public boolean isVerbose()
		{
		return (m_verbose);
		}
		
//-------------------------------------------------------------------
/**
	Adds a path to search when looking for targets that match the given
	pattern.
	
	@param pattern File pattern (regex) for this search path.
	@param path Path to search
*/
	public void addSearchPath(String pattern, String path)
		{
		addSearchPaths(pattern, new String[] {path});		
		}
		
//-------------------------------------------------------------------
/**
	Adds search paths to look in when trying to find files.
	
	@param paths Array of file paths to search
*/
	public void addSearchPaths(String... paths)
		{
		addSearchPaths(".*", paths);
		}
		
//-------------------------------------------------------------------
/**
	Adss paths to search when looking for targets that match the given
	pattern
	
	@param pattern File pattern (regex) for this search path.
	@param paths Paths to search
*/
	public void addSearchPaths(String pattern, String... paths)
		{
		addSearchPaths(pattern, Arrays.asList(paths));
		/* for (int I = 0; I < paths.length; I++)
			m_fileManager.addSearchPath(new SearchPath(pattern, paths[I])); */
		}
		
//------------------------------------------------------------------------------
/**
*/
	public void addSearchPaths(String pattern, Iterable<String> paths)
		{
		for (String p : paths)
			m_fileManager.addSearchPath(new SearchPath(pattern, p));
		}
		
//-------------------------------------------------------------------
/**
	Tries to locate a file by looking in the search paths.
	
	@param file The file name to look for.
	
	@return The file object or null if none is found.
*/
	public CachedFile locateFile(String file)
		{
		return (m_fileManager.locateFile(file));
		}
		
//--------------------------------------------------------------------
/**
	Tries to locate a file by looking in the search paths.  This method also
	check the rules to see if the file can be created, if so a file object is
	returned with the path to where the file would be based on the rule.
	
	@param file The file name to look for.
	
	@return The file object or null if none is found.
	
	@since 1.0.0
*/
	public CachedFile locateFileUsingRules(String file)
		{
		return (m_engine.locateFileUsingRules(file));
		}


	//---------------------------------------------------------------------------
	public Set<String> getNamedRules()
		{
		return (m_engine.getNamedRules());
		}
		
//--------------------------------------------------------------------
/**
	
*/
	/*project*/ String getPath(File file)
		{
		String ret;
		
		ret = file.getPath();
		
		if (m_workingDir != null)
			{
			if (ret.startsWith(m_workingDir.getPath()))
				ret = ret.substring(m_workingDir.getPath().length() +1); 
			}
		
		return (ret);
		}
//--------------------------------------------------------------------
/**
	Fixes the directory seperators for the current platform.
	
	It is encuraged to use only forward slashes in make files and then call
	this function when the path will be handed to a platform specific call.
	
	@param path Path to be checked for correct seperators
	
	@return The path with the directory sperators appropriat for the platform.
*/
	public static String fixPath(String path)
		{
		String slash = System.getProperty("file.separator");
		
		if (slash.equals("/"))
			path = path.replace('\\', '/');
		else
			path = path.replace('/', '\\');
			
		return (path);
		}

//--------------------------------------------------------------------
/**
	Fixes the directory seperators for the current platform.
	
	It is encuraged to use only forward slashes in make files and then call
	this function when the path will be handed to a platform specific call.
	
	@param paths Array of paths to be checked for correct seperators
	
	@return The path with the directory sperators appropriat for the platform.
*/
	public static String[] fixPath(String[] paths)
		{
		String[] newPaths = new String[paths.length];
		
		for (int I = 0; I < paths.length; I++)
			{
			newPaths[I] = fixPath(paths[I]);
			}
			
		return (newPaths);
		}

//--------------------------------------------------------------------
	public void setCaptureSource(boolean capture)
		{
		m_engine.setCaptureSource(capture);
		}

//--------------------------------------------------------------------
	public Set<File> getSourceFiles()
		{
		return (m_engine.getSourceFiles());
		}
		
//--------------------------------------------------------------------
/**
	Sets the target that will be build if none is specified on the command line.
	
	@param target Target to be built.
*/
	public void setDefaultTarget(String target)
		{
		if (m_primaryTarget == null)
			m_primaryTarget = target;
		}
		
//--------------------------------------------------------------------
/**
	Reutrns the number of threads to be used when processing the build queue
	
	@return thread count
*/
	public int getThreadCount()
		{
		return (m_threadCount);
		}
		
//---------------------------------------------------------------------
/**
	Sets the number of threads to be used when processing the build queue.
	
	@param threadCount Number of threads to use.
*/
	public void setThreadCount(int threadCount)
		{
		m_threadCount = threadCount;
		}
		
//---------------------------------------------------------------------
/**
	Sets properties for this instance of Tablesaw
	
	@param name Property name.
	@param value Value of the property.
*/
	public void setProperty(String name, String value)
		{
		m_modifiedEnv = true;
		m_properties.setProperty(name, value);
		}
		
//---------------------------------------------------------------------
/**
	Removes a property
	
	@param name Property name
*/
	public void clearProperty(String name)
		{
		m_modifiedEnv = true;
		m_properties.remove(name);
		}
		
//---------------------------------------------------------------------
/**
	Gets a property from Tablesaw.  These propeties are a combination of
	the System properties, tablesaw.properties and any properties files that
	are specified on the command line.
	
	@param name Name of the property to get.
	@param def Default value to return if no property is found.
	
	@return property value of the property or the default if none is found.
*/
	public String getProperty(String name, String def)
		{
		String value = getProperty(name);
		
		if (value == null)
			return (def);
		else
			return (value);
			
		}		


//---------------------------------------------------------------------
	private volatile int m_propertyCallCount = 0;
/**
	Gets a property from Tablesaw.  These propeties are a combination of
	the System properties, tablesaw.properties and any properties files that
	are specified on the command line.
	
	@param name Name of the property to get.
	
	@return property value of the property.
*/
	public String getProperty(String name)
		{
		m_propertyCallCount ++;

		if (m_propertyCallCount > 100)
			throw new RuntimeException("Recursive property detected for "+name);

		String value = m_properties.getProperty(name);
		
		if (value == null)
			value = m_properties.getProperty(name.replace('.', '_'));

		if (value != null)
			value = substituteProperty(value);

		m_propertyCallCount --;
		return (value);
		}


	private static Pattern s_tokenPattern = Pattern.compile("\\$\\{([^}]*)\\}");


	//---------------------------------------------------------------------
	private String substituteProperty(String value)
		{
		StringBuffer sb = new StringBuffer();
		Matcher myMatcher = s_tokenPattern.matcher(value);

		while (myMatcher.find())
			{
			String prop = myMatcher.group(1);
			myMatcher.appendReplacement(sb, "");

			if (m_properties.containsKey(prop))
				sb.append(getProperty(prop));
			else
				sb.append(myMatcher.group());
			}

		myMatcher.appendTail(sb);
		return sb.toString();
		}
		
//---------------------------------------------------------------------
/**
	Returns the Tablesaw properites object.
*/
	public Properties getProperties()
		{
		return (m_properties);
		}
		
		
	//---------------------------------------------------------------------------
	/**
		Method for registering a generic object with this instance of Tablesaw.
	*/
	public void setObject(String key, Object obj)
		{
		m_objects.put(key, obj);
		}
		
		
	//---------------------------------------------------------------------------
	/**
		Gets an object that has been registerd with this instance of Tablesaw or null
		if not registered.
	*/
	public Object getObject(String key)
		{
		return (m_objects.get(key));
		}
		
//---------------------------------------------------------------------
/**
	Executes a command and waits until it is finnished.
	
	Executes a command and then pipes the stdin and stdout of the process to 
	System.in and System.out.
	
	@param cmd command line of the process to start.
	@param logFile Output is sent to this log file as well
	@return process exit code
*/
	public int exec(String cmd, String logFile)
			throws TablesawException
		{
		return exec(null, splitString(cmd), true, logFile, null);
		}

//---------------------------------------------------------------------
/**
	Executes a command and waits until it is finnished.
	
	Executes a command and then pipes the stdin and stdout of the process to 
	System.in and System.out.
	
	@param cmd command line of the process to start.
	@param logFile Output is sent to this log file as well
	@return process exit code
	@since 1.0.0
*/
	public int exec(String[] cmd, String logFile)
			throws TablesawException
		{
		return exec(null, cmd, true, logFile, null);
		}
		
//---------------------------------------------------------------------
/**
	Executes a command and waits until it is finnished.
	
	Executes a command and then pipes the stdin and stdout of the process to 
	System.in and System.out.
	
	@param cmd command line of the process to start.
	@return process exit code
*/
	public int exec(String cmd)
			throws TablesawException
		{
		return exec(null, splitString(cmd), true, null, null);
		}

//---------------------------------------------------------------------
/**
	Executes a command and waits until it is finnished.
	
	Executes a command and then pipes the stdin and stdout of the process to 
	System.in and System.out.
	
	@param cmd command line of the process to start.
	@return process exit code
	@since 1.0.0
*/
	public int exec(String[] cmd)
			throws TablesawException
		{
		return exec(null, cmd, true, null, null);
		}
		
//-------------------------------------------------------------------
/**
	Executes a command and waits until it is finnished.
	
	Executes a command and then pipes the stdin and stdout of the process to 
	System.in and System.out.
	
	@param cmd command line of the process to start.
	@param exitOnError If the process reutrns an error the make will stop.
	@param logFile Output is sent to this log file as well
	@return process exit code
*/
	public int exec(String cmd, boolean exitOnError, String logFile)
			throws TablesawException
		{
		return exec(null, splitString(cmd), exitOnError, logFile, null);
		}

//-------------------------------------------------------------------
/**
	Executes a command and waits until it is finnished.
	
	Executes a command and then pipes the stdin and stdout of the process to 
	System.in and System.out.
	
	@param cmd command line of the process to start.
	@param exitOnError If the process reutrns an error the make will stop.
	@param logFile Output is sent to this log file as well
	@return process exit code
	@since 1.0.0
*/
	public int exec(String[] cmd, boolean exitOnError, String logFile)
			throws TablesawException
		{
		return exec(null, cmd, exitOnError, logFile, null);
		}
		
//-------------------------------------------------------------------
/**
	Executes a command and waits until it is finnished.
	
	Executes a command and then pipes the stdin and stdout of the process to 
	System.in and System.out.
	
	@param cmd command line of the process to start.
	@param exitOnError If the process reutrns an error the make will stop.
	@return process exit code
*/
	public int exec(String cmd, boolean exitOnError)
			throws TablesawException
		{
		return exec(null, splitString(cmd), exitOnError, null, null);
		}

//-------------------------------------------------------------------
/**
	Executes a command and waits until it is finnished.
	
	Executes a command and then pipes the stdin and stdout of the process to 
	System.in and System.out.
	
	@param cmd command line of the process to start.
	@param exitOnError If the process reutrns an error the make will stop.
	@return process exit code
	@since 1.0.0
*/
	public int exec(String[] cmd, boolean exitOnError)
			throws TablesawException
		{
		return (exec(null, cmd, exitOnError, null, null));
		}
		
//-------------------------------------------------------------------
/**
	Executes a command and waits until it is finnished.
	
	Executes a command and then pipes the stdin and stdout of the process to 
	System.in and System.out.  Passing in a current working directory requires 
	the path in cmd to be the absolute path.  You can use the fullPath method
	to aid in retrieving the absolute path.
	
	@param cwd Sets the current working directory of the process.
	@param cmd command line of the process to start.
	@param exitOnError If the process reutrns an error the make will stop.
	@return process exit code
*/
	public int exec(String cwd, String cmd, boolean exitOnError)
			throws TablesawException
		{
		return (exec(cwd, splitString(cmd), exitOnError, null, null));
		}

//-------------------------------------------------------------------
/**
	Executes a command and waits until it is finnished.
	
	Executes a command and then pipes the stdin and stdout of the process to 
	System.in and System.out.  Passing in a current working directory requires 
	the path in cmd to be the absolute path.  You can use the fullPath method
	to aid in retrieving the absolute path.
	
	@param cwd Sets the current working directory of the process.
	@param cmd command line of the process to start.
	@param exitOnError If the process reutrns an error the make will stop.
	@return process exit code
	@since 1.0.0
*/
	public int exec(String cwd, String[] cmd, boolean exitOnError)
			throws TablesawException
		{
		return (exec(cwd, cmd, exitOnError, null, null));
		}
		
//-------------------------------------------------------------------
/**
	Executes a command and waits until it is finnished.
	
	Executes a command and then pipes the stdin and stdout of the process to 
	System.in and System.out.  Passing in a current working directory requires 
	the path in cmd to be the absolute path.  You can use the fullPath method
	to aid in retrieving the absolute path.
	
	@param cwd Sets the current working directory of the process. My be null.
	@param cmd command line of the process to start.
	@param exitOnError If the process reutrns an error the make will stop.
	@param logFile Output is sent to this log file as well. May be null.
	@param redirect File to redirect output to. May be null.
	@return process exit code
*/
	public int exec(String cwd, String cmd, boolean exitOnError, String logFile, 
			String redirect)
			throws TablesawException
		{
		return (exec(cwd, splitString(cmd), exitOnError, logFile, redirect));
		}

//-------------------------------------------------------------------
	/**
	This method splits a string appart on a space.  You can also quote a section
	and have it end up as one string.
	ex I like \"apples\" "and bananas"
	turns into {"I", "Like", ""apples"", "and bananas"}
	*/
	/*package*/ String[] splitString(String str)
			throws TablesawException
		{
		char[] strarray = str.trim().toCharArray();
		ArrayList array = new ArrayList();
		StringWriter writer = new StringWriter();
		boolean inQuote = false;
		
		for (int I = 0; I < strarray.length; I++)
			{
			switch (strarray[I])
				{
				case '\\':
					if (((I+1) < strarray.length)&&(strarray[I+1] == '"'))
						I++;
					writer.write(strarray[I]);
					break;
				case '"':
					inQuote = !inQuote;
					break;
				case ' ':
					if (inQuote)
						writer.write(strarray[I]);
					else
						{
						array.add(writer.toString());
						writer = new StringWriter();
						while ((strarray[I+1] == ' ')&&((I+1) < strarray.length))
							I++;
						}
					break;
				default:
					writer.write(strarray[I]);
				}
			}
			
		if (inQuote)
			throw new TablesawException("Unbalanced quotes in string \""+str+"\"", -1);
			
		array.add(writer.toString());
		
		return ((String[])array.toArray(new String[0]));
		}
		
//-------------------------------------------------------------------
/**
	Executes a command and waits until it is finnished.
	
	Executes a command and then pipes the stdin and stdout of the process to 
	System.in and System.out.  Passing in a current working directory requires 
	the path in cmd to be the absolute path.  You can use the fullPath method
	to aid in retrieving the absolute path.
	
	@param cwd Sets the current working directory of the process. My be null.
	@param cmdArr command line of the process to start.
	@param exitOnError If the process reutrns an error the make will stop.
	@param logFile Output is sent to this log file as well. May be null.
	@param redirect File to redirect output to. May be null.
	@return process exit code
	@since 1.0.0
*/

	public int exec(String cwd, String[] cmdArr, boolean exitOnError,
			String logFile, String redirect)
			throws TablesawException
		{
		Process proc = null;
		BufferedReader br;
		String line;
		File cwdf = null;
		StreamPipe stderr, stdout;
		boolean multiThreaded = false;
		OutputStream out;
		int ret = 1;
		
		if (((m_activeThreadCnt - m_waitingThreadCnt) > 1)||
				(getProperty(PROP_MULTI_THREAD_OUTPUT, PROP_VALUE_OFF).equals(PROP_VALUE_ON)))
			multiThreaded = true;
		
		if (cwd != null)
			{
			cwdf = new File(m_workingDir, cwd);
			//cmd = cwdf.getPath()+getProperty("file.separator")+cmd;
			}
		else
			cwdf = m_workingDir;
		
		if (redirect == null)
			out = System.out;
		else
			{
			try
				{
				out = new FileOutputStream(new File(m_workingDir, redirect));
				}
			catch (FileNotFoundException fnfe)
				{
				throw new TablesawException("Cannot open "+redirect+" for output", -1);
				}
			}
			
		if (m_verbose)
			{
			if (cwd != null)
				System.out.print(cwdf.getPath()+" ");
				
			for (int I = 0; I < cmdArr.length; I++)
				{
				if (cmdArr[I].indexOf(' ') == -1)
					System.out.print(cmdArr[I]+" ");
				else
					System.out.print("\""+cmdArr[I]+"\" ");
				}
			System.out.println();
			}
			
		try
			{
			proc = Runtime.getRuntime().exec(cmdArr, getEnvArr(), cwdf);
			
			if (m_inputWatcher != null)
				m_inputWatcher.addProcess(proc);
			
			if (logFile == null)
				{
				stdout = new StreamPipe(proc.getInputStream(), out, multiThreaded);
				stdout.setBanner(m_banner);
				stdout.startPipe();
				stderr = new StreamPipe(proc.getErrorStream(), out, multiThreaded);
				stderr.setBanner(m_banner);
				stderr.startPipe();
				//new StreamPipe(System.in, proc.getOutputStream());
				}
			else
				{
				stdout = new StreamPipe(proc.getInputStream(), out, new File(m_workingDir, logFile), multiThreaded);
				stdout.setBanner(m_banner);
				stdout.startPipe();
				stderr = new StreamPipe(proc.getErrorStream(), out, new File(m_workingDir, logFile), multiThreaded);
				stderr.setBanner(m_banner);
				stderr.startPipe();
				}
			
			stdout.waitForClose();
			stderr.waitForClose();
			
			ret = proc.waitFor();
			if ((ret != 0)&&(exitOnError))
				{
				throw new TablesawException("Error "+proc.exitValue()+" while running "+cmdArr[0], proc.exitValue());
				}
			
			if (redirect != null)
				{
				out.flush();
				out.close();
				}
			}
		catch (IOException ioe) 
			{
			Debug.print(ioe);
			if (exitOnError)
				{
				throw new TablesawException("Unable to run command: "+cmdArr[0]+" - "+ioe.getMessage());
				}
			/*System.out.println("Tablesaw Error! Unable to run command!");
			System.out.println(ioe);
			Debug.print(ioe);
			System.exit(-1);*/
			}
		catch (InterruptedException ie)
			{
			//ie.printStackTrace();
			System.out.println(ie);
			Debug.print(ie);
			//System.exit(-1);
			}
		finally
			{
			if ((m_inputWatcher != null) && (proc != null))
				m_inputWatcher.removeProcess(proc);
			}
			
		return (ret);
		}
		
//---------------------------------------------------------------------
/**
	Creates an AsyncProcess object using the specified parameters.  The process
	is not started until you call AsyncProcess.run()
	@param cwd Current working directory for the new process
	@param cmd Command line to run asynchronously
	@return Returns an <code>AsyncProcess</code> object.
*/
	public AsyncProcess createAsyncProcess(String cwd, String cmd)
			throws TablesawException
		{
		AsyncProcess proc = new AsyncProcess(this, cwd, cmd);
		m_asyncProcesses.add(proc);
		return (proc);
		}
		

		
//---------------------------------------------------------------------
/**
	Deletes the entire sub tree.
	Changed this to project scope because of recursive issues.
*/
	/*project*/ void deltree(File directory)
		{
		if (!directory.exists())
			return;
		File[] list = directory.listFiles();
		
		if (list.length > 0)
			{
			for (int I = 0; I < list.length; I++)
				{
				if (list[I].isDirectory())
					deltree(list[I]);
				
				list[I].delete();
				}
			}
		
		directory.delete();
		}		
		
//---------------------------------------------------------------------
/**
	Deletes the entire sub tree including the directory.
	
	@param directory Directory you wish to delete.
	@since 1.0.0
*/
	public void deltree(String directory)
		{
		File f = new File(directory);
		
		if (f.isAbsolute())
			deltree(f);
		else
			deltree(new File(m_workingDir, directory));
		}
		
//-------------------------------------------------------------------
/**
	Deletes a file
	@param file File to delete.
	@since 1.0.0
*/
	public void delete(String file)
		{
		File f = new File(m_workingDir, file);
		f.delete();
		}
		
//-------------------------------------------------------------------
/**
	Added for use in Rhino script
	@param file File to delete.
	@since 1.0.0
*/
	public void del(String file)
		{
		delete(file);
		}
		
//-------------------------------------------------------------------
/**
	Copies all files in the source directory that match the file pattern to the
	destination directory if they are newer.
	
	@param sourceDir Source directory.
	@param filePattern Regex file pattern.
	@param dest Destination directory to copy files to.
	@since 1.0.0
*/
	public int copyIfNewer(String sourceDir, String filePattern, String dest)
			throws TablesawException
		{
		int copyCnt = 0;
		List<String> files = new RegExFileSet(sourceDir, filePattern).getFileNames();
		//List<String> files = createFileList(sourceDir, filePattern);
		
		File destination = new File(dest);
		if (!destination.exists())
			destination.mkdirs();
		
		for (int I = 0; I < files.size(); I++)
			if (copyIfNewer(sourceDir+"/"+files.get(I), dest+"/"+files.get(I)))
				copyCnt ++;
			
		return (copyCnt);
		}
		
//-------------------------------------------------------------------
/**
	Performes a file copy if the source is newer then the destination
	
	@param source Source file to copy
	@param dest Destination directory or file to copy to
	@return Returns true if the file was copied.
	@since 1.0.0
*/
	public boolean copyIfNewer(String source, String dest)
			throws TablesawException
		{
		boolean ret = false;
		File destf = new File(m_workingDir, dest);
		File sourcef = new File(m_workingDir, source);
		
		if (destf.isDirectory())
			destf = new File(destf, sourcef.getName());
		
		if (!sourcef.exists())
			{
			throw new TablesawException("Cannot copy "+source+" file does not exist!", -1);
			}
			
		if (sourcef.lastModified() != destf.lastModified())
			{
			copy(source, dest);
			ret = true;
			}
		return (ret);
		}
		
//-------------------------------------------------------------------
/**
	Copies files specified in fileList parameter from the source directory
	to the destination directory.  Files will retain relative paths when copied.
	This means that if the source directory were "src" and the destination were 
	"build" and the file list contained {"tablesaw/file1", "tablesaw/file2"} the files
	src/tablesaw/file1 and<br/>
	src/tablesaw/file2<br/>
	will be copied to <br/>
	build/tablesaw/file1 and <br/>
	build/tablesaw/file2<br/>
	And the files will only be copied if they are newer then the destination.  Also
	the directories will be created automatically.  It is good to use this with the 
	output from createFileList to copy files.
	
	@param sourceDir Source directory
	@param fileList List of files to copy.
	@param dest Destination directory.
	@since 1.0.0
*/
	public int copyIfNewer(String sourceDir, String[] fileList, String dest)
			throws TablesawException
		{
		return (copyIfNewer(sourceDir, Arrays.asList(fileList), dest));
		}
		
	public int copyIfNewer(String sourceDir, List<String> fileList, String dest)
			throws TablesawException
		{
		int copyCnt = 0;
		
		File destination = new File(dest);
		if (!destination.exists())
			destination.mkdirs();
		
		for (int I = 0; I < fileList.size(); I++)
			if (copyIfNewer(sourceDir+"/"+fileList.get(I), dest+"/"+fileList.get(I)))
				copyCnt ++;
				
		return (copyCnt);
		}
		
//-------------------------------------------------------------------
/**
	Copies a list of files to the destination directory.  File lists can be
	created using createFileList and then passed to this method.  All of the
	files in the list will end up in the destination directory.
	
	@param fileList List of files to copy
	@param dest Destination directory to copy files to.
	@since 1.0.0
*/
	public int copyIfNewer(String[] fileList, String dest)
			throws TablesawException
		{
		return (copyIfNewer(Arrays.asList(fileList), dest));
		}
		
	public int copyIfNewer(List<String> fileList, String dest)
			throws TablesawException
		{
		int copyCnt = 0;
		
		File destination = new File(dest);
		if (!destination.exists())
			destination.mkdirs();
		
		for (int I = 0; I < fileList.size(); I++)
			if (copyIfNewer(fileList.get(I), dest))
				copyCnt ++;
				
		return (copyCnt);
		}
		
//-------------------------------------------------------------------
/**
	Copies all files in the source directory that match the file pattern to the 
	destination directory.
	
	@param sourceDir Source directory.
	@param filePattern Regex file pattern.
	@param dest Destination directory to copy files to.
*/
	public void copy(String sourceDir, String filePattern, String dest)
		{
		List<String> files = new RegExFileSet(sourceDir, filePattern).getFileNames();
		//List<String> files = createFileList(sourceDir, filePattern);
		
		File destination = new File(dest);
		if (!destination.exists())
			destination.mkdirs();
		
		for (int I = 0; I < files.size(); I++)
			copy(sourceDir+"/"+files.get(I), dest+"/"+files.get(I));
		}
//-------------------------------------------------------------------
/**
	Performs a file copy.  The file denoted by source is copied to dest.
	dest can either be a file name or a directory.  The directory structure
	in the destination will be created automatically.  This will not work if 
	the destination is the directory to copy to and it does not exist.
*/
	public void copy(String source, String dest)
		{
		File destf = new File(m_workingDir, dest);
		File sourcef = new File(m_workingDir, source);
		InputStream in;
		OutputStream out;
		//FileChannel in;
		//FileChannel out;
		byte[] buff = new byte[32*1024];
		int len;
		
		if (sourcef.isDirectory())
			return;
		
		if (m_verbose)
			System.out.println("Copying "+source+" to "+dest);
			
		if (destf.isDirectory())
			destf = new File(destf, sourcef.getName());
		
		try
			{
			// FIXME: use a custom static buffer instead of Buffered classes
			File parent = destf.getParentFile();
			if (parent != null)
				destf.getParentFile().mkdirs();
			
			/* in = new FileInputStream(source).getChannel();
			out = new FileOutputStream(dest).getChannel();
			in.transferTo( 0, in.size(), out); */
			
			/* in = new FileInputStream(sourcef);
			out = new FileOutputStream(destf, false);
			FileChannel inputChannel = in.getChannel();
			FileChannel ouputChannel = out.getChannel();
			ouputChannel.transferFrom(inputChannel, 0, inputChannel.size());
			ouputChannel.close();
			inputChannel.close(); */
			
			in = new BufferedInputStream(new FileInputStream(sourcef));
			out = new BufferedOutputStream(new FileOutputStream(destf));
			while ((len = in.read(buff)) > 0)
				out.write(buff, 0, len);
				
			in.close();
			out.close();
			destf.setLastModified(sourcef.lastModified());
			/* if (getProperty(PROP_COPY_PERMISSIONS, "on").equals("off"))
				duplicateProperties(sourcef, destf); */
			}
		catch (FileNotFoundException fnfe)
			{
			System.out.println(fnfe);
			Debug.print(fnfe);
			System.exit(-1);
			}
		catch (IOException ioe)
			{
			System.out.println(ioe);
			Debug.print(ioe);
			System.exit(-1);
			}
		}
		
//-------------------------------------------------------------------
/**
	Reads a list of GNU style dependency files and adds them to the Tablesaw
	dependency list.  If a file does not exist and create is set to true
	Tablesaw will try and find a rule to build the file.
	
	@param files List of GNU style dependency files.
	@param create Attempst to create the file if it does not exist
	@param soft Tells Tablesaw that the dependencies in this file are to be
		treated as soft meaning if the file does not exist do not try to build it.
*/
	public void processMakeDependencyFiles(String[] files, boolean create,
			boolean soft)
			throws TablesawException
		{
		for (int I = 0; I < files.length; I ++)
			processMakeDependencyFile(files[I], create, soft);
		}
//-------------------------------------------------------------------
/**
	Reads a GNU make file and addes the dependencies to the build.
	If the file does not exist and create is set to true Tablesaw will
	try and find a rule to build this file.
	
	@param file GNU make dependency file to include
	@param create Attempt to create the file if it does not exist
	@param soft Tells Tablesaw that the dependencies in this file are to be
		treated as soft meaning if the file does not exist do not try to build it.
*/
	public void processMakeDependencyFile(String file, boolean create,
			boolean soft)
			throws TablesawException
		{
		File f = new File(m_workingDir, file);
		BufferedReader br;
		String line;
		String[] splitLine;
		String[] targets;
		ArrayList dependencies;
		boolean continued = false;
		
		if ((!f.exists())&&(create))
			buildTarget(file, false);
		
		if (f.exists())
			{
			try
				{
				br = new BufferedReader(new FileReader(f));
				
				while ((line = br.readLine()) != null)
					{
					line = line.trim();
					if (line.charAt(0) == '#') //Skip comment lines
						continue;
						
					if (line.length() == 0)
						continue;
					
					dependencies = new ArrayList();
					splitLine = line.split(": ");
					
					if (splitLine.length == 1) //Emtpy dependency
						continue;
						
					targets = splitLine[0].trim().split(" ");
					line = splitLine[1].trim();
					if (line.charAt(line.length() - 1) == '\\')
						{
						continued = true;
						line = line.substring(0, (line.length()-1)).trim(); //chop off the \
						}
					
					loadArrayList(dependencies, line.split(" "));
					while(continued)
						{
						line = br.readLine();
						line = line.trim();
						if (line.charAt(line.length() - 1) == '\\')
							{
							continued = true;
							line = line.substring(0, (line.length()-1)).trim(); //chop off the \
							}
						else
							continued = false;
							
						loadArrayList(dependencies, line.split(" "));
						}
					
					if (soft)
						{
						String[] temp = (String[])dependencies.toArray(new String[0]);
						dependencies = new ArrayList();
						for (int I = 0; I < temp.length; I++)
							{
							if ((new File(m_workingDir, temp[I])).exists())
								dependencies.add(temp[I]);
							}
						}
					
					for (int I = 0; I < targets.length; I++)
						{
						// FIXME: createExplicitDependency(targets[I].replace('\\', '/'), (String[])dependencies.toArray(new String[0]));
						}
					}
				br.close();
				}
			catch (Exception e)
				{
				}
			}
		
		}
		
//-------------------------------------------------------------------
/**
	Clears all rules and dependencies.
*/
	public void clearMakeRules()
		{
		m_rules.clear();
		//m_dependencies.clear();
		}

//-------------------------------------------------------------------
/**
	Includes an additional script file that has a rule to generate it.
	Tablesaw will call build target on the include file before processing it.
	
	@param fileName Name of the script file to include.
*/
	public void include(String fileName)
			throws TablesawException
		{
		File f = new File(m_workingDir, fileName);
		
		buildTarget(fileName, false); //Calling this makes sure the make file is up to date
			
		try
			{
			m_interpreter.source(fileName);
			}
		catch (Exception e) {} //Should never happen because the build would have thrown an exception
		}
		
	//---------------------------------------------------------------------------
	/**
	*/
	public void includeOnce(String fileName)
			throws TablesawException
	{
		try
		{
			File f = new File(m_workingDir, fileName).getCanonicalFile();
			
			if (m_includedScripts.add(f))
				include(fileName);
		}
		catch (IOException ioe)
		{
			throw new TablesawException(ioe);
		}
	}

//-------------------------------------------------------------------
	public static List<String> substitute(String srcPattern, String destPattern, List<String> srcList)
		{
		List<String> dest = new ArrayList<String>();
		
		for (int I = 0; I < srcList.size(); I++)
			{
			dest.add(srcList.get(I).replaceAll(srcPattern, destPattern));
			}
		
		return (dest);
		}
/**
	Perform pattern substitution on the srcList String array.  This method 
	goes through each String in srcList and calls replaceAll on the string using 
	srcPattern and destPattern as arguments.
	
	@param srcPattern Regular expression search pattern.
	@param destPattern Replacement pattern.
	@param srcList Array of strings to perform pattern substitution on.
	@return New string array containing the substituted strings
*/
	public static String[] substitute(String srcPattern, String destPattern, String[] srcList)
		{
		String[] dest = new String[srcList.length];
		
		for (int I = 0; I < srcList.length; I++)
			{
			dest[I] = srcList[I].replaceAll(srcPattern, destPattern);
			}
		
		return (dest);
		}
		
//-------------------------------------------------------------------
/**
	Creates a directory
	
	@param directory Directory to create
*/
	public void mkdir(String directory)
		{
		File f = new File(m_workingDir, directory);
		f.mkdirs();
		}
		
//-------------------------------------------------------------------
/**
	Returns the absolute path from a relative path
	
	@param relPath Relative Path
	@return absolute path.
*/
	public String fullPath(String relPath)
		{
		File f = new File(m_workingDir, relPath);
		return(f.getAbsolutePath());
		}
		
//-------------------------------------------------------------------
/**
	Throws and exception.
	This is for testing purposes only
	@since 1.0.0
*/
	public static void throwException(String msg)
			throws TablesawException
		{
		throw new TablesawException(msg, -1);
		}
		
//-------------------------------------------------------------------
	private boolean isInCleanDir(String target)
		{
		Iterator it = m_autoCleanDirs.iterator();
		String dir;
		
		String targetDir = new File(target).getParent();
		//System.out.println("inDir="+target);
		while (it.hasNext())
			{
			dir = (String)it.next();
			
			if (targetDir.startsWith(dir))
				{
				Debug.print("File "+target+" inside Directory "+dir);
				return (true);
				}
			}
			
		return (false);
		}
		
//-------------------------------------------------------------------
/**
	Combines two String arrays into one.
	@since 1.0.0
*/
	public static String[] combine(String[] arr1, String[] arr2)
		{
		String[] ret = new String[arr1.length + arr2.length];
		
		System.arraycopy(arr1, 0, ret, 0, arr1.length);
		System.arraycopy(arr2, 0, ret, arr1.length, arr2.length);
		
		return (ret);
		}
		
//-------------------------------------------------------------------		
/**
	Returns the working directory for this instance of Tablesaw.  The working
	directory is defined to be the directory where the build file is located.
	@since 1.0.0
*/
	public File getWorkingDirectory()
		{
		return (m_workingDir);
		}

//-------------------------------------------------------------------		
/**
	Sets variables in script namespace.  This can be used, when calling Tablesaw
	recursivly, to pass variables from one instance to the other.
	@since 1.0.0
*/		
	public void setScriptObject(String name, Object obj)
		{
		m_scriptObjects.put(name, obj);
		}
		
//-------------------------------------------------------------------
	private static final Class[] parameters = new Class[] {URL.class};
/**
	Modifies the JVM class path to include additional jar files or directories.
	This is kind of a hack.  It uses reflection to get at the addURL method on 
	the system ClassLoader.
	
	@param path Class path to add.
	@since 1.0.0
*/
	public void addClasspath(String path)
			throws TablesawException
		{
		TablesawClassLoader loader = (TablesawClassLoader)Thread.currentThread().getContextClassLoader();

		try
			{
			loader.addURL(new File(path).toURL());
			}
		catch (MalformedURLException e)
			{
			Debug.print(e);
			throw new TablesawException("Error, could not add URL to system classloader", -1);
			}

		/*try
			{
			Method method = sysclass.getDeclaredMethod("addURL",parameters);
			method.setAccessible(true);
			method.invoke(sysloader,new Object[]{ new File(path).toURL() });
			} 
		catch (Throwable t) 
			{
			Debug.print(t);
			throw new TablesawException("Error, could not add URL to system classloader", -1);
			}*/
		}
		
//-------------------------------------------------------------------
/**
	This function will search the input file for lines that match the given 
	regular expression.

	@param file File to be searched.
	@param regexPattern Regular expression to be searched for.
	@return A GrepResult object containing the lines that match the grep pattern.
	@since 1.0.0
	
*/
	public GrepResult grep(String file, String regexPattern)
			throws TablesawException
		{
		Pattern pat = Pattern.compile(regexPattern);
		ArrayList results = new ArrayList();
		ArrayList lineNumbers = new ArrayList();
		int lineCount = 0;
		
		try
			{
			BufferedReader reader = new BufferedReader(new FileReader(new File(m_workingDir, file)));
			String line;
			
			while ((line = reader.readLine()) != null)
				{
				lineCount ++;
				if (pat.matcher(line).matches())
					{
					results.add(line);
					lineNumbers.add(new Integer(lineCount));
					}
				}

			reader.close();
			}
		catch (IOException ioe)
			{
			throw new TablesawException(ioe.toString(), -1);
			}
			
		return (new GrepResult((String[])results.toArray(new String[0]), 
				(Integer[])lineNumbers.toArray(new Integer[0])));
		}

//------------------------------------------------------------------------------
/**
	This method either creates an empty file or updateds the last modified time if the 
	file exists.  Similar to the unix touch command.
	
	@param file File to touch.
	@since 1.0.0
*/
	public void touch(String file)
			throws IOException
		{
		File f = new File(m_workingDir, file);
		if (!f.createNewFile())
			f.setLastModified(System.currentTimeMillis());
		}		
		
//------------------------------------------------------------------------------
/**
	This returns a File object for the given file path.  The file path specified must
	be relatvie to the build file in which the call is being made, which may not be
	the current working directory.  This is useful for build files that are being 
	ran in directories other then the one where Tablesaw is originaly called from.
	For example if Tablesaw is called from the work directory and the build file
	specified is proj/build.bsh, Tablesaw internaly will set the working directory
	as /work/proj and all file paths will be relative to there.  If in the build
	script you wish to open your own file handle and call new File("f1");  The File
	object returned will point at /work/f1 not /work/proj/f1.  By calling getRelativeFile("f1")
	in this case will return a File object pointing to /work/proj/f1
	
	@param file Path to the file.  This path is relative to the build script.
	@return A file object pointing to the file specified.
	@since 1.0.0
*/
	public File file(String file)
		{
		return (new File(m_workingDir, file));
		}
		
//------------------------------------------------------------------------------
/**
	Formats a string similar to the standard library API "printf".  Supported flags 
	are d, f, x, X and s.  It suports left and right alignment padding and precision.
	
	@param formatStr Formated string similar to the standard library API printf.
	@param args Array of objects to put into the formated string.
	@since 1.0.0
*/
	public String printf(String formatStr, Object[] args)
		{
		return (Printf.print(formatStr, args));
		}
		
		
//------------------------------------------------------------------------------
/**
	Includes one of the Tablesaw scripts as part of the build.
	
	@param script  Name of the script file to include.
	@since 1.0.0
*/
	public void includeTablesawScript(String script)
			throws TablesawException
		{
		ClassLoader cl = this.getClass().getClassLoader();
		InputStream is = cl.getResourceAsStream("scripts/"+script);
		
		if (is == null)
			throw new TablesawException("Unable to read from file "+script, -1);
			
		InputStreamReader isr = new InputStreamReader(is);
		StringBuffer sb = new StringBuffer();
		int chr;
		
		try
			{
			while ((chr = isr.read()) != -1)
				{
				sb.append((char)chr);
				}
				
			isr.close();
			}
		catch (IOException ioe)
			{
			throw new TablesawException("Unable to read from file "+script, -1);
			}
		
		m_interpreter.eval(sb.toString());
		}
		
//------------------------------------------------------------------------------
	private void initDefinitions()
			throws TablesawException
		{
		m_definitionManager = new DefinitionManager();
		
		m_definitionManager.includeDefinitionFile("etc/definitions.xml");
		new DefinitionsRule(m_definitionManager);
		}
		
	public void includeDefinitionFile(String file)
			throws TablesawException
		{
		if (m_definitionManager == null)
			initDefinitions();
			
		m_definitionManager.includeDefinitionFile(file);
		}
		
//------------------------------------------------------------------------------
	public Definition getDefinition(String name)
			throws TablesawException
		{
		if (m_definitionManager == null)
			initDefinitions();
			
		Definition def = m_definitionManager.getDefinition(name);
		if (def == null)
			throw new TablesawException("Unable to find definition for "+name, -1);
			
		return (def);
		}
		
		
	//===========================================================================
	// New Methods for version 2.0
	//---------------------------------------------------------------------------
/**
	Loads in the ant jar files so ant tasks can be used within tablesaw build scripts.
	This required the ANT_HOME environment variable being set.
	
	@since 1.0.0
*/
	public void initializeAnt()
			throws TablesawException
		{
		m_antClasspath = new Classpath();
		String antHome = getProperty("ANT_HOME");
		if (antHome == null)
			throw new TablesawException("You must set the ANT_HOME environment variable before using ant tasks");
			
		List<String> antJars = new RegExFileSet(antHome+"/lib", ".*\\.jar").getFullFilePaths();
		for (String antJar : antJars)
			{
			//print("Adding "+antJar);
			addClasspath(antJar);
			m_antClasspath.addPath(antJar);
			}
		}
		
	//---------------------------------------------------------------------------
/**
	Returns the Classpath object containing the ant jar files.  Must call
	initializeAnt first.
	
	@since 1.0.0
*/
	public Classpath getAntClasspath()
		{
		return (m_antClasspath);
		}
	}

