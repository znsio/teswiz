package com.znsio.teswiz.testng;

import com.znsio.teswiz.steps.Hooks;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class TeswizTestNgListener implements ITestListener {
    private final AtomicInteger runningTestNumber = new AtomicInteger(0);
    private final AtomicInteger passedCount = new AtomicInteger(0);
    private final AtomicInteger failedCount = new AtomicInteger(0);
    private final Map<String, List<TestOutcome>> outcomesByGroup = new ConcurrentHashMap<>();

    private record TestOutcome(String testName, boolean passed) { }

    @Override
    public void onTestStart(ITestResult result) {
        TestNgTestExecutionContextFactory.create(result.getName(), runningTestNumber.incrementAndGet());
        new Hooks().beforeScenario(result.getName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        passedCount.incrementAndGet();
        recordOutcomeByGroup(result, true);
        new Hooks().afterScenario(result.getName(), false);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        failedCount.incrementAndGet();
        recordOutcomeByGroup(result, false);
        new Hooks().afterScenario(result.getName(), true);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        new Hooks().afterScenario(result.getName(), true);
    }

    private void recordOutcomeByGroup(ITestResult result, boolean passed) {
        TestOutcome outcome = new TestOutcome(result.getName(), passed);
        for (String group : result.getMethod().getGroups()) {
            outcomesByGroup.computeIfAbsent(group, g -> new CopyOnWriteArrayList<>()).add(outcome);
        }
    }

    TestNgExecutionResult getExecutionResult() {
        return new TestNgExecutionResult(passedCount.get(), failedCount.get(), buildGroupCoverage());
    }

    private List<TestNgGroupCoverage> buildGroupCoverage() {
        return outcomesByGroup.entrySet().stream()
                .map(entry -> toGroupCoverage(entry.getKey(), entry.getValue()))
                .toList();
    }

    private TestNgGroupCoverage toGroupCoverage(String groupName, List<TestOutcome> outcomes) {
        List<String> passedTestNames = outcomes.stream().filter(TestOutcome::passed).map(TestOutcome::testName).toList();
        List<String> failedTestNames = outcomes.stream().filter(outcome -> !outcome.passed()).map(TestOutcome::testName).toList();
        return new TestNgGroupCoverage(groupName, passedTestNames, failedTestNames);
    }
}
