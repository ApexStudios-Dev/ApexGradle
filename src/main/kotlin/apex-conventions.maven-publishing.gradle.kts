import dev.apexstudios.gradle.ApexExtension
import dev.apexstudios.gradle.single.ApexSingleExtension

plugins {
    `maven-publish`
}

val single = project.extensions.findByType(ApexSingleExtension::class.java)
val publishName = (single?.getModId() ?: project.name.lowercase()) as String

publishing {
    publications.create("release", MavenPublication::class.java) {
        afterEvaluate {
            groupId = project.group as String
            artifactId = publishName
            version = project.version as String
        }

        from(components["java"])
    }

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
        }

        if(ApexExtension.GITHUB_ACTOR != null && ApexExtension.GITHUB_TOKEN != null) {
            maven("https://maven.pkg.github.com/ApexStudios-Dev/Issues") {
                name = "ApexStudios-GitHub-Packages"

                credentials {
                    username = ApexExtension.GITHUB_ACTOR
                    password = ApexExtension.GITHUB_TOKEN
                }
            }
        }

        if(!ApexExtension.IS_CI)  {
            maven { url = uri(layout.buildDirectory.dir("mavenLocal")) }
            // mavenLocal()
        }
    }
}
