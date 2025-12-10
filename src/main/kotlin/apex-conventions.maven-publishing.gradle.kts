import dev.apexstudios.gradle.ApexExtension
import dev.apexstudios.gradle.single.ApexSingleExtension

plugins {
    `maven-publish`
}

val single = project.extensions.findByType(ApexSingleExtension::class.java)
val publishName = single?.getModId()?.get() ?: project.name.lowercase()

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

        if(!ApexExtension.IS_CI)  {
            maven { url = uri(layout.buildDirectory.dir("mavenLocal")) }
            // mavenLocal()
        }
    }
}
