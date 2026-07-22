package com.znsio.teswiz.web.browser;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.assertj.core.api.SoftAssertions;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.znsio.teswiz.config.browser.PlaywrightBrowserConfig;
import com.znsio.teswiz.config.browser.PlaywrightBrowserConfigResolver;
import com.znsio.teswiz.context.SessionContext;
import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.Setup;
import com.znsio.teswiz.session.UserPersonaDetails;
import com.znsio.teswiz.tools.ScreenShotManager;
import com.znsio.teswiz.web.playwright.PlaywrightWebDriver;
import com.znsio.teswiz.web.playwright.PlaywrightWorkerClient;
import com.znsio.teswiz.web.playwright.PlaywrightWorkerManager;
import com.znsio.teswiz.web.playwright.PlaywrightWorkerSession;
import com.znsio.teswiz.web.provider.LocalWebExecutionProvider;
import com.znsio.teswiz.web.provider.WebExecutionProvider;
import com.znsio.teswiz.web.provider.WebExecutionProviderResolver;

class BrowserDriverManagerTest {
    private static final String CONFIG_FILE = "./configs/theapp/theapp_local_web_config.properties";

    @AfterEach
    void cleanUp() {
        System.clearProperty("WEB_ENGINE");
        System.clearProperty("HEADLESS");
        SessionContext.remove(Thread.currentThread().getId());
    }

    @Test
    void shouldRoutePlaywrightWebEngineThroughBrowserManager() throws Exception {
        enablePlaywrightHeadless();
        TestExecutionContext context = createContext("browser-manager-playwright-routing");
        UserPersonaDetails userPersonaDetails = (UserPersonaDetails) context
                .getTestState(TEST_CONTEXT.CURRENT_USER_PERSONA_DETAILS);
        userPersonaDetails.addAppName("buyer", Runner.DEFAULT);

        FakePlaywrightWorkerClient workerClient = new FakePlaywrightWorkerClient();
        PlaywrightWorkerManager manager = new PlaywrightWorkerManager(() -> workerClient,
                new StubPlaywrightBrowserConfigResolver(), new StubWebExecutionProviderResolver());

        WebDriverSessionResult sessionResult = BrowserDriverManager.createWebSessionForUser(
                "buyer", "chrome", Platform.web, context, manager);

        assertThat(sessionResult.webDriver()).isInstanceOf(PlaywrightWebDriver.class);
        assertThat(sessionResult.capabilities()).isNotNull();
        assertThat(sessionResult.sessionHandle()).isNotNull();
        assertThat(sessionResult.sessionHandle().engine()).isEqualTo("playwright-ts");
        assertThat(workerClient.lastNavigatedUrl()).isEqualTo(Runner.getFromEnvironmentConfiguration("BASE_URL"));
        assertThat(context.getTestStateAsString(TEST_CONTEXT.WEB_BROWSER_ON)).isEqualTo("local");
    }

    @Test
    void shouldClosePlaywrightSessionsThroughTheBrowserManager() throws Exception {
        enablePlaywrightHeadless();
        TestExecutionContext context = createContext("browser-manager-playwright-close");
        UserPersonaDetails userPersonaDetails = (UserPersonaDetails) context
                .getTestState(TEST_CONTEXT.CURRENT_USER_PERSONA_DETAILS);
        userPersonaDetails.addAppName("buyer", Runner.DEFAULT);

        FakePlaywrightWorkerClient workerClient = new FakePlaywrightWorkerClient();
        PlaywrightWorkerManager manager = new PlaywrightWorkerManager(() -> workerClient,
                new StubPlaywrightBrowserConfigResolver(), new StubWebExecutionProviderResolver());

        WebDriverSessionResult sessionResult = BrowserDriverManager.createWebSessionForUser(
                "buyer", "chrome", Platform.web, context, manager);
        Driver driver = new Driver("browser-manager-playwright-close-buyer", Platform.web, "buyer",
                Runner.DEFAULT, sessionResult.webDriver(), sessionResult.headless());

        BrowserDriverManager.closeWebDriver("buyer", driver);

        assertThat(workerClient.wasClosed()).isTrue();
    }

    private void enablePlaywrightHeadless() {
        System.setProperty("WEB_ENGINE", "playwright-ts");
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

    private static class FakePlaywrightWorkerClient extends PlaywrightWorkerClient {
        private final AtomicInteger startCount = new AtomicInteger();
        private boolean running;
        private String lastNavigatedUrl;
        private boolean closed;

        FakePlaywrightWorkerClient() {
            super(Path.of("ignored-worker.mjs"));
        }

        @Override
        public synchronized void start() {
            startCount.incrementAndGet();
            running = true;
        }

        @Override
        public synchronized boolean isRunning() {
            return running;
        }

        @Override
        public synchronized PlaywrightWorkerSession createSession(String userPersona, String browserName,
                JSONObject browserConfig, Path artifactPath) {
            return new PlaywrightWorkerSession("playwright-session-1", userPersona, browserName, "context-1",
                    "page-1");
        }

        @Override
        public synchronized void navigateTo(String sessionId, String url) {
            lastNavigatedUrl = url;
        }

        @Override
        public synchronized void closeSession(String sessionId) {
            closed = true;
        }

        String lastNavigatedUrl() {
            return lastNavigatedUrl;
        }

        boolean wasClosed() {
            return closed;
        }
    }

    private static class StubPlaywrightBrowserConfigResolver extends PlaywrightBrowserConfigResolver {
        @Override
        public PlaywrightBrowserConfig resolve(String browserName, TestExecutionContext context) {
            return new PlaywrightBrowserConfig(browserName, true, List.of("--disable-gpu"), null, null,
                    Map.of("ignoreHTTPSErrors", true), Map.of());
        }
    }

    private static class StubWebExecutionProviderResolver extends WebExecutionProviderResolver {
        @Override
        public WebExecutionProvider resolve() {
            return new LocalWebExecutionProvider();
        }
    }
}
