package dev.apexstudios.gradle.common.impl.meta;

import dev.apexstudios.gradle.common.api.IApexExtension;
import dev.apexstudios.gradle.common.api.meta.IMod;
import dev.apexstudios.gradle.common.api.meta.IRun;
import dev.apexstudios.gradle.common.api.meta.ISubMod;
import dev.apexstudios.gradle.common.api.util.Util;
import dev.apexstudios.gradle.common.impl.BaseApexPlugin;
import java.util.Locale;
import javax.inject.Inject;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;

public abstract class Mod extends ModProperties implements IMod {
    private final Project project;
    private final NamedDomainObjectContainer<ISubMod> children;

    @Inject
    public Mod(Project project, String name) {
        super(name);

        this.project = project;

        children = project.getObjects().domainObjectContainer(ISubMod.class, subModName -> project.getObjects().newInstance(SubMod.class, subModName, this));

        getAutoDetectAccessTransformers().convention(true);
        getAutoDetectMixinConfigs().convention(true);
        getAutoDetectInterfaceInjection().convention(true);
        getAutoDetectEnumExtensions().convention(true);
        getAutoDetectFeatureFlags().convention(true);

        getInjectDependencies().convention(true);
        getDirectory().convention(project.getLayout().getProjectDirectory().dir(name));
        getHasDataGen().convention(true);
        getHasGameTest().convention(false);

        getModId().convention(name.toLowerCase(Locale.ROOT));
        getVersion().convention("${file.jarVersion}");
        getDisplayName().convention(name);
        getLogoFile().convention("logo.png");
    }

    @Override
    public NamedDomainObjectContainer<ISubMod> getChildren() {
        return children;
    }

    @Override
    public IRun run(String name, Action<IRun> action) {
        var run = Util.getExtension(project, IApexExtension.class).getRuns().maybeCreate(Util.createName(getName(), name));
        run.getSourceSet().set(BaseApexPlugin.getModSourceSet(project, this, SourceSet.MAIN_SOURCE_SET_NAME));
        action.execute(run);
        return run;
    }
}
