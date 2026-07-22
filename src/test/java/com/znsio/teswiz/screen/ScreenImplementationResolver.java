package com.znsio.teswiz.screen;

import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.NotImplementedException;

import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.web.WebEngine;

public final class ScreenImplementationResolver {
    private static final String ROOT_PACKAGE = "com.znsio.teswiz.screen";
    private static final Map<ScreenKey, String> OVERRIDES = Map.ofEntries(
            Map.entry(new ScreenKey("com.znsio.teswiz.screen.pdfValidator.PDFValidatorScreen", Platform.android, null),
                    "com.znsio.teswiz.screen.android.PDFValidator.PDFValidatorScreenAndroid"),
            Map.entry(new ScreenKey("com.znsio.teswiz.screen.pdfValidator.PDFValidatorScreen", Platform.web,
                    WebEngine.SELENIUM), "com.znsio.teswiz.screen.web.PDFValidator.PDFValidatorScreenWeb"),
            Map.entry(new ScreenKey("com.znsio.teswiz.screen.pdfValidator.PDFValidatorScreen", Platform.pdf, null),
                    "com.znsio.teswiz.screen.pdf.PDFValidatorScreenPDF"));

    private ScreenImplementationResolver() {
    }

    public static <T> Class<? extends T> resolve(Class<T> screenContract, Platform platform, WebEngine webEngine) {
        String override = OVERRIDES.get(new ScreenKey(screenContract.getName(), platform, webEngine));
        if (null == override) {
            override = OVERRIDES.get(new ScreenKey(screenContract.getName(), platform, null));
        }
        if (null != override) {
            return loadClass(screenContract, override);
        }
        return loadClass(screenContract, resolveByConvention(screenContract, platform, webEngine));
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
            case PLAYWRIGHT_TS -> "web.playwright";
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
                case PLAYWRIGHT_TS -> "PlaywrightTs";
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

    private record ScreenKey(String contractClassName, Platform platform, WebEngine webEngine) {
        private ScreenKey {
            Objects.requireNonNull(contractClassName, "contractClassName");
            Objects.requireNonNull(platform, "platform");
        }
    }
}
