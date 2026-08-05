package com.znsio.teswiz.web.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.JavascriptExecutor;

import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.session.SessionHandle;

import java.util.Map;

class WebExecutionProviderAdapterTest {
    @AfterEach
    void cleanUp() {
        System.clearProperty("cloudName");
    }

    @Test
    void shouldResolveKnownProvidersAndFallbackToLocal() {
        assertThat(new WebExecutionProviderResolver().resolve("browserstack")).isInstanceOf(BrowserStackWebExecutionProvider.class);
        assertThat(new WebExecutionProviderResolver().resolve("lambdatest")).isInstanceOf(LambdaTestWebExecutionProvider.class);
        assertThat(new WebExecutionProviderResolver().resolve("headspin")).isInstanceOf(HeadSpinWebExecutionProvider.class);
        assertThat(new WebExecutionProviderResolver().resolve(null)).isInstanceOf(LocalWebExecutionProvider.class);
        assertThat(new WebExecutionProviderResolver().resolve("not-set")).isInstanceOf(LocalWebExecutionProvider.class);
    }

    @Test
    void browserStackProviderShouldIgnoreExecutorFailures() {
        JavascriptExecutor failingExecutor = new FailingJavascriptExecutor();

        assertThatNoException().isThrownBy(() -> new BrowserStackWebExecutionProvider()
                .updateSessionStatus(failingExecutor, "passed", "Scenario passed"));
        assertThatNoException().isThrownBy(() -> new BrowserStackWebExecutionProvider()
                .updateSessionName(failingExecutor, "sample-test"));
    }

    @Test
    void lambdaTestProviderShouldIgnoreExecutorFailures() {
        JavascriptExecutor failingExecutor = new FailingJavascriptExecutor();

        assertThatNoException().isThrownBy(() -> new LambdaTestWebExecutionProvider()
                .updateSessionStatus(failingExecutor, "failed", "Assertion failure"));
        assertThatNoException().isThrownBy(() -> new LambdaTestWebExecutionProvider()
                .updateSessionName(failingExecutor, "sample-test"));
    }

    @Test
    void shouldBuildProviderSpecificWebReportMessages() {
        assertThat(new BrowserStackWebExecutionProvider().buildReportMessage(new SessionHandle(
                "buyer",
                Platform.web,
                "playwright-ts",
                "session-1",
                "/tmp/artifacts",
                Map.of("providerReportUrl", "https://browserstack.example/session-1"))))
                .hasValue("BrowserStack Report link available here: https://browserstack.example/session-1");

        assertThat(new LambdaTestWebExecutionProvider().buildReportMessage(new SessionHandle(
                "seller",
                Platform.web,
                "playwright-java",
                "session-2",
                "/tmp/artifacts",
                Map.of("providerSessionId", "lt-session-1"))))
                .hasValue("LambdaTest Report link available here: https://automation.lambdatest.com/logs/?sessionID=lt-session-1");

        assertThat(new LocalWebExecutionProvider().buildReportMessage(new SessionHandle(
                "guest",
                Platform.web,
                "selenium",
                "session-3",
                "/tmp/artifacts",
                Map.of())))
                .isEmpty();
    }

    private static final class FailingJavascriptExecutor implements JavascriptExecutor {
        @Override
        public Object executeScript(String script, Object... args) {
            throw new RuntimeException("Session not started or terminated");
        }

        @Override
        public Object executeAsyncScript(String script, Object... args) {
            throw new RuntimeException("Session not started or terminated");
        }
    }
}
