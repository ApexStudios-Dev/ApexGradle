package dev.apexstudios.gradle

import net.neoforged.moddevgradle.dsl.NeoForgeExtension
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.tasks.SourceSet
import org.gradle.jvm.toolchain.JvmVendorSpec
import javax.inject.Inject

abstract class BaseApexExtension {
    @Inject abstract fun getProject(): Project
    abstract fun getJavaVendor(): Property<JvmVendorSpec>
    abstract fun extendCompilerErrors(extendWarnings: Boolean = false)
    abstract fun withSourceSet(name: String, mutator: Action<SourceSet>? = null): SourceSet

    fun neoVersion(loaderVersion: String, parchmentVersion: String?, parchmentMappings: String?) {
        // <major>.<minor>[-beta][-pr-#-<branch>]
        val tokens = loaderVersion.split("-", limit = 3)

        if(tokens.size >= 3) {
            val prNum = tokens[2].split("-")[1]

            getProject().repositories.maven {
                name = "NeoForge - PR #$prNum"
                setUrl("https://prmaven.neoforged.net/NeoForge/pr$prNum")

                content {
                    includeModule("net.neoforged", "testframework")
                    includeModule("net.neoforged", "neoforge")
                }
            }
        }

        neoForge {
            version = loaderVersion

            parchment {
                if(parchmentMappings != null) {
                    var gameVersion = parchmentVersion

                    if(parchmentVersion == null) {
                        val neoVersion = tokens[0]
                        gameVersion = "1.${neoVersion.substring(0, neoVersion.lastIndexOf("."))}"
                    }

                    minecraftVersion.set(gameVersion)
                    mappingsVersion.set(parchmentMappings)
                }
            }
        }
    }

    fun neoVersion(loaderVersion: String, parchmentMappings: String?) = neoVersion(loaderVersion, null, parchmentMappings)
    fun neoVersion(loaderVersion: String) = neoVersion(loaderVersion, null, null)

    fun neoForge(action: Action<NeoForgeExtension>) = configureExtension(NeoForgeExtension::class.java, action)
    fun publishing(action: Action<PublishingExtension>) = configureExtension(PublishingExtension::class.java, action)

    fun <TExtension> configureExtension(extensionType: Class<TExtension>, action: Action<TExtension>) {
        val extension = getProject().extensions.findByType(extensionType)

        if(extension != null) {
            action.execute(extension)
        }
    }
}
