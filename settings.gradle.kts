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

        version("ideaext", "1.1.9")
        plugin("ideaext", "org.jetbrains.gradle.plugin.idea-ext").versionRef("ideaext")
        library("ideaext", "gradle.plugin.org.jetbrains.gradle.plugin.idea-ext", "gradle-idea-ext").versionRef("ideaext")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "ApexGradle"

include("mdg")
include("common")
