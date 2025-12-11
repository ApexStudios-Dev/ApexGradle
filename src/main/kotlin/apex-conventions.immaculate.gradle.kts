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

        custom("jspecifyNullable") {
            it.replace("javax.annotation.Nullable", "org.jspecify.annotations.Nullable")
                .replace("org.jetbrains.annotations.Nullable", "org.jspecify.annotations.Nullable")
        }

        custom("jspecifyNonNull") {
            it.replace("javax.annotation.Nonnull", "org.jspecify.annotations.NonNull")
                .replace("org.jetbrains.annotations.NotNull", "org.jspecify.annotations.NonNull")
        }
    }
}

val generatePackageInfos = tasks.register("generatePackageInfos", GeneratePackageInfos::class.java) {
    sourceSets.forEach { it ->
        files.from(it.allSource.filter { it.extension == "java" })
    }

    basePackage.set(provider { project.group as String })
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