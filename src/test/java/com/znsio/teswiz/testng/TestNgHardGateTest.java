package com.znsio.teswiz.testng;

import com.znsio.teswiz.runner.Setup;
import com.znsio.teswiz.testng.fixtures.AlwaysFailingTestNgTest;
import com.znsio.teswiz.testng.fixtures.AlwaysPassingTestNgTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// Runner.getStatus's hard-gate truth table is already exhaustively covered by
// RunnerTest (same package, package-private access) - this test only covers the
// new thing: that TestNgRunner correctly counts passed/failed tests, which is what
// Runner.runTestNgMode feeds into that existing, unchanged truth table.
class TestNgHardGateTest {
    private static final String cliConfigFilePath = "./configs/cli_local_config.properties";

    @BeforeEach
    void loadTeswizConfig() {
        Setup.load(cliConfigFilePath);
        Setup.getExecutionArguments();
    }

    @Test
    void shouldCountPassedAndFailedTestsSeparately() {
        TestNgExecutionResult result = TestNgRunner.run(
                List.of(AlwaysPassingTestNgTest.class.getName(), AlwaysFailingTestNgTest.class.getName()),
                new TestNgGroupSelection(List.of("fixture"), List.of()),
                1);

        assertThat(result.passedCount()).isEqualTo(1);
        assertThat(result.failedCount()).isEqualTo(1);
        assertThat(result.allTestsPassed()).isFalse();
    }

    @Test
    void allTestsPassedShouldBeTrueWhenNothingFailed() {
        TestNgExecutionResult result = TestNgRunner.run(
                List.of(AlwaysPassingTestNgTest.class.getName()),
                new TestNgGroupSelection(List.of("fixture"), List.of()),
                1);

        assertThat(result.allTestsPassed()).isTrue();
    }
}
