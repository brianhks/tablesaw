/*
 * Copyright (c) 2010, Brian Hawkins
 * Permission is granted to use this code without restriction as long
 * as this copyright notice appears in all source files.
 */
 
import tablesaw.*;
import tablesaw.rules.*;
 
import java.io.File
import tablesaw.definitions.Definition;

bldoutdir = 'build'
bld = saw.getProperty('build', 'debug')
progBin = 'tablesawtest'

saw.includeDefinitionFile("compilers.xml")

/*
objExt and exeExt are set in the platform specific make file
*/
if (saw.getProperty("os.name").startsWith("Windows")) 
{
	compilerDef = saw.getDefinition("vc7_compiler")
	linkerDef = saw.getDefinition("vc7_linker")
}
else
{
	compilerDef = saw.getDefinition("linux_gcc_compiler")
	linkerDef = saw.getDefinition("linux_gcc_linker")
}

compilerDef.add("user_define", "_CRT_SECURE_NO_WARNINGS=1")

bldoutdir += "/"+compilerDef.getProperty("platform")

//Set generic options for the compilers
compilerDef.setMode("debug")
linkerDef.setMode(["debug", "executable"])
compilerDef.set("warning_hi")
compilerDef.set("error_on_warning")

//Retreive the platform specific object file extension.  Used in rule creation
objExt = compilerDef.getProperty("object_extension");

progBin = progBin + linkerDef.getProperty("executable_sufix")

srcFiles = [
		"main.cpp", 
		"employee.cpp", 
		"company.cpp"]

compileRule = new PatternRule().addSources(srcFiles)
		.setSourcePattern("(.*)\\.cpp")
		.setTargetPattern(bldoutdir+"/\$1"+objExt)
		.setMakeAction("compile");
		
includeDirs = [ "include" ]
compilerDef.set("user_include_path", includeDirs)

//-------------------------------------------------------------------
//==-- SET SEARCH PATHS --==
saw.addSearchPath(".*\\.hpp", "include")
saw.addSearchPath(".*\\.cpp", "src")

//-------------------------------------------------------------------
//==-- RULE FOR CREATING DIRECTORIES --==
compileRule.addDepend(new DirectoryRule(bldoutdir));
	
//-------------------------------------------------------------------
//==-- COMPILE OBJECT RULE --==
def compile(Rule cpRule) 
	{
	source = cpRule.getSource();
	//This bit is to print out which file is being compiled.  VC does that automatically so we dont want it twice
	if (!compilerDef.getName().equals("vc7_compiler"))
		println(new File(source).getName());
	
	compilerDef.set("source_file", source);
	compilerDef.set("object_file", cpRule.getTarget());
	cmd = compilerDef.getCommand();
	saw.exec(cmd);
	}
	
//-------------------------------------------------------------------
//==-- LINK EXECUTABLE --==
linkRule = new SimpleRule().addTarget(bldoutdir+"/"+progBin)
		.addDepend(compileRule)
		.setMakeAction("link");
def link(Rule rule) 
	{
	println("Linking "+rule.getTarget()+" for "+linkerDef.getProperty("platform"));
	linkerDef.set("object_files", compileRule.getPatternTargets());
	linkerDef.set("out_file", rule.getTarget());
	cmd = linkerDef.getCommand();
	saw.exec(cmd);
	}
	
//-------------------------------------------------------------------
//==-- TEST --==
new SimpleRule("test").addDepend(linkRule).setMakeAction("test");
def test(Rule rule) 
	{
	println("Running Test")
	saw.exec(bldoutdir, saw.fullPath(bldoutdir)+"/"+progBin, true)
	}
	
saw.setDefaultTarget(bldoutdir+"/"+progBin)

