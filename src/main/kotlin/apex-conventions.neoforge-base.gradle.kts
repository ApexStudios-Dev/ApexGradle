import dev.apexstudios.gradle.ApexExtension
import net.neoforged.moddevgradle.dsl.ModDevExtension
import org.slf4j.event.Level

plugins {
    id("apex-conventions.java")
}

extensions.configure(ModDevExtension::class.java) {
    validateAccessTransformers.set(true)

    val atFile = file("src/${SourceSet.MAIN_SOURCE_SET_NAME}/resources/META-INF/accesstransformer.cfg")
    val ifaceFile = project.file("src/${SourceSet.MAIN_SOURCE_SET_NAME}/resources/META-INF/interfaces.json")

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

    mods {
        create(SourceSet.MAIN_SOURCE_SET_NAME) {
            sourceSet(sourceSets[SourceSet.MAIN_SOURCE_SET_NAME])
        }
    }

    runs {
        create("client") {
            client()
        }

        create("server") {
            server()
        }

        configureEach {
            logLevel.set(Level.DEBUG)
            gameDirectory.set(type.map { layout.projectDirectory.dir("run/$it") })
            // sourceSet.convention(sourceSets[SourceSet.MAIN_SOURCE_SET_NAME])
            // loadedMods.convention(mods)
            systemProperty("terminal.ansi", "true") // fix terminal not having colors
            ideFolderName.set(type.map { it.capitalize() })

            jvmArguments.addAll(type.map {
                if(ApexExtension.IS_CI || !(it.equals("client") || it.equals("server")))
                    return@map emptyList()

                return@map listOf(
                    "-XX:+AllowEnhancedClassRedefinition",
                    "-XX:+IgnoreUnrecognizedVMOptions",
                    "-XX:+AllowRedefinitionToAddDeleteMethods",
                    "-XX:+ClassUnloading"
                )
            })
        }
    }

    afterEvaluate {
        tasks.withType(Jar::class.java) {
            manifest {
                attributes["Minecraft-Version"] = minecraftVersion

                if(parchment.enabled.get()) {
                    attributes["Parchment"] = "${parchment.minecraftVersion.get()}-${parchment.mappingsVersion.get()}"
                }
            }
        }
    }
}
