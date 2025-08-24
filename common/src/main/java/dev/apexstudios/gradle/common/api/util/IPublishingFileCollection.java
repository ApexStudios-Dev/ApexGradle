package dev.apexstudios.gradle.common.api.util;

import org.gradle.api.file.ConfigurableFileCollection;
import org.jetbrains.annotations.ApiStatus;

public interface IPublishingFileCollection {
    ConfigurableFileCollection getFiles();

    @ApiStatus.NonExtendable
    default void from(Object... paths) {
        getFiles().from(paths);
    }

    void publish(Object artifactNotation);
}
