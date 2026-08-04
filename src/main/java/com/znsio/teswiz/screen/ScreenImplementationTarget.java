package com.znsio.teswiz.screen;

import java.util.List;

enum ScreenImplementationTarget {
    ANDROID("android", "android", "Android"),
    IOS("ios", "ios", "IOS"),
    WINDOWS("windows", "windows", "Windows"),
    PDF("pdf", "pdf", "PDF"),
    WEB_SELENIUM("web-selenium", "web", "Web"),
    WEB_PLAYWRIGHT_JAVA("web-playwright-java", "web.playwrightjava", "PlaywrightJava"),
    WEB_PLAYWRIGHT_TS("web-playwright-ts", "web.playwright", "PlaywrightTsAdapter");

    private static final String ROOT_PACKAGE = "com.znsio.teswiz.screen";

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
        String domain = domainOf(contractClassName);
        String implementationPackage = joinPackage(packageSegment, domain);
        return implementationPackage + "." + simpleNameOf(contractClassName) + classSuffix;
    }

    static List<ScreenImplementationTarget> supportedTargets() {
        return List.of(values());
    }

    private static String simpleNameOf(String className) {
        return className.substring(className.lastIndexOf('.') + 1);
    }

    private static String domainOf(String contractClassName) {
        String contractPackage = contractClassName.substring(0, contractClassName.lastIndexOf('.'));
        if (contractPackage.equals(ROOT_PACKAGE)) {
            return "";
        }
        return contractPackage.substring((ROOT_PACKAGE + ".").length());
    }

    private static String joinPackage(String packageSegment, String domain) {
        return domain.isBlank()
                ? ROOT_PACKAGE + "." + packageSegment
                : ROOT_PACKAGE + "." + packageSegment + "." + domain;
    }
}
