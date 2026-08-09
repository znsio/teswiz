package com.znsio.teswiz.visual;

import java.util.List;

import com.applitools.eyes.TestResults;

public record PlaywrightVisualResults(List<Entry> entries) {
    public static PlaywrightVisualResults single(TestResults testResults) {
        return new PlaywrightVisualResults(List.of(new Entry(null, testResults)));
    }

    public record Entry(String browserInfo, TestResults testResults) {
    }
}
