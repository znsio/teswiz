package com.znsio.teswiz.screen;

import java.util.List;

enum ScreenImplementationTarget {
    ANDROID("android", "android", "Android"),
    IOS("ios", "ios", "IOS"),
    WINDOWS("windows", "windows", "Windows"),
    PDF("pdf", "pdf", "PDF"),
    WEB_SELENIUM("web-selenium", "web", "Web"),
    WEB_PLAYWRIGHT_JAVA("web-playwright-java", "web.playwrightjava", "PlaywrightJava"),
    WEB_PLAYWRIGHT_TS("web-playwright-ts", null, null);

    private final String displayName;
    private final String packageSegment;
    private final String classSuffix;

    ScreenImplementationTarget(String displayName, String packageSegment, String classSuffix) {
        this.displayName = displayName;
        this.packageSegment = packageSegment;
        this.classSuffix = classSuffix;
    }

    String displayName() {
        return displayName;
    }

    String expectedClassName(String contractClassName) {
        if (isPlaywrightTsModuleTarget()) {
            throw new IllegalStateException("playwright-ts uses TypeScript screen modules, not Java implementation classes");
        }
        String contractPackage = packageNameOf(contractClassName);
        String rootPackage = ScreenPackageConvention.rootPackageOf(contractPackage);
        String domain = ScreenPackageConvention.domainOf(contractPackage);
        String implementationPackage = joinPackage(rootPackage, packageSegment, domain);
        return implementationPackage + "." + simpleNameOf(contractClassName) + classSuffix;
    }

    String expectedReference(String contractClassName) {
        if (isPlaywrightTsModuleTarget()) {
            return "src/test/resources/playwright/screens/"
                    + new PlaywrightTsScreenModuleSupport().expectedModulePathFor(contractClassName);
        }
        return expectedClassName(contractClassName);
    }

    static List<ScreenImplementationTarget> supportedTargets() {
        return List.of(values());
    }

    private boolean isPlaywrightTsModuleTarget() {
        return this == WEB_PLAYWRIGHT_TS;
    }

    private static String simpleNameOf(String className) {
        return className.substring(className.lastIndexOf('.') + 1);
    }

    private static String packageNameOf(String className) {
        return className.substring(0, className.lastIndexOf('.'));
    }

    private static String joinPackage(String rootPackage, String packageSegment, String domain) {
        return domain.isBlank()
                ? rootPackage + "." + packageSegment
                : rootPackage + "." + packageSegment + "." + domain;
    }
}
