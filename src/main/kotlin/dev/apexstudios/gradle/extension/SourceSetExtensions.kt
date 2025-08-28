package dev.apexstudios.gradle.extension

import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.getByType

object SourceSetExtensions {
    fun SourceSet.extend(project: Project, source: String) {
        sourceSets(project).findByName(source)?.let { extend(it) }
    }

    fun SourceSet.extend(source: SourceSet) {
        compileClasspath += source.output
        runtimeClasspath += source.output
    }

    fun sourceSets(project: Project): SourceSetContainer = project.extensions.getByType(JavaPluginExtension::class).sourceSets
}
