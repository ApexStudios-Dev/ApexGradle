pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.apexstudios.dev/releases")
        maven("https://maven.apexstudios.dev/private")
    }
}

dependencyResolutionManagement {
    versionCatalogs.create("libs") {
        library("moddevgradle", "net.neoforged.moddev", "net.neoforged.moddev.gradle.plugin").version {
            strictly("[2.0.72,2.1.0)")
        }

        plugin("ideaext", "org.jetbrains.gradle.plugin.idea-ext").version("1.1.9")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "ApexGradle"
