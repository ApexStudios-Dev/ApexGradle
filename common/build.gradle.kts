plugins {
    `java-library`
    `maven-publish`

    alias(libs.plugins.ideaext)
}

val IS_CI = providers.environmentVariable("CI").map(String::toBoolean).getOrElse(false)
val MAVEN_USERNAME = providers.environmentVariable("MAVEN_USERNAME")
val MAVEN_PASSWORD = providers.environmentVariable("MAVEN_PASSWORD")

group = "dev.apexstudios.gradle.common"
version = providers.environmentVariable("VERSION").getOrElse("9.9.999")
base.archivesName = "apexgradle-common"
println("ApexGradle-Common: $version")

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
    maven("https://maven.apexstudios.dev/releases")
    maven("https://maven.apexstudios.dev/private")
}

dependencies {
    implementation(gradleApi())
    implementation(libs.ideaext)
    implementation(libs.toml4j)
}

publishing {
    repositories {
        if(MAVEN_USERNAME.isPresent && MAVEN_PASSWORD.isPresent) {
            maven("https://maven.apexstudios.dev/releases") {
                name = "ApexStudios-Releases"

                credentials {
                    username = MAVEN_USERNAME.get()
                    password = MAVEN_PASSWORD.get()
                }

                authentication.create<BasicAuthentication>("basic")
            }

            maven("https://maven.apexstudios.dev/private") {
                name = "ApexStudios-Private"

                credentials {
                    username = MAVEN_USERNAME.get()
                    password = MAVEN_PASSWORD.get()
                }

                authentication.create<BasicAuthentication>("basic")
            }
        }

        if(!IS_CI)  {
            maven { url = uri(layout.buildDirectory.dir("mavenLocal")) }
            // mavenLocal()
        }
    }
}
