package com.znsio.teswiz.web.provider.selenium;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.znsio.teswiz.context.SessionContext;
import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.runner.Setup;
import com.znsio.teswiz.tools.JsonFile;

class SeleniumRemoteWebDriverRequestResolverTest {
    private static final String LOCAL_WEB_CONFIG = "./configs/theapp/theapp_local_web_config.properties";
    private static final String BROWSERSTACK_WEB_CONFIG = "./configs/theapp/theapp_browserstack_web_config.properties";
    private static final String LAMBDATEST_WEB_CONFIG = "./configs/theapp/theapp_lambdatest_web_config.properties";
    private final SeleniumRemoteWebDriverRequestResolver resolver = new SeleniumRemoteWebDriverRequestResolver();

    @AfterEach
    void cleanUp() {
        SessionContext.remove(Thread.currentThread().getId());
        System.clearProperty(Setup.RUN_IN_CI);
        System.clearProperty(Setup.CAPS);
        System.clearProperty(Setup.CLOUD_USERNAME);
        System.clearProperty(Setup.CLOUD_KEY);
        try {
            setLoadedCapabilityFile(null);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    @Test
    void shouldResolveDefaultRemoteUrlForLocalGrid() {
        loadConfig(LOCAL_WEB_CONFIG);
        Setup.addToConfigs(Setup.REMOTE_WEBDRIVER_GRID_HOST_NAME, "selenium-grid");
        Setup.addToConfigs(Setup.REMOTE_WEBDRIVER_GRID_PORT, "4444");

        SeleniumRemoteWebDriverRequest request = resolver.resolve(new DesiredCapabilities());

        assertThat(request.remoteUrl()).isEqualTo("http://selenium-grid:4444/wd/hub");
        assertThat(request.capabilities()).isInstanceOf(MutableCapabilities.class);
    }

    @Test
    void shouldResolveBrowserStackRemoteUrlAndCapabilities() throws Exception {
        new TestExecutionContext("browserstack-remote-request-test");
        Path caps = createCapabilityFile("""
                {
                  "web": {
                    "browserName": "chrome",
                    "browserstackOptions": {
                      "os": "Windows",
                      "osVersion": "11"
                    }
                  },
                  "serverConfig": {
                    "server": {
                      "plugin": {
                        "device-farm": {
                          "cloud": {
                            "cloudName": "browserstack",
                            "url": "https://hub-cloud.browserstack.com",
                            "devices": []
                          }
                        }
                      }
                    }
                  }
                }
                """);
        setupCloudConfig(BROWSERSTACK_WEB_CONFIG, caps, "browserstack_user", "browserstack_key", true);

        SeleniumRemoteWebDriverRequest request = resolver.resolve(new DesiredCapabilities());

        assertThat(request.remoteUrl())
                .isEqualTo("https://browserstack_user:browserstack_key@hub-cloud.browserstack.com/wd/hub");
        assertThat(request.capabilities().getCapability("browserName")).isEqualTo("chrome");
        assertThat(request.capabilities().getCapability("bstack:options")).isNotNull();
    }

    @Test
    void shouldResolveLambdaTestRemoteUrlAndCapabilities() throws Exception {
        new TestExecutionContext("lambdatest-remote-request-test");
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
                            "url": "https://hub.lambdatest.com",
                            "devices": []
                          }
                        }
                      }
                    }
                  }
                }
                """);
        setupCloudConfig(LAMBDATEST_WEB_CONFIG, caps, "lt_user", "lt_key", true);

        SeleniumRemoteWebDriverRequest request = resolver.resolve(new DesiredCapabilities());

        assertThat(request.remoteUrl()).isEqualTo("https://lt_user:lt_key@hub.lambdatest.com/wd/hub");
        assertThat(request.capabilities().getCapability("browserName")).isEqualTo("chrome");
        assertThat(request.capabilities().getCapability("LT:Options")).isNotNull();
    }

    @Test
    void shouldResolveHeadSpinRemoteUrl() throws Exception {
        Path caps = createCapabilityFile("""
                {
                  "hostMachines": [
                    {
                      "machineIP": "headspin.example"
                    }
                  ],
                  "serverConfig": {
                    "server": {
                      "plugin": {
                        "device-farm": {
                          "cloud": {
                            "cloudName": "headspin",
                            "url": "https://headspin.example",
                            "devices": []
                          }
                        }
                      }
                    }
                  }
                }
                """);
        setupCloudConfig(LAMBDATEST_WEB_CONFIG, caps, null, "headspin_key", true);

        SeleniumRemoteWebDriverRequest request = resolver.resolve(new DesiredCapabilities());

        assertThat(request.remoteUrl()).isEqualTo("https://headspin.example/headspin_key/wd/hub");
    }

    private static Path createCapabilityFile(String contents) throws IOException {
        Path caps = Files.createTempFile("selenium-remote-request-", ".json");
        Files.writeString(caps, contents);
        return caps;
    }

    private static void loadConfig(String configFile) {
        Setup.load(configFile);
        Setup.loadAndUpdateConfigParameters(configFile);
        Setup.getExecutionArguments();
    }

    private static void setupCloudConfig(String configFile, Path caps, String cloudUser, String cloudKey,
            boolean runInCi) throws ReflectiveOperationException {
        System.setProperty(Setup.RUN_IN_CI, String.valueOf(runInCi));
        System.setProperty(Setup.CAPS, caps.toString());
        loadConfig(configFile);
        Setup.addToConfigs(Setup.CAPS, caps.toString());
        if (null != cloudUser) {
            System.setProperty(Setup.CLOUD_USERNAME, cloudUser);
            Setup.addToConfigs(Setup.CLOUD_USERNAME, cloudUser);
        }
        if (null != cloudKey) {
            System.setProperty(Setup.CLOUD_KEY, cloudKey);
            Setup.addToConfigs(Setup.CLOUD_KEY, cloudKey);
        }
        setLoadedCapabilityFile(JsonFile.loadJsonFile(caps.toString()));
    }

    private static void setLoadedCapabilityFile(Map<String, Map> loadedCapabilities)
            throws NoSuchFieldException, IllegalAccessException {
        Field loadedCapabilityFileField = Setup.class.getDeclaredField("loadedCapabilityFile");
        loadedCapabilityFileField.setAccessible(true);
        loadedCapabilityFileField.set(null, loadedCapabilities);
    }
}
