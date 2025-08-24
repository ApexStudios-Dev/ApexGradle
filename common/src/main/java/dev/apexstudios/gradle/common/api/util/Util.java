package dev.apexstudios.gradle.common.api.util;

import java.util.Locale;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

public interface Util {
    static <T> T getExtension(ExtensionAware extensions, Class<T> type) {
        return extensions.getExtensions().getByType(type);
    }

    static JavaPluginExtension getJavaExtension(Project project) {
        return getExtension(project, JavaPluginExtension.class);
    }

    static SourceSetContainer getSourceSets(Project project) {
        return getJavaExtension(project).getSourceSets();
    }

    static String capitalize(String str) {
        if(str.isBlank())
            return str;

        return str.substring(0, 1).toUpperCase(Locale.ROOT) + str.substring(1);
    }

    static String createName(String... parts) {
        StringBuilder result = new StringBuilder(parts[0].toLowerCase(Locale.ROOT));

        for(var i = 1; i < parts.length; i++) {
            result.append(Util.capitalize(parts[i]));
        }

        return result.toString();
    }

    static void extendSourceSet(DependencyHandler dependencies, SourceSet from, SourceSet into) {
        var output = from.getOutput();
        dependencies.add(into.getImplementationConfigurationName(), output);
        dependencies.add(into.getRuntimeOnlyConfigurationName(), output);
    }
}
