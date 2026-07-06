package com.znsio.teswiz.runner;

import java.util.List;
import java.util.Map;

record PlaywrightBrowserConfig(
        String browserName,
        boolean headless,
        List<String> launchArgs,
        String channel,
        String executablePath,
        Map<String, Object> contextOptions,
        Map<String, Object> launchOptions) {
}
