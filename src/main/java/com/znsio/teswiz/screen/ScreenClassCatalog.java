package com.znsio.teswiz.screen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final class ScreenClassCatalog {
    private static final String ROOT_PACKAGE = "com.znsio.teswiz.screen";
    private static final Set<String> IMPLEMENTATION_ROOTS = Set.of(
            "android",
            "ios",
            "web",
            "windows",
            "pdf");

    private final Path sourceRoot;

    ScreenClassCatalog(Path sourceRoot) {
        this.sourceRoot = sourceRoot;
    }

    List<String> discoverContractClassNames() throws IOException {
        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            return paths.filter(Files::isRegularFile)
                    .filter(this::isContractFile)
                    .map(this::toClassName)
                    .sorted()
                    .toList();
        }
    }

    List<String> discoverImplementationClassNames(String contractClassName) {
        String screenName = simpleNameOf(contractClassName);
        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            return paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().startsWith(screenName))
                    .filter(this::isJavaFile)
                    .filter(this::isImplementationPath)
                    .map(this::toClassName)
                    .sorted()
                    .collect(Collectors.toCollection(LinkedHashSet::new))
                    .stream()
                    .toList();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to scan screen implementations", exception);
        }
    }

    String toClassName(Path path) {
        Path relativePath = sourceRoot.relativize(path);
        String suffix = relativePath.toString().replace('/', '.').replace('\\', '.');
        suffix = suffix.substring(0, suffix.length() - ".java".length());
        return ROOT_PACKAGE + "." + suffix;
    }

    private boolean isContractFile(Path path) {
        return isJavaFile(path)
                && path.toString().endsWith("Screen.java")
                && !isImplementationPath(path);
    }

    private boolean isJavaFile(Path path) {
        return path.toString().endsWith(".java");
    }

    private boolean isImplementationPath(Path path) {
        Path relativePath = sourceRoot.relativize(path);
        return relativePath.getNameCount() > 0
                && IMPLEMENTATION_ROOTS.contains(relativePath.getName(0).toString());
    }

    private static String simpleNameOf(String className) {
        return className.substring(className.lastIndexOf('.') + 1);
    }
}
