package tablesaw.rules;

import tablesaw.MakeAction;
import tablesaw.interpreters.ScriptInterpreter;
import tablesaw.TablesawException;

import java.io.*;
import java.util.*;

public class AntBuildRule extends SimpleRule
		implements MakeAction, Overridable
	{
	private String m_antFile;
	
	public AntBuildRule()
		{
		this("build.xml");
		}
		
	//---------------------------------------------------------------------------
	/**
		@param antFile Name of the ant file to create
	*/
	public AntBuildRule(String antFile)
		{
		super("ant");
		
		addTargets(antFile);
		setMakeAction(this);
		m_antFile = antFile;
		setDescription("Generates build.xml file for running tablesaw under ant");
		}
		
	//---------------------------------------------------------------------------
	/**
		break a path down into individual elements and add to a list.
		example : if a path is /a/b/c/d.txt, the breakdown will be [d.txt,c,b,a]
		@param f input file
		@return a List collection with the individual elements of the path in
			reverse order
	*/
	private List<String> getPathList(File f)
			throws TablesawException
		{
		List<String> l = new ArrayList<String>();
		File r;
		try 
			{
			r = f.getCanonicalFile();
			while(r != null) 
				{
				l.add(r.getName());
				r = r.getParentFile();
				}
			}
		catch (IOException e) 
			{
			throw new TablesawException(e);
			}
			
		return l;
		}

	//---------------------------------------------------------------------------
	/**
		figure out a string representing the relative path of
		'f' with respect to 'r'
		@param r home path
		@param f path of file
	*/
	private String matchPathLists(List<String> r,List<String> f) {
		int i;
		int j;
		String s;
		// start at the beginning of the lists
		// iterate while both lists are equal
		s = "";
		i = r.size()-1;
		j = f.size()-1;
		int commonCount = 0;

		// first eliminate common root
		while((i >= 0)&&(j >= 0)&&(r.get(i).equals(f.get(j)))) 
			{
			commonCount ++;
			i--;
			j--;
			}

		// for each remaining level in the home path, add a ..
		// if commonCount == 1 then we hit the root and no common paths 
		// so we return full path to second file
		if (commonCount > 1)
			{
			for(;i>=0;i--)
				s += ".." + File.separator;
			}
		else
			s += File.separator;

		// for each level in the file path, add the path
		for(;j>=1;j--)
			s += f.get(j) + File.separator;

		// file name
		s += f.get(j);
		return s;
		}

	//---------------------------------------------------------------------------
	/**
		get relative path of File 'f' with respect to 'home' directory
		example : home = /a/b/c
					f    = /a/d/e/x.txt
					s = getRelativePath(home,f) = ../../d/e/x.txt
		@param home base path, should be a directory, not a file, or it doesn't
			make sense
		@param f file to generate path for
		@return path from home to f as a string
	*/
	public String getRelativePath(File home, File f)
			throws TablesawException
		{
		File r;
		List<String> homelist;
		List<String> filelist;
		String s;

		homelist = getPathList(home);
		filelist = getPathList(f);
		s = matchPathLists(homelist,filelist);

		return s;
		}

	//---------------------------------------------------------------------------
	public void doMakeAction(Rule rule)
			throws TablesawException
		{
		try
			{
			System.out.println("Generating build.xml");
			String currentDir = System.getProperty("user.dir");
			ClassLoader cl = m_make.getClass().getClassLoader();
			String tablesawPath = cl.getResource("tablesaw/Tablesaw.class").getPath();
			tablesawPath = tablesawPath.substring(5, tablesawPath.indexOf('!'));
			
			tablesawPath = getRelativePath(new File(currentDir), new File(tablesawPath));
			
			ScriptInterpreter si = m_make.getScriptInterpreter();
			if (si == null)
				throw new TablesawException("No script interpeter loaded, unable to create ant file");
				
			Class<?> scriptClass = si.getInterpreterClass();
			String interpPath = scriptClass.getName().replace('.', '/')+".class";
			cl = scriptClass.getClassLoader();
			interpPath = cl.getResource(interpPath).getPath();
			
			interpPath = interpPath.substring(5, interpPath.indexOf('!'));
			
			interpPath = getRelativePath(new File(currentDir), new File(interpPath));
			
			PrintWriter pw = new PrintWriter(m_antFile);
			
			pw.println("<project name=\"tablesaw\" basedir=\".\" default=\"compile\">");
			pw.println("	<path id=\"tablesaw-path\">");
			pw.println("		<pathelement location=\""+tablesawPath+"\"/>");
			pw.println("		<pathelement location=\""+interpPath+"\"/>");
			pw.println("	</path>");
			pw.println();
			
			for (String r : m_make.getNamedRules())
				{
				if (r.equals("ant"))
					continue;
					
				Rule namedRule = m_make.findRule(r);
				String description = namedRule.getDescription() == null ? "" : namedRule.getDescription();
				pw.println("	<target name=\""+r+"\" description=\""+escapeXML(description)+"\">");
				pw.println("		<java classname=\"tablesaw.Tablesaw\" fork=\"true\" classpathref=\"tablesaw-path\" failonerror=\"true\">");
				pw.println("			<arg value=\""+r+"\"/>");
				pw.println("		</java>");
				pw.println("	</target>");
				pw.println();
				}
				
			pw.println("</project>");
			pw.close();
			
			}
		catch (IOException ioe)
			{
			throw new TablesawException(ioe);
			}
		
		}

	private static String escapeXML(String text)
		{
		return (text.replace("&", "&amp;").replace("<", "&lt;")
			.replace(">", "&gt;").replace("\"", "&quot;"));
		}
	}
