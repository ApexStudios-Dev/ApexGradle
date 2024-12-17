plugins {
    id("dev.lukebemish.immaculate")
}

immaculate {
    workflows.create("java") {
        java()
        trailingNewline()
        noTabs()
        googleFixImports()

        toggleOff = "spotless:off"
        toggleOn = "spotless:on"

        custom("jetbrainsNullable") {
            it.replace("javax.annotation.Nullable", "org.jetbrains.annotations.Nullable")
        }
    }
}