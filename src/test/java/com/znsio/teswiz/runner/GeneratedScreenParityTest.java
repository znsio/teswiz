package com.znsio.teswiz.runner;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.znsio.teswiz.context.SessionContext;
import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import com.znsio.teswiz.screen.ScreenRegistry;
import com.znsio.teswiz.screen.generateddemo.GeneratedPlaywrightTsTestScreen;
import com.znsio.teswiz.session.SessionHandle;
import com.znsio.teswiz.session.UserPersonaDetails;
import com.znsio.teswiz.tools.ScreenShotManager;
import com.znsio.teswiz.web.WebEngine;

class GeneratedScreenParityTest {
    private static final String CONFIG_FILE = "./configs/theapp/theapp_local_web_config.properties";
    private static final String USER_PERSONA = "buyer";

    private SharedWebDriverFixture fixture;

    @AfterEach
    void cleanUp() {
        if (null != fixture) {
            fixture.close();
            fixture = null;
        }
        System.clearProperty(Setup.WEB_ENGINE);
        SessionContext.remove(Thread.currentThread().getId());
    }

    @Test
    void shouldExecuteGeneratedScreenContractThroughPlaywrightTsBridge(@TempDir Path tempDir) throws Exception {
        assertGeneratedScreenContract(tempDir, WebEngine.PLAYWRIGHT_TS, new PlaywrightWebDriverFixture());
    }

    @Test
    void shouldExecuteGeneratedScreenContractThroughSeleniumWebImplementation(@TempDir Path tempDir)
            throws Exception {
        SeleniumContractConditions.assumeEnabled();
        assertGeneratedScreenContract(tempDir, WebEngine.SELENIUM, new SeleniumWebDriverFixture());
    }

    private void assertGeneratedScreenContract(Path tempDir, WebEngine webEngine, SharedWebDriverFixture nextFixture)
            throws Exception {
        fixture = nextFixture;
        setupConfig(webEngine);

        TestExecutionContext context = createContext(tempDir, webEngine);
        Driver driver = createDriver(context, tempDir);
        GeneratedPlaywrightTsTestScreen screen = ScreenRegistry.getScreen(GeneratedPlaywrightTsTestScreen.class);

        Path fixturePage = writeFixturePage(tempDir);
        screen.open(fixturePage.toUri().toString())
                .enterValue("screen parity");

        assertThat(screen.readValue()).isEqualTo("screen parity");
        assertThat(screen.readValues()).containsExactly("screen parity", "screen parity-copy");
        assertThat(context.getTestState(TEST_CONTEXT.SCREENSHOT_MANAGER)).isNotNull();
        assertThat(driver.getVisual()).isNotNull();
    }

    private static void setupConfig(WebEngine webEngine) {
        System.setProperty(Setup.WEB_ENGINE, webEngine.getConfigValue());
        Setup.load(CONFIG_FILE);
        Setup.loadAndUpdateConfigParameters(CONFIG_FILE);
    }

    private TestExecutionContext createContext(Path tempDir, WebEngine webEngine) {
        TestExecutionContext context = new TestExecutionContext("generated-screen-parity-" + webEngine.getConfigValue());
        Path screenshotsDir = tempDir.resolve("screenshots");
        UserPersonaDetails userPersonaDetails = new UserPersonaDetails();
        context.addTestState(TEST_CONTEXT.SCENARIO_LOG_DIRECTORY, tempDir.toString());
        context.addTestState(TEST_CONTEXT.SCREENSHOT_DIRECTORY, screenshotsDir.toString());
        context.addTestState(TEST_CONTEXT.SOFT_ASSERTIONS, new SoftAssertions());
        context.addTestState(TEST_CONTEXT.SCREENSHOT_MANAGER, new ScreenShotManager());
        context.addTestState(TEST_CONTEXT.CURRENT_USER_PERSONA_DETAILS, userPersonaDetails);
        context.addTestState(TEST_CONTEXT.CURRENT_USER_PERSONA, USER_PERSONA);
        context.addTestState(TEST_CONTEXT.CURRENT_PLATFORM, Platform.web);

        userPersonaDetails.addPlatform(USER_PERSONA, Platform.web);
        userPersonaDetails.addSessionHandle(USER_PERSONA, SessionHandle.create(USER_PERSONA, Platform.web,
                webEngine.getConfigValue(), tempDir.toString(), Map.of()));
        return context;
    }

    private Driver createDriver(TestExecutionContext context, Path tempDir) {
        Driver driver = new Driver(context.getTestName(), Platform.web, USER_PERSONA, "generated-demo",
                fixture.createDriver(USER_PERSONA), true);
        UserPersonaDetails userPersonaDetails = Drivers.getUserPersonaDetails(context);
        userPersonaDetails.addDriver(USER_PERSONA, driver);
        return driver;
    }

    private static Path writeFixturePage(Path tempDir) throws IOException {
        Path page = tempDir.resolve("generated-screen-parity.html");
        String html = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8" />
                  <title>Generated Screen Parity</title>
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
