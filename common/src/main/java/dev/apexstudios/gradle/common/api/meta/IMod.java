package dev.apexstudios.gradle.common.api.meta;

import dev.apexstudios.gradle.common.impl.BaseApexPlugin;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.jetbrains.annotations.ApiStatus;

public interface IMod extends IModProperties {
    Property<Boolean> getAutoDetectAccessTransformers();

    Property<Boolean> getAutoDetectMixinConfigs();

    Property<Boolean> getAutoDetectInterfaceInjection();

    Property<Boolean> getAutoDetectEnumExtensions();

    Property<Boolean> getAutoDetectFeatureFlags();

    Property<Boolean> getInjectDependencies();

    DirectoryProperty getDirectory();

    @ApiStatus.NonExtendable
    default Provider<Directory> directory(String path) {
        return getDirectory().dir(path);
    }

    @ApiStatus.NonExtendable
    default Provider<RegularFile> file(String path) {
        return getDirectory().file(path);
    }

    @ApiStatus.NonExtendable
    default FileCollection files(Object... paths) {
        return getDirectory().files(paths);
    }

    NamedDomainObjectContainer<IMod> getRequiredMods();

    NamedDomainObjectContainer<ISubMod> getChildren();

    @ApiStatus.NonExtendable
    default NamedDomainObjectContainer<ISubMod> children(Action<NamedDomainObjectContainer<ISubMod>> action) {
        var children = getChildren();
        action.execute(children);
        return children;
    }

    Property<Boolean> getHasDataGen();

    Property<Boolean> getHasGameTest();

    IRun run(String name, Action<IRun> action);

    @ApiStatus.NonExtendable
    default IRun clientRun(Action<IRun> action) {
        return run("client", action);
    }

    @ApiStatus.NonExtendable
    default IRun serverRun(Action<IRun> action) {
        return run("server", action);
    }

    @ApiStatus.NonExtendable
    default IRun dataRun(Action<IRun> action) {
        return run(BaseApexPlugin.DATA, action);
    }

    @ApiStatus.NonExtendable
    default IRun gameTestRun(Action<IRun> action) {
        return run(SourceSet.TEST_SOURCE_SET_NAME, action);
    }

    Property<String> getModLoader();

    Property<String> getLoaderVersion();

    Property<String> getLicense();

    Property<String> getIssueTrackerURL();

    Property<Boolean> getShowAsResourcePack();

    Property<Boolean> getShowAsDataPack();

    Property<String> getEnumExtensions();

    Property<String> getFeatureFlags();

    ListProperty<String> getAccessTransformers();

    ListProperty<String> getMixinConfigs();

    NamedDomainObjectContainer<IModDependency> getDependencies();

    @ApiStatus.NonExtendable
    default NamedDomainObjectContainer<IModDependency> dependencies(Action<NamedDomainObjectContainer<IModDependency>> action) {
        var dependencies = getDependencies();
        action.execute(dependencies);
        return dependencies;
    }
}
