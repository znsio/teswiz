package com.znsio.teswiz.testng;

import com.znsio.teswiz.runner.Setup;
import com.znsio.teswiz.testng.fixtures.AlwaysFailingButExcludableTestNgTest;
import com.znsio.teswiz.testng.fixtures.AlwaysFailingTestNgTest;
import com.znsio.teswiz.testng.fixtures.AlwaysPassingTestNgTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class TestNgGroupCoverageTest {
    private static final String cliConfigFilePath = "./configs/cli_local_config.properties";

    @BeforeEach
    void loadTeswizConfig() {
        Setup.load(cliConfigFilePath);
        Setup.getExecutionArguments();
    }

    @Test
    void shouldGroupCoverageByTestNgGroupAcrossMultipleClasses() {
        TestNgExecutionResult result = TestNgRunner.run(
                List.of(AlwaysPassingTestNgTest.class.getName(),
                        AlwaysFailingTestNgTest.class.getName(),
                        AlwaysFailingButExcludableTestNgTest.class.getName()),
                new TestNgGroupSelection(List.of(), List.of()),
                1);

        List<TestNgGroupCoverage> coverage = result.groupCoverage();

        TestNgGroupCoverage fixtureGroup = findGroup(coverage, "fixture");
        assertThat(fixtureGroup.totalCount()).isEqualTo(3);
        assertThat(fixtureGroup.passedTestNames()).containsExactly("alwaysPasses");
        assertThat(fixtureGroup.failedTestNames()).containsExactlyInAnyOrder("alwaysFails", "alwaysFailsButShouldBeExcludable");

        TestNgGroupCoverage excludeMeGroup = findGroup(coverage, "excludeme");
        assertThat(excludeMeGroup.totalCount()).isEqualTo(1);
        assertThat(excludeMeGroup.failedTestNames()).containsExactly("alwaysFailsButShouldBeExcludable");
    }

    private TestNgGroupCoverage findGroup(List<TestNgGroupCoverage> coverage, String groupName) {
        Optional<TestNgGroupCoverage> found = coverage.stream()
                .filter(group -> group.groupName().equals(groupName))
                .findFirst();
        assertThat(found).as("Expected group '%s' to be present in coverage", groupName).isPresent();
        return found.get();
    }
}
