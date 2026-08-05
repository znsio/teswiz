package com.znsio.teswiz.web.playwright;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;
import com.znsio.teswiz.config.browser.PlaywrightBrowserConfig;
import com.znsio.teswiz.context.SessionContext;
import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.Setup;
import com.znsio.teswiz.session.UserPersonaDetails;
import com.znsio.teswiz.tools.ScreenShotManager;
import com.znsio.teswiz.web.browser.WebDriverSessionResult;
import com.znsio.teswiz.web.provider.playwright.PlaywrightExecutionProviderConfig;

class PlaywrightJavaDriverManagerTest {
    private static final String CONFIG_FILE = "./configs/theapp/theapp_local_web_config.properties";

    @AfterEach
    void cleanUp() {
        System.clearProperty("WEB_ENGINE");
        System.clearProperty("HEADLESS");
        SessionContext.remove(Thread.currentThread().getId());
    }

    @Test
    void shouldCreatePlaywrightJavaSessionResultAndLoadBaseUrl() throws Exception {
        enablePlaywrightJavaHeadless();
        TestExecutionContext context = createContext("playwright-java-manager");
        UserPersonaDetails userPersonaDetails = (UserPersonaDetails) context
                .getTestState(TEST_CONTEXT.CURRENT_USER_PERSONA_DETAILS);
        userPersonaDetails.addAppName("buyer", Runner.DEFAULT);

        Playwright playwright = mock(Playwright.class);
        Browser browser = mock(Browser.class);
        BrowserContext browserContext = mock(BrowserContext.class);
        Page page = mock(Page.class);
        Tracing tracing = mock(Tracing.class);
        when(browser.version()).thenReturn("126.0");
        when(browser.newContext(any(Browser.NewContextOptions.class))).thenReturn(browserContext);
        when(browserContext.tracing()).thenReturn(tracing);
        when(browserContext.newPage()).thenReturn(page);

        PlaywrightJavaDriverManager manager = new PlaywrightJavaDriverManager(
                (browserName, currentContext) -> new PlaywrightBrowserConfig(browserName, true, List.of("--headless=new"),
                        "chrome", null, Map.of("ignoreHTTPSErrors", true), Map.of()),
                () -> PlaywrightExecutionProviderConfig.local(),
                (browserConfig, artifactDirectory) -> new PlaywrightJavaRuntime(playwright, browser));

        WebDriverSessionResult result = manager.createWebSessionForUser("buyer", "chrome", Platform.web, context);

        assertThat(result.webDriver()).isInstanceOf(PlaywrightJavaWebDriver.class);
        assertThat(result.headless()).isTrue();
        assertThat(result.capabilities().getCapability("engine")).isEqualTo("playwright-java");
        assertThat(result.sessionHandle()).isNotNull();
        assertThat(result.sessionHandle().engine()).isEqualTo("playwright-java");
        assertThat(result.sessionHandle().metadata()).containsEntry("browserVersion", "126.0");
        verify(page).navigate(anyString(), any(Page.NavigateOptions.class));
    }

    @Test
    void shouldReuseSharedBrowserRuntimeAcrossPersonasAndCloseItAfterLastSession() throws Exception {
        enablePlaywrightJavaHeadless();
        TestExecutionContext context = createContext("playwright-java-shared-runtime");
        UserPersonaDetails userPersonaDetails = (UserPersonaDetails) context
                .getTestState(TEST_CONTEXT.CURRENT_USER_PERSONA_DETAILS);
        userPersonaDetails.addAppName("buyer", Runner.DEFAULT);
        userPersonaDetails.addAppName("seller", Runner.DEFAULT);

        Playwright playwright = mock(Playwright.class);
        Browser browser = mock(Browser.class);
        BrowserContext buyerContext = mock(BrowserContext.class);
        BrowserContext sellerContext = mock(BrowserContext.class);
        Page buyerPage = mock(Page.class);
        Page sellerPage = mock(Page.class);
        Tracing buyerTracing = mock(Tracing.class);
        Tracing sellerTracing = mock(Tracing.class);
        when(browser.version()).thenReturn("126.0");
        when(browser.newContext(any(Browser.NewContextOptions.class))).thenReturn(buyerContext, sellerContext);
        when(buyerContext.tracing()).thenReturn(buyerTracing);
        when(sellerContext.tracing()).thenReturn(sellerTracing);
        when(buyerContext.newPage()).thenReturn(buyerPage);
        when(sellerContext.newPage()).thenReturn(sellerPage);

        PlaywrightJavaDriverManager manager = new PlaywrightJavaDriverManager(
                (browserName, currentContext) -> new PlaywrightBrowserConfig(browserName, true, List.of("--headless=new"),
                        "chrome", null, Map.of(), Map.of()),
                () -> PlaywrightExecutionProviderConfig.local(),
                (browserConfig, artifactDirectory) -> new PlaywrightJavaRuntime(playwright, browser));

        WebDriverSessionResult buyerResult = manager.createWebSessionForUser("buyer", "chrome", Platform.web, context);
        WebDriverSessionResult sellerResult = manager.createWebSessionForUser("seller", "chrome", Platform.web, context);

        assertThat(buyerResult.webDriver()).isInstanceOf(PlaywrightJavaWebDriver.class);
        assertThat(sellerResult.webDriver()).isInstanceOf(PlaywrightJavaWebDriver.class);
        verify(browser, times(2)).newContext(any(Browser.NewContextOptions.class));

        buyerResult.webDriver().quit();
        verify(buyerContext).close();
        verify(browser, never()).close();
        verify(playwright, never()).close();

        sellerResult.webDriver().quit();
        verify(sellerContext).close();
        verify(browser).close();
        verify(playwright).close();
    }

