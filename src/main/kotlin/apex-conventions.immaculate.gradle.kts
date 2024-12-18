plugins {
    id("dev.lukebemish.immaculate")
}

immaculate {
    workflows.create("java") {
        java()

        noTrailingSpaces()
        noTabs()
        googleFixImports()

        toggleOff = "formatter:off"
        toggleOn = "formatter:on"

        custom("jetbrainsNullable") {
            it.replace("javax.annotation.Nullable", "org.jetbrains.annotations.Nullable")
        }
    }
}