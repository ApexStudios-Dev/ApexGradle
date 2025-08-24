package dev.apexstudios.gradle.common.impl.util;

import dev.apexstudios.gradle.common.api.util.IPublishingFileCollection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.model.ObjectFactory;
import org.gradle.internal.Either;

public final class PublishingFileCollection implements IPublishingFileCollection {
    private ConfigurableFileCollection files;
    private Either<List<Object>, Consumer<Object>> publishing = Either.left(new LinkedList<>());

    public PublishingFileCollection(ObjectFactory objects) {
        files = objects.fileCollection();
    }

    @Override
    public ConfigurableFileCollection getFiles() {
        return files;
    }

    @Override
    public void publish(Object artifactNotation) {
        publishing.apply(
                artifacts -> artifacts.add(artifactNotation),
                handler -> handler.accept(artifactNotation)
        );
    }

    public void setFiles(ConfigurableFileCollection files) {
        files.from(this.files);
        this.files = files;
    }

    public void setPublishing(Consumer<Object> publishing) {
        this.publishing.apply(
                artifacts -> artifacts.forEach(publishing),
                handler -> { }
        );

        this.publishing = Either.right(publishing);
    }

    public void hotswap(ConfigurableFileCollection files, Consumer<Object> publishing) {
        setFiles(files);
        setPublishing(publishing);
    }
}
