package com.znsio.teswiz.runner;

import com.znsio.teswiz.context.SessionContext;
import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import com.znsio.teswiz.screen.ScreenRegistry;
import com.znsio.teswiz.screen.duckduckgo.DuckDuckGoScreen;
import com.znsio.teswiz.session.SessionHandle;
import com.znsio.teswiz.session.UserPersonaDetails;
import com.znsio.teswiz.tools.ScreenShotManager;
import com.znsio.teswiz.web.WebEngine;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlaywrightTsUnsupportedScreenModuleTest {
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
    void shouldFailWithEngineAwareUnsupportedMessageWhenTsModuleMarksActionUnsupported() throws Exception {
        fixture = new PlaywrightWebDriverFixture();
        setupConfig();

        TestExecutionContext context = createContext();
        createDriver(context);

        DuckDuckGoScreen screen = ScreenRegistry.getScreen(DuckDuckGoScreen.class);

        assertThatThrownBy(screen::launchBrowser)
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("DuckDuckGo")
                .hasMessageContaining("WEB_ENGINE=playwright-ts");
    }

    private static void setupConfig() {
        System.setProperty(Setup.WEB_ENGINE, WebEngine.PLAYWRIGHT_TS.getConfigValue());
        Setup.load(CONFIG_FILE);
        Setup.loadAndUpdateConfigParameters(CONFIG_FILE);
    }

    private TestExecutionContext createContext() throws Exception {
        TestExecutionContext context = new TestExecutionContext("playwright-ts-unsupported-screen-module");
        Path scenarioDir = Files.createTempDirectory("playwright-ts-unsupported-screen-module");
        Path screenshotsDir = Files.createDirectory(scenarioDir.resolve("screenshots"));
        UserPersonaDetails userPersonaDetails = new UserPersonaDetails();
        context.addTestState(TEST_CONTEXT.SCENARIO_LOG_DIRECTORY, scenarioDir.toString());
        context.addTestState(TEST_CONTEXT.SCREENSHOT_DIRECTORY, screenshotsDir.toString());
        context.addTestState(TEST_CONTEXT.SOFT_ASSERTIONS, new SoftAssertions());
        context.addTestState(TEST_CONTEXT.SCREENSHOT_MANAGER, new ScreenShotManager());
        context.addTestState(TEST_CONTEXT.CURRENT_USER_PERSONA_DETAILS, userPersonaDetails);
        context.addTestState(TEST_CONTEXT.CURRENT_USER_PERSONA, USER_PERSONA);
        context.addTestState(TEST_CONTEXT.CURRENT_PLATFORM, Platform.web);

        userPersonaDetails.addPlatform(USER_PERSONA, Platform.web);
        userPersonaDetails.addSessionHandle(USER_PERSONA, SessionHandle.create(USER_PERSONA, Platform.web,
                WebEngine.PLAYWRIGHT_TS.getConfigValue(), scenarioDir.toString(), Map.of()));
        return context;
    }

    private void createDriver(TestExecutionContext context) {
        Driver driver = new Driver(context.getTestName(), Platform.web, USER_PERSONA, "unsupported-module-test",
                fixture.createDriver(USER_PERSONA), true);
        Drivers.getUserPersonaDetails(context).addDriver(USER_PERSONA, driver);
    }
}
