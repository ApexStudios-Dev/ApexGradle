package dev.apexstudios.gradle.common.impl;

import dev.apexstudios.gradle.common.api.IApexExtension;
import dev.apexstudios.gradle.common.api.meta.IMod;
import dev.apexstudios.gradle.common.api.util.Util;
import java.io.File;
import java.util.Collections;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.plugins.JavaLibraryPlugin;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.util.PatternFilterable;
import org.gradle.plugins.ide.idea.model.IdeaModel;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.gradle.ext.IdeaExtPlugin;

// TODO: Generate mods.toml file
public abstract class BaseApexPlugin implements Plugin<Project> {
    public static final boolean IS_CI = Boolean.parseBoolean(System.getenv("CI"));
    public static final String DATA = "data";
    public static final String GENERATED = "generated";

    @Override
    public final void apply(Project project) {
        project.getExtensions().create(IApexExtension.class, IApexExtension.NAME, ApexExtension.class);

        applyCommon(project);
        applyPlugin(project);
        setJavaVersion(project);
    }

    protected abstract void applyPlugin(Project project);

    protected void applyCommon(Project project) {
        project.getRootProject().getPlugins().apply(IdeaExtPlugin.class);
        project.getPlugins().apply(JavaLibraryPlugin.class);

        var idea = Util.getExtension(project.getRootProject(), IdeaModel.class);

        idea.module(module -> {
            if(!IS_CI) {
                module.setDownloadJavadoc(true);
                module.setDownloadSources(true);
            }

            module.getExcludeDirs().addAll(project.files(
                    ".gradle",
                    ".idea",
                    "gradle"
            ).getFiles());
        });

        setJavaVersion(project);
        project.getTasks().withType(JavaCompile.class).configureEach(task -> task.getOptions().setEncoding("UTF-8"));

        var repositories = project.getRepositories();

        repositories.maven(repo -> {
            repo.setName("ApexStudios - Main");
            repo.setUrl("https://maven.apexstudios.dev/releases");
        });

        repositories.maven(repo -> {
            repo.setName("ApexStudios - Private");
            repo.setUrl("https://maven.apexstudios.dev/private");
        });
    }

    private void setJavaVersion(Project project) {
        Util.getJavaExtension(project).toolchain(spec -> {
            var apex = Util.getExtension(project, IApexExtension.class);
            spec.getLanguageVersion().set(apex.getJavaVersion());
            spec.getVendor().set(apex.getJvmVendor());
        });
    }

    public static void setupModCommon(Project project, IMod mod) {
        var apex = Util.getExtension(project, IApexExtension.class);
        var ideaModule = Util.getExtension(project.getRootProject(), IdeaModel.class).getModule();
        var main = createModSourceSet(project, mod, SourceSet.MAIN_SOURCE_SET_NAME);
        var resources = mod.directory("src/" + SourceSet.MAIN_SOURCE_SET_NAME + "/resources").get();

        // discover mod files
        // accesstransformers
        injectAndDiscoverFiles(
                -1,
                mod,
                resources,
                "AccessTransfer",
                mod.getAutoDetectAccessTransformers(),
                spec -> spec.include("**/access*transformer*.cfg"),
                mod.getAccessTransformers(),
                apex.getAccessTransformers().getFiles()::from
        );

        // mixin configs
        discoverExistingFiles(
                -1,
                mod,
                resources,
                "MixinConfig",
                mod.getAutoDetectMixinConfigs(),
                spec -> spec.include("**/*.mixin*.json"),
                appendRelativePath(resources, mod.getMixinConfigs())
        );

        // interface injection
        discoverExistingFiles(
                -1,
                mod,
                resources,
                "InterfaceInjection",
                mod.getAutoDetectInterfaceInjection(),
                spec -> spec.include("**/interface*.json"),
                apex.getInterfaceInjection().getFiles()::from
        );

        // enum extensions
        discoverExistingFiles(
                1,
                mod,
                resources,
                "EnumExtensions",
                mod.getAutoDetectEnumExtensions(),
                spec -> spec.include("**/enum*extension*.json"),
                asRelativePath(resources, path -> mod.getEnumExtensions().set(path))
        );

        // feature flags
        discoverExistingFiles(
                1,
                mod,
                resources,
                "FeatureFlags",
                mod.getAutoDetectFeatureFlags(),
                spec -> spec.include("**/feature*flag*.json"),
                asRelativePath(resources, path -> mod.getFeatureFlags().set(path))
        );

        // exclude unwanted project dirs
        ideaModule.getExcludeDirs().addAll(mod.files(
                ".gradle",
                ".idea",
                "build",
                "gradle",
                "run"
        ).getFiles());

        // setup datagen
        if(mod.getHasDataGen().get()) {
            var data = createModSourceSet(project, mod, DATA);
            Util.extendSourceSet(project.getDependencies(), main, data);
            data.resources(spec -> spec.setSrcDirs(Collections.emptyList()));
            main.resources(spec -> spec.srcDir(mod.directory("src/" + DATA + '/' + GENERATED)));
            ideaModule.getExcludeDirs().add(mod.directory("src/" + DATA + '/' + GENERATED + "/.cache").get().getAsFile());
            ideaModule.getGeneratedSourceDirs().add(mod.directory("src/" + DATA + '/' + GENERATED + "/.cache").get().getAsFile());

            mod.dataRun(run -> {
                run.clientData();
                run.getIdeName().set(mod.getName() + " - Data");
                run.getSourceSet().set(data);
                run.getProgramArguments().addAll(
                        "--mod", mod.getModId().get(),
                        "--all",
                        "--output", mod.directory("src/" + BaseApexPlugin.DATA + '/' + BaseApexPlugin.GENERATED).get().getAsFile().getAbsolutePath(),
                        "--existing", mod.directory("src/" + SourceSet.MAIN_SOURCE_SET_NAME + "/resources").get().getAsFile().getAbsolutePath()
                );
            });
        }

        // setup game tests
        if(mod.getHasGameTest().get()) {
            var test = createModSourceSet(project, mod, SourceSet.TEST_SOURCE_SET_NAME);
            Util.extendSourceSet(project.getDependencies(), main, test);
            ideaModule.getTestSources().from(mod.directory("src/" + SourceSet.TEST_SOURCE_SET_NAME + "/java"));
            ideaModule.getTestResources().from(mod.directory("src/" + SourceSet.TEST_SOURCE_SET_NAME + "/resources"));

            mod.gameTestRun(run -> {
                run.getIdeName().set(mod.getName() + " - GameTest");
                run.getSourceSet().set(test);
            });
        }
    }

