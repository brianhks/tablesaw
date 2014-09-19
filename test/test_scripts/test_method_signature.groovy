
name = "test"

single = "${name}Single"
list = ["${name}One", "${name}Two", "${name}Three"]

javacDef = saw.getDefinition("sun_javac");

javacDef.set("source", single)
javacDef.set("source", list)
javacDef.set("source", "${name}A", "${name}B")

