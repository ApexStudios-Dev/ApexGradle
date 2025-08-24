plugins {
    id("net.neoforged.moddev")
    id("apex-conventions.neoforge-base")
}

neoForge {
    afterEvaluate {
        tasks.withType(Jar::class.java) {
            manifest {
                // .version would point to project.version
                attributes.put("NeoForge-Version", this@neoForge.version)
            }
        }
    }
}
