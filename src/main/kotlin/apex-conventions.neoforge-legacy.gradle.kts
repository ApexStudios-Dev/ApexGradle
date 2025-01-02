plugins {
    id("net.neoforged.moddev.legacyforge")
    id("apex-conventions.neoforge-base")
}

legacyForge {
    // while this should work fine for mods
    // legacy MinecraftForge ATs are not valid
    // causing the compile to fail running into invalid AT files
    validateAccessTransformers.set(false)
}

dependencies {
    // not included in legacy versions
    compileOnly("org.jetbrains:annotations:26.0.1")
}