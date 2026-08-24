package com.znsio.teswiz.testng;

import com.znsio.teswiz.runner.FileLocations;
import com.znsio.teswiz.runner.Setup;
import com.znsio.teswiz.testng.fixtures.AlwaysPassingTestNgTest;
import com.znsio.teswiz.tools.OsUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TestNgRunnerReportTest {
    private static final String cliConfigFilePath = "./configs/cli_local_config.properties";

    @BeforeEach
    void loadTeswizConfig() {
        Setup.load(cliConfigFilePath);
        Setup.getExecutionArguments();
    }

    @Test
    void shouldProduceAnHtmlReportUnderTheReportsDirectory() {
        TestNgRunner.run(
                List.of(AlwaysPassingTestNgTest.class.getName()),
                new TestNgGroupSelection(List.of("fixture"), List.of()),
                1);

        File reportsDirectory = new File(OsUtils.getUserDirectory(), FileLocations.REPORTS_DIRECTORY + "testngHtmlReport");
        assertThat(reportsDirectory).exists().isDirectory();
        assertThat(reportsDirectory.listFiles((dir, name) -> name.endsWith(".html"))).isNotEmpty();
    }

    @Test
    void shouldProduceATagCoverageReportAlongsideTheEmailableReport() {
        TestNgRunner.run(
                List.of(AlwaysPassingTestNgTest.class.getName()),
                new TestNgGroupSelection(List.of("fixture"), List.of()),
                1);

        File tagCoverageReport = new File(OsUtils.getUserDirectory(),
                FileLocations.REPORTS_DIRECTORY + "testngHtmlReport" + File.separator + "tagCoverageReport.html");
        assertThat(tagCoverageReport).exists();
    }
}
