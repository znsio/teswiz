package com.znsio.teswiz.runner;

import java.util.LinkedHashMap;
import java.util.Map;

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

    SessionHandle createSessionHandle(String userPersona, String browserName, Platform forPlatform,
            TestExecutionContext context) {
        PlaywrightWorkerSession workerSession = createSession(userPersona, browserName, context);
        String artifactPath = context.getTestStateAsString(TEST_CONTEXT.SCENARIO_LOG_DIRECTORY);
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("browserName", workerSession.browserName());
        metadata.put("contextId", workerSession.contextId());
        metadata.put("pageId", workerSession.pageId());
        metadata.put("workerSessionId", workerSession.sessionId());
        return new SessionHandle(userPersona, forPlatform, WebEngine.PLAYWRIGHT_TS.getConfigValue(),
                workerSession.sessionId(), artifactPath, metadata);
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
}
