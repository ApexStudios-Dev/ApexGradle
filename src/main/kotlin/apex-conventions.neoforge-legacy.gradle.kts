plugins {
    id("net.neoforged.moddev.legacyforge")
    id("apex-conventions.neoforge-base")
}

legacyForge {
    // while this should work fine for mods
    // legacy MinecraftForge ATs are not valid
    // causing the compile to fail running into invalid AT files
    validateAccessTransformers.set(false)

    afterEvaluate {
        tasks.withType(Jar::class.java) {
            manifest {
                attributes["MCP-Version"] = mcpVersion
                // .version would point to project.version
                attributes["Forge-Version"] = this@legacyForge.version
            }
        }
    }
}

dependencies {
    // not included in legacy versions
    compileOnly("org.jetbrains:annotations:26.0.1")
}
