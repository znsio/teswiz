package com.znsio.teswiz.runner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;

import com.znsio.teswiz.context.SessionContext;
import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import com.znsio.teswiz.session.SessionHandle;

class DriversSessionHandleMetadataTest {
    private static final String CONFIG_FILE = "./configs/theapp/theapp_lambdatest_web_config.properties";

    @AfterEach
    void cleanUp() {
        SessionContext.remove(Thread.currentThread().getId());
    }

    @Test
    void shouldBuildSeleniumWebSessionHandleWithNormalizedCloudMetadata() throws Exception {
        setupConfig();
        TestExecutionContext context = new TestExecutionContext("drivers-session-handle-cloud-metadata");
        Path scenarioDir = Files.createTempDirectory("drivers-session-handle-cloud-metadata");
        context.addTestState(TEST_CONTEXT.SCENARIO_LOG_DIRECTORY, scenarioDir.toString());

        RemoteWebDriver remoteWebDriver = mock(RemoteWebDriver.class);
        when(remoteWebDriver.getSessionId()).thenReturn(new SessionId("remote-session-1"));
        when(remoteWebDriver.executeScript("lambdatest_action: {\"action\": \"getTestDetails\"}"))
                .thenReturn("""
                        {
                          "data": {
                            "session_id": "lt-session-1",
                            "build_id": "lt-build-1"
                          }
                        }
                        """);
        Driver driver = mock(Driver.class);
        when(driver.getInnerDriver()).thenReturn(remoteWebDriver);
        SessionHandle sessionHandle = invokeBuildSessionHandle("buyer", "chrome", Platform.web, context, driver);

        assertThat(sessionHandle.engine()).isEqualTo("selenium");
        assertThat(sessionHandle.sessionId()).isEqualTo("remote-session-1");
        assertThat(sessionHandle.metadata())
                .containsEntry("browserName", "chrome")
                .containsEntry("provider", "lambdatest")
                .containsEntry("remoteSessionId", "remote-session-1")
                .containsEntry("providerSessionId", "lt-session-1")
                .containsEntry("providerBuildId", "lt-build-1")
                .containsEntry("providerReportUrl",
                        "https://automation.lambdatest.com/logs/?sessionID=lt-session-1");
    }

    private static void setupConfig() {
        Setup.load(CONFIG_FILE);
        Setup.loadAndUpdateConfigParameters(CONFIG_FILE);
        Setup.getExecutionArguments();
    }

    private static SessionHandle invokeBuildSessionHandle(String userPersona, String browserName, Platform platform,
            TestExecutionContext context, Driver driver) throws Exception {
        Method buildSessionHandle = Drivers.class.getDeclaredMethod("buildSessionHandle", String.class, String.class,
                Platform.class, TestExecutionContext.class, Driver.class);
        buildSessionHandle.setAccessible(true);
        return (SessionHandle) buildSessionHandle.invoke(null, userPersona, browserName, platform, context, driver);
    }
}
