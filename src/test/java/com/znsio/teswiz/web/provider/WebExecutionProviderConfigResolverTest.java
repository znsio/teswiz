package com.znsio.teswiz.web.provider;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.znsio.teswiz.context.SessionContext;
import com.znsio.teswiz.runner.Setup;
import com.znsio.teswiz.tools.JsonFile;

class WebExecutionProviderConfigResolverTest {
    @AfterEach
    void cleanUp() {
        SessionContext.remove(Thread.currentThread().getId());
        System.clearProperty(Setup.CAPS);
        System.clearProperty(Setup.CLOUD_USERNAME);
        System.clearProperty(Setup.CLOUD_KEY);
        try {
            setLoadedCapabilityFile(null);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    @Test
    void shouldResolveLocalProviderWhenCapabilitiesAreUnavailable() {
        WebExecutionProviderConfig config = new WebExecutionProviderConfigResolver().resolve();

        assertThat(config.providerName()).isEqualTo("local");
        assertThat(config.isRemote()).isFalse();
    }

    @Test
    void shouldResolveBrowserStackProviderDetailsFromCapabilities() throws Exception {
        Path caps = createCapabilityFile("""
                {
                  "web": {
                    "browserName": "chrome",
                    "platform": "Windows 11"
                  },
                  "serverConfig": {
                    "server": {
                      "plugin": {
                        "device-farm": {
                          "cloud": {
                            "cloudName": "browserstack",
                            "url": "https://hub-cloud.browserstack.com/wd/hub",
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
        System.setProperty(Setup.CLOUD_USERNAME, "browserstack_user");
        System.setProperty(Setup.CLOUD_KEY, "browserstack_key");
        Setup.addToConfigs(Setup.CLOUD_USERNAME, "browserstack_user");
        Setup.addToConfigs(Setup.CLOUD_KEY, "browserstack_key");
        setLoadedCapabilityFile(JsonFile.loadJsonFile(caps.toString()));

        WebExecutionProviderConfig config = new WebExecutionProviderConfigResolver().resolve();

        assertThat(config.providerName()).isEqualTo("browserstack");
        assertThat(config.isRemote()).isTrue();
        assertThat(config.remoteUrl()).isEqualTo("https://hub-cloud.browserstack.com/wd/hub");
        assertThat(config.apiUrl()).isEqualTo("https://api-cloud.browserstack.com/app-automate/");
        assertThat(config.username()).isEqualTo("browserstack_user");
        assertThat(config.accessKey()).isEqualTo("browserstack_key");
        assertThat(config.webCapabilities()).containsEntry("browserName", "chrome");
    }

    @Test
    void shouldResolveLambdaTestProviderDetailsFromCapabilities() throws Exception {
        Path caps = createCapabilityFile("""
                {
                  "web": {
                    "browserName": "chrome",
                    "platform": "Windows 11"
                  },
                  "serverConfig": {
                    "server": {
                      "plugin": {
                        "device-farm": {
                          "cloud": {
                            "cloudName": "lambdatest",
                            "url": "https://hub.lambdatest.com/wd/hub",
                            "apiUrl": "https://manual-api.lambdatest.com",
                            "devices": []
                          }
                        }
                      }
                    }
                  }
                }
                """);
        Setup.addToConfigs(Setup.CAPS, caps.toString());
        System.setProperty(Setup.CLOUD_USERNAME, "lt_user");
        System.setProperty(Setup.CLOUD_KEY, "lt_key");
        Setup.addToConfigs(Setup.CLOUD_USERNAME, "lt_user");
        Setup.addToConfigs(Setup.CLOUD_KEY, "lt_key");
        setLoadedCapabilityFile(JsonFile.loadJsonFile(caps.toString()));

        WebExecutionProviderConfig config = new WebExecutionProviderConfigResolver().resolve();

        assertThat(config.providerName()).isEqualTo("lambdatest");
        assertThat(config.isRemote()).isTrue();
        assertThat(config.remoteUrl()).isEqualTo("https://hub.lambdatest.com/wd/hub");
        assertThat(config.apiUrl()).isEqualTo("https://manual-api.lambdatest.com");
        assertThat(config.username()).isEqualTo("lt_user");
        assertThat(config.accessKey()).isEqualTo("lt_key");
        assertThat(config.webCapabilities())
                .containsEntry("browserName", "chrome")
                .containsEntry("platform", "Windows 11");
    }

    private static Path createCapabilityFile(String contents) throws IOException {
        Path caps = Files.createTempFile("web-provider-config-", ".json");
        Files.writeString(caps, contents);
        return caps;
    }

    private static void setLoadedCapabilityFile(Map<String, Map> loadedCapabilities)
            throws NoSuchFieldException, IllegalAccessException {
        Field loadedCapabilityFileField = Setup.class.getDeclaredField("loadedCapabilityFile");
        loadedCapabilityFileField.setAccessible(true);
        loadedCapabilityFileField.set(null, loadedCapabilities);
    }
}
