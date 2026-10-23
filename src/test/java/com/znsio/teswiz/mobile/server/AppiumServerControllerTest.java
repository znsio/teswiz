package com.znsio.teswiz.mobile.server;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AppiumServerControllerTest {
    @ParameterizedTest
    @CsvSource({
            "http://localhost:4723, http://localhost:4723/wd/hub",
            "http://localhost:4723/, http://localhost:4723/wd/hub",
            "http://localhost:4723/wd, http://localhost:4723/wd/hub",
            "http://localhost:4723/wd/hub, http://localhost:4723/wd/hub"
    })
    void shouldNormalizeRemoteHubUrls(String providedUrl, String expectedUrl) {
        assertThat(AppiumServerController.normalizeRemoteHubUrl(providedUrl)).isEqualTo(expectedUrl);
    }
}
