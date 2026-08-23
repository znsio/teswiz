package com.znsio.teswiz.testng;

import com.znsio.teswiz.runner.Setup;
import com.znsio.teswiz.testng.fixtures.AlwaysFailingButExcludableTestNgTest;
import com.znsio.teswiz.testng.fixtures.AlwaysFailingTestNgTest;
import com.znsio.teswiz.testng.fixtures.AlwaysPassingTestNgTest;
import com.znsio.teswiz.testng.fixtures.ThreadRecordingTestNgTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TestNgRunnerTest {
    private static final String cliConfigFilePath = "./configs/cli_local_config.properties";

    @BeforeEach
    void loadTeswizConfig() {
        Setup.load(cliConfigFilePath);
        Setup.getExecutionArguments();
    }

    @Test
    void shouldReportSuccessWhenAllTestsPass() {
        boolean result = TestNgRunner.run(
                List.of(AlwaysPassingTestNgTest.class.getName()),
                includedOnly("fixture"),
                1);

        assertThat(result).isTrue();
    }

    @Test
    void shouldReportFailureWhenAnyTestFails() {
        boolean result = TestNgRunner.run(
                List.of(AlwaysFailingTestNgTest.class.getName()),
                includedOnly("fixture"),
                1);

        assertThat(result).isFalse();
    }

    @Test
    void shouldSkipExcludedGroupEvenWhenItAlsoMatchesAnIncludedGroup() {
        boolean result = TestNgRunner.run(
                List.of(AlwaysPassingTestNgTest.class.getName(), AlwaysFailingButExcludableTestNgTest.class.getName()),
                new TestNgGroupSelection(List.of("fixture"), List.of("excludeme")),
                1);

        assertThat(result).as("The excluded, always-failing test should not have run").isTrue();
    }

    @Test
    void shouldRunTestMethodsAcrossMultipleThreadsWhenThreadCountIsGreaterThanOne() {
        ThreadRecordingTestNgTest.observedThreadIds.clear();

        TestNgRunner.run(
                List.of(ThreadRecordingTestNgTest.class.getName()),
                includedOnly("fixture"),
                2);

        Set<Long> distinctThreadIds = Set.copyOf(ThreadRecordingTestNgTest.observedThreadIds);
        assertThat(distinctThreadIds).as("Expected test methods to run on more than one thread")
                .hasSizeGreaterThan(1);
    }

    private static TestNgGroupSelection includedOnly(String... groups) {
        return new TestNgGroupSelection(List.of(groups), List.of());
    }
}
