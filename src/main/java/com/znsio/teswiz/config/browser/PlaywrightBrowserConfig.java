package com.znsio.teswiz.config.browser;

import java.util.List;
import java.util.Map;

public record PlaywrightBrowserConfig(
        String browserName,
        boolean headless,
        List<String> launchArgs,
        String channel,
        String executablePath,
        Map<String, Object> contextOptions,
        Map<String, Object> launchOptions) {
}
