package com.znsio.teswiz.web.playwright;

import java.util.LinkedHashMap;
import java.util.Map;
import java.nio.file.Path;

import org.json.JSONObject;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.znsio.teswiz.config.browser.PlaywrightBrowserConfig;
import com.znsio.teswiz.config.browser.PlaywrightBrowserConfigResolver;
import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import com.znsio.teswiz.session.SessionHandle;
import com.znsio.teswiz.web.WebEngine;

public final class PlaywrightWorkerManager {
    private final PlaywrightWorkerClientFactory clientFactory;
    private final PlaywrightBrowserConfigResolver browserConfigResolver;

    public PlaywrightWorkerManager() {
        this(PlaywrightWorkerClient::new, new PlaywrightBrowserConfigResolver());
    }

    public PlaywrightWorkerManager(PlaywrightWorkerClientFactory clientFactory,
            PlaywrightBrowserConfigResolver browserConfigResolver) {
        this.clientFactory = clientFactory;
        this.browserConfigResolver = browserConfigResolver;
    }

    public PlaywrightWorkerClient getOrStart(TestExecutionContext context) {
        PlaywrightWorkerClient existingClient = (PlaywrightWorkerClient) context
                .getTestState(TEST_CONTEXT.PLAYWRIGHT_WORKER_CLIENT);
        if (null != existingClient) {
            if (!existingClient.isRunning()) {
                existingClient.start();
            }
            return existingClient;
        }

        PlaywrightWorkerClient workerClient = clientFactory.create();
        workerClient.start();
        context.addTestState(TEST_CONTEXT.PLAYWRIGHT_WORKER_CLIENT, workerClient);
        return workerClient;
    }

    public PlaywrightWorkerSession createSession(String userPersona, String browserName, TestExecutionContext context) {
        return getOrStart(context).createSession(userPersona, browserName,
                toJson(browserConfigResolver.resolve(browserName, context)),
                Path.of(context.getTestStateAsString(TEST_CONTEXT.SCENARIO_LOG_DIRECTORY)));
    }

    public ManagedPlaywrightSession createManagedSession(String userPersona, String browserName, Platform forPlatform,
            TestExecutionContext context) {
        PlaywrightWorkerClient workerClient = getOrStart(context);
        PlaywrightWorkerSession workerSession = workerClient.createSession(userPersona, browserName,
                toJson(browserConfigResolver.resolve(browserName, context)),
                Path.of(context.getTestStateAsString(TEST_CONTEXT.SCENARIO_LOG_DIRECTORY)));
        String artifactPath = context.getTestStateAsString(TEST_CONTEXT.SCENARIO_LOG_DIRECTORY);
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("browserName", workerSession.browserName());
        metadata.put("contextId", workerSession.contextId());
        metadata.put("pageId", workerSession.pageId());
        metadata.put("workerSessionId", workerSession.sessionId());
        SessionHandle sessionHandle = new SessionHandle(userPersona, forPlatform, WebEngine.PLAYWRIGHT_TS.getConfigValue(),
                workerSession.sessionId(), artifactPath, metadata);
        return new ManagedPlaywrightSession(workerClient, workerSession, sessionHandle);
    }

    public SessionHandle createSessionHandle(String userPersona, String browserName, Platform forPlatform,
            TestExecutionContext context) {
        return createManagedSession(userPersona, browserName, forPlatform, context).sessionHandle();
    }

    public void shutdown(TestExecutionContext context) {
        PlaywrightWorkerClient workerClient = (PlaywrightWorkerClient) context
                .getTestState(TEST_CONTEXT.PLAYWRIGHT_WORKER_CLIENT);
        if (null == workerClient) {
            return;
        }

        workerClient.close();
        context.addTestState(TEST_CONTEXT.PLAYWRIGHT_WORKER_CLIENT, null);
    }

    public interface PlaywrightWorkerClientFactory {
        PlaywrightWorkerClient create();
    }

    private JSONObject toJson(PlaywrightBrowserConfig browserConfig) {
        return new JSONObject()
                .put("browserName", browserConfig.browserName())
                .put("headless", browserConfig.headless())
                .put("launchArgs", browserConfig.launchArgs())
                .put("channel", browserConfig.channel())
                .put("executablePath", browserConfig.executablePath())
                .put("contextOptions", browserConfig.contextOptions())
                .put("launchOptions", browserConfig.launchOptions());
    }

    public record ManagedPlaywrightSession(PlaywrightWorkerClient workerClient, PlaywrightWorkerSession workerSession,
            SessionHandle sessionHandle) {
        public PlaywrightWebDriver createWebDriver() {
            return new PlaywrightWebDriver(workerClient, workerSession);
        }

        public DesiredCapabilities createCapabilities() {
            DesiredCapabilities capabilities = new DesiredCapabilities();
            capabilities.setCapability("browserName", sessionHandle.metadata().get("browserName"));
            capabilities.setCapability("engine", WebEngine.PLAYWRIGHT_TS.getConfigValue());
            capabilities.setCapability("playwrightContextId", sessionHandle.metadata().get("contextId"));
            capabilities.setCapability("playwrightPageId", sessionHandle.metadata().get("pageId"));
            return capabilities;
        }
    }
}
