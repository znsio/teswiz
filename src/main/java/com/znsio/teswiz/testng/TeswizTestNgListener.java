package com.znsio.teswiz.testng;

import com.znsio.teswiz.steps.Hooks;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.util.concurrent.atomic.AtomicInteger;

public class TeswizTestNgListener implements ITestListener {
    private final AtomicInteger runningTestNumber = new AtomicInteger(0);

    @Override
    public void onTestStart(ITestResult result) {
        TestNgTestExecutionContextFactory.create(result.getName(), runningTestNumber.incrementAndGet());
        new Hooks().beforeScenario(result.getName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        new Hooks().afterScenario(result.getName(), false);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        new Hooks().afterScenario(result.getName(), true);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        new Hooks().afterScenario(result.getName(), true);
    }
}
