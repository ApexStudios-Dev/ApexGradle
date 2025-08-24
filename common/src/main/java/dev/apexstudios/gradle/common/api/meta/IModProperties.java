package dev.apexstudios.gradle.common.api.meta;

import java.io.Serializable;
import org.gradle.api.Named;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

public interface IModProperties extends Named, Serializable {
    Property<String> getModId();

    Property<String> getNamespace();

    Property<String> getVersion();

    Property<String> getDisplayName();

    Property<String> getDescription();

    Property<String> getLogoFile();

    Property<Boolean> getLogoBlur();

    Property<String> getUpdateJsonURL();

    Property<String> getModURL();

    ListProperty<String> getCredits();

    ListProperty<String> getAuthors();

    Property<String> getDisplayURL();
}
