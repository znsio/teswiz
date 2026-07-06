package com.znsio.teswiz.runner;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.znsio.teswiz.context.SessionContext;
import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.entities.TEST_CONTEXT;

class PlaywrightWorkerManagerTest {
    @AfterEach
    void cleanUp() {
        SessionContext.remove(Thread.currentThread().getId());
    }

    @Test
    void shouldReuseSameWorkerClientForContextAndStartItOnlyOnce() {
        TestExecutionContext context = new TestExecutionContext("playwright-worker-manager");
        FakePlaywrightWorkerClient workerClient = new FakePlaywrightWorkerClient();
        PlaywrightWorkerManager manager = new PlaywrightWorkerManager(() -> workerClient,
                new StubPlaywrightBrowserConfigResolver());

        PlaywrightWorkerClient firstClient = manager.getOrStart(context);
        PlaywrightWorkerClient secondClient = manager.getOrStart(context);

        assertThat(firstClient).isSameAs(workerClient);
        assertThat(secondClient).isSameAs(workerClient);
        assertThat(workerClient.startCount()).isEqualTo(1);
    }

    @Test
    void shouldCreateSessionHandleWithPlaywrightSessionMetadata() {
        TestExecutionContext context = new TestExecutionContext("playwright-session-handle");
        context.addTestState(TEST_CONTEXT.SCENARIO_LOG_DIRECTORY, "/tmp/playwright-session-handle");
        FakePlaywrightWorkerClient workerClient = new FakePlaywrightWorkerClient();
        PlaywrightWorkerManager manager = new PlaywrightWorkerManager(() -> workerClient,
                new StubPlaywrightBrowserConfigResolver());

        SessionHandle sessionHandle = manager.createSessionHandle("buyer", "chrome", Platform.web, context);

        assertThat(sessionHandle.userPersona()).isEqualTo("buyer");
        assertThat(sessionHandle.platform()).isEqualTo(Platform.web);
        assertThat(sessionHandle.engine()).isEqualTo(WebEngine.PLAYWRIGHT_TS.getConfigValue());
        assertThat(sessionHandle.sessionId()).isEqualTo("playwright-session-1");
        assertThat(sessionHandle.artifactPath()).isEqualTo("/tmp/playwright-session-handle");
        assertThat(sessionHandle.metadata()).containsEntry("browserName", "chrome");
        assertThat(sessionHandle.metadata()).containsEntry("contextId", "context-1");
        assertThat(sessionHandle.metadata()).containsEntry("pageId", "page-1");
        assertThat(sessionHandle.metadata()).containsEntry("workerSessionId", "playwright-session-1");
        assertThat(workerClient.lastBrowserConfig()).isNotNull();
        assertThat(workerClient.lastBrowserConfig().getBoolean("headless")).isTrue();
        assertThat(workerClient.lastBrowserConfig().getJSONArray("launchArgs").toList()).contains("--disable-gpu");
    }

    @Test
    void shouldCloseWorkerAndClearItFromContextOnShutdown() {
        TestExecutionContext context = new TestExecutionContext("playwright-shutdown");
        FakePlaywrightWorkerClient workerClient = new FakePlaywrightWorkerClient();
        PlaywrightWorkerManager manager = new PlaywrightWorkerManager(() -> workerClient,
                new StubPlaywrightBrowserConfigResolver());

        manager.getOrStart(context);
        manager.shutdown(context);

        assertThat(workerClient.closeCount()).isEqualTo(1);
        assertThat(context.getTestState(TEST_CONTEXT.PLAYWRIGHT_WORKER_CLIENT)).isNull();
    }

    private static class FakePlaywrightWorkerClient extends PlaywrightWorkerClient {
        private final AtomicInteger startCount = new AtomicInteger();
        private final AtomicInteger closeCount = new AtomicInteger();
        private boolean running;
        private JSONObject lastBrowserConfig;

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
        public synchronized PlaywrightWorkerSession createSession(String userPersona, String browserName) {
            return new PlaywrightWorkerSession("playwright-session-1", userPersona, browserName, "context-1",
                    "page-1");
        }

        @Override
        public synchronized PlaywrightWorkerSession createSession(String userPersona, String browserName,
                JSONObject browserConfig) {
            lastBrowserConfig = browserConfig;
            return createSession(userPersona, browserName);
        }

        @Override
        public synchronized void close() {
            closeCount.incrementAndGet();
            running = false;
        }

        int startCount() {
            return startCount.get();
        }

        int closeCount() {
            return closeCount.get();
        }

        JSONObject lastBrowserConfig() {
            return lastBrowserConfig;
        }
    }

    private static class StubPlaywrightBrowserConfigResolver extends PlaywrightBrowserConfigResolver {
        @Override
        PlaywrightBrowserConfig resolve(String browserName, TestExecutionContext context) {
            return new PlaywrightBrowserConfig(browserName, true, List.of("--disable-gpu"), null, null,
                    Map.of("ignoreHTTPSErrors", true), Map.of());
        }
    }
}
