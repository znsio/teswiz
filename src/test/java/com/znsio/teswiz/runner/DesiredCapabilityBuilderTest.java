package com.znsio.teswiz.runner;

import com.znsio.teswiz.context.SessionContext;
import com.znsio.teswiz.tools.JsonFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DesiredCapabilityBuilderTest {
    private static final String LAMBDATEST_ANDROID_CONFIG =
            "./configs/theapp/theapp_lambdatest_android_config.properties";
    private static boolean createdDefaultCapsFile;

    @AfterEach
    void cleanup() {
        SessionContext.remove(Thread.currentThread().getId());
        resetCustomCapabilitiesSingleton();
        if (createdDefaultCapsFile) {
            try {
                Files.deleteIfExists(Path.of("caps", "capabilities.json"));
            } catch (IOException ignored) {
            }
            createdDefaultCapsFile = false;
        }
    }

    @Test
    void shouldKeepLtOptionsUnprefixedAndPickCloudDeviceForLambdaTest() throws Exception {
        String capsFile = createCapabilitiesFile("""
                {
                  "android": {
                    "automationName": "UiAutomator2",
                    "platformName": "Android",
                    "newCommandTimeout": 120,
                    "lt:options": {
                      "video": true
                    },
                    "app": "https://example.com/theapp.apk"
                  },
                  "serverConfig": {
                    "server": {
                      "plugin": {
                        "device-farm": {
                          "cloud": {
                            "cloudName": "lambdatest",
                            "devices": [
                              { "deviceName": "Pixel 8" }
                            ]
                          }
                        }
                      }
                    }
                  }
                }
                """);
        setupConfigWithCaps(LAMBDATEST_ANDROID_CONFIG, capsFile);

        DesiredCapabilities caps = new DesiredCapabilityBuilder().buildDesiredCapability(capsFile, 0);

        assertThat(caps.getCapability("platformName").toString()).isEqualToIgnoringCase("Android");
        assertThat(caps.getCapability("appium:platformName")).isNull();
        assertThat(caps.getCapability("lt:options")).isNotNull();
        assertThat(caps.getCapability("appium:lt:options")).isNull();
        assertThat(caps.getCapability("appium:newCommandTimeout")).isEqualTo(120);
        assertThat(caps.getCapability("appium:deviceName")).isEqualTo("Pixel 8");
    }

    @Test
    void shouldNotSetCloudDeviceWhenRequestedIndexIsOutOfRange() throws Exception {
        String capsFile = createCapabilitiesFile("""
                {
                  "android": {
                    "automationName": "UiAutomator2",
                    "platformName": "Android",
                    "lt:options": {
                      "video": true
                    }
                  },
                  "serverConfig": {
                    "server": {
                      "plugin": {
                        "device-farm": {
                          "cloud": {
                            "cloudName": "lambdatest",
                            "devices": [
                              { "deviceName": "Pixel 8" }
                            ]
                          }
                        }
                      }
                    }
                  }
                }
                """);
        setupConfigWithCaps(LAMBDATEST_ANDROID_CONFIG, capsFile);

        DesiredCapabilities caps = new DesiredCapabilityBuilder().buildDesiredCapability(capsFile, 3);

        assertThat(caps.getCapability("appium:deviceName")).isNull();
        assertThat(caps.getCapability("lt:options")).isNotNull();
    }

    @Test
    void shouldNotPickCloudDeviceForNonBrowserstackAndNonLambdaCloud() throws Exception {
        String capsFile = createCapabilitiesFile("""
                {
                  "android": {
                    "automationName": "UiAutomator2",
                    "platformName": "Android"
                  },
                  "serverConfig": {
                    "server": {
                      "plugin": {
                        "device-farm": {
                          "cloud": {
                            "cloudName": "headspin",
                            "devices": [
                              { "deviceName": "ThisShouldNotBeUsed" }
                            ]
                          }
                        }
                      }
                    }
                  }
                }
                """);
        setupConfigWithCaps(LAMBDATEST_ANDROID_CONFIG, capsFile);

        DesiredCapabilities caps = new DesiredCapabilityBuilder().buildDesiredCapability(capsFile, 0);

        assertThat(caps.getCapability("appium:deviceName")).isNull();
    }

    private static void setupConfigWithCaps(String configPath, String capsPath) throws Exception {
        Setup.load(configPath);
        Setup.loadAndUpdateConfigParameters(configPath);
        Setup.addToConfigs(Setup.CAPS, capsPath);
        setLoadedCapabilityFile(JsonFile.loadJsonFile(capsPath));
        ensureDefaultCapsFileExists();
        resetCustomCapabilitiesSingleton();
    }

    private static String createCapabilitiesFile(String json) throws IOException {
        Path file = Files.createTempFile("desired-capabilities-", ".json");
        Files.writeString(file, json);
        return file.toString();
    }

    private static void setLoadedCapabilityFile(Map<String, Map> loadedCapabilities)
            throws NoSuchFieldException, IllegalAccessException {
        Field loadedCapabilityFileField = Setup.class.getDeclaredField("loadedCapabilityFile");
        loadedCapabilityFileField.setAccessible(true);
        loadedCapabilityFileField.set(null, loadedCapabilities);
    }

    private static void ensureDefaultCapsFileExists() throws IOException {
        Path defaultCaps = Path.of("caps", "capabilities.json");
        if (!Files.exists(defaultCaps)) {
            Files.createDirectories(defaultCaps.getParent());
            Files.writeString(defaultCaps, """
                    {
                      "android": {
                        "automationName": "UiAutomator2"
                      },
                      "serverConfig": {
                        "server": {
                          "plugin": {
                            "device-farm": {
                              "cloud": {
                                "cloudName": "lambdatest",
                                "devices": []
                              }
                            }
                          }
                        }
                      }
                    }
                    """);
            createdDefaultCapsFile = true;
        }
    }

    private static void resetCustomCapabilitiesSingleton() {
        try {
            Field instanceField = CustomCapabilities.class.getDeclaredField("instance");
            instanceField.setAccessible(true);
            instanceField.set(null, null);
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }
    }
}
