package com.znsio.teswiz.runner;

import com.applitools.eyes.BatchInfo;
import com.znsio.teswiz.entities.APPLITOOLS;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SetupTest {
    private static final Logger LOGGER = LogManager.getLogger(BrowserStackDeviceFilterTest.class.getName());
    private static final String configFilePath = "./configs/ajio/ajio_local_android_config.properties";

    @BeforeAll
    public static void setupBefore() {
        LOGGER.info("Running SetupTest");
    }

    @BeforeEach
    public void beforeMethod() {
        System.clearProperty(APPLITOOLS.PROXY_KEY);
        System.clearProperty(APPLITOOLS.PROXY_URL);
        System.clearProperty(Setup.APPLITOOLS_BATCH_NAME_SUFFIX);
        System.clearProperty("rp.attributes");
    }

    @Test
    void checkNoApplitoolsProxyKey() {
        Setup.load(configFilePath);
        Setup.loadAndUpdateConfigParameters(configFilePath);
        Setup.initialiseApplitoolsConfiguration();
        Map applitoolsConfiguration = Runner.getApplitoolsConfiguration();
        System.out.println(applitoolsConfiguration.get(APPLITOOLS.PROXY_KEY));
        System.out.println(applitoolsConfiguration.get(APPLITOOLS.PROXY_URL));
        assertThat(applitoolsConfiguration.get(APPLITOOLS.PROXY_KEY)).as("Applitools Proxy key is invalid").hasToString(
                APPLITOOLS.PROXY_KEY);
        assertThat(applitoolsConfiguration.get(APPLITOOLS.PROXY_URL)).as("Applitools Proxy url is invalid").isNull();
    }

    @Test
    void checkWithApplitoolsProxyKeyInConfig() {
        Setup.load(configFilePath);
        Setup.loadAndUpdateConfigParameters(configFilePath);
        Setup.addToConfigs(Setup.APPLITOOLS_CONFIGURATION, "./configs/applitools_config_withValidProxyKey.json");
        Setup.initialiseApplitoolsConfiguration();
        Map applitoolsConfiguration = Runner.getApplitoolsConfiguration();
        System.out.println(applitoolsConfiguration.get(APPLITOOLS.PROXY_KEY));
        System.out.println(applitoolsConfiguration.get(APPLITOOLS.PROXY_URL));
        assertThat(applitoolsConfiguration.get(APPLITOOLS.PROXY_KEY)).as("Applitools Proxy key is invalid").hasToString(
                "user.dir");
        assertThat(applitoolsConfiguration.get(APPLITOOLS.PROXY_URL)).as("Applitools Proxy url is invalid").isNotNull();
    }

    @Test
    void checkWithInvalidApplitoolsProxyKeyInConfig() {
        Setup.load(configFilePath);
        Setup.loadAndUpdateConfigParameters(configFilePath);
        Setup.addToConfigs(Setup.APPLITOOLS_CONFIGURATION, "./configs/applitools_config_withInvalidProxyKey.json");
        Setup.initialiseApplitoolsConfiguration();
        Map applitoolsConfiguration = Runner.getApplitoolsConfiguration();
        System.out.println(applitoolsConfiguration.get(APPLITOOLS.PROXY_KEY));
        System.out.println(applitoolsConfiguration.get(APPLITOOLS.PROXY_URL));
        assertThat(applitoolsConfiguration.get(APPLITOOLS.PROXY_KEY)).as("Applitools Proxy key is invalid").hasToString(
                "user.di");
        assertThat(applitoolsConfiguration.get(APPLITOOLS.PROXY_URL)).as("Applitools Proxy url is invalid").isNull();
    }

    @Test
    void checkWithEmptyApplitoolsProxyKeyInConfig() {
        Setup.load(configFilePath);
        Setup.loadAndUpdateConfigParameters(configFilePath);
        Setup.addToConfigs(Setup.APPLITOOLS_CONFIGURATION, "./configs/applitools_config_withEmptyProxyKey.json");
        Setup.initialiseApplitoolsConfiguration();
        Map applitoolsConfiguration = Runner.getApplitoolsConfiguration();
        System.out.println(applitoolsConfiguration.get(APPLITOOLS.PROXY_KEY));
        System.out.println(applitoolsConfiguration.get(APPLITOOLS.PROXY_URL));
        assertThat(applitoolsConfiguration.get(APPLITOOLS.PROXY_KEY)).as("Applitools Proxy key is invalid").hasToString(
                APPLITOOLS.PROXY_KEY);
        assertThat(applitoolsConfiguration.get(APPLITOOLS.PROXY_URL)).as("Applitools Proxy url is invalid").isNull();
    }

    @Test
    void checkInvalidApplitoolsProxyKey() {
        String proxyKey = "user.di";
        System.setProperty(APPLITOOLS.PROXY_KEY, proxyKey);
        Setup.load(configFilePath);
        Setup.loadAndUpdateConfigParameters(configFilePath);
        Setup.initialiseApplitoolsConfiguration();
        Map applitoolsConfiguration = Runner.getApplitoolsConfiguration();
        System.out.println(applitoolsConfiguration.get(APPLITOOLS.PROXY_KEY));
        System.out.println(applitoolsConfiguration.get(APPLITOOLS.PROXY_URL));
        assertThat(applitoolsConfiguration.get(APPLITOOLS.PROXY_KEY)).as("Applitools Proxy key is invalid").hasToString(
                proxyKey);
        assertThat(applitoolsConfiguration.get(APPLITOOLS.PROXY_URL)).as("Applitools Proxy url is invalid").isNull();
    }

    @Test
    void checkValidApplitoolsProxyKey() {
        String proxyKey = "user.dir";
        System.setProperty(APPLITOOLS.PROXY_KEY, proxyKey);
        Setup.load(configFilePath);
        Setup.loadAndUpdateConfigParameters(configFilePath);
        Setup.initialiseApplitoolsConfiguration();
        Map applitoolsConfiguration = Runner.getApplitoolsConfiguration();
        System.out.println(applitoolsConfiguration.get(APPLITOOLS.PROXY_KEY));
        System.out.println(applitoolsConfiguration.get(APPLITOOLS.PROXY_URL));
        assertThat(applitoolsConfiguration.get(APPLITOOLS.PROXY_KEY)).as("Applitools Proxy key is invalid").hasToString(
                proxyKey);
        assertThat(applitoolsConfiguration.get(APPLITOOLS.PROXY_URL)).as("Applitools Proxy url is invalid").isNotNull();
    }

    @Test
    void checkInvalidApplitoolsProxyKeyOverridingConfig() {
        String proxyKey = "user.nam";
        System.setProperty(APPLITOOLS.PROXY_KEY, proxyKey);
        Setup.load(configFilePath);
        Setup.loadAndUpdateConfigParameters(configFilePath);
        Setup.addToConfigs(Setup.APPLITOOLS_CONFIGURATION, "./configs/applitools_config_withValidProxyKey.json");
        Setup.initialiseApplitoolsConfiguration();
        Map applitoolsConfiguration = Runner.getApplitoolsConfiguration();
        System.out.println(applitoolsConfiguration.get(APPLITOOLS.PROXY_KEY));
        System.out.println(applitoolsConfiguration.get(APPLITOOLS.PROXY_URL));
        assertThat(applitoolsConfiguration.get(APPLITOOLS.PROXY_KEY)).as("Applitools Proxy key is invalid").hasToString(
                proxyKey);
        assertThat(applitoolsConfiguration.get(APPLITOOLS.PROXY_URL)).as("Applitools Proxy url is invalid").isNull();
    }

    @Test
    void checkValidApplitoolsProxyKeyOverridingConfig() {
        String proxyKey = "user.name";
        System.setProperty(APPLITOOLS.PROXY_KEY, proxyKey);
        Setup.load(configFilePath);
        Setup.loadAndUpdateConfigParameters(configFilePath);
        Setup.addToConfigs(Setup.APPLITOOLS_CONFIGURATION, "./configs/applitools_config_withValidProxyKey.json");
        Setup.initialiseApplitoolsConfiguration();
        Map applitoolsConfiguration = Runner.getApplitoolsConfiguration();
        System.out.println(applitoolsConfiguration.get(APPLITOOLS.PROXY_KEY));
        System.out.println(applitoolsConfiguration.get(APPLITOOLS.PROXY_URL));
        assertThat(applitoolsConfiguration.get(APPLITOOLS.PROXY_KEY)).as("Applitools Proxy key is invalid").hasToString(
                proxyKey);
        assertThat(applitoolsConfiguration.get(APPLITOOLS.PROXY_URL)).as("Applitools Proxy url is invalid").isNotNull();
    }

    @Test
    void checkApplitoolsBatchNameSuffix() {
        String batchNameSuffix = " - #661";
        System.setProperty(Setup.APPLITOOLS_BATCH_NAME_SUFFIX, batchNameSuffix);
        Setup.load(configFilePath);
        Setup.loadAndUpdateConfigParameters(configFilePath);
        Setup.initialiseApplitoolsConfiguration();
        Map applitoolsConfiguration = Runner.getApplitoolsConfiguration();
        BatchInfo batchInfo = (BatchInfo) applitoolsConfiguration.get(APPLITOOLS.BATCH_INFO);
        assertThat(batchInfo.getName()).as("Applitools Batch name suffix is not set as expected").endsWith(batchNameSuffix);
    }

    @Test
    void shouldMakeApplitoolsBatchNameAndPropertiesEngineAwareForWebRuns() {
        String webConfigFilePath = "./configs/theapp/theapp_local_web_config.properties";
        System.setProperty(Setup.WEB_ENGINE, "playwright-ts");
        Setup.load(webConfigFilePath);
        Setup.loadAndUpdateConfigParameters(webConfigFilePath);
        Setup.initialiseApplitoolsConfiguration();

        Map applitoolsConfiguration = Runner.getApplitoolsConfiguration();
        BatchInfo batchInfo = (BatchInfo) applitoolsConfiguration.get(APPLITOOLS.BATCH_INFO);

        assertThat(batchInfo.getName()).contains("-web-playwright-ts");
        assertThat(batchInfo.getProperties())
                .anySatisfy(property -> assertThat(property)
                        .containsEntry("name", Setup.WEB_ENGINE)
                        .containsEntry("value", "playwright-ts"))
                .anySatisfy(property -> assertThat(property)
                        .containsEntry("name", Setup.PLATFORM)
                        .containsEntry("value", "web"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"selenium", "playwright-java", "playwright-ts"})
    void shouldIncludeProviderAndWebEngineInReportPortalAttributesForWebRuns(String webEngine) {
        String webConfigFilePath = "./configs/theapp/theapp_local_web_config.properties";
        System.setProperty(Setup.WEB_ENGINE, webEngine);
        Setup.load(webConfigFilePath);
        Setup.loadAndUpdateConfigParameters(webConfigFilePath);
        Setup.getExecutionArguments();

        assertThat(System.getProperty("rp.attributes"))
                .contains("Platform:web;")
                .contains("WebEngine:" + webEngine + ";")
                .contains("Provider:local;");
    }
}
