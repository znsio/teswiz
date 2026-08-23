package com.znsio.teswiz.testng;

import com.znsio.teswiz.exceptions.InvalidTestDataException;
import com.znsio.teswiz.runner.Runner;

import java.util.ArrayList;
import java.util.List;

// Translates a Cucumber-style tag expression (e.g. "@cli and not @wip") into TestNG
// include/exclude groups. TestNG's plain group model can only express "belongs to any
// included group AND belongs to no excluded group" - it has no way to require a method
// belong to two groups simultaneously (a true AND of two positive tags), so that specific
// combination is rejected rather than silently mishandled.
public final class TestNgTagExpressionParser {
    private static final String AND = "and";
    private static final String OR = "or";
    private static final String NOT = "not";

    private TestNgTagExpressionParser() { }

    public static TestNgGroupSelection parse(String rawTagExpression) {
        if (null == rawTagExpression || rawTagExpression.isEmpty() || rawTagExpression.equalsIgnoreCase(Runner.NOT_SET)) {
            return new TestNgGroupSelection(List.of(), List.of());
        }

        List<String> includedGroups = new ArrayList<>();
        List<String> excludedGroups = new ArrayList<>();
        boolean previousClauseWasPositiveTag = false;
        boolean nextTagIsAndJoined = false;
        boolean nextTagIsNegated = false;

        for (String token : rawTagExpression.split("\\s+")) {
            if (token.equalsIgnoreCase(AND)) {
                nextTagIsAndJoined = true;
            } else if (token.equalsIgnoreCase(OR)) {
                nextTagIsAndJoined = false;
            } else if (token.equalsIgnoreCase(NOT)) {
                nextTagIsNegated = true;
            } else {
                String group = token.startsWith("@") ? token.substring(1) : token;
                if (nextTagIsNegated) {
                    excludedGroups.add(group);
                } else if (nextTagIsAndJoined && previousClauseWasPositiveTag) {
                    throw new InvalidTestDataException(
                            "Unsupported TestNG tag expression: '%s' - TestNG's group model cannot require a test belong to two groups at once (a true AND of two positive tags). Use a single composite @Test(groups=...) on the test method instead."
                                    .formatted(rawTagExpression));
                } else {
                    includedGroups.add(group);
                    previousClauseWasPositiveTag = true;
                }
                nextTagIsNegated = false;
            }
        }

        return new TestNgGroupSelection(includedGroups, excludedGroups);
    }
}
