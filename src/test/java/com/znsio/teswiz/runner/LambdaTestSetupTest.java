package com.znsio.teswiz.runner;

import com.znsio.teswiz.context.SessionContext;
import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.exceptions.InvalidTestDataException;
import com.znsio.teswiz.tools.JsonFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LambdaTestSetupTest {
    private static final String LAMBDATEST_WEB_CONFIG = "./configs/theapp/theapp_lambdatest_web_config.properties";

    @AfterEach
    void cleanUp() {
        SessionContext.remove(Thread.currentThread().getId());
        System.clearProperty(Setup.CLOUD_UPLOAD_APP);
        System.clearProperty(Setup.APP_PATH);
    }

    @Test
    void shouldBuildW3CCompatibleLambdaTestWebCapabilities() {
        setupConfig(LAMBDATEST_WEB_CONFIG);
        new TestExecutionContext("lambda-web-w3c-cap-test");

        MutableCapabilities capabilities = LambdaTestSetup.updateLambdaTestCapabilities(
                new DesiredCapabilities());

        assertThat(capabilities.getCapability("browserName")).isEqualTo("chrome");
        assertThat(capabilities.getCapability("browserVersion")).isEqualTo("latest");
        assertThat(capabilities.getCapability("platformName")).hasToString("Windows 11");

        // Must not be present at top-level; Selenium 4 W3C validation rejects these.
        assertThat(capabilities.getCapability("build")).isNull();
        assertThat(capabilities.getCapability("name")).isNull();
        assertThat(capabilities.getCapability("version")).isNull();
        assertThat(capabilities.getCapability("platform")).isNull();
        assertThat(capabilities.getCapability("resolution")).isNull();
        assertThat(capabilities.getCapability("network")).isNull();
        assertThat(capabilities.getCapability("console")).isNull();
        assertThat(capabilities.getCapability("visual")).isNull();
        assertThat(capabilities.getCapability("tunnel")).isNull();

        Map<String, Object> ltOptions = (Map<String, Object>) capabilities.getCapability("LT:Options");
        assertThat(ltOptions).isNotNull();
        assertThat(ltOptions.get("username")).isEqualTo("lambdatest_username");
        assertThat(ltOptions.get("accessKey")).isEqualTo("lambdatest_accesskey");
        assertThat(ltOptions.get("w3c")).isEqualTo(true);
        assertThat(ltOptions.get("resolution")).isEqualTo("1920x1080");
        assertThat(ltOptions.get("network")).isEqualTo(true);
        assertThat(ltOptions.get("appProfiling")).isEqualTo(true);
        assertThat(ltOptions.get("console")).isEqualTo(true);
        assertThat(ltOptions.get("visual")).isEqualTo(true);
        assertThat(ltOptions.get("tunnel")).isEqualTo(false);
    }

    @Test
    void shouldDerivePlatformNameFromOsAndOsVersionWhenPlatformMissing() throws IOException {
        setupConfig(LAMBDATEST_WEB_CONFIG);
        new TestExecutionContext("lambda-web-os-derive-test");

        Path tempCaps = Files.createTempFile("lt-web-caps-", ".json");
        String tempCapabilities = """
                {
                  "web": {
                    "browser": "Chrome",
                    "browser_version": "109.0",
                    "os": "Windows",
                    "os_version": "10",
                    "resolution": "1024x768",
                    "network": true,
                    "appProfiling": false,
                    "console": true,
                    "visual": true,
                    "tunnel": true
                  },
                  "serverConfig": {
                    "server": {
                      "plugin": {
                        "device-farm": {
                          "cloud": {
                            "cloudName": "lambdatest",
                            "url": "https://mobile-hub.lambdatest.com",
                            "devices": []
                          }
                        }
                      }
                    }
                  }
                }
                """;
        Files.writeString(tempCaps, tempCapabilities);
        Setup.addToConfigs(Setup.CAPS, tempCaps.toString());

        MutableCapabilities capabilities = LambdaTestSetup.updateLambdaTestCapabilities(
                new DesiredCapabilities());

        assertThat(capabilities.getCapability("browserName")).isEqualTo("Chrome");
        assertThat(capabilities.getCapability("browserVersion")).isEqualTo("109.0");
        assertThat(capabilities.getCapability("platformName")).hasToString("Windows 10");

        Map<String, Object> ltOptions = (Map<String, Object>) capabilities.getCapability("LT:Options");
        assertThat(ltOptions.get("resolution")).isEqualTo("1024x768");
        assertThat(ltOptions.get("tunnel")).isEqualTo(true);
        assertThat(ltOptions.get("network")).isEqualTo(true);
        assertThat(ltOptions.get("appProfiling")).isEqualTo(false);
    }

    @Test
    void shouldUseMobileLambdaTestNetworkAndAppProfilingFromCapabilities() throws IOException {
        setupConfig("./configs/theapp/theapp_lambdatest_android_config.properties");
        Path tempCaps = Files.createTempFile("lt-mobile-caps-", ".json");
        String tempCapabilities = """
                {
                  "android": {
                    "app": "lt://APP123",
                    "browserName": "chrome",
                    "platformName": "Android",
                    "deviceName": "Galaxy S23",
                    "platformVersion": "13",
                    "lt:options": {
                      "network": false,
                      "visual": true
                    }
                  },
                  "serverConfig": {
                    "server": {
                      "plugin": {
                        "device-farm": {
                          "cloud": {
                            "cloudName": "lambdatest",
                            "url": "https://mobile-hub.lambdatest.com",
                            "apiUrl": "https://manual-api.lambdatest.com",
                            "devices": []
                          }
                        }
                      }
                    }
                  }
                }
                """;
        Files.writeString(tempCaps, tempCapabilities);
        Setup.addToConfigs(Setup.CAPS, tempCaps.toString());
        Setup.addToConfigs(Setup.CLOUD_UPLOAD_APP, "false");
        System.setProperty(Setup.CLOUD_UPLOAD_APP, "false");

        LambdaTestSetup.updateLambdaTestCapabilities("https://manual-api.lambdatest.com");

        Map<String, Map> updatedCaps = JsonFile.loadJsonFile(Setup.getFromConfigs(Setup.CAPS));
        Map<String, Object> androidCaps = updatedCaps.get("android");
        Map<String, Object> ltOptions = (Map<String, Object>) androidCaps.get("lt:options");

        assertThat(ltOptions.get("network")).isEqualTo(false);
        assertThat(ltOptions.get("appProfiling")).isNull();
    }

    @Test
    void shouldUseLambdaTestAppReferenceFromAppPathWhenUploadIsDisabled() throws IOException {
        System.setProperty(Setup.CLOUD_UPLOAD_APP, "false");
        System.setProperty(Setup.APP_PATH, "lt://APP123");
        setupConfig("./configs/theapp/theapp_lambdatest_ios_config.properties");
        Path tempCaps = Files.createTempFile("lt-mobile-ios-caps-", ".json");
        String tempCapabilities = """
                {
                  "iOS": {
                    "app": "temp/sampleApps/TheApp.ipa",
                    "platformName": "iOS",
                    "deviceName": "iPhone 15",
                    "platformVersion": "17",
                    "lt:options": {
                      "network": true
                    }
                  },
                  "serverConfig": {
                    "server": {
                      "plugin": {
                        "device-farm": {
                          "cloud": {
                            "cloudName": "lambdatest",
                            "url": "https://mobile-hub.lambdatest.com",
                            "apiUrl": "https://manual-api.lambdatest.com",
                            "devices": []
                          }
                        }
                      }
                    }
                  }
                }
                """;
        Files.writeString(tempCaps, tempCapabilities);
        Setup.addToConfigs(Setup.CAPS, tempCaps.toString());

        LambdaTestSetup.updateLambdaTestCapabilities("https://manual-api.lambdatest.com");

        Map<String, Map> updatedCaps = JsonFile.loadJsonFile(Setup.getFromConfigs(Setup.CAPS));
        Map<String, Object> iosCaps = updatedCaps.get("iOS");

        assertThat(iosCaps.get("app")).isEqualTo("lt://APP123");
    }

    @Test
    void shouldFailFastWhenLambdaTestUploadIsDisabledWithoutAppReference() throws IOException {
        System.setProperty(Setup.CLOUD_UPLOAD_APP, "false");
        System.setProperty(Setup.APP_PATH, "temp/sampleApps/TheApp.ipa");
        setupConfig("./configs/theapp/theapp_lambdatest_ios_config.properties");
        Path tempCaps = Files.createTempFile("lt-mobile-ios-missing-app-ref-", ".json");
        String tempCapabilities = """
                {
                  "iOS": {
                    "app": "temp/sampleApps/TheApp.ipa",
                    "platformName": "iOS",
                    "deviceName": "iPhone 15",
                    "platformVersion": "17"
                  },
                  "serverConfig": {
                    "server": {
                      "plugin": {
                        "device-farm": {
                          "cloud": {
                            "cloudName": "lambdatest",
                            "url": "https://mobile-hub.lambdatest.com",
                            "apiUrl": "https://manual-api.lambdatest.com",
                            "devices": []
                          }
                        }
                      }
                    }
                  }
                }
                """;
        Files.writeString(tempCaps, tempCapabilities);
        Setup.addToConfigs(Setup.CAPS, tempCaps.toString());

        InvalidTestDataException exception = assertThrows(InvalidTestDataException.class,
                () -> LambdaTestSetup.updateLambdaTestCapabilities("https://manual-api.lambdatest.com"));

        assertThat(exception.getMessage()).contains("valid LambdaTest app id");
    }

    @Test
    void cleanupCloudExecutionShouldSupportLambdaTest() throws Exception {
        setupConfig(LAMBDATEST_WEB_CONFIG);
        String capsPath = Setup.getFromConfigs(Setup.CAPS);
        Map<String, Map> loadedCaps = JsonFile.loadJsonFile(capsPath);
        setLoadedCapabilityFile(loadedCaps);

        assertDoesNotThrow(DeviceSetup::cleanupCloudExecution);
    }

    private static void setupConfig(String configPath) {
        Setup.load(configPath);
        Setup.loadAndUpdateConfigParameters(configPath);
    }

    private static void setLoadedCapabilityFile(Map<String, Map> loadedCapabilities)
            throws NoSuchFieldException, IllegalAccessException {
        Field loadedCapabilityFileField = Setup.class.getDeclaredField("loadedCapabilityFile");
        loadedCapabilityFileField.setAccessible(true);
        loadedCapabilityFileField.set(null, loadedCapabilities);
    }
}
