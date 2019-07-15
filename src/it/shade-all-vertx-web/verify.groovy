build_jar = "shade-all-it-1.0.0.jar"

File originalJar = new File(basedir, "target/original-${build_jar}")
assert originalJar.isFile()

File shadedJar = new File(basedir, "target/${build_jar}")
assert shadedJar.isFile()

// TODO look into the content of the shaded stuff!