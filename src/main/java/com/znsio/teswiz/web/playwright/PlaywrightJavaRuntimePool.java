package com.znsio.teswiz.web.playwright;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.znsio.teswiz.config.browser.PlaywrightBrowserConfig;
import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.TEST_CONTEXT;

final class PlaywrightJavaRuntimePool {
    private static final String PLAYWRIGHT_JAVA_RUNTIMES = TEST_CONTEXT.class.getName() + ".playwrightJavaRuntimes";
    private final TestExecutionContext context;
    private final PlaywrightJavaDriverManager.PlaywrightJavaRuntimeFactory runtimeFactory;

    PlaywrightJavaRuntimePool(TestExecutionContext context,
            PlaywrightJavaDriverManager.PlaywrightJavaRuntimeFactory runtimeFactory) {
        this.context = context;
        this.runtimeFactory = runtimeFactory;
    }

    PlaywrightJavaRuntime getOrCreate(PlaywrightBrowserConfig browserConfig, Path artifactDirectory) {
        Map<String, PlaywrightJavaRuntime> runtimes = getRuntimeMap();
        String runtimeKey = RuntimeKey.forBrowserConfig(browserConfig);
        return runtimes.computeIfAbsent(runtimeKey, ignored -> runtimeFactory.create(browserConfig, artifactDirectory));
    }

    void remove(PlaywrightBrowserConfig browserConfig) {
        getRuntimeMap().remove(RuntimeKey.forBrowserConfig(browserConfig));
    }

    @SuppressWarnings("unchecked")
    private Map<String, PlaywrightJavaRuntime> getRuntimeMap() {
        Object existing = context.getTestState(PLAYWRIGHT_JAVA_RUNTIMES);
        if (existing instanceof Map<?, ?> runtimeMap) {
            return (Map<String, PlaywrightJavaRuntime>) runtimeMap;
        }
        Map<String, PlaywrightJavaRuntime> runtimes = new LinkedHashMap<>();
        context.addTestState(PLAYWRIGHT_JAVA_RUNTIMES, runtimes);
        return runtimes;
    }

    private record RuntimeKey(String browserName, boolean headless, String channel, String executablePath) {
        private static String forBrowserConfig(PlaywrightBrowserConfig browserConfig) {
            return new RuntimeKey(browserConfig.browserName(), browserConfig.headless(), browserConfig.channel(),
                    browserConfig.executablePath()).toString();
        }

        @Override
        public String toString() {
            return String.join("|",
                    safeValue(browserName),
                    Boolean.toString(headless),
                    safeValue(channel),
                    safeValue(executablePath));
        }

        private static String safeValue(String value) {
            return Objects.toString(value, "");
        }
    }
}
