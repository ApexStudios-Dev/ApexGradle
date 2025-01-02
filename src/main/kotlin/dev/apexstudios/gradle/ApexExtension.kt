package dev.apexstudios.gradle

import net.neoforged.moddevgradle.dsl.NeoForgeExtension
import net.neoforged.moddevgradle.dsl.RunModel
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JvmVendorSpec
import javax.inject.Inject

abstract class ApexExtension {
    abstract fun getModId(): Property<String>
    abstract fun getJavaVendor(): Property<JvmVendorSpec>
    @Inject abstract fun getProject(): Project

    fun extendCompilerErrors(extendWarnings: Boolean = false) {
        getProject().tasks.withType(JavaCompile::class.java) {
            options.compilerArgs.addAll(arrayOf("-Xmaxerrs", "9000"))

            if(extendWarnings) {
                options.compilerArgs.addAll(arrayOf("-Xmaxwarns", "9000"))
            }
        }
    }

    fun withDataGen(runMutator: Action<RunModel>? = null) {
        val mainSource = SourceSetHelper.sourceSets(getProject()).getByName(SourceSet.MAIN_SOURCE_SET_NAME) {
            resources {
                exclude(".cache")
                srcDir("src/$DATA_NAME/generated")
            }
        }

        val dataSource = withSourceSet(DATA_NAME)

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

                programArguments.addAll(getProject().provider {
                    listOf(
                        "--mod", getModId().get(),
                        "--all",
                        "--output", getProject().file("src/$DATA_NAME/generated").absolutePath,
                        "--existing", getProject().file("src/$DATA_NAME/resources").absolutePath,
                        "--existing", getProject().file("src/${SourceSet.MAIN_SOURCE_SET_NAME}/resources").absolutePath
                    )
                })

                runMutator?.execute(this)
            }
        }
    }

    fun withSourceSet(name: String, extendsMain: Boolean = true, mutator: Action<SourceSet>? = null): SourceSet {
        return SourceSetHelper.sourceSets(getProject()).create(name) {
            if(extendsMain)
                SourceSetHelper.extend(getProject(), this, SourceSet.MAIN_SOURCE_SET_NAME)

            mutator?.execute(this)
        }
    }

    fun neoVersion(loaderVersion: String, parchmentVersion: String?, parchmentMappings: String?) {
        // <major>.<minor>[-beta][-pr-#-<branch>]
        val tokens = loaderVersion.split("-", limit = 3)

        if(tokens.size >= 3) {
            val prNum = tokens[2].split("-")[1]

            getProject().repositories.maven {
                name = "NeoForge - PR #$prNum"
                setUrl("https://prmaven.neoforged.net/NeoForge/pr$prNum")

                content {
                    includeModule("net.neoforged", "testframework")
                    includeModule("net.neoforged", "neoforge")
                }
            }
        }

        neoForge {
            version = loaderVersion

            parchment {
                if(parchmentMappings != null) {
                    var gameVersion = parchmentVersion

                    if(parchmentVersion == null) {
                        val neoVersion = tokens[0]
                        gameVersion = "1.${neoVersion.substring(0, neoVersion.lastIndexOf("."))}"
                    }

                    minecraftVersion.set(gameVersion)
                    mappingsVersion.set(parchmentMappings)
                }
            }
        }
    }

    fun neoVersion(loaderVersion: String, parchmentMappings: String?) = neoVersion(loaderVersion, null, parchmentMappings)
    fun neoVersion(loaderVersion: String) = neoVersion(loaderVersion, null, null)

    @Inject
    constructor(project: Project) {
        getJavaVendor().convention(project.provider { if(IS_CI) JvmVendorSpec.ADOPTIUM else JvmVendorSpec.JETBRAINS })
        getModId().convention(project.provider { project.name.lowercase() })
    }

    private fun neoForge(action: Action<NeoForgeExtension>) = getProject().extensions.configure(NeoForgeExtension::class.java, action)

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
