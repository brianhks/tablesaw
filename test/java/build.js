/*
 * Copyright (c) 2004, Brian Hawkins
 * Permission is granted to use this code without restriction as long
 * as this copyright notice appears in all source files.
 
 THIS BUILD FILE DOES NOT WORK
 
 */
 
//importPackage(Packages.cpmake);
//importPackage(Packages.cpmake.java.JavaProgram);

var jp = saw.initPlugin(new Packages.tablesaw.java.JavaProgram("src", "build", "cpmaketest.jar"));
jp.setManifest("manifest.txt");
jp.createRules();
jp.setDefaultTarget();


