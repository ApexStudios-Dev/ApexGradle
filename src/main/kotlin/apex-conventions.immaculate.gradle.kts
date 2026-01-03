import dev.apexstudios.gradle.immaculate.GeneratePackageInfos

plugins {
    id("dev.lukebemish.immaculate")
}

immaculate {
    workflows.create("java") {
        java()

        noTrailingSpaces()
        noTabs()

        toggleOff = "formatter:off"
        toggleOn = "formatter:on"

        custom("noWildcardImports") {
            if(it.contains("*;\n")) {
                throw GradleException("No wildcard imports are allowed!")
            }

            it
        }
    }
}

val generatePackageInfos = tasks.register("generatePackageInfos", GeneratePackageInfos::class.java) {
    sourceSets.forEach { it ->
        files.from(it.allSource.filter { it.extension == "java" })
    }

    basePackage.set(provider { project.group as String })
    onlyIf { annoations.isPresent }
}

tasks.register("applyAllFormatting") {
    dependsOn(generatePackageInfos)
    dependsOn(tasks.immaculateApply)

    group = "verification"
}

tasks.register("checkFormatting") {
    dependsOn(tasks.immaculateCheck)
    group = "verification"
}