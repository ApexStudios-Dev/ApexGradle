package dev.apexstudios.gradle

abstract class ApexExtension {
    companion object {
        val IS_CI = System.getenv("CI").toBoolean()
    }
}
