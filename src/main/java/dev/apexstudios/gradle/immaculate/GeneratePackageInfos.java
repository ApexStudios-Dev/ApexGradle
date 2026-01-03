package dev.apexstudios.gradle.immaculate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.provider.ListProperty;
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

    @Input
    public abstract ListProperty<String> getAnnoations();

    @TaskAction
    void generatePackageInfos() {
        var annotations = getAnnoations().map(list -> list.stream().filter(Predicate.not(String::isBlank)).distinct().toList()).getOrElse(Collections.emptyList());

        if(annotations.isEmpty()) {
            throw new GradleException("GeneratePackageInfos task requires at least 1 annotation");
        }

        var basePackage = getBasePackage()
                .map(s -> s.replace('.', File.separatorChar))
                .map(s -> s.endsWith(File.separator) ? s : s + File.separatorChar)
                .get();

        if(basePackage.isBlank()) {
            throw new GradleException("GeneratePackageInfos: Missing required 'basePackage' property");
        }

        for (var javaFile : getFiles()) {
            var packageInfoFile = new File(javaFile.getParent(), "package-info.java");

            if (!packageInfoFile.exists()) {
                var pkgName = javaFile.toString().replaceAll(Matcher.quoteReplacement(File.separator), File.separator);
                pkgName = pkgName.substring(pkgName.indexOf(basePackage), pkgName.lastIndexOf(File.separator));
                pkgName = pkgName.replace(File.separatorChar, '.');
                pkgName = "package " + pkgName + ';';

                var contents = Stream.concat(annotations.stream().map(str -> '@' + str), Stream.of(pkgName)).collect(Collectors.joining("\n"));

                try {
                    // System.out.println("Wrote file: " + packageInfoFile.toPath());
                    Files.writeString(packageInfoFile.toPath(), contents);
                    // return;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
