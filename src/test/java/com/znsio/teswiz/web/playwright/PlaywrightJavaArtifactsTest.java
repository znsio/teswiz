package com.znsio.teswiz.web.playwright;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.znsio.teswiz.context.SessionContext;
import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.Setup;
import com.znsio.teswiz.session.UserPersonaDetails;
import com.znsio.teswiz.tools.ScreenShotManager;
import com.znsio.teswiz.web.browser.WebDriverSessionResult;

class PlaywrightJavaArtifactsTest {
    private static final String CONFIG_FILE = "./configs/theapp/theapp_local_web_config.properties";

    @AfterEach
    void cleanUp() {
        System.clearProperty("WEB_ENGINE");
        System.clearProperty("HEADLESS");
        SessionContext.remove(Thread.currentThread().getId());
    }

    @Test
    void shouldWriteTraceConsoleAndHarArtifactsWhenSessionCloses() throws Exception {
        enablePlaywrightJavaHeadless();
        TestExecutionContext context = createContext("playwright-java-artifacts");
        UserPersonaDetails userPersonaDetails = (UserPersonaDetails) context
                .getTestState(TEST_CONTEXT.CURRENT_USER_PERSONA_DETAILS);
        userPersonaDetails.addAppName("buyer", Runner.DEFAULT);

        PlaywrightJavaDriverManager manager = new PlaywrightJavaDriverManager();
        WebDriverSessionResult result = manager.createWebSessionForUser("buyer", "chrome", Platform.web, context);

        String sessionId = result.sessionHandle().sessionId();
        Path artifactDir = Path.of(context.getTestStateAsString(TEST_CONTEXT.SCENARIO_LOG_DIRECTORY));

        result.webDriver().get(
                "data:text/html,<html><body><script>console.log('buyer-ready')</script><h1>Ready</h1></body></html>");
        result.webDriver().quit();

        Path traceFile = artifactDir.resolve("buyer-" + sessionId + "-trace.zip");
        Path harFile = artifactDir.resolve("buyer-" + sessionId + "-network.har");
        Path consoleFile = artifactDir.resolve("buyer-" + sessionId + "-console.log");

        assertThat(traceFile).exists();
        assertThat(harFile).exists();
        assertThat(consoleFile).exists();
        assertThat(Files.readString(consoleFile)).contains("buyer-ready");
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
