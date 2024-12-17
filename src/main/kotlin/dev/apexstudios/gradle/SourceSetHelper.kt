package dev.apexstudios.gradle

import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer

object SourceSetHelper {
    fun extend(project: Project, target: String, source: String) {
        val sourceSets = sourceSets(project)
        val targetSource = sourceSets.findByName(target)
        val sourceSource = sourceSets.findByName(source)

        if(targetSource != null && sourceSource != null)
            extend(targetSource, sourceSource)
    }

    fun extend(project: Project, target: String, source: SourceSet) {
        sourceSets(project).findByName(target)?.let { extend(it, source) }
    }

    fun extend(project: Project, target: SourceSet, source: String) {
        sourceSets(project).findByName(source)?.let { extend(target, it) }
    }

    fun extend(target: SourceSet, source: SourceSet) {
        target.compileClasspath += source.output
        target.runtimeClasspath += source.output
    }

    fun sourceSets(project: Project): SourceSetContainer = project.extensions.getByType(JavaPluginExtension::class.java).sourceSets
}