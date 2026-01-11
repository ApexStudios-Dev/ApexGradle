package dev.apexstudios.gradle

import org.gradle.api.Project

abstract class ApexExtension {
    companion object {
        val IS_CI = System.getenv("CI").toBoolean()

        fun modId(project: Project): String {
            return project.name.replace('-', '_').lowercase()
        }
    }
}
