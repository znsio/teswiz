package com.znsio.teswiz.web.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.JavascriptExecutor;

class WebCloudSessionMetadataResolverTest {
    private final WebCloudSessionMetadataResolver resolver = new WebCloudSessionMetadataResolver();

    @Test
    void shouldResolveBrowserStackSessionMetadata() {
        JavascriptExecutor executor = mock(JavascriptExecutor.class);
        when(executor.executeScript("browserstack_executor: {\"action\": \"getSessionDetails\"}"))
                .thenReturn("""
                        {
                          "hashed_id": "bs-session-1",
                          "build_hashed_id": "bs-build-1",
                          "browser_url": "https://browserstack.example/session/bs-session-1",
                          "public_url": "https://browserstack.example/public/bs-session-1",
                          "logs": "https://browserstack.example/logs/bs-session-1",
                          "video_url": "https://browserstack.example/video/bs-session-1",
                          "browser_console_logs_url": "https://browserstack.example/console/bs-session-1",
                          "har_logs_url": "https://browserstack.example/har/bs-session-1",
                          "playwright_logs_url": "https://browserstack.example/pw/bs-session-1"
                        }
                        """);

        Map<String, String> metadata = resolver.resolve(executor, "browserstack");

        assertThat(metadata)
                .containsEntry("providerSessionId", "bs-session-1")
                .containsEntry("providerBuildId", "bs-build-1")
                .containsEntry("providerReportUrl", "https://browserstack.example/session/bs-session-1")
                .containsEntry("providerPublicUrl", "https://browserstack.example/public/bs-session-1")
                .containsEntry("providerLogsUrl", "https://browserstack.example/logs/bs-session-1")
                .containsEntry("providerVideoUrl", "https://browserstack.example/video/bs-session-1")
                .containsEntry("providerConsoleLogsUrl", "https://browserstack.example/console/bs-session-1")
                .containsEntry("providerNetworkLogsUrl", "https://browserstack.example/har/bs-session-1")
                .containsEntry("providerPlaywrightLogsUrl", "https://browserstack.example/pw/bs-session-1");
    }

    @Test
    void shouldResolveLambdaTestSessionMetadata() {
        JavascriptExecutor executor = mock(JavascriptExecutor.class);
        when(executor.executeScript("lambdatest_action: {\"action\": \"getTestDetails\"}"))
                .thenReturn("""
                        {
                          "data": {
                            "session_id": "lt-session-1",
                            "test_id": "lt-test-1",
                            "build_id": "lt-build-1",
                            "video_url": "https://lambdatest.example/video/lt-session-1",
                            "console_logs_url": "https://lambdatest.example/console/lt-session-1",
                            "network_logs_url": "https://lambdatest.example/network/lt-session-1",
                            "command_logs_url": "https://lambdatest.example/commands/lt-session-1",
                            "screenshot_url": "https://lambdatest.example/screenshots/lt-session-1"
                          }
                        }
                        """);

        Map<String, String> metadata = resolver.resolve(executor, "lambdatest");

        assertThat(metadata)
                .containsEntry("providerSessionId", "lt-session-1")
                .containsEntry("providerBuildId", "lt-build-1")
                .containsEntry("providerReportUrl", "https://automation.lambdatest.com/logs/?sessionID=lt-session-1")
                .containsEntry("providerVideoUrl", "https://lambdatest.example/video/lt-session-1")
                .containsEntry("providerConsoleLogsUrl", "https://lambdatest.example/console/lt-session-1")
                .containsEntry("providerNetworkLogsUrl", "https://lambdatest.example/network/lt-session-1")
                .containsEntry("providerCommandLogsUrl", "https://lambdatest.example/commands/lt-session-1")
                .containsEntry("providerScreenshotUrl", "https://lambdatest.example/screenshots/lt-session-1");
    }

    @Test
    void shouldReturnEmptyMetadataForLocalOrUnknownProviders() {
        JavascriptExecutor executor = mock(JavascriptExecutor.class);

        assertThat(resolver.resolve(executor, "local")).isEmpty();
        assertThat(resolver.resolve(executor, "unsupported-provider")).isEmpty();
        assertThat(resolver.resolve(executor, null)).isEmpty();

        verifyNoInteractions(executor);
    }
}
