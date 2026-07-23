package com.znsio.teswiz.runner;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.json.JSONArray;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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

    @Test
    void shouldInvokeTypeScriptScreenActionsAgainstCurrentPage(@TempDir Path tempDir) throws IOException {
        workerClient = new PlaywrightWorkerClient();
        workerClient.start();

        PlaywrightWorkerSession session = workerClient.createSession("buyer", "chrome");
        Path pagePath = writeTheAppLikePage(tempDir);
        workerClient.navigateTo(session.sessionId(), pagePath.toUri().toString());

        workerClient.invokeScreenAction(session.sessionId(), "theapp/app-launch.screen.ts", "selectLogin",
                new JSONArray());
        workerClient.invokeScreenAction(session.sessionId(), "theapp/login.screen.ts", "enterLoginDetails",
                new JSONArray().put("znsio1").put("invalid password"));
        workerClient.invokeScreenAction(session.sessionId(), "theapp/login.screen.ts", "login", new JSONArray());

        Object invalidLoginError = workerClient.invokeScreenAction(session.sessionId(), "theapp/login.screen.ts",
                "getInvalidLoginError", new JSONArray());

        assertThat(invalidLoginError).isEqualTo("Your username is invalid!");
    }

    private Path writeTheAppLikePage(Path tempDir) throws IOException {
        Path page = tempDir.resolve("theapp-login.html");
        String html = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8" />
                  <title>The App Fixture</title>
                </head>
                <body>
                  <a href="#login-form" id="login-link">Form Authentication</a>
                  <section id="login-form">
                    <input id="username" type="text" />
                    <input id="password" type="password" />
                    <button id="login-button" type="button">Login</button>
                    <div id="flash"></div>
                  </section>
                  <script>
                    document.getElementById("login-button").addEventListener("click", function () {
                      document.getElementById("flash").textContent = "Your username is invalid!";
                    });
                  </script>
                </body>
                </html>
                """;
        Files.writeString(page, html);
        return page;
    }
}