    public static SourceSet createModSourceSet(Project project, IMod mod, String name) {
        return Util.getSourceSets(project).create(getModSourceSetName(mod, name), sourceSet -> {
            sourceSet.java(spec -> spec.setSrcDirs(mod.files("src/" + name + "/java")));
            sourceSet.resources(spec -> spec.setSrcDirs(mod.files("src/" + name + "/resources")));
        });
    }

    public static SourceSet getModSourceSet(Project project, IMod mod, String name) {
        return Util.getSourceSets(project).getByName(getModSourceSetName(mod, name));
    }

    public static String getModSourceSetName(IMod mod, String name) {
        return Util.createName(mod.getName(), name);
    }

    private static void injectAndDiscoverFiles(int maxCount, IMod mod, Directory directory, String name, @Nullable Property<Boolean> autoDetect, Action<PatternFilterable> filter, ListProperty<String> modFiles, Action<File> action) {
        injectExistingFiles(maxCount, mod, directory, name, autoDetect, modFiles, action);
        discoverExistingFiles(maxCount, mod, directory, name, autoDetect, filter, action);
    }

    private static void discoverExistingFiles(int maxCount, IMod mod, Directory directory, String name, @Nullable Property<Boolean> autoDetect, Action<PatternFilterable> filter, Action<File> action) {
        if(autoDetect != null && !autoDetect.get())
            return;

        var files = directory.getAsFileTree().getAsFileTree().matching(filter).getFiles();

        if(maxCount != -1 && files.size() > maxCount)
            throw new IllegalStateException("Found " + files.size() + ' ' + name + " files for mod " + mod.getName() + " but only " + maxCount + " are allowed!");

        files.forEach(file -> injectFile(mod, directory, name, "auto-detect", file, action));
    }

    private static void injectExistingFiles(int maxCount, IMod mod, Directory directory, String name, @Nullable Property<Boolean> autoDetect, ListProperty<String> modFiles, Action<File> action) {
        if(autoDetect != null && !autoDetect.get())
            return;

        var existingPaths = modFiles.getOrElse(Collections.emptyList());

        if(maxCount != -1 && existingPaths.size() > maxCount)
            throw new IllegalStateException("Found " + existingPaths.size() + ' ' + name + " files for mod " + mod.getName() + " but only " + maxCount + " are allowed!");

        for(var existingPath : existingPaths) {
            var file = directory.file(existingPath).getAsFile();

            if(file.exists())
                injectFile(mod, directory, name, "existing", file, action);
        }
    }

    private static void injectFile(IMod mod, Directory directory, String name, String suffix, File file, Action<File> action) {
        var relativePath = directory.getAsFile().toPath().relativize(file.toPath()).toString();
        System.out.println("Injected " + name + " file '" + relativePath + "' for mod '" + mod.getName() + "' (" + suffix + ')');
        action.execute(file);
    }

    private static Action<File> asRelativePath(Directory directory, Action<String> action) {
        return file -> action.execute(directory.getAsFile().toPath().relativize(file.toPath()).toString());
    }

    private static Action<File> appendRelativePath(Directory directory, ListProperty<String> files) {
        return asRelativePath(directory, files::add);
    }
}
