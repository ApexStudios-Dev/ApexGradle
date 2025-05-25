pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    versionCatalogs.create("libs") {
        library("moddevgradle", "net.neoforged.moddev", "net.neoforged.moddev.gradle.plugin").version {
            strictly("[2.0.72,2.1.0)")
        }

        plugin("ideaext", "org.jetbrains.gradle.plugin.idea-ext").version("1.1.9")

        version("immaculate", "0.1.6")
        library("immaculate", "dev.lukebemish.immaculate", "dev.lukebemish.immaculate.gradle.plugin").versionRef("immaculate")
        plugin("immaculate", "dev.lukebemish.immaculate").versionRef("immaculate")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "ApexGradle"