    @Test
    void shouldAddRemoteProviderMetadataForPlaywrightJavaSessions() throws Exception {
        enablePlaywrightJavaHeadless();
        TestExecutionContext context = createContext("playwright-java-cloud-metadata");
        UserPersonaDetails userPersonaDetails = (UserPersonaDetails) context
                .getTestState(TEST_CONTEXT.CURRENT_USER_PERSONA_DETAILS);
        userPersonaDetails.addAppName("buyer", Runner.DEFAULT);

        Playwright playwright = mock(Playwright.class);
        Browser browser = mock(Browser.class);
        BrowserContext browserContext = mock(BrowserContext.class);
        Page page = mock(Page.class);
        Tracing tracing = mock(Tracing.class);
        when(browser.version()).thenReturn("126.0");
        when(browser.newContext(any(Browser.NewContextOptions.class))).thenReturn(browserContext);
        when(browserContext.tracing()).thenReturn(tracing);
        when(browserContext.newPage()).thenReturn(page);

        PlaywrightJavaDriverManager manager = new PlaywrightJavaDriverManager(
                (browserName, currentContext) -> new PlaywrightBrowserConfig(browserName, true, List.of("--headless=new"),
                        "chrome", null, Map.of(), Map.of()),
                () -> new PlaywrightExecutionProviderConfig("browserstack",
                        "https://hub-cloud.browserstack.com",
                        "https://api-cloud.browserstack.com/app-automate/",
                        "browserstack_user",
                        "browserstack_key"),
                (browserConfig, artifactDirectory) -> new PlaywrightJavaRuntime(playwright, browser));

        WebDriverSessionResult result = manager.createWebSessionForUser("buyer", "chrome", Platform.web, context);

        assertThat(result.sessionHandle().metadata())
                .containsEntry("provider", "browserstack")
                .containsEntry("remoteUrl", "https://hub-cloud.browserstack.com")
                .containsEntry("apiUrl", "https://api-cloud.browserstack.com/app-automate/");
    }

    private void enablePlaywrightJavaHeadless() {
        System.setProperty("WEB_ENGINE", "playwright-java");
        System.setProperty("HEADLESS", "true");
        Setup.load(CONFIG_FILE);
        Setup.loadAndUpdateConfigParameters(CONFIG_FILE);
        Setup.getExecutionArguments();
    }

    private TestExecutionContext createContext(String testName) throws Exception {
        TestExecutionContext context = new TestExecutionContext(testName);
        Path scenarioDir = Files.createTempDirectory(testName);
        Path screenshotsDir = Files.createDirectory(scenarioDir.resolve("screenshots"));
        context.addTestState(TEST_CONTEXT.SCENARIO_LOG_DIRECTORY, scenarioDir.toString());
        context.addTestState(TEST_CONTEXT.SCREENSHOT_DIRECTORY, screenshotsDir.toString());
        context.addTestState(TEST_CONTEXT.CURRENT_USER_PERSONA_DETAILS, new UserPersonaDetails());
        context.addTestState(TEST_CONTEXT.SOFT_ASSERTIONS, new SoftAssertions());
        context.addTestState(TEST_CONTEXT.SCREENSHOT_MANAGER, new ScreenShotManager());
        return context;
    }
}
