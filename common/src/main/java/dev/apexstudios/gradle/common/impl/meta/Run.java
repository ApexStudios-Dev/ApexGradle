package dev.apexstudios.gradle.common.impl.meta;

import dev.apexstudios.gradle.common.api.meta.IRun;
import dev.apexstudios.gradle.common.api.util.Util;
import javax.inject.Inject;
import org.gradle.api.Project;
import org.gradle.api.tasks.SourceSet;
import org.slf4j.event.Level;

public abstract class Run implements IRun {
    private final String name;

    @Inject
    public Run(Project project, String name) {
        this.name = name;

        getGameDirectory().convention(getType().map(type -> project.getLayout().getProjectDirectory().dir("run/" + type)));
        getLogLevel().convention(Level.INFO);
        getSourceSet().convention(Util.getSourceSets(project).getByName(SourceSet.MAIN_SOURCE_SET_NAME));
        getDevLogin().convention(false);

        var ideName = Util.capitalize(name);

        if(project != project.getRootProject())
            ideName = project.getName() + " - " + ideName;

        getIdeName().convention(ideName);
    }

    @Override
    public String getName() {
        return name;
    }
}
