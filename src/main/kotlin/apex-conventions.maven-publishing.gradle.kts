import dev.apexstudios.gradle.ApexExtension

plugins {
    `maven-publish`
}

publishing {
    publications.create("release", MavenPublication::class.java) {
        afterEvaluate {
            groupId = project.group as String
            artifactId = project.name.lowercase()
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
