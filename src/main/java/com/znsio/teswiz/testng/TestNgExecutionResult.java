package com.znsio.teswiz.testng;

import java.util.List;

public record TestNgExecutionResult(int passedCount, int failedCount, List<TestNgGroupCoverage> groupCoverage) {
    public int totalCount() {
        return passedCount + failedCount;
    }

    public boolean allTestsPassed() {
        return failedCount == 0;
    }
}
