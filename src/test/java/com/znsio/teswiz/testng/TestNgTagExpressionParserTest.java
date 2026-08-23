package com.znsio.teswiz.testng;

import com.znsio.teswiz.exceptions.InvalidTestDataException;
import com.znsio.teswiz.runner.Runner;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TestNgTagExpressionParserTest {

    @Test
    void shouldReturnNoGroupsWhenTagIsNotSet() {
        TestNgGroupSelection selection = TestNgTagExpressionParser.parse(Runner.NOT_SET);

        assertThat(selection.includedGroups()).isEmpty();
        assertThat(selection.excludedGroups()).isEmpty();
    }

    @Test
    void shouldReturnNoGroupsWhenTagIsNull() {
        TestNgGroupSelection selection = TestNgTagExpressionParser.parse(null);

        assertThat(selection.includedGroups()).isEmpty();
        assertThat(selection.excludedGroups()).isEmpty();
    }

    @Test
    void shouldIncludeSingleGroupForASingleTag() {
        TestNgGroupSelection selection = TestNgTagExpressionParser.parse("@calculator");

        assertThat(selection.includedGroups()).containsExactly("calculator");
        assertThat(selection.excludedGroups()).isEmpty();
    }

    @Test
    void shouldIncludeAllGroupsForSpaceSeparatedTags() {
        TestNgGroupSelection selection = TestNgTagExpressionParser.parse("@cli @calculator");

        assertThat(selection.includedGroups()).containsExactly("cli", "calculator");
        assertThat(selection.excludedGroups()).isEmpty();
    }

    @Test
    void shouldIncludeAllGroupsWhenJoinedWithExplicitOr() {
        TestNgGroupSelection selection = TestNgTagExpressionParser.parse("@schedule or @signup");

        assertThat(selection.includedGroups()).containsExactly("schedule", "signup");
        assertThat(selection.excludedGroups()).isEmpty();
    }

    @Test
    void shouldExcludeGroupForAndNotClause() {
        TestNgGroupSelection selection = TestNgTagExpressionParser.parse("@cli and not @wip");

        assertThat(selection.includedGroups()).containsExactly("cli");
        assertThat(selection.excludedGroups()).containsExactly("wip");
    }

    @Test
    void shouldExcludeMultipleGroupsForMultipleAndNotClauses() {
        TestNgGroupSelection selection = TestNgTagExpressionParser.parse("@schedule and not @wip and not @flaky");

        assertThat(selection.includedGroups()).containsExactly("schedule");
        assertThat(selection.excludedGroups()).containsExactly("wip", "flaky");
    }

    @Test
    void shouldThrowForPureAndOfTwoPositiveTagsSinceTestNgGroupsCannotExpressIt() {
        assertThatThrownBy(() -> TestNgTagExpressionParser.parse("@schedule and @signup"))
                .isInstanceOf(InvalidTestDataException.class)
                .hasMessageContaining("@schedule and @signup");
    }
}
