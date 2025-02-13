package dev.apexstudios.gradle.multi

import dev.apexstudios.gradle.ApexExtension
import dev.apexstudios.gradle.extension.SourceSetExtensions
import dev.apexstudios.gradle.extension.SourceSetExtensions.extend
import dev.apexstudios.gradle.single.ApexSingleExtension
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSet
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.plugins.ide.idea.model.IdeaModel

class ModuleBuilder {
    private val id: String
    private val modId: String
    private var hasData: Boolean = false
    private var dependencies: Set<String> = mutableSetOf()
    private var basePath: String? = null

    constructor(id: String, modId: String) {
        this.id = id
        this.modId = modId
    }

    fun hasData(): ModuleBuilder {
        hasData = true
        return this
    }

    fun dependency(vararg dependencies: String): ModuleBuilder {
        this.dependencies += dependencies
        return this
    }

    fun basePath(basePath: String): ModuleBuilder {
        this.basePath = basePath
        return this
    }

    internal fun build(): ApexModule = ApexModule(id, modId, hasData, dependencies.toSet(), basePath)

    class ModulesBuilder {
        var modules: Map<String, ApexModule> = mutableMapOf()

        fun module(id: String, modId: String? = null, action: Action<ModuleBuilder>? = null) {
            val builder = ModuleBuilder(id, modId ?: id.lowercase())
            action?.execute(builder)
            val module = builder.build()
            modules += Pair(id.lowercase(), module)
        }

        fun module(id: String, action: Action<ModuleBuilder>? = null) = module(id, id.lowercase(), action)

        internal fun initialize(single : ApexSingleExtension?, apex: ApexExtension) {
            // stupid hack to fix modules replacing the 'loadedMods' and 'sourceSet' property
            if(single != null) {
                apex.neoForge {
                    runs.configureEach {
                        loadedMods.set(loadedMods.get())
                        sourceSet.set(sourceSet.get())
                    }
                }
            }

            modules.values.forEach { initializeModule(apex, it) }
            modules.values.forEach { setupModuleDependencies(apex, it) }
        }

        private fun initializeModule(apex: ApexExtension, module: ApexModule) {
            val project = apex.getProject()
            val javaExt = project.extensions.getByType(JavaPluginExtension::class.java)
            val mainId = module.id(SourceSet.MAIN_SOURCE_SET_NAME)
            val mainDir = module.path("src/${SourceSet.MAIN_SOURCE_SET_NAME}")

            project.extensions.configure(IdeaModel::class.java) {
                module {
                    excludeDirs.addAll(project.files(
                        module.path(".gradle"),
                        module.path(".idea"),
                        module.path("build"),
                        module.path("gradle"),
                        module.path("run"),
                        module.path("src/${ApexExtension.DATA_NAME}/generated/.cache"),
                    ))
                }
            }

            val main = apex.withSourceSet(mainId) {
                extend(project, SourceSet.MAIN_SOURCE_SET_NAME)
                java.setSrcDirs(project.files("$mainDir/java"))
                resources.setSrcDirs(project.files("$mainDir/resources"))
            }

            javaExt.registerFeature(mainId) {
                withSourcesJar()
                usingSourceSet(main)
                capability(project.group as String, module.modId, project.version as String)
            }

            apex.neoForge {
                val atFile = project.file(module.path("src/${SourceSet.MAIN_SOURCE_SET_NAME}/resources/META-INF/accesstransformer.cfg"))
                val ifaceFile = project.file(module.path("src/${SourceSet.MAIN_SOURCE_SET_NAME}/resources/META-INF/interfaces.json"))

                if(atFile.exists()) {
                    accessTransformers {
                        from(atFile)
                        publish(atFile)
                    }
                }

                if(ifaceFile.exists()) {
                    interfaceInjectionData {
                        from(ifaceFile)
                        publish(ifaceFile)
                    }
                }

                val mainMod = mods.create(mainId) {
                    sourceSet(main)
                }

                runs.configureEach {
                    if(type.map { it == "client" || it == "server" }.getOrElse(false)) {
                        loadedMods.add(mainMod)
                    }
                }
            }

            if(module.hasData) {
                val dataId = module.id(ApexExtension.DATA_NAME)
                val dataDir = module.path("src/${ApexExtension.DATA_NAME}")

                main.resources.srcDir(project.file("${dataDir}/generated"))

                val data = apex.withSourceSet(dataId) {
                    java.setSrcDirs(project.files("$dataDir/java"))
                    resources.setSrcDirs(project.files("$dataDir/resources"))
                    extend(project, SourceSet.MAIN_SOURCE_SET_NAME)
                    extend(main)
                }

                javaExt.registerFeature(dataId) {
                    usingSourceSet(data)
                    withSourcesJar()
                    capability(project.group as String, "${module.modId}-${ApexExtension.DATA_NAME}", project.version as String)
                }

                apex.neoForge {
                    val dataMod = mods.create(dataId) {
                        sourceSet(main)
                        sourceSet(data)
                    }

                    runs.create(dataId) {
                        if(versionCapabilities.splitDataRuns())
                            clientData()
                        else
                            data()

                        sourceSet.set(data)
                        loadedMods.set(listOf(dataMod))
                        ideName.set("${module.modId.capitalized()} - Data")

                        programArguments.addAll(
                            "--mod", module.modId,
                            "--all",
                            "--output", project.file(module.path("src/${ApexExtension.DATA_NAME}/generated")).absolutePath,
                            "--existing", project.file(module.path("src/${ApexExtension.DATA_NAME}/resources")).absolutePath,
                            "--existing", project.file(module.path("src/${SourceSet.MAIN_SOURCE_SET_NAME}/resources")).absolutePath
                        )
                    }
                }
            }
        }

        private fun setupModuleDependencies(apex: ApexExtension, module: ApexModule) {
            val project = apex.getProject()
            val sourceSets = SourceSetExtensions.sourceSets(project)

            val moduleMainId = module.id(SourceSet.MAIN_SOURCE_SET_NAME)
            val moduleDataId = module.id(ApexExtension.DATA_NAME)

            module.dependencies.mapNotNull { modules[it.lowercase()] }.forEach { dep ->
                val depMainId = dep.id(SourceSet.MAIN_SOURCE_SET_NAME)
                sourceSets.getByName(moduleMainId).extend(project, depMainId)

                if(module.hasData && dep.hasData) {
                    val depDataId = dep.id(ApexExtension.DATA_NAME)
                    sourceSets.getByName(moduleDataId).extend(project, depMainId)
                    sourceSets.getByName(moduleDataId).extend(project, depDataId)

                    apex.neoForge {
                        runs.getByName(moduleDataId) {
                            loadedMods.add(mods.getByName(depMainId))
                            loadedMods.add(mods.getByName(depDataId))
                        }
                    }
                }
            }
        }
    }

    companion object {
        fun modules(project: Project, action: Action<ModulesBuilder>) {
            var builder = ModulesBuilder()
            action.execute(builder)
            builder.initialize(project.extensions.findByType(ApexSingleExtension::class.java), ApexExtension.getOrCreate(project))
        }
    }
}
