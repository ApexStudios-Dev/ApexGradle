dependencyResolutionManagement {
    versionCatalogs.create("libs") {
        library("moddevgradle", "net.neoforged.moddev", "net.neoforged.moddev.gradle.plugin").version {
            strictly("[2.0.50-beta,)")
        }

        library("immaculate", "dev.lukebemish.immaculate", "dev.lukebemish.immaculate.gradle.plugin").version("0.1.6")
        library("semver", "net.swiftzer.semver", "semver").version("2.0.0")

        plugin("ideaext", "org.jetbrains.gradle.plugin.idea-ext").version("1.1.8")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
