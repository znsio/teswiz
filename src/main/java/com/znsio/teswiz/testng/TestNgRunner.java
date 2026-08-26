package com.znsio.teswiz.testng;

import com.znsio.teswiz.runner.FileLocations;
import com.znsio.teswiz.runner.Setup;
import com.znsio.teswiz.tools.FileUtils;
import com.znsio.teswiz.tools.OsUtils;
import org.testng.TestNG;
import org.testng.reporters.EmailableReporter2;
import org.testng.xml.XmlSuite;

import java.io.File;
import java.util.List;

public final class TestNgRunner {
    private static final String TESTNG_HTML_REPORT_DIRECTORY = FileLocations.REPORTS_DIRECTORY + "testngHtmlReport";
    private static final String TAG_COVERAGE_REPORT_FILE_NAME = "tagCoverageReport.html";

    private TestNgRunner() { }

    public static TestNgExecutionResult run(List<String> testClassNames, TestNgGroupSelection groupSelection, int threadCount) {
        TestNG testNg = new TestNG();
        testNg.setTestClasses(resolveTestClasses(testClassNames));
        testNg.setParallel(XmlSuite.ParallelMode.METHODS);
        testNg.setThreadCount(threadCount);
        testNg.setUseDefaultListeners(false);
        File reportDirectory = FileUtils.createDirectoryIn(OsUtils.getUserDirectory(), TESTNG_HTML_REPORT_DIRECTORY);
        testNg.setOutputDirectory(reportDirectory.getAbsolutePath());
        testNg.addListener(new EmailableReporter2());
        TeswizTestNgListener teswizTestNgListener = new TeswizTestNgListener();
        testNg.addListener(teswizTestNgListener);
        if (!groupSelection.includedGroups().isEmpty()) {
            testNg.setGroups(String.join(",", groupSelection.includedGroups()));
        }
        if (!groupSelection.excludedGroups().isEmpty()) {
            testNg.setExcludedGroups(String.join(",", groupSelection.excludedGroups()));
        }

        testNg.run();

        TestNgExecutionResult executionResult = teswizTestNgListener.getExecutionResult();
        TestNgTagCoverageReportWriter.write(executionResult.groupCoverage(), new File(reportDirectory, TAG_COVERAGE_REPORT_FILE_NAME));
        TestNgCucumberStyleReportWriter.write(teswizTestNgListener.getScenarioReportData(), reportDirectory,
                Setup.getFromConfigs(Setup.APP_NAME));
        return executionResult;
    }

    private static Class<?>[] resolveTestClasses(List<String> testClassNames) {
        return testClassNames.stream()
                .map(TestNgRunner::loadClass)
                .toArray(Class<?>[]::new);
    }

    private static Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("TestNG test class not found: " + className, e);
        }
    }
}
