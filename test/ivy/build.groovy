import tablesaw.rules.*
import tablesaw.addons.java.*
import tablesaw.addons.ivy.*

ivy = new IvyAddon().setup()

jp = new JavaProgram().setup()

jp.getCompileRule().addDepends(ivy.getResolveRule("default"))


