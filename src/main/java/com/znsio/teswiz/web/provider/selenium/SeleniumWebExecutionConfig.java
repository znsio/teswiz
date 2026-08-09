package com.znsio.teswiz.web.provider.selenium;

record SeleniumWebExecutionConfig(
        String appName,
        String launchName,
        String logDir,
        String sessionName,
        String cloudUser,
        String cloudKey,
        String proxyUrl,
        boolean cloudUseLocalTesting,
        boolean cloudUseProxy) {
}
