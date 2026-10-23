package com.znsio.teswiz.web.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;

class WebSessionMetadataBuilderTest {
    @Test
    void shouldBuildLocalSessionMetadataForLocalWebDriver() {
        WebExecutionProviderResolver providerResolver = mock(WebExecutionProviderResolver.class);
        when(providerResolver.resolve()).thenReturn(new LocalWebExecutionProvider());
        WebSessionMetadataBuilder builder = new WebSessionMetadataBuilder(providerResolver,
                mock(WebCloudSessionMetadataResolver.class));

        org.openqa.selenium.WebDriver webDriver = mock(org.openqa.selenium.WebDriver.class);
        Map<String, String> metadata = builder.build("chrome", webDriver);

        assertThat(metadata)
                .containsEntry("browserName", "chrome")
                .containsEntry("provider", "local")
                .containsKey("driverClass")
                .doesNotContainKeys("remoteSessionId", "providerSessionId", "providerReportUrl");
    }

    @Test
    void shouldMergeBrowserStackCloudMetadataForRemoteWebDriver() {
        WebExecutionProviderResolver providerResolver = mock(WebExecutionProviderResolver.class);
        when(providerResolver.resolve()).thenReturn(new BrowserStackWebExecutionProvider());
        WebCloudSessionMetadataResolver cloudSessionMetadataResolver = mock(WebCloudSessionMetadataResolver.class);
        WebSessionMetadataBuilder builder = new WebSessionMetadataBuilder(providerResolver, cloudSessionMetadataResolver);
        RemoteWebDriver remoteWebDriver = mock(RemoteWebDriver.class);
        when(remoteWebDriver.getSessionId()).thenReturn(new SessionId("remote-session-1"));
        when(cloudSessionMetadataResolver.resolve(remoteWebDriver, "browserstack"))
                .thenReturn(Map.of(
                        "providerSessionId", "bs-session-1",
                        "providerReportUrl", "https://browserstack.example/session/bs-session-1"));

        Map<String, String> metadata = builder.build("chrome", remoteWebDriver);

        assertThat(metadata)
                .containsEntry("browserName", "chrome")
                .containsEntry("provider", "browserstack")
                .containsEntry("remoteSessionId", "remote-session-1")
                .containsEntry("providerSessionId", "bs-session-1")
                .containsEntry("providerReportUrl", "https://browserstack.example/session/bs-session-1");
    }

    @Test
    void shouldMergeLambdaTestCloudMetadataForRemoteWebDriver() {
        WebExecutionProviderResolver providerResolver = mock(WebExecutionProviderResolver.class);
        when(providerResolver.resolve()).thenReturn(new LambdaTestWebExecutionProvider());
        WebCloudSessionMetadataResolver cloudSessionMetadataResolver = mock(WebCloudSessionMetadataResolver.class);
        WebSessionMetadataBuilder builder = new WebSessionMetadataBuilder(providerResolver, cloudSessionMetadataResolver);
        RemoteWebDriver remoteWebDriver = mock(RemoteWebDriver.class);
        when(remoteWebDriver.getSessionId()).thenReturn(new SessionId("remote-session-2"));
        when(cloudSessionMetadataResolver.resolve(remoteWebDriver, "lambdatest"))
                .thenReturn(Map.of(
                        "providerSessionId", "lt-session-1",
                        "providerReportUrl", "https://automation.lambdatest.com/logs/?sessionID=lt-session-1"));

        Map<String, String> metadata = builder.build("firefox", remoteWebDriver);

        assertThat(metadata)
                .containsEntry("browserName", "firefox")
                .containsEntry("provider", "lambdatest")
                .containsEntry("remoteSessionId", "remote-session-2")
                .containsEntry("providerSessionId", "lt-session-1")
                .containsEntry("providerReportUrl", "https://automation.lambdatest.com/logs/?sessionID=lt-session-1");
    }
}
