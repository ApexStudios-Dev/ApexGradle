package dev.apexstudios.gradle.common.api;

import dev.apexstudios.gradle.common.api.meta.IMod;
import dev.apexstudios.gradle.common.api.meta.IRun;
import dev.apexstudios.gradle.common.api.util.IPublishingFileCollection;
import org.gradle.api.Action;
import org.gradle.api.JavaVersion;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.provider.Property;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.gradle.jvm.toolchain.JvmVendorSpec;
import org.jetbrains.annotations.ApiStatus;

public interface IApexExtension {
    String NAME = "apex";

    Property<JavaLanguageVersion> getJavaVersion();

    @ApiStatus.NonExtendable
    default void javaVersion(int version) {
        getJavaVersion().set(JavaLanguageVersion.of(version));
    }

    @ApiStatus.NonExtendable
    default void javaVersion(JavaVersion version) {
        getJavaVersion().set(JavaLanguageVersion.of(version.getMajorVersion()));
    }

    Property<JvmVendorSpec> getJvmVendor();

    Property<String> getNeoForgeVersion();

    Property<String> getParchmentMappings();

    Property<String> getParchmentVersion();

    Property<Boolean> getValidateAccessTransformers();

    IPublishingFileCollection getAccessTransformers();

    IPublishingFileCollection getInterfaceInjection();

    NamedDomainObjectContainer<IMod> getMods();

    @ApiStatus.NonExtendable
    default NamedDomainObjectContainer<IMod> mods(Action<NamedDomainObjectContainer<IMod>> action) {
        var mods = getMods();
        action.execute(mods);
        return mods;
    }

    NamedDomainObjectContainer<IRun> getRuns();

    @ApiStatus.NonExtendable
    default NamedDomainObjectContainer<IRun> runs(Action<NamedDomainObjectContainer<IRun>> action) {
        var runs = getRuns();
        action.execute(runs);
        return runs;
    }
}

/*
    apex {
        neoForgeVersion: String, required
        parchmentMappings: String, optional
        parchmentVersion: String, optional - if omitted but 'parchmentMappings' is provided we try to derive the value from 'neoForgeVersion'

        validateAccessTransformers: Boolean, optional, true
        accessTransformers: List<String>, optional
        interfaceInjection: List<String>, optional

        mods {
            val mod = create("<name>") {
                // workspace settings
                // injects any matching files into the matching mod property
                autoDetectAccessTransformers: Boolean, optional, true, "META-INF/accesstransformer*.cfg"
                autoDetectMixinConfigs: Boolean, optional, true, "META-INF/*.mixins.json"
                autoDetectInterfaceInjection: Boolean, optional, true, "META-INF/interfaces.json"
                autoDetectEnumExtensions: Boolean, optional, true, "META-INF/enum_extensions.json"
                autoDetectFeatureFlags: Boolean, optional, true, "META-INF/feature_flags.json"

                injectDependencies: Boolean, optional, true - injects NeoForge/Minecraft dependencies into the mods toml
                directoryName: String, optional

                requiredMods: List<Mod>, optional

                // these matching source sets and runs
                hasDataGen: Boolean, optional, true
                hasGameTest: Boolean, optional, false

                // create run configs for this mod specifically
                // these runs are preset to launch with only this mod (and its dependencies) only
                runs { }

                // properties used for generating 'neoforge.mods.toml', may also used as workspace settings
                modLoader: String, optional
                loaderVersion: String, optional
                license: String, optional
                issueTrackerURL: String, optional
                showAsResourcePack: Boolean, optional
                showAsDataPack: Boolean, optional

                modId: String, optional, `name->lowercased`
                namespace: String, optional
                version: String, optional, "${file.jarVersion}" - requires jar attributes
                displayName: String, optional, `name`
                description: String, optional
                logoFile: File, optional, "logo.png" - path relative to resources, only appended if file exists
                logoBlur: Boolean, optional
                updateJSONURL: String, optional
                modURL: String, optional
                credits: List<String>, optional
                authors: List<String>, optional
                displayURL: String, optional
                enumExtensions: File, optional
                featureFlags: File, optional

                accessTransformers: List<File>, optional, only existing files are appended
                mixinConfigs: List<File>, optional, only existing files are appended

                dependencies {
                    create("<modId>") {
                        type: ReleaseType, optional, REQUIRED, [ REQUIRED, OPTIONAL, INCOMPATIBLE, DISCOURAGED ]
                        versionRange: String, optional
                        ordering: Ordering, optional, NONE, [ BEFORE, AFTER, NONE ]
                        side: Side, optional, BOTH, [ CLIENT, SERVER, BOTH ]
                        referralURL: String, optional
                    }
                }
            }

            runs {
                create("<id>") {
                    ideName: String
                    gameDirectory: File, "run/<type>"
                    environment: Map<String, String>
                    systemProperties: Map<String, String>
                    mainClass: String
                    programArguments: List<String>
                    jvmArguments: List<String>
                    loadedMods: List<Mod>
                    type: String, required, [ client, server, clientData, serverData, data ]
                    logLevel: LogLevel, optional
                    loggingConfigFile: File
                    sourceSet: SourceSet
                    devLogin: Boolean, false
                }
            }
        }
    }
*/
