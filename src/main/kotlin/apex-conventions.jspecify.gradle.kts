import dev.apexstudios.gradle.immaculate.GeneratePackageInfos

plugins {
    id("apex-conventions.immaculate")
}

immaculate {
    workflows.getByName("java") {
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

tasks.named("generatePackageInfos", GeneratePackageInfos::class.java) {
    annoations.add("org.jspecify.annotations.NullMarked")
}