package dev.apexstudios.gradle

import net.neoforged.moddevgradle.dsl.NeoForgeExtension
import net.neoforged.moddevgradle.dsl.RunModel
import net.swiftzer.semver.SemVer
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JvmVendorSpec
import javax.inject.Inject

abstract class ApexExtension {
    abstract fun getModId(): Property<String>
    abstract fun getJavaVersion(): Property<JavaLanguageVersion>
    abstract fun getJavaVendor(): Property<JvmVendorSpec>
    @Inject abstract fun getProject(): Project

    fun javaVersion(javaVersion: Provider<Int>) = getJavaVersion().convention(javaVersion.map(JavaLanguageVersion::of))
    fun javaVersion(javaVersion: Int) = javaVersion(getProject().provider { javaVersion })

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

        getProject().extensions.configure(NeoForgeExtension::class.java) {
            addModdingDependenciesTo(dataSource)

            val mod = mods.create(DATA_NAME) {
                sourceSet(mainSource)
                sourceSet(dataSource)
            }

            runs.create(DATA_NAME) {
                sourceSet.set(dataSource)
                loadedMods.set(listOf(mod))
                type.set(version.map {
                    if(SemVer.parse(it) < TWENTY_ONE_FOUR)
                        return@map "data"
                    else
                        return@map "serverData"
                })

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

    @Inject
    constructor(project: Project) {
        getJavaVersion().convention(JavaLanguageVersion.of(21))
        getJavaVendor().convention(project.provider { if(IS_CI) JvmVendorSpec.ADOPTIUM else JvmVendorSpec.JETBRAINS })
        getModId().convention(project.provider { project.name.lowercase() })
    }

    companion object {
        const val DATA_NAME = "data"
        val IS_CI = System.getenv("IS_CI").toBoolean()

        private val TWENTY_ONE_FOUR = SemVer.parse("21.4.0")

        fun getOrCreate(project: Project): ApexExtension {
            var extension = project.extensions.findByType(ApexExtension::class.java)

            if(extension == null)
                extension = project.extensions.create("apex", ApexExtension::class.java)

            return extension
        }
    }
}
