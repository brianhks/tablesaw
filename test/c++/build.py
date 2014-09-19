"""
 Copyright (c) 2004, Brian Hawkins
 Permission is granted to use this code without restriction as long
 as this copyright notice appears in all source files.
 
 DOES NOT WORK
 
"""

bldoutdir = "build"
build = make.getProperty("build", "debug")
progBin = "tablesawtest"

# objExt and exeExt are set in the platform specific make file

if make.getProperty("os.name")[:7] == "Windows":
	execfile("windows.py")
else:
	execfile("linux.py")

progBin = progBin + exeExt

srcFiles = [
	"main.cpp",
	"employee.cpp",
	"company.cpp"
	]
	
objs = make.substitute("(.*)\\.cpp", bldoutdir+"/$1"+objExt, srcFiles)

includeDirs = [ "include" ]
	
#-------------------------------------------------------------------
#==-- SET SEARCH PATHS --==
make.addSearchPath(".*\\.hpp", "include")
make.addSearchPath(".*\\.cpp", "src")

#-------------------------------------------------------------------
#==-- RULE FOR CREATING DIRECTORIES --==
make.createDirectoryRule(bldoutdir, None, 0)	
	
#-------------------------------------------------------------------
#==-- COMPILE OBJECT RULE --==
make.createPatternDependency(bldoutdir+"/(.*)\\"+objExt, bldoutdir)
make.createPatternRule(bldoutdir+"/(.*)\\"+objExt, "$1.cpp", "compile", 1)
def compile(target, prereqs):
	cmd = getCompileCommand(target, prereqs[0], includeDirs)
	make.exec(cmd)
	
#-------------------------------------------------------------------
#==-- LINK EXECUTABLE --==
make.createExplicitRule(bldoutdir+"/"+progBin, objs, "link", 1)
def link(target, prereqs):
	print("Linking "+target+" for "+platform)
	cmd = getLinkCommand(target, prereqs)
	make.exec(cmd)
	
#-------------------------------------------------------------------
#==-- TEST --==
make.createPhonyRule("test", bldoutdir+"/"+progBin, "test")
def test(target, prereqs):
	print("Running Test")
	make.exec(bldoutdir, make.fullPath(bldoutdir)+"/"+progBin, 1)
	
	
make.setDefaultTarget(bldoutdir+"/"+progBin)

