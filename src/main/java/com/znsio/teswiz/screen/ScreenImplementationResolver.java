package com.znsio.teswiz.screen;

import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.NotImplementedException;

import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.exceptions.InvalidTestDataException;
import com.znsio.teswiz.web.WebEngine;

public final class ScreenImplementationResolver {
    private static final String ROOT_PACKAGE = "com.znsio.teswiz.screen";
    private static final Map<ScreenKey, Class<?>> OVERRIDES = Map.of();

    private ScreenImplementationResolver() {
    }

    public static <T> Class<? extends T> resolve(Class<T> screenContract, Platform platform, WebEngine webEngine) {
        if (isPlaywrightTs(platform, webEngine)) {
            throw missingPlaywrightTsModule(screenContract);
        }
        Class<?> override = OVERRIDES.get(new ScreenKey(screenContract, platform, webEngine));
        if (null == override) {
            override = OVERRIDES.get(new ScreenKey(screenContract, platform, null));
        }
        if (null != override) {
            return castOverride(screenContract, override);
        }
        return loadClass(screenContract, resolveByConvention(screenContract, platform, webEngine));
    }

    private static boolean isPlaywrightTs(Platform platform, WebEngine webEngine) {
        return (Platform.web.equals(platform) || Platform.electron.equals(platform))
                && WebEngine.PLAYWRIGHT_TS.equals(webEngine);
    }

    private static <T> InvalidTestDataException missingPlaywrightTsModule(Class<T> screenContract) {
        String expectedModulePath = new PlaywrightTsScreenModuleSupport().expectedModulePathFor(screenContract.getName());
        return new InvalidTestDataException(String.format(
                "WEB_ENGINE=playwright-ts requires a matching TypeScript screen module for %s. Expected module: src/test/resources/playwright/screens/%s",
                screenContract.getName(),
                expectedModulePath));
    }

    private static <T> String resolveByConvention(Class<T> screenContract, Platform platform, WebEngine webEngine) {
        String contractPackage = screenContract.getPackageName();
        String screenName = screenContract.getSimpleName();
        String domain = contractPackage.equals(ROOT_PACKAGE)
                ? ""
                : contractPackage.substring((ROOT_PACKAGE + ".").length());

        String implementationPackage = switch (platform) {
            case android -> joinPackage(ROOT_PACKAGE, "android", domain);
            case iOS -> joinPackage(ROOT_PACKAGE, "ios", domain);
            case windows -> joinPackage(ROOT_PACKAGE, "windows", domain);
            case pdf -> joinPackage(ROOT_PACKAGE, "pdf", domain);
            case web, electron -> joinPackage(ROOT_PACKAGE, webPackageSegment(webEngine), domain);
            default -> throw new NotImplementedException(
                    "Unsupported screen platform for " + screenName + ": " + platform);
        };

        return implementationPackage + "." + screenName + classSuffix(platform, webEngine);
    }

    private static String webPackageSegment(WebEngine webEngine) {
        return switch (webEngine) {
            case SELENIUM -> "web";
            case PLAYWRIGHT_JAVA -> "web.playwrightjava";
            case PLAYWRIGHT_TS -> throw new InvalidTestDataException(
                    "playwright-ts screen resolution is module-based and should not resolve Java implementation packages");
        };
    }

    private static String classSuffix(Platform platform, WebEngine webEngine) {
        return switch (platform) {
            case android -> "Android";
            case iOS -> "IOS";
            case windows -> "Windows";
            case pdf -> "PDF";
            case web, electron -> switch (webEngine) {
                case SELENIUM -> "Web";
                case PLAYWRIGHT_JAVA -> "PlaywrightJava";
                case PLAYWRIGHT_TS -> throw new InvalidTestDataException(
                        "playwright-ts screen resolution is module-based and should not resolve Java implementation suffixes");
            };
            default -> throw new NotImplementedException("Unsupported platform: " + platform);
        };
    }

    private static String joinPackage(String root, String platformPackage, String domain) {
        return domain.isBlank()
                ? root + "." + platformPackage
                : root + "." + platformPackage + "." + domain;
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<? extends T> castOverride(Class<T> screenContract, Class<?> implementationClass) {
        if (!screenContract.isAssignableFrom(implementationClass)) {
            throw new NotImplementedException(String.format(
                    "Invalid screen override. %s does not implement %s",
                    implementationClass.getName(), screenContract.getName()));
        }
        return (Class<? extends T>) implementationClass;
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<? extends T> loadClass(Class<T> screenContract, String className) {
        try {
            return (Class<? extends T>) Class.forName(className);
        } catch (ClassNotFoundException exception) {
            throw new NotImplementedException(
                    String.format("No screen implementation found for %s. Expected: %s",
                            screenContract.getSimpleName(), className),
                    exception);
        }
    }

    private record ScreenKey(Class<?> contractClass, Platform platform, WebEngine webEngine) {
        private ScreenKey {
            Objects.requireNonNull(contractClass, "contractClass");
            Objects.requireNonNull(platform, "platform");
        }
    }
}
