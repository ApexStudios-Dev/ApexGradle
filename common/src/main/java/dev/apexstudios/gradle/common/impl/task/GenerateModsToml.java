package dev.apexstudios.gradle.common.impl.task;

import com.moandjiezana.toml.TomlWriter;
import dev.apexstudios.gradle.common.api.meta.IMod;
import dev.apexstudios.gradle.common.api.meta.IModDependency;
import dev.apexstudios.gradle.common.api.meta.IModProperties;
import dev.apexstudios.gradle.common.impl.meta.ModDependency;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import javax.inject.Inject;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskAction;
import org.jetbrains.annotations.Nullable;

public abstract class GenerateModsToml extends DefaultTask {
    @Nullable private IMod mod;

    @Inject
    public GenerateModsToml() {
        getTableIndentation().convention(4);
        getValueIndentation().convention(4);
    }

    public void setMod(IMod mod) {
        this.mod = mod;
    }

    @Input
    public abstract Property<String> getNeoForgeVersion();

    @Input
    public abstract Property<String> getMinecraftVersion();

    @Input
    public abstract Property<Integer> getValueIndentation();

    @Input
    public abstract Property<Integer> getTableIndentation();

    @OutputFile
    public abstract RegularFileProperty getOutputFile();

    @TaskAction
    public void execute() throws IOException {
        Objects.requireNonNull(mod);

        var outputFile = getOutputFile().getAsFile().get();
        var directory = outputFile.getParentFile();

        if(outputFile.exists())
            outputFile.delete();
        if(directory != null)
            directory.mkdirs();

        var tomlWriter = new TomlWriter.Builder()
                .indentTablesBy(getTableIndentation().get())
                .indentValuesBy(getValueIndentation().get())
                .build();

        tomlWriter.write(serialize(getProject(), mod, getMinecraftVersion(), getNeoForgeVersion()), outputFile);
    }

    private static void appendDependency(ObjectFactory objects, List<Map<String, Object>> dependencies, Provider<String> property, String modId, Function<String, String> nextVersionMapper) {
        var dependency = objects.newInstance(ModDependency.class, modId);

        dependency.getVersionRange().set(property.map(version -> {
            var range = '[' + version + ',';
            var nextVersion = nextVersionMapper.apply(version);

            if(nextVersion != null && !nextVersion.isBlank())
                range += nextVersion;

            return range + ')';
        }));

        var dependencyMap = serialize(dependency);

        if(!dependencyMap.isEmpty())
            dependencies.add(dependencyMap);
    }

    private static Map<String, Object> serialize(Project project, IMod mod, Property<String> minecraftVersion, Property<String> neoForgeVersion) {
        var map = new LinkedHashMap<String, Object>();
        append(map, "modLoader", mod.getModLoader());
        append(map, "loaderVersion", mod.getLoaderVersion());
        append(map, "license", mod.getLicense());
        append(map, "showAsResourcePack", mod.getShowAsResourcePack());
        append(map, "showAsDataPack", mod.getShowAsDataPack());
        append(map, "issueTrackerURL", mod.getIssueTrackerURL());

        var modMaps = new LinkedList<Map<String, Object>>();

        var modMap = serialize(mod, mod);
        append(modMap, "enumExtensions", mod.getEnumExtensions());
        append(modMap, "featureFlags", mod.getFeatureFlags());
        modMaps.add(modMap);

        mod.getChildren().forEach(child -> {
            var childMap = serialize(mod, child);

            if(!childMap.isEmpty())
                modMaps.add(childMap);
        });

        map.put("mods", modMaps);

        appendExistingFiles(mod, map, "accessTransformers", mod.getAccessTransformers(), file -> Map.of("file", file));
        appendExistingFiles(mod, map, "mixins", mod.getMixinConfigs(), file -> Map.of("config", file));

        var dependencies = serializeDependencies(project, mod, minecraftVersion, neoForgeVersion);

        if(!dependencies.isEmpty()) {
            map.put("dependencies." + mod.getModId().get(), dependencies);

            mod.getChildren().forEach(child -> {
                var childDependencies = new LinkedList<>(dependencies);
                appendMod(project, childDependencies, mod);
                map.put("dependencies." + child.getModId().get(), childDependencies);
            });
        }

        return map;
    }

