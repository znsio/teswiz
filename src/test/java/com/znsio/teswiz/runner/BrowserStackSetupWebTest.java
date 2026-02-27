package com.znsio.teswiz.runner;

import com.znsio.teswiz.context.SessionContext;
import com.znsio.teswiz.context.TestExecutionContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class BrowserStackSetupWebTest {
    private static final String WEB_CONFIG = "./configs/theapp/theapp_browserstack_web_config.properties";

    @AfterEach
    void cleanup() {
        SessionContext.remove(Thread.currentThread().getId());
    }

    @Test
    void shouldSupportBstackOptionsWhenBrowserstackOptionsNodeIsMissing() throws IOException {
        Setup.load(WEB_CONFIG);
        Setup.loadAndUpdateConfigParameters(WEB_CONFIG);
        new TestExecutionContext("browserstack-web-options-fallback-test");

        Path caps = Files.createTempFile("bs-web-caps-", ".json");
        Files.writeString(caps, """
                {
                  "web": {
                    "browserName": "chrome",
                    "bstack:options": {
                      "os": "Windows",
                      "osVersion": "11",
                      "browserVersion": "latest"
                    }
                  },
                  "serverConfig": {
                    "server": {
                      "plugin": {
                        "device-farm": {
                          "cloud": {
                            "cloudName": "browserstack",
                            "url": "https://hub-cloud.browserstack.com",
                            "apiUrl": "https://api-cloud.browserstack.com/app-automate/",
                            "devices": []
                          }
                        }
                      }
                    }
                  }
                }
                """);
        Setup.addToConfigs(Setup.CAPS, caps.toString());

        MutableCapabilities inputCaps = new DesiredCapabilities();
        MutableCapabilities updatedCaps = BrowserStackSetup.updateBrowserStackCapabilities(inputCaps);

        assertThat(updatedCaps.getCapability("browserName")).isEqualTo("chrome");
        Map<String, Object> bstackOptions = (Map<String, Object>) updatedCaps.getCapability("bstack:options");
        assertThat(bstackOptions).isNotNull();
        assertThat(bstackOptions.get("os")).isEqualTo("Windows");
        assertThat(bstackOptions.get("osVersion")).isEqualTo("11");
        assertThat(bstackOptions.get("projectName")).isNotNull();
        assertThat(bstackOptions.get("buildName")).isNotNull();
        assertThat(bstackOptions.get("sessionName")).isEqualTo("browserstack-web-options-fallback-test");
    }
}
