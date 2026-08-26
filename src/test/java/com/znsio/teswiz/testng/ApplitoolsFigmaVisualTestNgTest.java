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
        Driver driver = createDriverWithFigmaDesignDetails();

        visuallyCheck(driver, "integrations page", "https://applitools.com/platform/integrations/");
        visuallyCheck(driver, "what's new page", "https://applitools.com/platform/whats-new/");
    }

    private Driver createDriverWithFigmaDesignDetails() {
        TestExecutionContext context = Runner.getTestExecutionContext(Thread.currentThread().getId());
        context.addTestState(TEST_CONTEXT.APPLITOOLS_FIGMA_APP_NAME, "Applitools website");
        context.addTestState(TEST_CONTEXT.APPLITOOLS_FIGMA_TEST_NAME, "Applitools Full Pages");
        context.addTestState(TEST_CONTEXT.APPLITOOLS_FIGMA_BASELINE_ENV_NAME, "Applitools Full Pages_1506");

        Drivers.createDriverFor(TEST_CONTEXT.I, Runner.getPlatform(), context);
        return Drivers.getDriverForCurrentUser(Thread.currentThread().getId());
    }

    private void visuallyCheck(Driver driver, String pageName, String url) {
        driver.getInnerDriver().get(url);
        driver.getVisual().checkWindow(pageName, url);
    }
}
