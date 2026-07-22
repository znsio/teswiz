package com.znsio.teswiz.runner;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.znsio.teswiz.web.playwright.PlaywrightWorkerClient;
import com.znsio.teswiz.web.playwright.PlaywrightWorkerSession;

class PlaywrightWorkerArtifactsTest {
    private PlaywrightWorkerClient workerClient;

    @AfterEach
    void tearDown() {
        if (null != workerClient) {
            workerClient.close();
        }
    }

    @Test
    void shouldWriteTraceConsoleAndHarArtifactsWhenSessionCloses() throws Exception {
        workerClient = new PlaywrightWorkerClient();
        workerClient.start();
        Path artifactDir = Files.createTempDirectory("playwright-worker-artifacts");

        PlaywrightWorkerSession session = workerClient.createSession("buyer", "chrome", artifactDir);
        workerClient.navigateTo(session.sessionId(),
                "data:text/html,<html><body><script>console.log('buyer-ready')</script><h1>Ready</h1></body></html>");

        workerClient.closeSession(session.sessionId());

        Path traceFile = artifactDir.resolve("buyer-" + session.sessionId() + "-trace.zip");
        Path harFile = artifactDir.resolve("buyer-" + session.sessionId() + "-network.har");
        Path consoleFile = artifactDir.resolve("buyer-" + session.sessionId() + "-console.log");

        assertThat(traceFile).exists();
        assertThat(harFile).exists();
        assertThat(consoleFile).exists();
        assertThat(Files.readString(consoleFile)).contains("buyer-ready");
    }
}
