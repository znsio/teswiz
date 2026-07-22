package com.znsio.teswiz.runner;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.znsio.teswiz.web.playwright.PlaywrightWorkerClient;
import com.znsio.teswiz.web.playwright.PlaywrightWorkerResponse;
import com.znsio.teswiz.web.playwright.PlaywrightWorkerSession;

class PlaywrightWorkerClientTest {
    private PlaywrightWorkerClient workerClient;

    @AfterEach
    void tearDown() {
        if (null != workerClient) {
            workerClient.close();
        }
    }

    @Test
    void shouldStartWorkerAndRespondToPing() {
        workerClient = new PlaywrightWorkerClient();

        workerClient.start();

        PlaywrightWorkerResponse response = workerClient.ping();
        assertThat(response.ok()).isTrue();
        assertThat(response.action()).isEqualTo("ping");
        assertThat(response.payload().getString("status")).isEqualTo("ok");
    }

    @Test
    void shouldCreateIsolatedSessionsForMultiplePersonas() {
        workerClient = new PlaywrightWorkerClient();
        workerClient.start();

        PlaywrightWorkerSession buyerSession = workerClient.createSession("buyer", "chrome");
        PlaywrightWorkerSession sellerSession = workerClient.createSession("seller", "chrome");

        assertThat(buyerSession.userPersona()).isEqualTo("buyer");
        assertThat(sellerSession.userPersona()).isEqualTo("seller");
        assertThat(buyerSession.sessionId()).isNotEqualTo(sellerSession.sessionId());
        assertThat(buyerSession.contextId()).isNotEqualTo(sellerSession.contextId());
        assertThat(buyerSession.pageId()).isNotEqualTo(sellerSession.pageId());
    }
}
