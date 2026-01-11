import net.neoforged.moddevgradle.dsl.ModDevExtension

plugins {
    id("apex-conventions.neoforge")
}

sourceSets {
    main {
        resources {
            exclude(".cache")
            srcDir("src/data/generated")
        }
    }

    create("data") {
        resources.setSrcDirs(files())

        compileClasspath += sourceSets[SourceSet.MAIN_SOURCE_SET_NAME].output
        runtimeClasspath += sourceSets[SourceSet.MAIN_SOURCE_SET_NAME].output
    }
}

extensions.configure(ModDevExtension::class.java) {
    afterEvaluate {
        addModdingDependenciesTo(sourceSets["data"])

        mods.create("data") {
            sourceSet(sourceSets[SourceSet.MAIN_SOURCE_SET_NAME])
            sourceSet(sourceSets["data"])
        }

        runs.create("data") {
            if(versionCapabilities.splitDataRuns()) {
                clientData()
            } else {
                data()
            }

            sourceSet.set(sourceSets["data"])
            loadedMods.set(listOf(mods["data"]))
            ideFolderName.set("Data")

            programArguments.addAll(
                "--mod", project.name.lowercase(),
                "--all",
                "--output", file("src/data/generated").absolutePath,
                "--existing", file("src/${SourceSet.MAIN_SOURCE_SET_NAME}/resources").absolutePath
            )
        }
    }
}