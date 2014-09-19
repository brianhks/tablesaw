"""
 * Copyright (c) 2004, Brian Hawkins
 * Permission is granted to use this code without restriction as long
 * as this copyright notice appears in all source files.
 
 THIS BUILD FILE DOES NOT WORK
 
"""
 
bldoutdir = "build"
srcdir = "src"
 
sourceFiles = make.createFileList(srcdir, ".*\\.java")	

classFiles = make.substitute("(.*)\\.java", bldoutdir+"/$1.class", sourceFiles)
jarFiles = make.substitute("(.*)\\.java", "$1.class", sourceFiles)

compileList = ""

#//-------------------------------------------------------------------
#//==-- SET SEARCH PATHS --==
make.addSearchPath(srcdir)

#//-------------------------------------------------------------------
#//==-- RULE FOR CREATING DIRECTORIES --==
make.createDirectoryRule(bldoutdir, None, 1)
make.createDirectoryRule("doc", None, 1)

#//-------------------------------------------------------------------
#//==-- REMOVE OLD CLASS FILES --==
make.createPatternRule(bldoutdir+"/(.*).class", "$1.java", "removeClass", 0)
def removeClass(target, prereqs):
	print(prereqs[0])
	make.delete(target)
	global compileList
	compileList = compileList + prereqs[0] + " "

#//-------------------------------------------------------------------
#//==-- COMPILE CLASS FILES --==
make.createPhonyRule("compile", bldoutdir+" "+make.arrayToString(classFiles), "compile")	
def compile(target, prereqs):
	cmd = "javac -classpath "+bldoutdir+" -d "+bldoutdir+" "+compileList
	
	make.exec(cmd, 1)
	
#//-------------------------------------------------------------------
#//==-- CREATE JAR FILE --==
make.createExplicitRule("tablesawtest.jar", "compile", "createJar", 1)	
def createJar(target, prereqs):		
	print("Creating "+target)
	cmd = "jar -cfm "+target+" manifest.txt -C "+bldoutdir+" ."

	make.exec(cmd)

#//-------------------------------------------------------------------
#//==-- CREATE JAVADOCS --==
make.createPhonyRule("javadoc", "doc", "javadoc")
def javadoc(target, prereqs):
	make.exec(srcdir, "javadoc -public -d ../doc "+make.arrayToString(sourceFiles), 1)
	
#//-------------------------------------------------------------------
#//==-- TEST --==
make.createPhonyRule("test", "tablesawtest.jar", "test");
def test(target, prereqs):
	print("Running test")
	make.exec("java -jar tablesawtest.jar")
	

make.setDefaultTarget("tablesawtest.jar")


