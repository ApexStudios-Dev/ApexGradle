package dev.apexstudios.gradle.multi

import org.gradle.configurationcache.extensions.capitalized

data class ApexModule(val id: String, val hasData: Boolean, val dependencies: Set<String>, val basePath: String?) {
    fun path(path: String): String {
        return if(basePath == null) "$id/$path" else "$basePath/$id/$path"
    }

    fun id(id: String): String {
        return "${this.id}${id.capitalized()}"
    }
}
