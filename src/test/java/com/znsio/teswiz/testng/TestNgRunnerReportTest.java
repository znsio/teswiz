package com.znsio.teswiz.testng;

import com.znsio.teswiz.runner.FileLocations;
import com.znsio.teswiz.runner.Setup;
import com.znsio.teswiz.testng.fixtures.AlwaysPassingTestNgTest;
import com.znsio.teswiz.tools.OsUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
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

    @Test
    void shouldProduceARichCucumberStyleReportAlongsideTheOtherReports() {
        TestNgRunner.run(
                List.of(AlwaysPassingTestNgTest.class.getName()),
                new TestNgGroupSelection(List.of("fixture"), List.of()),
                1);

        File overviewFeatures = new File(OsUtils.getUserDirectory(),
                FileLocations.REPORTS_DIRECTORY + "testngHtmlReport" + File.separator
                        + "cucumber-html-reports" + File.separator + "overview-features.html");
        assertThat(overviewFeatures).exists();
    }

    @Test
    void shouldLabelTheEmailableReportWithTheConfiguredLaunchNameInsteadOfTheTestNgDefault() throws Exception {
        TestNgRunner.run(
                List.of(AlwaysPassingTestNgTest.class.getName()),
                new TestNgGroupSelection(List.of("fixture"), List.of()),
                1);

        File emailableReport = new File(OsUtils.getUserDirectory(),
                FileLocations.REPORTS_DIRECTORY + "testngHtmlReport" + File.separator + "emailable-report.html");
        String content = Files.readString(emailableReport.toPath());
        String launchNameAsRenderedInHtml = Setup.getFromConfigs(Setup.LAUNCH_NAME).replace("'", "&apos;");
        assertThat(content)
                .as("the report should be labelled with the run's LAUNCH_NAME, not TestNG's generic default")
                .doesNotContain("Command line suite")
                .doesNotContain("Command line test")
                .contains(launchNameAsRenderedInHtml);
    }
}
