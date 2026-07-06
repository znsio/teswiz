package com.znsio.teswiz.runner;

import java.util.LinkedHashMap;
import java.util.Map;

import org.openqa.selenium.remote.DesiredCapabilities;

import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.entities.TEST_CONTEXT;

public final class PlaywrightWorkerManager {
    private final PlaywrightWorkerClientFactory clientFactory;

    public PlaywrightWorkerManager() {
        this(PlaywrightWorkerClient::new);
    }

    PlaywrightWorkerManager(PlaywrightWorkerClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    PlaywrightWorkerClient getOrStart(TestExecutionContext context) {
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

    PlaywrightWorkerSession createSession(String userPersona, String browserName, TestExecutionContext context) {
        return getOrStart(context).createSession(userPersona, browserName);
    }

    ManagedPlaywrightSession createManagedSession(String userPersona, String browserName, Platform forPlatform,
            TestExecutionContext context) {
        PlaywrightWorkerClient workerClient = getOrStart(context);
        PlaywrightWorkerSession workerSession = workerClient.createSession(userPersona, browserName);
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

    SessionHandle createSessionHandle(String userPersona, String browserName, Platform forPlatform,
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

    interface PlaywrightWorkerClientFactory {
        PlaywrightWorkerClient create();
    }

    record ManagedPlaywrightSession(PlaywrightWorkerClient workerClient, PlaywrightWorkerSession workerSession,
            SessionHandle sessionHandle) {
        PlaywrightWebDriver createWebDriver() {
            return new PlaywrightWebDriver(workerClient, workerSession);
        }

        DesiredCapabilities createCapabilities() {
            DesiredCapabilities capabilities = new DesiredCapabilities();
            capabilities.setCapability("browserName", sessionHandle.metadata().get("browserName"));
            capabilities.setCapability("engine", WebEngine.PLAYWRIGHT_TS.getConfigValue());
            capabilities.setCapability("playwrightContextId", sessionHandle.metadata().get("contextId"));
            capabilities.setCapability("playwrightPageId", sessionHandle.metadata().get("pageId"));
            return capabilities;
        }
    }
}
