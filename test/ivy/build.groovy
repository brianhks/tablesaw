import tablesaw.rules.*
import tablesaw.addons.java.*
import tablesaw.addons.ivy.*

ivy = new IvyAddon().addSettingsFile("test/ivy/ivysettings.xml").setup()

jp = new JavaProgram().setup()

jp.getCompileRule().addDepends(ivy.getResolveRule("default"))


