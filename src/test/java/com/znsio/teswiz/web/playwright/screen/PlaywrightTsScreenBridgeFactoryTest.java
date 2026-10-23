package com.znsio.teswiz.web.playwright.screen;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.znsio.teswiz.context.SessionContext;
import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Setup;
import com.znsio.teswiz.screen.theapp.AppLaunchScreen;
import com.znsio.teswiz.screen.generateddemo.GeneratedPlaywrightTsTestScreen;
import com.znsio.teswiz.tools.ScreenShotManager;
import com.znsio.teswiz.web.playwright.PlaywrightWebDriver;
import com.znsio.teswiz.web.playwright.PlaywrightWorkerClient;
import com.znsio.teswiz.web.playwright.PlaywrightWorkerSession;

class PlaywrightTsScreenBridgeFactoryTest {
    private static final String CONFIG_FILE = "./configs/theapp/theapp_local_web_config.properties";

    private PlaywrightWorkerClient workerClient;

    @AfterEach
    void cleanUp() {
        if (null != workerClient) {
            workerClient.close();
        }
        SessionContext.remove(Thread.currentThread().getId());
    }

    @Test
    void shouldCreateAdapterFreeBridgeWhenMatchingTypeScriptScreenExists(@TempDir Path tempDir) throws IOException {
        setupConfig();
        TestExecutionContext context = setUpContext(tempDir);
        workerClient = new PlaywrightWorkerClient();
        workerClient.start();

        PlaywrightWorkerSession session = workerClient.createSession("buyer", "chromium", tempDir);
        Driver driver = createDriver(session);
        GeneratedPlaywrightTsTestScreen screen = new PlaywrightTsScreenBridgeFactory()
                .createIfSupported(GeneratedPlaywrightTsTestScreen.class, driver, driver.getVisual())
                .orElseThrow();

        Path fixturePage = writeFixturePage(tempDir);
        screen.open(fixturePage.toUri().toString())
                .enterValue("framework-owned bridge");

        assertThat(screen.readValue()).isEqualTo("framework-owned bridge");
        assertThat(screen.readValues()).containsExactly("framework-owned bridge", "framework-owned bridge-copy");
        assertThat(context.getTestState(TEST_CONTEXT.SCREENSHOT_MANAGER)).isNotNull();
    }

    @Test
    void shouldFailFastWhenTypeScriptScreenMarksActionAsUnsupported(@TempDir Path tempDir) throws IOException {
        setupConfig();
        setUpContext(tempDir);
        workerClient = new PlaywrightWorkerClient();
        workerClient.start();

        PlaywrightWorkerSession session = workerClient.createSession("buyer", "chromium", tempDir);
        Driver driver = createDriver(session);
        AppLaunchScreen screen = new PlaywrightTsScreenBridgeFactory()
                .createIfSupported(AppLaunchScreen.class, driver, driver.getVisual())
                .orElseThrow();

        assertThatThrownBy(screen::goToClipboardDemo)
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("Clipboard Demo")
                .hasMessageContaining("playwright-ts");
    }

    private static void setupConfig() {
        Setup.load(CONFIG_FILE);
        Setup.loadAndUpdateConfigParameters(CONFIG_FILE);
    }

    private TestExecutionContext setUpContext(Path tempDir) {
        TestExecutionContext context = new TestExecutionContext("playwright-ts-screen-bridge");
        Path screenshotsDir = tempDir.resolve("screenshots");
        context.addTestState(TEST_CONTEXT.SCENARIO_LOG_DIRECTORY, tempDir.toString());
        context.addTestState(TEST_CONTEXT.SCREENSHOT_DIRECTORY, screenshotsDir.toString());
        context.addTestState(TEST_CONTEXT.SOFT_ASSERTIONS, new SoftAssertions());
        context.addTestState(TEST_CONTEXT.SCREENSHOT_MANAGER, new ScreenShotManager());
        return context;
    }

    private Driver createDriver(PlaywrightWorkerSession session) {
        PlaywrightWebDriver webDriver = new PlaywrightWebDriver(workerClient, session);
        return new Driver("playwright-ts-screen-bridge", Platform.web, "buyer", "generated-demo",
                webDriver, true);
    }

    private Path writeFixturePage(Path tempDir) throws IOException {
        Path page = tempDir.resolve("generated-playwright-ts-bridge.html");
        String html = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8" />
                  <title>Generated Playwright TS Bridge</title>
                </head>
                <body>
                  <input id="demo-input" type="text" />
                  <div id="demo-output"></div>
                </body>
                </html>
                """;
        Files.writeString(page, html);
        return page;
    }
}
