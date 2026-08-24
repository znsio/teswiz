package com.znsio.teswiz.testng;

import com.znsio.teswiz.exceptions.InvalidTestDataException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

// A lightweight, TestNG-native coverage-by-tag report - not a replacement for the
// Cucumber/masterthought coverage report, which teswiz has no equivalent library for
// when running in TestNG mode. Deliberately a single self-contained HTML file with no
// external CSS/JS, matching this framework's preference for lightweight reporting.
public final class TestNgTagCoverageReportWriter {
    private TestNgTagCoverageReportWriter() { }

    public static void write(List<TestNgGroupCoverage> groupCoverage, File outputFile) {
        String html = buildHtml(groupCoverage);
        try {
            Files.writeString(outputFile.toPath(), html, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new InvalidTestDataException("Unable to write TestNG tag coverage report to: " + outputFile, e);
        }
    }

    private static String buildHtml(List<TestNgGroupCoverage> groupCoverage) {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>TestNG Tag Coverage Report</title></head><body>");
        html.append("<h1>TestNG Tag Coverage Report</h1>");
        html.append("<table border=\"1\" cellpadding=\"6\" cellspacing=\"0\">");
        html.append("<tr><th>Group</th><th>Total</th><th>Passed</th><th>Failed</th></tr>");
        for (TestNgGroupCoverage group : groupCoverage) {
            appendGroupRow(html, group);
        }
        html.append("</table>");
        html.append("</body></html>");
        return html.toString();
    }

    private static void appendGroupRow(StringBuilder html, TestNgGroupCoverage group) {
        html.append("<tr>");
        html.append("<td>").append(group.groupName()).append("</td>");
        html.append("<td>").append(group.totalCount()).append("</td>");
        html.append("<td>").append(formatTestNames(group.passedTestNames())).append("</td>");
        html.append("<td>").append(formatTestNames(group.failedTestNames())).append("</td>");
        html.append("</tr>");
    }

    private static String formatTestNames(List<String> testNames) {
        return testNames.isEmpty() ? "" : String.join(", ", testNames);
    }
}