    private static Map<String, Object> serialize(IMod mod, IModProperties properties) {
        var map = new LinkedHashMap<String, Object>();
        append(map, "modId", properties.getModId());
        append(map, "namespace", properties.getNamespace());
        append(map, "version", properties.getVersion());
        append(map, "displayName", properties.getDisplayName());
        append(map, "description", properties.getDescription());

        var logoFile = properties.getLogoFile();

        if(logoFile.isPresent()) {
            var file = mod.file("src/" + SourceSet.MAIN_SOURCE_SET_NAME + "/resources/" + logoFile.get());

            if(file.get().getAsFile().exists()) {
                map.put("logoFile", logoFile.get());
                append(map, "logoBlur", properties.getLogoBlur());
            }
        }

        append(map, "updateJSONURL", properties.getUpdateJsonURL());
        append(map, "modUrl", properties.getModURL());
        appendJoining(map, "credits", properties.getCredits(), ",");
        appendJoining(map, "authors", properties.getAuthors(), ",");
        append(map, "displayURL", properties.getDisplayURL());

        return map;
    }

    private static Map<String, Object> serialize(IModDependency dependency) {
        var map = new LinkedHashMap<String, Object>();
        map.put("modId", dependency.getModId());
        append(map, "type", dependency.getType());
        append(map, "reason", dependency.getReason());
        append(map, "versionRange", dependency.getVersionRange());
        append(map, "ordering", dependency.getOrdering());
        append(map, "side", dependency.getSide());
        append(map, "referralUrl", dependency.getReferralURL());
        return map;
    }

    private static List<Map<String, Object>> serializeDependencies(Project project, IMod mod, Property<String> minecraftVersion, Property<String> neoForgeVersion) {
        var dependencies = new LinkedList<Map<String, Object>>();

        if(mod.getInjectDependencies().get()) {
            var objects = project.getObjects();
            appendDependency(objects, dependencies, minecraftVersion, "minecraft", GenerateModsToml::incrementVersion);
            appendDependency(objects, dependencies, neoForgeVersion, "neoforge", GenerateModsToml::incrementNeoForgeVersion);
            mod.getRequiredMods().forEach(requiredMod -> appendMod(project, dependencies, requiredMod));
        }

        mod.getDependencies().forEach(dependency -> {
            var dependencyMap = serialize(dependency);

            if(!dependencyMap.isEmpty())
                dependencies.add(dependencyMap);
        });

        return dependencies;
    }

    private static void appendMod(Project project, List<Map<String, Object>> dependencies, IModProperties properties) {
        appendDependency(project.getObjects(), dependencies, properties.getVersion().map(version -> {
            // mods can have '${file.jarVersion}'
            if(!version.equals("${file.jarVersion}"))
                return version;

            // attempt to lookup project version if using jar marker
            // default to '9.9.999' if no project version was set
            var projectVersion = project.getVersion().toString();
            return projectVersion.isBlank() || projectVersion.equalsIgnoreCase("unspecified") ? "9.9.999" : projectVersion;
        }), properties.getModId().get(), GenerateModsToml::incrementNeoForgeVersion);
    }

    private static void append(Map<String, Object> map, String key, Provider<?> property) {
        var value = property.getOrNull();

        if(value != null)
            map.put(key, value);
    }

    private static void appendJoining(Map<String, Object> map, String key, ListProperty<String> property, String delimiter) {
        var list = property.getOrNull();

        if(list != null && !list.isEmpty())
            map.put(key, String.join(delimiter, list));
    }

    private static <R> void appendExistingFiles(IMod mod, Map<String, Object> map, String key, ListProperty<String> property, Function<String, R> mapper) {
        var files = property.getOrNull();

        if(files == null || files.isEmpty())
            return;

        var resources = mod.directory("src/" + SourceSet.MAIN_SOURCE_SET_NAME + "/resources").get();
        var result = new LinkedList<R>();

        for(var file : files) {
            var actual = resources.file(file).getAsFile();

            if(actual.exists()) {
                System.out.println(file);
                result.add(mapper.apply(file));
            }
        }

        if(!result.isEmpty())
            map.put(key, result);
    }

    @Nullable
    private static String incrementVersion(String version) {
        var index = version.lastIndexOf('.');

        if(index == -1)
            return null;

        var numIndex = index + 1;
        var num = Integer.parseInt(version.substring(numIndex)) + 1;
        return version.substring(0, numIndex) + num;
    }

    @Nullable
    private static String incrementNeoForgeVersion(String version) {
        // <major>.<minor>.<build>[-alpha|beta][-pr#]
        var tokens = version.split("-");
        // <major>.<minor>.<build>
        var baseVersion = tokens.length == 0 ? version : tokens[0];
        // [<major>, <minor>, <build>]
        tokens = baseVersion.split("\\.");

        if(tokens.length != 3)
            return null;

        baseVersion = tokens[0] + '.' + tokens[1];
        return incrementVersion(baseVersion);
    }
}
