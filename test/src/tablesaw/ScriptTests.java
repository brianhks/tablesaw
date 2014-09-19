package tablesaw;


import org.junit.Test;

public class ScriptTests
{
	//---------------------------------------------------------------------------
	public void processBuildFile(String dir, String buildFile)
			throws Exception
		{
		Tablesaw make = new Tablesaw(dir);
		make.init();
		make.processBuildFile(buildFile);
		make.close();
		}
		
	//---------------------------------------------------------------------------
	public void buildTarget(String dir, String buildFile, String target)
			throws Exception
		{
		Tablesaw make = new Tablesaw(dir);
		//make.setVerbose(true);
		make.init();
		make.processBuildFile(buildFile);
		make.buildTarget(target);
		make.close();
		}
	
	//---------------------------------------------------------------------------
	@Test
	public void groovyMethodSignatureTest()
		throws Exception
	{
		processBuildFile("test/test_scripts", "test/test_scripts/test_method_signature.groovy");
	}
	
	//---------------------------------------------------------------------------
	@Test
	public void beanShellMethodSignatureTest()
		throws Exception
	{
		processBuildFile("test/test_scripts", "test/test_scripts/test_method_signature.bsh");
	}
	
	//---------------------------------------------------------------------------
	@Test
	public void rhinoMethodSignatureTest()
		throws Exception
	{
		processBuildFile("test/test_scripts", "test/test_scripts/test_method_signature.js");
	}
	
	//---------------------------------------------------------------------------
	@Test
	public void rubyMethodSignatureTest()
		throws Exception
	{
		processBuildFile("test/test_scripts", "test/test_scripts/test_method_signature.rb");
	}
	
	//---------------------------------------------------------------------------
	@Test
	public void jythonMethodSignatureTest()
		throws Exception
	{
		processBuildFile("test/test_scripts", "test/test_scripts/test_method_signature.py");
	}
	
	//---------------------------------------------------------------------------
	@Test
	public void javaBuildTest_jar()
		throws Exception
	{
		buildTarget("test/java", "test/java/build.bsh", "jar");
	}
	
	//---------------------------------------------------------------------------
	@Test
	public void javaBuildTest_clean()
		throws Exception
	{
		buildTarget("test/java", "test/java/build.bsh", "clean");
	}
	
	//---------------------------------------------------------------------------
	@Test
	public void java_optBuildTest_jar()
		throws Exception
	{
		buildTarget("test/java_opt", "test/java_opt/build.bsh", "jar");
	}
	
	//---------------------------------------------------------------------------
	@Test
	public void java_optBuildTest_clean()
		throws Exception
	{
		buildTarget("test/java_opt", "test/java_opt/build.bsh", "clean");
	}
	
	//---------------------------------------------------------------------------
	@Test
	public void cppBuildTest_link()
		throws Exception
	{
		buildTarget("test/c++", "test/c++/build.bsh", "link");
	}
	
	//---------------------------------------------------------------------------
	@Test
	public void cppBuildTest_clean()
		throws Exception
	{
		buildTarget("test/c++", "test/c++/build.bsh", "clean");
	}
}
