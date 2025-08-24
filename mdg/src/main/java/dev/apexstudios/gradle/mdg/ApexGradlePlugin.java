package dev.apexstudios.gradle.mdg;

import dev.apexstudios.gradle.common.api.IApexExtension;
import dev.apexstudios.gradle.common.api.meta.IMod;
import dev.apexstudios.gradle.common.api.meta.IRun;
import dev.apexstudios.gradle.common.api.util.IPublishingFileCollection;
import dev.apexstudios.gradle.common.api.util.Util;
import dev.apexstudios.gradle.common.impl.BaseApexPlugin;
import dev.apexstudios.gradle.common.impl.task.GenerateModsToml;
import dev.apexstudios.gradle.common.impl.util.PublishingFileCollection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import net.neoforged.moddevgradle.boot.ModDevPlugin;
import net.neoforged.moddevgradle.dsl.DataFileCollection;
import net.neoforged.moddevgradle.dsl.NeoForgeExtension;
import net.neoforged.moddevgradle.dsl.RunModel;
import org.gradle.api.Named;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;

public class ApexGradlePlugin extends BaseApexPlugin {
    @Override
    protected void applyPlugin(Project project) {
        var apex = Util.getExtension(project, IApexExtension.class);
        project.getPlugins().apply(ModDevPlugin.class);
        var neoForge = Util.getExtension(project, NeoForgeExtension.class);

        hotswap(apex.getAccessTransformers(), neoForge.getAccessTransformers());
        hotswap(apex.getInterfaceInjection(), neoForge.getInterfaceInjectionData());
        neoForge.getValidateAccessTransformers().set(apex.getValidateAccessTransformers());

        neoForge.parchment(parchment -> {
            parchment.getMappingsVersion().set(apex.getParchmentMappings());
            parchment.getMinecraftVersion().set(apex.getParchmentVersion());
            parchment.getEnabled().set(apex.getParchmentMappings().map($ -> true).orElse(false));
        });

//        apex.getRuns().all(run -> setupRun(project, run));
        project.afterEvaluate(ApexGradlePlugin::deferredSetup);
    }

    private static void deferredSetup(Project project) {
        var apex = Util.getExtension(project, IApexExtension.class);
        var neoForge = Util.getExtension(project, NeoForgeExtension.class);

        neoForge.enable(settings -> settings.setVersion(apex.getNeoForgeVersion().get()));
        apex.getMods().all(mod -> setupMod(project, mod));
        apex.getRuns().all(run -> setupRun(project, run));

        project.getTasks().withType(GenerateModsToml.class, task -> {
            task.getNeoForgeVersion().set(neoForge.getVersion());
            task.getMinecraftVersion().set(neoForge.getMinecraftVersion());
        });
    }

    private static void hotswap(IPublishingFileCollection ours, DataFileCollection theirs) {
        ((PublishingFileCollection) ours).hotswap(
                theirs.getFiles(),
                theirs::publish
        );
    }

    private static void setupMod(Project project, IMod mod) {
        BaseApexPlugin.setupModCommon(project, mod);

        var neoForge = Util.getExtension(project, NeoForgeExtension.class);
        var neoMods = neoForge.getMods();

        var main = BaseApexPlugin.getModSourceSet(project, mod, SourceSet.MAIN_SOURCE_SET_NAME);
        neoForge.addModdingDependenciesTo(main);
        neoMods.create(main.getName(), neoMod -> neoMod.sourceSet(main));

        if(mod.getHasDataGen().get()) {
            var data = BaseApexPlugin.getModSourceSet(project, mod, BaseApexPlugin.DATA);
            neoForge.addModdingDependenciesTo(data);

            neoMods.create(data.getName(), neoMod -> {
                neoMod.sourceSet(main);
                neoMod.sourceSet(data);
            });

            mod.dataRun(run -> {
                if(neoForge.getVersionCapabilities().splitDataRuns())
                    run.clientData();
                else
                    run.data();
            });
        }

        if(mod.getHasGameTest().get()) {
            // TODO: Add NeoForge TestFramework dependency
            // TODO: Create GameTest run
            var test = BaseApexPlugin.getModSourceSet(project, mod, SourceSet.TEST_SOURCE_SET_NAME);
            neoForge.addModdingDependenciesTo(test);

            neoMods.create(test.getName(), neoMod -> {
                neoMod.sourceSet(main);
                neoMod.sourceSet(test);
            });
        }
    }

    private static void setupRun(Project project, IRun run) {
        var neoForge = Util.getExtension(project, NeoForgeExtension.class);
        var neoRun = neoForge.getRuns().maybeCreate(run.getName());
        copyRun(neoForge, run, neoRun);
    }

    private static void copyRun(NeoForgeExtension neoForge, IRun run, RunModel neoRun) {
        var neoMods = neoForge.getMods();

        neoRun.getLoadedMods().set(run.getLoadedMods()
                // determine loaded mods from specified list and run type
                // run type determines the neo mod name
                .zip(run.getType(), (mods, type) -> {
                    String suffix;
                    var lowerType = type.toLowerCase(Locale.ROOT);

                    if(lowerType.contains(BaseApexPlugin.DATA))
                        suffix = BaseApexPlugin.DATA;
                    else if(lowerType.contains(SourceSet.TEST_SOURCE_SET_NAME))
                        suffix = SourceSet.TEST_SOURCE_SET_NAME;
                    else
                        suffix = SourceSet.MAIN_SOURCE_SET_NAME;

                    return mods.stream()
                            .map(Named::getName)
                            .map(name -> Util.createName(name, suffix))
                            .map(neoMods::findByName)
                            .filter(Objects::nonNull)
                            .toList();
                })
                // no mods specified try determine mods from source set
                .orElse(run.getSourceSet().map(sourceSet -> {
                    var mod = neoMods.findByName(sourceSet.getName());
                    return mod == null ? Collections.emptyList() : List.of(mod);
                })));

        neoRun.getIdeName().set(run.getIdeName());
        neoRun.getGameDirectory().set(run.getGameDirectory());
        neoRun.getEnvironment().set(run.getEnvironment());
        neoRun.getSystemProperties().set(run.getSystemProperties());
        neoRun.getMainClass().set(run.getMainClass());
        neoRun.getProgramArguments().set(run.getProgramArguments());
        neoRun.getJvmArguments().set(run.getJvmArguments());
        neoRun.getType().set(run.getType());
        neoRun.getLogLevel().set(run.getLogLevel());
        neoRun.getLoggingConfigFile().set(run.getLoggingConfigFile());
        neoRun.getSourceSet().set(run.getSourceSet());
        neoRun.getDevLogin().set(run.getDevLogin());
    }
}
