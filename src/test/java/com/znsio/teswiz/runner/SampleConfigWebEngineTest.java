package com.znsio.teswiz.runner;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.junit.jupiter.api.Test;

class SampleConfigWebEngineTest {
    private static final String CONFIGS_DIR = "configs";
    private static final String WEB_ENGINE = "WEB_ENGINE";
    private static final String SELENIUM = "selenium";

    @Test
    void shouldExplicitlySetWebEngineToSeleniumInAllCheckedInSampleConfigs() throws IOException {
        try (var paths = Files.walk(Path.of(CONFIGS_DIR))) {
            assertThat(paths.filter(path -> path.toString().endsWith(".properties")))
                    .allSatisfy(this::assertWebEngineIsExplicitlySetToSelenium);
        }
    }

    private void assertWebEngineIsExplicitlySetToSelenium(Path configFile) {
        Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(configFile)) {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Unable to read config file: " + configFile, e);
        }

        assertThat(properties)
                .as("Config file %s should declare WEB_ENGINE explicitly", configFile)
                .containsKey(WEB_ENGINE);
        assertThat(properties.getProperty(WEB_ENGINE))
                .as("Config file %s should default WEB_ENGINE to selenium", configFile)
                .isEqualTo(SELENIUM);
    }
}
