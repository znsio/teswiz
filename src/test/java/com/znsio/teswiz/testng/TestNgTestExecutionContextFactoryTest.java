package com.znsio.teswiz.testng;

import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import com.znsio.teswiz.runner.Setup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

class TestNgTestExecutionContextFactoryTest {
    private static final String cliConfigFilePath = "./configs/cli_local_config.properties";

    @BeforeEach
    void loadTeswizConfig() {
        Setup.load(cliConfigFilePath);
        Setup.getExecutionArguments();
    }

    @Test
    void shouldPopulateScreenshotAndLogDirectoriesForTheTest() {
        TestExecutionContext context = TestNgTestExecutionContextFactory.create("sampleTestMethod", 1);

        String screenshotDirectory = context.getTestStateAsString(TEST_CONTEXT.SCREENSHOT_DIRECTORY);
        String scenarioLogDirectory = context.getTestStateAsString(TEST_CONTEXT.SCENARIO_LOG_DIRECTORY);
        String deviceLogsDirectory = context.getTestStateAsString(TEST_CONTEXT.DEVICE_LOGS_DIRECTORY);

        assertThat(screenshotDirectory).isNotBlank();
        assertThat(new File(screenshotDirectory)).exists().isDirectory();
        assertThat(new File(scenarioLogDirectory)).exists().isDirectory();
        assertThat(new File(deviceLogsDirectory)).exists().isDirectory();
        assertThat(context.getTestState(TEST_CONTEXT.NORMALISED_SCENARIO_NAME)).isEqualTo("sampleTestMethod");
        assertThat(context.getTestState(TEST_CONTEXT.SCENARIO_RUN_COUNT)).isEqualTo(1);
    }
}
