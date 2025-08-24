package dev.apexstudios.gradle.common.api.meta;

import java.io.Serializable;
import org.gradle.api.Named;
import org.gradle.api.provider.Property;

public interface IModDependency extends Named, Serializable {
    String getModId();

    Property<ReleaseType> getType();

    Property<String> getReason();

    Property<String> getVersionRange();

    Property<Ordering> getOrdering();

    Property<Side> getSide();

    Property<String> getReferralURL();

    @Override
    default String getName() {
        return getModId();
    }
}
