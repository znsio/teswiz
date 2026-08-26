package com.znsio.teswiz.testng;

import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;
import org.testng.annotations.Test;

public class ApplitoolsFigmaVisualTestNgTest {
    @Test(groups = {"web", "figma", "applitools"})
    public void compareApplitoolsPagesWithFigmaDesign() {
        TestExecutionContext context = Runner.getTestExecutionContext(Thread.currentThread().getId());
        addFigmaDesignDetailsToContext(context, "Applitools website", "Applitools Full Pages", "Applitools Full Pages_1506");

        Drivers.createDriverFor(TEST_CONTEXT.I, Runner.getPlatform(), context);
        Driver driver = Drivers.getDriverForCurrentUser(Thread.currentThread().getId());

        visuallyCheck(driver, "integrations page", "https://applitools.com/platform/integrations/");
        visuallyCheck(driver, "what's new page", "https://applitools.com/platform/whats-new/");
    }

    private void addFigmaDesignDetailsToContext(TestExecutionContext context, String appName, String testName, String baselineName) {
        context.addTestState(TEST_CONTEXT.APPLITOOLS_FIGMA_APP_NAME, appName);
        context.addTestState(TEST_CONTEXT.APPLITOOLS_FIGMA_TEST_NAME, testName);
        context.addTestState(TEST_CONTEXT.APPLITOOLS_FIGMA_BASELINE_ENV_NAME, baselineName);
    }

    private void visuallyCheck(Driver driver, String pageName, String url) {
        driver.getInnerDriver().get(url);
        driver.getVisual().checkWindow(pageName, url);
    }
}
