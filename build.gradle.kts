plugins {
    `java-library`
    `kotlin-dsl`
    `maven-publish`

    alias(libs.plugins.ideaext)
    alias(libs.plugins.immaculate)
}

val IS_CI = providers.environmentVariable("CI").map(String::toBoolean).getOrElse(false)
val MAVEN_USERNAME = providers.environmentVariable("MAVEN_USERNAME")
val MAVEN_PASSWORD = providers.environmentVariable("MAVEN_PASSWORD")

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
    maven("https://maven.apexstudios.dev/private")
    gradlePluginPortal()
    mavenCentral()
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
