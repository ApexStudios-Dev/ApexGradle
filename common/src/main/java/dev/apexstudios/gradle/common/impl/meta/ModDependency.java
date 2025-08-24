package dev.apexstudios.gradle.common.impl.meta;

import dev.apexstudios.gradle.common.api.meta.IModDependency;
import dev.apexstudios.gradle.common.api.meta.Ordering;
import dev.apexstudios.gradle.common.api.meta.ReleaseType;
import dev.apexstudios.gradle.common.api.meta.Side;
import javax.inject.Inject;

public abstract class ModDependency implements IModDependency {
    private final String modId;

    @Inject
    public ModDependency(String modId) {
        this.modId = modId;

        getType().convention(ReleaseType.REQUIRED);
        getOrdering().convention(Ordering.NONE);
        getSide().convention(Side.BOTH);
    }

    @Override
    public String getModId() {
        return modId;
    }
}
