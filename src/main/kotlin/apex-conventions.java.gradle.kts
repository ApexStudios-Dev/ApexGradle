import dev.apexstudios.gradle.ApexExtension

plugins {
    `java-library`
    id("org.jetbrains.gradle.plugin.idea-ext")
}

val apex = ApexExtension.getOrCreate(project)

base.archivesName = project.name.lowercase()

idea.module {
    if(!ApexExtension.IS_CI) {
        isDownloadSources = true
        isDownloadJavadoc = true
    }

    excludeDirs.addAll(files(
        ".gradle",
        ".idea",
        "gradle",
    ))
}

java {
    toolchain {
        vendor.set(apex.getJavaVendor())
    }

    withSourcesJar()
}

javaToolchains.compilerFor {
    vendor.set(apex.getJavaVendor())
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

repositories {
    maven("https://maven.apexstudios.dev/releases") {
        content {
            includeGroup("dev.apexstudios")
        }
    }
}