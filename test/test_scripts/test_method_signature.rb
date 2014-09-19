
single = "single"
list = ["One", "Two", "Three"]

javacDef = $saw.getDefinition("sun_javac")


javacDef.set("source", single)
javacDef.set("source", list)
javacDef.set("source", "A", "B")

