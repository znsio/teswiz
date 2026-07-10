package com.znsio.teswiz.runner;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

abstract class AbstractSharedWebDriverContractTest {
    private SharedWebDriverFixture fixture;

    @AfterEach
    void tearDownFixture() {
        if (null != fixture) {
            fixture.close();
        }
    }

    @Test
    void shouldNavigateCapturePageSourceAndTakeScreenshot() throws Exception {
        WebDriver driver = createDriver("buyer");
        SharedWebDriverContract.assertNavigationPageSourceAndScreenshot(driver, SharedWebDriverTestPages.writeTestPage());
    }

    @Test
    void shouldFindElementsAndPerformCoreInteractions() throws Exception {
        WebDriver driver = createDriver("seller");
        SharedWebDriverContract.assertCoreInteractions(driver, SharedWebDriverTestPages.writeTestPage());
    }

    @Test
    void shouldExecuteJavascriptWithElementAndLiteralArguments() throws Exception {
        WebDriver driver = createDriver("host");
        SharedWebDriverContract.assertJavascriptExecution(driver, SharedWebDriverTestPages.writeTestPage());
    }

    @Test
    void shouldOpenAndSwitchBetweenTabs() throws Exception {
        WebDriver driver = createDriver("guest");
        SharedWebDriverContract.assertTabHandling(driver, SharedWebDriverTestPages.writeTestPage());
    }

    @Test
    void shouldSwitchIntoFrameAndBackToDefaultContent() throws Exception {
        WebDriver driver = createDriver("frame-user");
        SharedWebDriverContract.assertFrameHandling(driver, SharedWebDriverTestPages.writeFrameTestPage());
    }

    @Test
    void shouldSwitchIntoFrameUsingWebElement() throws Exception {
        WebDriver driver = createDriver("frame-element-user");
        driver.get(SharedWebDriverTestPages.writeFrameTestPage().toUri().toString());
        org.openqa.selenium.WebElement frameElement = driver.findElement(By.id("details-frame"));
        driver.switchTo().frame(frameElement);

        assertThat(driver.findElement(By.id("inside")).getText()).isEqualTo("Inside Frame");
        driver.switchTo().defaultContent();
        assertThat(driver.findElement(By.id("outside")).getText()).isEqualTo("Outside Frame");
    }

    @Test
    void shouldAccessOpenShadowDomThroughShadowRootSearchContext() throws Exception {
        WebDriver driver = createDriver("shadow-user");
        SharedWebDriverContract.assertOpenShadowDomHandling(driver, SharedWebDriverTestPages.writeShadowDomTestPage());
    }

    @Test
    void shouldSupportWindowSizingApis() throws Exception {
        WebDriver driver = createDriver("window-user");
        SharedWebDriverContract.assertWindowHandling(driver, SharedWebDriverTestPages.writeTestPage());
    }

    @Test
    void shouldHandleAlertsAndPrompts() throws Exception {
        WebDriver driver = createDriver("alert-user");
        SharedWebDriverContract.assertAlertHandling(driver, SharedWebDriverTestPages.writeAlertTestPage());
    }

    @Test
    void shouldReturnFocusedElementAsActiveElement() throws Exception {
        WebDriver driver = createDriver("focus-user");
        SharedWebDriverContract.assertActiveElementHandling(driver, SharedWebDriverTestPages.writeTestPage());
    }

    @Test
    void shouldManageCookies() throws Exception {
        try (SharedWebDriverContract.LocalCookieServer server = new SharedWebDriverContract.LocalCookieServer()) {
            WebDriver driver = createDriver("cookie-user");
            SharedWebDriverContract.assertCookieHandling(driver, server.url());
        }
    }

    @Test
    void shouldHonorImplicitWaitAndExposeConfiguredTimeouts() throws Exception {
        WebDriver driver = createDriver("timeout-user");
        SharedWebDriverContract.assertImplicitWaitAndTimeoutHandling(driver,
                SharedWebDriverTestPages.writeDelayedElementPage());
    }

    @Test
    void shouldExposeBrowserLogsThroughManageLogs() throws Exception {
        WebDriver driver = createDriver("logs-user");
        SharedWebDriverContract.assertBrowserLogs(driver, SharedWebDriverTestPages.writeConsoleLogPage());
    }

    protected abstract SharedWebDriverFixture createFixture();

    private WebDriver createDriver(String userPersona) {
        fixture = createFixture();
        return fixture.createDriver(userPersona);
    }
}
