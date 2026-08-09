package com.znsio.teswiz.mobile.provider;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;

class MobileExecutionProviderTest {
    @Test
    void shouldResolveKnownProvidersAndFallbackToLocal() {
        MobileExecutionProviderResolver resolver = new MobileExecutionProviderResolver();

        assertThat(resolver.resolve("browserstack")).isInstanceOf(BrowserStackMobileExecutionProvider.class);
        assertThat(resolver.resolve("lambdatest")).isInstanceOf(LambdaTestMobileExecutionProvider.class);
        assertThat(resolver.resolve("headspin")).isInstanceOf(HeadSpinMobileExecutionProvider.class);
        assertThat(resolver.resolve("pCloudy")).isInstanceOf(PCloudyMobileExecutionProvider.class);
        assertThat(resolver.resolve("not-set")).isInstanceOf(LocalMobileExecutionProvider.class);
        assertThat(resolver.resolve(null)).isInstanceOf(LocalMobileExecutionProvider.class);
    }

    @Test
    void shouldBuildProviderSpecificReportMessages() {
        assertThat(new HeadSpinMobileExecutionProvider().buildReportMessage("session-1", Optional::empty))
                .hasValueSatisfying(message -> {
                    assertThat(message).contains("Headspin Report link available here");
                    assertThat(message).contains("https://ui-dev.headspin.io/sessions/session-1/waterfall");
                });

        assertThat(new LambdaTestMobileExecutionProvider().buildReportMessage("session-1", Optional::empty))
                .hasValueSatisfying(message -> {
                    assertThat(message).contains("LambdaTest Report link available here");
                    assertThat(message).contains("https://automation.lambdatest.com/logs/?sessionID=session-1");
                });

        assertThat(new BrowserStackMobileExecutionProvider()
                .buildReportMessage("session-1", () -> Optional.of("https://browserstack.example/session-1")))
                .hasValueSatisfying(message -> {
                    assertThat(message).contains("BrowserStack Report link available here");
                    assertThat(message).contains("https://browserstack.example/session-1");
                });

        assertThat(new PCloudyMobileExecutionProvider()
                .buildReportMessage("session-1", () -> Optional.of("https://pcloudy.example/report")))
                .hasValueSatisfying(message -> {
                    assertThat(message).contains("pCloudy Report link available here");
                    assertThat(message).contains("https://pcloudy.example/report");
                });
    }

    @Test
    void localProviderShouldNotEmitReportMessage() {
        assertThat(new LocalMobileExecutionProvider().buildReportMessage("session-1", Optional::empty)).isEmpty();
    }
}
