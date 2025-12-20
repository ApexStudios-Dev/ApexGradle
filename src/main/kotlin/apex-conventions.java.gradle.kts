import java.text.SimpleDateFormat
import java.util.*

plugins {
    `java-library`
    id("org.jetbrains.gradle.plugin.idea-ext")
}

val IS_CI = providers.environmentVariable("CI").map(String::toBoolean).getOrElse(false)

version = providers.environmentVariable("VERSION").getOrElse("9.9.999")
base.archivesName = project.name.lowercase()

idea.module {
    if(!IS_CI) {
        isDownloadSources = true
        isDownloadJavadoc = true
    }

    excludeDirs.addAll(files(
        ".gradle",
        ".idea",
        "gradle",
    ))
}

sourceSets.main {
    resources.exclude("**/*.bbmodel")
}

java {
    withSourcesJar()
}

tasks.withType(Jar::class.java) {
    manifest {
        attributes.putAll(mutableMapOf(
            "Specification-Title" to project.name,
            "Specification-Vendor" to "ApexStudios",
            "Specification-Version" to "1",

            "Implementation-Title" to project.name,
            "Implementation-Vendor" to "ApexStudios",
            "Implementation-Version" to project.version,
            "Implementation-Timestamp" to SimpleDateFormat("dd-MM-yyyy'T'HH:mm:ssZ").format(Date())
        ))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

repositories {
    maven("https://maven.apexstudios.dev/proxy")
}
