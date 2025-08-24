package dev.apexstudios.gradle.common.impl.meta;

import dev.apexstudios.gradle.common.api.meta.IMod;
import dev.apexstudios.gradle.common.api.meta.ISubMod;
import java.util.Locale;
import javax.inject.Inject;

public abstract class SubMod extends ModProperties implements ISubMod {
    @Inject
    public SubMod(String name, IMod parent) {
        super(name);

        getModId().convention(parent.getModId().map(modId -> modId + '_' + name.toLowerCase(Locale.ROOT)));
        getVersion().convention(parent.getVersion());
        getDisplayName().convention(parent.getDisplayName().map(displayName -> displayName + " - " + name));
        getLogoFile().convention(parent.getLogoFile());
        getLogoBlur().convention(parent.getLogoBlur());
        getUpdateJsonURL().convention(parent.getUpdateJsonURL());
        getModURL().convention(parent.getModURL());
        getCredits().convention(parent.getCredits());
        getAuthors().convention(parent.getAuthors());
        getDisplayURL().convention(parent.getDisplayURL());
    }
}
