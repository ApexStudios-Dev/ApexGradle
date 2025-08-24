plugins {
    id("net.neoforged.moddev")
    id("apex-conventions.neoforge-base")
}

neoForge {
    afterEvaluate {
        tasks.withType(Jar::class.java) {
            manifest {
                attributes.put("NeoForm-Version", neoFormVersion)
            }
        }
    }
}
