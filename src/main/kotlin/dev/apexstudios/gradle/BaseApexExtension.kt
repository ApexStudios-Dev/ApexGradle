package dev.apexstudios.gradle

import net.neoforged.moddevgradle.dsl.NeoForgeExtension
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
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

    fun neoFormVersion(neoformVersion: String, parchmentVersion: String?, parchmentMappings: String?) {
        // 1.21.5-20250325.162830
        // <version>-<timestamp>

        neoForge {
            enable {
                // different case to not conflict
                neoFormVersion = neoformVersion
                isDisableRecompilation = ApexExtension.IS_CI
            }
        }

        if(parchmentMappings != null) {
            val gameVersion = parchmentVersion ?: neoformVersion.substring(0, neoformVersion.indexOf("-"))
            parchment(gameVersion, parchmentMappings)
        }
    }

    fun neoFormVersion(neoFormVersion: String, parchmentMappings: String?) = neoFormVersion(neoFormVersion, null, parchmentMappings)
    fun neoFormVersion(neoFormVersion: String) = neoFormVersion(neoFormVersion, null, null)

    fun neoVersion(loaderVersion: String, parchmentVersion: String?, parchmentMappings: String?) {
        // <major>.<minor>[-beta][-pr-<num>-<branch>]
        // 0: <major>.<minor>
        // 1: [-beta]
        // 2: [-pr-<num>-<branch>]
        // var testVersion = "21.8.0"
        // testVersion += "-beta.5"
        // testVersion += "-pr-2574-feature.5"

        if(loaderVersion.contains("pr-")) {
            var prNum = loaderVersion.substring(loaderVersion.indexOf("pr-"))
            prNum = prNum.substring("pr-".length) // <num>-<branch>
            val prNumTokens = prNum.split("-", limit = 2) // [<num>, <branch>]
            // println(prNumTokens)
            neoPrMaven(getProject().repositories, prNumTokens[0].toInt())
        }

        neoForge {
            enable {
                version = loaderVersion
                isDisableRecompilation = ApexExtension.IS_CI
            }
        }

        if(parchmentMappings != null) {
            val tokens = loaderVersion.split("-")
            val gameVersion = parchmentVersion ?: "1.${tokens[0].substring(0, tokens[0].lastIndexOf("."))}"
            parchment(gameVersion, parchmentMappings)
        }
    }

    fun neoVersion(loaderVersion: String, parchmentMappings: String?) = neoVersion(loaderVersion, null, parchmentMappings)
    fun neoVersion(loaderVersion: String) = neoVersion(loaderVersion, null, null)

    fun parchment(parchmentVersion: String, parchmentMappings: String) {
        neoForge {
            parchment {
                minecraftVersion.set(parchmentVersion)
                mappingsVersion.set(parchmentMappings)
            }
        }
    }

    fun neoForge(action: Action<NeoForgeExtension>) = configureExtension(NeoForgeExtension::class.java, action)
    fun publishing(action: Action<PublishingExtension>) = configureExtension(PublishingExtension::class.java, action)

    fun neoPrMaven(repositories: RepositoryHandler, prNum: Int) = repositories.maven {
        name = "NeoForge - PR #$prNum"
        setUrl("https://prmaven.neoforged.net/NeoForge/pr$prNum")

        content {
            includeModule("net.neoforged", "testframework")
            includeModule("net.neoforged", "neoforge")
        }
    }

    fun <TExtension> configureExtension(extensionType: Class<TExtension>, action: Action<TExtension>) {
        val extension = getProject().extensions.findByType(extensionType)

        if(extension != null) {
            action.execute(extension)
        }
    }
}
