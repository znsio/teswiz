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
    private final List<TestNgScenarioReportData> scenarioReportData = new CopyOnWriteArrayList<>();

    private record TestOutcome(String testName, boolean passed) { }

    @Override
    public void onTestStart(ITestResult result) {
        TestNgTestExecutionContextFactory.create(result.getName(), runningTestNumber.incrementAndGet());
        TestNgStepRecorder.startCapturingStepsForCurrentThread();
        new Hooks().beforeScenario(result.getName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        passedCount.incrementAndGet();
        recordOutcomeByGroup(result, true);
        recordScenarioReportData(result, TestNgCapturedStep.PASSED);
        new Hooks().afterScenario(result.getName(), false);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        failedCount.incrementAndGet();
        recordOutcomeByGroup(result, false);
        recordScenarioReportData(result, TestNgCapturedStep.FAILED);
        new Hooks().afterScenario(result.getName(), true);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        recordScenarioReportData(result, TestNgCapturedStep.FAILED);
        new Hooks().afterScenario(result.getName(), true);
    }

    private void recordScenarioReportData(ITestResult result, String status) {
        List<TestNgCapturedStep> steps = TestNgStepRecorder.stopCapturingAndGetStepsForCurrentThread();
        String featureName = result.getTestClass().getRealClass().getSimpleName();
        String scenarioName = scenarioNameFor(result);
        List<String> tags = List.of(result.getMethod().getGroups());
        long durationMillis = result.getEndMillis() - result.getStartMillis();
        scenarioReportData.add(new TestNgScenarioReportData(featureName, scenarioName, tags, status, durationMillis, steps));
    }

    private String scenarioNameFor(ITestResult result) {
        Object[] parameters = result.getParameters();
        if (parameters.length == 0) {
            return result.getName();
        }
        String parameterSummary = java.util.Arrays.stream(parameters)
                .map(String::valueOf)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        return result.getName() + " [" + parameterSummary + "]";
    }

    List<TestNgScenarioReportData> getScenarioReportData() {
        return List.copyOf(scenarioReportData);
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
