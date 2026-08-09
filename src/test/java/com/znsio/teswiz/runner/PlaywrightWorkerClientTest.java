package com.znsio.teswiz.runner;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.applitools.eyes.MatchLevel;
import com.znsio.teswiz.web.playwright.PlaywrightWorkerClient;
import com.znsio.teswiz.web.playwright.PlaywrightWorkerResponse;
import com.znsio.teswiz.web.playwright.PlaywrightWorkerSession;
import com.znsio.teswiz.visual.PlaywrightVisualSessionRequest;

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
    void shouldCreateIsolatedSessionsForMultiplePersonas(@TempDir Path tempDir) {
        workerClient = new PlaywrightWorkerClient();
        workerClient.start();

        PlaywrightWorkerSession buyerSession = workerClient.createSession("buyer", "chrome", tempDir);
        PlaywrightWorkerSession sellerSession = workerClient.createSession("seller", "chrome", tempDir);

        assertThat(buyerSession.userPersona()).isEqualTo("buyer");
        assertThat(sellerSession.userPersona()).isEqualTo("seller");
        assertThat(buyerSession.sessionId()).isNotEqualTo(sellerSession.sessionId());
        assertThat(buyerSession.contextId()).isNotEqualTo(sellerSession.contextId());
        assertThat(buyerSession.pageId()).isNotEqualTo(sellerSession.pageId());
        assertThat(buyerSession.traceFile()).endsWith("-trace.zip");
        assertThat(buyerSession.harFile()).endsWith("-network.har");
        assertThat(buyerSession.consoleFile()).endsWith("-console.log");
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

    @Test
    void shouldAcceptCloudControlScriptsWithoutTreatingThemAsRegularJavaScript() {
        workerClient = new PlaywrightWorkerClient();
        workerClient.start();

        PlaywrightWorkerSession session = workerClient.createSession("buyer", "chrome");

        assertThat(workerClient.executeScript(session.sessionId(),
                "browserstack_executor: {\"action\":\"setSessionStatus\",\"arguments\":{\"status\":\"passed\",\"reason\":\"ok\"}}"))
                        .isIn(null, JSONObject.NULL);

        assertThat(workerClient.executeScript(session.sessionId(), "lambda-status=failed")).isEqualTo("failed");
        assertThat(workerClient.executeScript(session.sessionId(), "lambda-comment=Assertion failure"))
                .isIn(null, JSONObject.NULL);
        assertThat(workerClient.executeScript(session.sessionId(), "lambda-name=buyer-test"))
                .isIn(null, JSONObject.NULL);
    }

    @Test
    void shouldOpenDisabledVisualSessionThroughWorker(@TempDir Path tempDir) throws IOException {
        shouldOpenDisabledVisualSessionThroughWorker(tempDir, false);
    }

    @Test
    void shouldOpenDisabledUfgVisualSessionThroughWorker(@TempDir Path tempDir) throws IOException {
        shouldOpenDisabledVisualSessionThroughWorker(tempDir, true);
    }

    private void shouldOpenDisabledVisualSessionThroughWorker(Path tempDir, boolean useUfg) throws IOException {
        workerClient = new PlaywrightWorkerClient();
        workerClient.start();

        PlaywrightWorkerSession session = workerClient.createSession("buyer", "chrome");
        Path pagePath = writeTheAppLikePage(tempDir);
        workerClient.navigateTo(session.sessionId(), pagePath.toUri().toString());

        workerClient.openVisualSession(session.sessionId(), new PlaywrightVisualSessionRequest(
                "theapp-web-playwright-ts",
                "worker-visual-session-playwright-ts",
                "https://eyes.applitools.com/",
                "dummykey",
                "local-branch",
                "local",
                null,
                MatchLevel.STRICT,
                true,
                false,
                false,
                useUfg,
                1,
                null,
                tempDir.resolve("applitools-playwright-ts.log").toString(),
                new com.applitools.eyes.RectangleSize(1280, 720),
                new PlaywrightVisualSessionRequest.BatchMetadata("teswiz-local-web-playwright-ts",
                        "batch-local-playwright-ts", java.util.Map.of("WEB_ENGINE", "playwright-ts")),
                java.util.Map.of("WEB_ENGINE", "playwright-ts", "BROWSER_NAME", "chrome"),
                useUfg
                        ? java.util.List.of(
                                new PlaywrightVisualSessionRequest.UfgTarget(1920, 1024, "CHROME", null, null),
                                new PlaywrightVisualSessionRequest.UfgTarget(null, null, null, "iPhone 15 Pro",
                                        "PORTRAIT"))
                        : java.util.List.of()));

        assertThat(workerClient.isVisualSessionDisabled(session.sessionId())).isTrue();
        assertThat(workerClient.closeVisualSession(session.sessionId())).isNull();
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
