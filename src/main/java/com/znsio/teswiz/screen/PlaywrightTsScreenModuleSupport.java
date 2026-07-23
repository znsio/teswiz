package com.znsio.teswiz.screen;

import com.znsio.teswiz.web.playwright.screen.PlaywrightTsScreenModuleResolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class PlaywrightTsScreenModuleSupport {
    private static final String SCREEN_ROOT_PACKAGE = "com.znsio.teswiz.screen";
    private static final Pattern EXPORTED_FUNCTION_PATTERN = Pattern.compile(
            "export\\s+(?:async\\s+)?function\\s+([A-Za-z0-9_]+)\\s*\\(");

    private final PlaywrightTsScreenModuleResolver moduleResolver = new PlaywrightTsScreenModuleResolver();

    boolean hasModuleFor(String contractClassName) {
        return modulePathFor(contractClassName) != null;
    }

    String expectedModulePathFor(String contractClassName) {
        try {
            Class<?> contractClass = Class.forName(contractClassName);
            return deriveModulePath(contractClass);
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("Unable to load contract class: " + contractClassName, exception);
        }
    }

    String modulePathFor(String contractClassName) {
        try {
            Class<?> contractClass = Class.forName(contractClassName);
            return moduleResolver.findModulePath(contractClass).orElse(null);
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("Unable to load contract class: " + contractClassName, exception);
        }
    }

    Set<String> exportedFunctionsFor(String contractClassName) {
        String modulePath = modulePathFor(contractClassName);
        if (null == modulePath) {
            return Set.of();
        }
        Path moduleFile = Path.of("playwright", "screens").resolve(modulePath);
        try {
            String fileContents = Files.readString(moduleFile);
            Matcher matcher = EXPORTED_FUNCTION_PATTERN.matcher(fileContents);
            java.util.LinkedHashSet<String> exportedFunctions = new java.util.LinkedHashSet<>();
            while (matcher.find()) {
                exportedFunctions.add(matcher.group(1));
            }
            return exportedFunctions;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read Playwright TS screen module: " + moduleFile, exception);
        }
    }

    private String deriveModulePath(Class<?> contractClass) {
        String packageName = contractClass.getPackageName();
        String domain = packageName.equals(SCREEN_ROOT_PACKAGE)
                ? ""
                : packageName.substring((SCREEN_ROOT_PACKAGE + ".").length()).replace('.', '/');
        String fileName = toKebabCase(trimScreenSuffix(contractClass.getSimpleName())) + ".screen.ts";
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
