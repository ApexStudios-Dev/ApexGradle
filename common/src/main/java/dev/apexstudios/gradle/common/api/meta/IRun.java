package dev.apexstudios.gradle.common.api.meta;

import java.io.Serializable;
import org.gradle.api.Named;
import org.gradle.api.Task;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.event.Level;

public interface IRun extends Named, Serializable {
    Property<String> getIdeName();

    @ApiStatus.NonExtendable
    default void disableIdeRun() {
        getIdeName().set("");
    }

    DirectoryProperty getGameDirectory();

    MapProperty<String, String> getEnvironment();

    MapProperty<String, String> getSystemProperties();

    Property<String> getMainClass();

    ListProperty<String> getProgramArguments();

    ListProperty<String> getJvmArguments();

    ListProperty<IMod> getLoadedMods();

    Property<String> getType();

    @ApiStatus.NonExtendable
    default void client() {
        getType().set("client");
    }

    @ApiStatus.NonExtendable
    default void server() {
        getType().set("server");
    }

    @ApiStatus.NonExtendable
    default void clientData() {
        getType().set("clientData");
    }

    @ApiStatus.NonExtendable
    default void serverData() {
        getType().set("serverData");
    }

    @ApiStatus.NonExtendable
    default void data() {
        getType().set("data");
    }

    Property<Level> getLogLevel();

    RegularFileProperty getLoggingConfigFile();

    Property<SourceSet> getSourceSet();

    Property<Boolean> getDevLogin();

    private static TaskProvider<?> provider(Task task) {
        return task.getProject().getTasks().named(task.getName());
    }
}
