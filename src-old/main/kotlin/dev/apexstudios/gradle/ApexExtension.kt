package dev.apexstudios.gradle

import dev.apexstudios.gradle.extension.SourceSetExtensions
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JvmVendorSpec
import javax.inject.Inject

abstract class ApexExtension @Inject constructor(project: Project) : BaseApexExtension() {
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
            val main = SourceSetExtensions.sourceSets(project).getByName(SourceSet.MAIN_SOURCE_SET_NAME)

            val jarTask = project.tasks.register(jarTaskName, Jar::class.java) {
                archiveBaseName.set(project.provider { "${project.name.lowercase()}-$name" })
                from(output)
            }

            val sourcesJar = project.tasks.register(sourcesJarTaskName, Jar::class.java) {
                archiveClassifier.set("sources")
                archiveBaseName.set(project.provider { "${project.name.lowercase()}-$name" })
                from(allSource)
            }

            project.artifacts {
                add("archives", jarTask)
                add("archives", sourcesJar)
            }

            project.tasks.findByName("compileJava")?.finalizedBy(compileJavaTaskName)
            project.tasks.findByName("classes")?.finalizedBy(classesTaskName)
            // project.tasks.findByName("javadoc")?.finalizedBy(javadocTaskName)
            project.tasks.findByName("processResources")?.finalizedBy(processResourcesTaskName)
            project.tasks.findByName(main.jarTaskName)?.dependsOn(jarTask)
            // project.tasks.findByName(main.javadocJarTaskName)?.dependsOn(javadocJarTaskName)
            project.tasks.findByName(main.sourcesJarTaskName)?.dependsOn(sourcesJar)
        }

        resources.exclude(".cache", "**/*.bbmodel")

        neoForge { addModdingDependenciesTo(this@create) }

        mutator?.execute(this)
    }

    init {
        getJavaVendor().convention(project.provider { if(IS_CI) JvmVendorSpec.ADOPTIUM else JvmVendorSpec.JETBRAINS })
    }

    companion object {
        const val DATA_NAME = "data"
        val IS_CI = System.getenv("IS_CI").toBoolean()

        fun getOrCreate(project: Project): ApexExtension {
            var extension = project.extensions.findByType(ApexExtension::class.java)

            if(extension == null)
                extension = project.extensions.create("apex", ApexExtension::class.java)

            return extension
        }
    }
}
