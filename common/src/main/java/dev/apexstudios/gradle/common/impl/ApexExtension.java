package dev.apexstudios.gradle.common.impl;

import dev.apexstudios.gradle.common.api.IApexExtension;
import dev.apexstudios.gradle.common.api.meta.IMod;
import dev.apexstudios.gradle.common.api.meta.IRun;
import dev.apexstudios.gradle.common.api.util.IPublishingFileCollection;
import dev.apexstudios.gradle.common.impl.meta.Mod;
import dev.apexstudios.gradle.common.impl.meta.Run;
import dev.apexstudios.gradle.common.impl.util.PublishingFileCollection;
import javax.inject.Inject;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.jvm.toolchain.JavaLanguageVersion;
import org.gradle.jvm.toolchain.JvmVendorSpec;

public abstract class ApexExtension implements IApexExtension {
    private final IPublishingFileCollection accessTransformers;
    private final IPublishingFileCollection interfaceInjection;
    private final NamedDomainObjectContainer<IMod> mods;
    private final NamedDomainObjectContainer<IRun> runs;

    @Inject
    public ApexExtension(ObjectFactory objects) {
        accessTransformers = new PublishingFileCollection(objects);
        interfaceInjection = new PublishingFileCollection(objects);
        mods = objects.domainObjectContainer(IMod.class, name -> objects.newInstance(Mod.class, name));
        runs = objects.domainObjectContainer(IRun.class, name -> objects.newInstance(Run.class, name));

        getJavaVersion().convention(JavaLanguageVersion.of(21));
        getJvmVendor().convention(JvmVendorSpec.ADOPTIUM);
    }

    @Override
    public IPublishingFileCollection getAccessTransformers() {
        return accessTransformers;
    }

    @Override
    public IPublishingFileCollection getInterfaceInjection() {
        return interfaceInjection;
    }

    @Override
    public NamedDomainObjectContainer<IMod> getMods() {
        return mods;
    }

    @Override
    public NamedDomainObjectContainer<IRun> getRuns() {
        return runs;
    }
}
