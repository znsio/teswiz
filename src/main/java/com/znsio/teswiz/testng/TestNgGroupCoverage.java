package com.znsio.teswiz.testng;

import java.util.List;

public record TestNgGroupCoverage(String groupName, List<String> passedTestNames, List<String> failedTestNames) {
    public int totalCount() {
        return passedTestNames.size() + failedTestNames.size();
    }
}
