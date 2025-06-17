import gradle.kotlin.dsl.accessors._d7f529001d8c5530765df58ed8ac1325.neoForge

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
