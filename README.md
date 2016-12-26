Tablesaw
========

Universal build tool written in Java.

Just like the power tool, your project evolves around a good build tool. 
Your build tool can either expand the possibilities of your project 
or just get in the way. Tablesaw is designed to be small and easy to use. The 
only dependency your project will have to build is Java.


Why should I use Tablesaw?

There are three reasons to use Tablesaw: Dependencies, Size, Flexibility.
*   Dependencies: Building someone else's project is a pain without requiring extra
dependencies for the build tool.  Like requiring Maven 3.2 or higher or requiring 
python 2.7 with a special json library.  Yes you say but Tablesaw requires Java to 
be installed.  Java is probably one of the easiest languages to install on a system.
If you want you can just extract java into some folder and point your path to it. 
Don't I have to install Tablesaw?  No, Tablesaw is small and can be checked in as
part of your code.
*   Size: Tablesaw is only about 260k.  Depending on the script you prefer to use
it can add another 275k to 5M of data.  You can check it all in as part of your
source.  No external build tool.
*   Flexibility: Tablesaw is based on creating dependencies between build artifacts
and source.  Tablesaw can call out to any external program and then verifies the
targets are built.  Tablesaw can build anything as long as it has a command line
interface.

