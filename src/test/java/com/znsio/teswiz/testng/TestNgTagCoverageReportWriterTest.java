package com.znsio.teswiz.testng;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TestNgTagCoverageReportWriterTest {

    @Test
    void shouldWriteAnHtmlTableWithOneRowPerGroup(@TempDir Path tempDir) throws IOException {
        List<TestNgGroupCoverage> coverage = List.of(
                new TestNgGroupCoverage("calculator", List.of("addAndSubtract"), List.of()),
                new TestNgGroupCoverage("cryptoAPI", List.of("validatePriceChange1"), List.of("validatePriceChange2")));
        File outputFile = tempDir.resolve("tagCoverageReport.html").toFile();

        TestNgTagCoverageReportWriter.write(coverage, outputFile);

        String html = Files.readString(outputFile.toPath());
        assertThat(html).contains("calculator").contains("addAndSubtract");
        assertThat(html).contains("cryptoAPI").contains("validatePriceChange1").contains("validatePriceChange2");
        assertThat(html).contains("1</td>").contains("2</td>"); // total counts for each group
    }

    @Test
    void shouldWriteAValidEmptyReportWhenNoCoverageRecorded(@TempDir Path tempDir) throws IOException {
        File outputFile = tempDir.resolve("tagCoverageReport.html").toFile();

        TestNgTagCoverageReportWriter.write(List.of(), outputFile);

        assertThat(outputFile).exists();
        assertThat(Files.readString(outputFile.toPath())).contains("<html");
    }
}
