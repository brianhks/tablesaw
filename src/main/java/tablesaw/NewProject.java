package tablesaw;

import org.my_jargp.*;
import java.io.*;

public class NewProject
	{
	private static class CommandLine
		{
		public String projectName;
		public boolean beanShell;
		public boolean groovy;
		
		public CommandLine()
			{
			projectName = "test";
			}
		}
		
	private static final ParameterDef[] PARAMETERS =
		{
		new StringDef('n', "projectName"),
		new BoolDef('b', "beanShell"),
		new BoolDef('g', "groovy"),
		};
		
	private static String readStream(InputStream is)
			throws IOException
		{
		byte[] buffer = new byte[1024];
		StringBuilder sb = new StringBuilder();
		
		int size;
		while ((size = is.read(buffer)) != -1)
			{
			sb.append(new String(buffer, 0, size, "UTF-8"));
			}
			
		return (sb.toString());
		}
		
	private static void printHelp()
		{
		System.out.println("Tablesaw NewProject Help");
		System.out.println("Usage: java -cp tablesaw-x.x.x.jar tablesaw.NewProject -n <project name> [-b | -g]");
		System.out.println("   -n : Name of the project, a directory with this name will be created");
		System.out.println("   -b : Create a beanshell build script");
		System.out.println("   -g : Create a groovy build script");
		}
		
	public static void main(String[] args)
			throws Exception
		{
		CommandLine cl = new CommandLine();
		
		ArgumentProcessor proc = new ArgumentProcessor(PARAMETERS);
		proc.processArgs(args, cl);
		
		//Create dir structure
		File baseDir = new File(cl.projectName);
		baseDir.mkdir();
		new File(baseDir, "src/main/java").mkdirs();
		new File(baseDir, "src/test/java").mkdirs();
		new File(baseDir, "src/conf").mkdirs();
		new File(baseDir, "lib").mkdirs();
		
		//Create the build script
		ClassLoader loader = ClassLoader.getSystemClassLoader();
		InputStream buildFileStream;
		String buildFileName;
		
		if (cl.beanShell)
			{
			buildFileStream = loader.getResourceAsStream("tablesaw/templates/build.bsh");
			buildFileName = "build.bsh";
			}
		else if (cl.groovy)
			{
			buildFileStream = loader.getResourceAsStream("tablesaw/templates/build.groovy");
			buildFileName = "build.groovy";
			}
		else
			{
			System.out.println("You did not specify the build type");
			printHelp();
			return;
			}
			
		String buildFile = readStream(buildFileStream);
		buildFileStream.close();
		
		buildFile = buildFile.replace("%%%ProjectName%%%", cl.projectName);
		
		FileWriter buildFileWriter = new FileWriter(new File(baseDir, buildFileName));
		buildFileWriter.write(buildFile);
		buildFileWriter.flush();
		buildFileWriter.close();
		
		//Create manifest file
		InputStream manifestStream = loader.getResourceAsStream("tablesaw/templates/manifest.txt");
		String manifest = readStream(manifestStream);
		FileWriter manifestWriter = new FileWriter(new File(baseDir, "src/conf/manifest.txt"));
		manifestWriter.write(manifest);
		manifestWriter.flush();
		manifestWriter.close();
		}
	}
