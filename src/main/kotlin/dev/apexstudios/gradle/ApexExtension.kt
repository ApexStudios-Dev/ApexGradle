package dev.apexstudios.gradle

import dev.apexstudios.gradle.extension.SourceSetExtensions
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.provider.Property
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.internal.impldep.org.apache.http.client.ResponseHandler
import org.gradle.jvm.toolchain.JvmVendorSpec
import org.gradle.kotlin.dsl.maven
import javax.inject.Inject

abstract class ApexExtension : BaseApexExtension {
    abstract override fun getJavaVendor(): Property<JvmVendorSpec>

    override fun extendCompilerErrors(extendWarnings: Boolean) {
        getProject().tasks.withType(JavaCompile::class.java) {
            options.compilerArgs.addAll(arrayOf("-Xmaxerrs", "9000"))

            if(extendWarnings) {
                options.compilerArgs.addAll(arrayOf("-Xmaxwarns", "9000"))
            }
        }
    }

    override fun withSourceSet(name: String, mutator: Action<SourceSet>?): SourceSet = SourceSetExtensions.sourceSets(getProject()).create(name) {
        if(name != SourceSet.MAIN_SOURCE_SET_NAME) {
            val project = getProject()
            project.tasks.findByName("compileJava")?.finalizedBy(compileJavaTaskName)
            project.tasks.findByName("classes")?.finalizedBy(classesTaskName)
            project.tasks.findByName("javadoc")?.finalizedBy(javadocTaskName)
            project.tasks.findByName("processResources")?.finalizedBy(processResourcesTaskName)
            val main = SourceSetExtensions.sourceSets(project).getByName(SourceSet.MAIN_SOURCE_SET_NAME)
            project.tasks.findByName(main.jarTaskName)?.dependsOn(jarTaskName)
            project.tasks.findByName(main.javadocJarTaskName)?.dependsOn(javadocJarTaskName)
            project.tasks.findByName(main.sourcesJarTaskName)?.dependsOn(sourcesJarTaskName)
        }

        neoForge { addModdingDependenciesTo(this@create) }

        mutator?.execute(this)
    }

    @Inject
    constructor(project: Project) {
        getJavaVendor().convention(project.provider { if(IS_CI) JvmVendorSpec.ADOPTIUM else JvmVendorSpec.JETBRAINS })
    }

    companion object {
        const val DATA_NAME = "data"
        val IS_CI = System.getenv("IS_CI").toBoolean()
        val GITHUB_ACTOR = System.getProperty("gpr.user") ?: System.getenv("GITHUB_ACTOR")
        val GITHUB_TOKEN = System.getProperty("gpr.token") ?: System.getenv("GITHUB_TOKEN")
        val GITHUB_PACKAGE_URL = System.getenv("GITHUB_PACKAGE_URL")

        fun getOrCreate(project: Project): ApexExtension {
            var extension = project.extensions.findByType(ApexExtension::class.java)

            if(extension == null)
                extension = project.extensions.create("apex", ApexExtension::class.java)

            return extension
        }

        fun githubPackageUrl(user: String, repo: String): String = "https://maven.pkg.github.com/$user/$repo"

        fun withGithubMaven(repositories: RepositoryHandler, user: String, repo: String, action: Action<MavenArtifactRepository> = Action { }) {
            if(GITHUB_ACTOR != null && GITHUB_TOKEN != null) {
                repositories.maven(githubPackageUrl(user, repo)) {
                    action.execute(this)

                    credentials {
                        username = GITHUB_ACTOR
                        password = GITHUB_TOKEN
                    }
                }
            }
        }

        fun withApexStudiosGithubMaven(repositories: RepositoryHandler, repo: String) = withGithubMaven(repositories, "ApexStudios-Dev", repo) {
            content {
                includeGroup("dev.apexstudios")
            }
        }
    }
}
