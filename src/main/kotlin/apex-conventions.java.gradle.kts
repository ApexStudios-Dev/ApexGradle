import dev.apexstudios.gradle.ApexExtension


plugins {
    `java-library`
    id("org.jetbrains.gradle.plugin.idea-ext")
}

val apex = ApexExtension.Companion.getOrCreate(project)

base.archivesName = project.name.lowercase()

idea.module {
    if(!ApexExtension.Companion.IS_CI) {
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
        languageVersion.set(apex.getJavaVersion())
        vendor.set(apex.getJavaVendor())
    }

    withSourcesJar()
}

javaToolchains.compilerFor {
    languageVersion.set(apex.getJavaVersion())
    vendor.set(apex.getJavaVendor())
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(apex.getJavaVersion().map(JavaLanguageVersion::asInt))
}

repositories {
    maven("https://maven.apexstudios.dev/releases") {
        content {
            includeGroup("dev.apexstudios")
        }
    }
}