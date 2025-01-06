package dev.apexstudios.gradle.multi

import org.gradle.api.tasks.SourceSet
import org.gradle.configurationcache.extensions.capitalized

data class ApexModule(val id: String, val hasData: Boolean, val dependencies: Set<String>, val basePath: String) {
    fun path(path: String): String {
        if(path.contains("src")) {
            for (dep in dependencies) {
                if(id.startsWith("${dep}_")) {
                    val shortId = id.substring(dep.length + 1)
                    val tokens = path.split("/", limit = 3)
                    val newId = if(tokens[1] != SourceSet.MAIN_SOURCE_SET_NAME) "${shortId}${tokens[1].capitalized()}" else shortId
                    var newPath = "${tokens[0]}/$newId"

                    if(tokens.size > 2)
                        newPath += "/${tokens.drop(2).joinToString("/")}"

                    return "$basePath/$newPath"
                }
            }
        }

        return "$basePath/$path"
    }

    fun id(id: String): String {
        return "${this.id}${id.capitalized()}"
    }
}
