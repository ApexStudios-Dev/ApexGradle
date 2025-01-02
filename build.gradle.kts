plugins {
    `java-library`
    `kotlin-dsl`
    `maven-publish`

    alias(libs.plugins.ideaext)
    alias(libs.plugins.immaculate)
}

val IS_CI = System.getenv("CI").toBoolean()

group = "dev.apexstudios"
version = providers.environmentVariable("VERSION").getOrElse("9.9.999")
base.archivesName = "apexgradle"
println("ApexGradle: $version")

idea.module {
    if(!IS_CI) {
        isDownloadSources = true
        isDownloadJavadoc = true
    }

    excludeDirs.addAll(files(
        ".gradle",
        ".idea",
        "build",
        "gradle"
    ))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }

    withSourcesJar()
}

repositories {
    gradlePluginPortal()
    mavenCentral()
    maven("https://maven.neoforged.net/releases")
}

dependencies {
    implementation(libs.moddevgradle)
    implementation(libs.immaculate)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(17)
}

immaculate {
    // try to also match convention script
    workflows.create("java") {
        java()

        noTrailingSpaces()
        noTabs()
        googleFixImports()

        toggleOff = "formatter:off"
        toggleOn = "formatter:on"

        custom("jetbrainsNullable") {
            it.replace("javax.annotation.Nullable", "org.jetbrains.annotations.Nullable")
        }
    }

    workflows.create("kotlin") {
        files.from(fileTree("src").filter { it.extension == "kts" || it.extension == "kt" })

        noTrailingSpaces()
        noTabs()

        toggleOff = "formatter:off"
        toggleOn = "formatter:on"
    }
}

publishing {
    repositories {
        if(System.getenv("MAVEN_USERNAME") != null && System.getenv("MAVEN_PASSWORD") != null) {
            maven("https://maven.apexstudios.dev/releases") {
                name = "ApexStudios-Releases"

                credentials {
                    username = System.getenv("MAVEN_USERNAME")
                    password = System.getenv("MAVEN_PASSWORD")
                }

                authentication.create<BasicAuthentication>("basic")
            }
        } else {
            maven { url = uri(layout.buildDirectory.dir("mavenLocal")) }
            // mavenLocal()
        }
    }
}