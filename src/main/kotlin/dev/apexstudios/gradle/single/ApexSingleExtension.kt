package dev.apexstudios.gradle.single

import dev.apexstudios.gradle.ApexExtension
import dev.apexstudios.gradle.ApexExtension.Companion.DATA_NAME
import dev.apexstudios.gradle.BaseApexExtension
import dev.apexstudios.gradle.extension.SourceSetExtensions
import dev.apexstudios.gradle.extension.SourceSetExtensions.extend
import net.neoforged.moddevgradle.dsl.RunModel
import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Property
import org.gradle.api.tasks.SourceSet
import org.gradle.jvm.toolchain.JvmVendorSpec
import javax.inject.Inject

abstract class ApexSingleExtension : BaseApexExtension {
    abstract fun getModId(): Property<String>
    fun apex(): ApexExtension = ApexExtension.getOrCreate(getProject())
    override fun getJavaVendor(): Property<JvmVendorSpec> = apex().getJavaVendor()
    override fun extendCompilerErrors(extendWarnings: Boolean) = apex().extendCompilerErrors(extendWarnings)
    override fun withSourceSet(name: String, mutator: Action<SourceSet>?): SourceSet = apex().withSourceSet(name, mutator)

    fun withDataGen(runMutator: Action<RunModel>? = null) {
        val project = getProject()
        val mainSource = SourceSetExtensions.sourceSets(project).getByName(SourceSet.MAIN_SOURCE_SET_NAME) {
            resources {
                exclude(".cache")
                srcDir("src/$DATA_NAME/generated")
            }
        }

        val dataSource = withSourceSet(DATA_NAME, true)

        neoForge {
            addModdingDependenciesTo(dataSource)

            val mod = mods.create(DATA_NAME) {
                sourceSet(mainSource)
                sourceSet(dataSource)
            }

            runs.create(DATA_NAME) {
                if(versionCapabilities.splitDataRuns())
                    clientData()
                else
                    data()

                sourceSet.set(dataSource)
                loadedMods.set(listOf(mod))

                programArguments.addAll(project.provider {
                    listOf(
                        "--mod", getModId().get(),
                        "--all",
                        "--output", project.file("src/$DATA_NAME/generated").absolutePath,
                        "--existing", project.file("src/$DATA_NAME/resources").absolutePath,
                        "--existing", project.file("src/${SourceSet.MAIN_SOURCE_SET_NAME}/resources").absolutePath
                    )
                })

                runMutator?.execute(this)
            }
        }
    }

    fun withSourceSet(name: String, extendsMain: Boolean = true, mutator: Action<SourceSet>? = null): SourceSet = withSourceSet(name) {
        if(extendsMain)
            extend(getProject(), SourceSet.MAIN_SOURCE_SET_NAME)

        mutator?.execute(this)
    }

    @Inject
    constructor(project: Project) {
        getModId().convention(project.provider { project.name.lowercase() })
    }

    companion object {
        fun getOrCreate(project: Project): ApexSingleExtension {
            if(project.extensions.findByType(ApexSingleExtension::class.java) != null) {
                throw GradleException("Cannot apply ApexSingleExtension when ApexMultiExtension is already applied")
            }

            var extension = project.extensions.findByType(ApexSingleExtension::class.java)

            if(extension == null)
                extension = project.extensions.create("apex_single", ApexSingleExtension::class.java)

            return extension
        }
    }
}
