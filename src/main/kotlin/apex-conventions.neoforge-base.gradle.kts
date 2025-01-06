import dev.apexstudios.gradle.ApexExtension
import net.neoforged.moddevgradle.dsl.ModDevExtension
import org.slf4j.event.Level

plugins {
    id("apex-conventions.java")
}

extensions.configure(ModDevExtension::class.java) {
    validateAccessTransformers.set(true)

    val atFile = file("src/main/resources/META-INF/accesstransformer.cfg")

    if(atFile.exists()) {
        accessTransformers {
            from(atFile)
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

            jvmArguments.addAll(type.map {
                if(ApexExtension.IS_CI || !(it.equals("client") || it.equals("server")))
                    return@map emptyList<String>()

                return@map listOf(
                    "-XX:+AllowEnhancedClassRedefinition",
                    "-XX:+IgnoreUnrecognizedVMOptions",
                    "-XX:+AllowRedefinitionToAddDeleteMethods",
                    "-XX:+ClassUnloading"
                )
            })
        }
    }
}