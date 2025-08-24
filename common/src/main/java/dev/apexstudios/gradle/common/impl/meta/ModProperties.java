package dev.apexstudios.gradle.common.impl.meta;

import dev.apexstudios.gradle.common.api.meta.IModProperties;
import java.util.Locale;

public abstract class ModProperties implements IModProperties {
    private final String name;

    protected ModProperties(String name) {
        this.name = name;

        getModId().convention(name.toLowerCase(Locale.ROOT));
        getVersion().convention("${file.jarVersion}");
        getDisplayName().convention(name);
        getLogoFile().convention("logo.png");
    }

    @Override
    public String getName() {
        return name;
    }
}
