plugins {
    `java-library`
    `kotlin-dsl`
    `maven-publish`

    alias(libs.plugins.ideaext)
    alias(libs.plugins.immaculate)
}

val IS_CI = providers.environmentVariable("CI").map(String::toBoolean).getOrElse(false)
val MAVEN_USER = providers.environmentVariable("MAVEN_USER")
val MAVEN_PASSWORD = providers.environmentVariable("MAVEN_PASSWORD")
val GITHUB_ACTOR = providers.gradleProperty("gpr.user").orElse(providers.environmentVariable("GITHUB_ACTOR"))
val GITHUB_TOKEN = providers.gradleProperty("gpr.token").orElse(providers.environmentVariable("GITHUB_TOKEN"))

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
    implementation(libs.modpublish)
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
        if(MAVEN_USER.isPresent && MAVEN_PASSWORD.isPresent) {
            maven("https://maven.apexstudios.dev/releases") {
                name = "ApexStudios-Releases"

                credentials {
                    username = MAVEN_USER.get()
                    password = MAVEN_PASSWORD.get()
                }

                authentication.create<BasicAuthentication>("basic")
            }
        }

        if(GITHUB_ACTOR.isPresent && GITHUB_TOKEN.isPresent) {
            maven("https://maven.pkg.github.com/ApexStudios-Dev/ApexGradle") {
                name = "ApexStudios-GitHub-Packages"

                credentials {
                    username = GITHUB_ACTOR.get()
                    password = GITHUB_TOKEN.get()
                }
            }
        }

        if(!IS_CI)  {
            maven { url = uri(layout.buildDirectory.dir("mavenLocal")) }
            // mavenLocal()
        }
    }
}

// artifact must be lowercase for github packages
// unsure why its not already, the 'base.archiveName' is set to 'apexgradle'
// but publishing is generating as 'ApexGradle'
afterEvaluate {
    publishing.publications.withType(MavenPublication::class.java).forEach {
        it.artifactId = it.artifactId.lowercase()
    }
}
