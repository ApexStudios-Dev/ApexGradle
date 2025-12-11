package dev.apexstudios.gradle.immaculate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Matcher;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

public abstract class GeneratePackageInfos extends DefaultTask {
    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract ConfigurableFileCollection getFiles();

    @Input
    public abstract Property<String> getBasePackage();

    @TaskAction
    void generatePackageInfos() {
        var basePackage = getBasePackage()
                .map(s -> s.replace('.', File.separatorChar))
                .map(s -> s.endsWith(File.separator) ? s : s + File.separatorChar)
                .get();

        getFiles().forEach(javaFile -> {
            var packageInfoFile = new File(javaFile.getParent(), "package-info.java");

            if(!packageInfoFile.exists()) {
                var pkgName = javaFile.toString().replaceAll(Matcher.quoteReplacement(File.separator), File.separator);
                pkgName = pkgName.substring(pkgName.indexOf(basePackage), pkgName.lastIndexOf(File.separator));
                pkgName = pkgName.replace(File.separatorChar, '.');

                try {
                    Files.writeString(packageInfoFile.toPath(), """
                            @org.jspecify.annotations.NullMarked
                            package %s;
                            """.stripIndent().trim().formatted(pkgName));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
