package com.znsio.teswiz.web.playwright.screen;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class PlaywrightTsScreenModuleResolver {
    private static final String SCREEN_ROOT_PACKAGE = "com.znsio.teswiz.screen";
    private final List<Path> screenRootDirectories;

    public PlaywrightTsScreenModuleResolver() {
        this(List.of(
                Path.of("src", "test", "resources", "playwright", "screens").toAbsolutePath(),
                Path.of("playwright", "screens").toAbsolutePath()));
    }

    PlaywrightTsScreenModuleResolver(Path screenRootDirectory) {
        this(List.of(screenRootDirectory));
    }

    PlaywrightTsScreenModuleResolver(List<Path> screenRootDirectories) {
        this.screenRootDirectories = List.copyOf(screenRootDirectories);
    }

    public Optional<String> findModulePath(Class<?> screenContract) {
        String modulePath = modulePathFor(screenContract);
        return screenRootDirectories.stream()
                .map(rootDirectory -> rootDirectory.resolve(modulePath))
                .anyMatch(Files::exists)
                ? Optional.of(modulePath)
                : Optional.empty();
    }

    String modulePathFor(Class<?> screenContract) {
        String packageName = screenContract.getPackageName();
        String domain = packageName.equals(SCREEN_ROOT_PACKAGE)
                ? ""
                : packageName.substring((SCREEN_ROOT_PACKAGE + ".").length()).replace('.', '/');
        String fileName = toKebabCase(trimScreenSuffix(screenContract.getSimpleName())) + ".screen.ts";
        return domain.isBlank() ? fileName : domain + "/" + fileName;
    }

    private String trimScreenSuffix(String screenName) {
        return screenName.endsWith("Screen")
                ? screenName.substring(0, screenName.length() - "Screen".length())
                : screenName;
    }

    private String toKebabCase(String value) {
        StringBuilder kebabCase = new StringBuilder();
        for (int index = 0; index < value.length(); index++) {
            char currentCharacter = value.charAt(index);
            if (Character.isUpperCase(currentCharacter) && index > 0) {
                kebabCase.append('-');
            }
            kebabCase.append(Character.toLowerCase(currentCharacter));
        }
        return kebabCase.toString().toLowerCase(Locale.ROOT);
    }
}
