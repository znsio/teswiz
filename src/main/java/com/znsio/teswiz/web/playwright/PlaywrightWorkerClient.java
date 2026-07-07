package com.znsio.teswiz.web.playwright;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.time.ZoneOffset;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.znsio.teswiz.exceptions.CommandLineExecutorException;
import com.znsio.teswiz.exceptions.InvalidTestDataException;
import com.znsio.teswiz.tools.cmd.CommandLineExecutor;

public class PlaywrightWorkerClient implements AutoCloseable {
    private static final Logger LOGGER = LogManager.getLogger(PlaywrightWorkerClient.class.getName());
    private static final String NODE_EXECUTABLE = "node";

    private final Path workerScriptPath;
    private Process process;
    private BufferedWriter requestWriter;
    private BufferedReader responseReader;

    public PlaywrightWorkerClient() {
        this(Paths.get("playwright", "worker.mjs").toAbsolutePath());
    }

    public PlaywrightWorkerClient(Path workerScriptPath) {
        this.workerScriptPath = workerScriptPath;
    }

    public synchronized void start() {
        if (isRunning()) {
            return;
        }
        if (!CommandLineExecutor.isCommandAvailable(NODE_EXECUTABLE)) {
            throw new InvalidTestDataException("Node.js is required to start the Playwright worker");
        }
        if (!workerScriptPath.toFile().exists()) {
            throw new InvalidTestDataException(
                    String.format("Playwright worker script not found: '%s'", workerScriptPath));
        }

        ProcessBuilder processBuilder = new ProcessBuilder(List.of(NODE_EXECUTABLE, workerScriptPath.toString()));
        try {
            process = processBuilder.start();
            requestWriter = process.outputWriter();
            responseReader = process.inputReader();
        } catch (IOException e) {
            throw new CommandLineExecutorException("Unable to start Playwright worker", e);
        }
    }

    public synchronized boolean isRunning() {
        return null != process && process.isAlive();
    }

    public synchronized PlaywrightWorkerResponse ping() {
        return sendCommand("ping", new JSONObject());
    }

    public synchronized PlaywrightWorkerSession createSession(String userPersona, String browserName) {
        return createSession(userPersona, browserName, new JSONObject());
    }

    public synchronized PlaywrightWorkerSession createSession(String userPersona, String browserName, Path artifactPath) {
        return createSession(userPersona, browserName, new JSONObject(), artifactPath);
    }

    public synchronized PlaywrightWorkerSession createSession(String userPersona, String browserName,
            JSONObject browserConfig) {
        JSONObject payload = new JSONObject()
                .put("userPersona", userPersona)
                .put("browserName", browserName)
                .put("browserConfig", browserConfig);
        return createSession(payload);
    }

    public synchronized PlaywrightWorkerSession createSession(String userPersona, String browserName,
            JSONObject browserConfig, Path artifactPath) {
        JSONObject payload = new JSONObject()
                .put("userPersona", userPersona)
                .put("browserName", browserName)
                .put("browserConfig", browserConfig);
        if (null != artifactPath) {
            payload.put("artifactPath", artifactPath.toAbsolutePath().toString());
        }
        return createSession(payload);
    }

    private PlaywrightWorkerSession createSession(JSONObject payload) {
        PlaywrightWorkerResponse response = sendCommand("createSession", payload);
        JSONObject sessionPayload = response.payload();
        return new PlaywrightWorkerSession(
                sessionPayload.getString("sessionId"),
                sessionPayload.getString("userPersona"),
                sessionPayload.getString("browserName"),
                sessionPayload.getString("contextId"),
                sessionPayload.getString("pageId"));
    }

    public synchronized void navigateTo(String sessionId, String url) {
        sendCommand("navigateTo", new JSONObject().put("sessionId", sessionId).put("url", url));
    }

    public synchronized String getCurrentUrl(String sessionId) {
        return sendCommand("getCurrentUrl", new JSONObject().put("sessionId", sessionId))
                .payload().getString("url");
    }

    public synchronized String getTitle(String sessionId) {
        return sendCommand("getTitle", new JSONObject().put("sessionId", sessionId))
                .payload().getString("title");
    }

    public synchronized String getPageSource(String sessionId) {
        return sendCommand("getPageSource", new JSONObject().put("sessionId", sessionId))
                .payload().getString("content");
    }

    public synchronized Set<String> getWindowHandles(String sessionId) {
        org.json.JSONArray handles = sendCommand("getWindowHandles", new JSONObject().put("sessionId", sessionId))
                .payload().getJSONArray("handles");
        Set<String> values = new LinkedHashSet<>();
        for (int index = 0; index < handles.length(); index++) {
            values.add(handles.getString(index));
        }
        return values;
    }

    public synchronized String getWindowHandle(String sessionId) {
        return sendCommand("getWindowHandle", new JSONObject().put("sessionId", sessionId))
                .payload().getString("handle");
    }

    public synchronized String switchToWindow(String sessionId, String handle) {
        return sendCommand("switchToWindow", new JSONObject().put("sessionId", sessionId).put("handle", handle))
                .payload().getString("handle");
    }

    public synchronized String openNewWindow(String sessionId, String windowType) {
        return sendCommand("openNewWindow", new JSONObject()
                .put("sessionId", sessionId)
                .put("windowType", windowType))
                .payload().getString("handle");
    }

    public synchronized void switchToFrame(String sessionId, String nameOrId) {
        sendCommand("switchToFrame", new JSONObject()
                .put("sessionId", sessionId)
                .put("nameOrId", nameOrId));
    }

    public synchronized void switchToFrame(String sessionId, int index) {
        sendCommand("switchToFrameByIndex", new JSONObject()
                .put("sessionId", sessionId)
                .put("index", index));
    }

    public synchronized void switchToFrame(String sessionId, PlaywrightLocatorReference locatorReference) {
        sendCommand("switchToFrameElement", new JSONObject()
                .put("sessionId", sessionId)
                .put("locator", locatorReference.toJson()));
    }

    public synchronized void switchToDefaultContent(String sessionId) {
        sendCommand("switchToDefaultContent", new JSONObject().put("sessionId", sessionId));
    }

    public synchronized void switchToParentFrame(String sessionId) {
        sendCommand("switchToParentFrame", new JSONObject().put("sessionId", sessionId));
    }

    public synchronized org.openqa.selenium.Dimension getWindowSize(String sessionId) {
        JSONObject payload = sendCommand("getWindowSize", new JSONObject().put("sessionId", sessionId))
                .payload();
        return new org.openqa.selenium.Dimension(payload.getInt("width"), payload.getInt("height"));
    }

    public synchronized org.openqa.selenium.Point getWindowPosition(String sessionId) {
        JSONObject payload = sendCommand("getWindowPosition", new JSONObject().put("sessionId", sessionId))
                .payload();
        return new org.openqa.selenium.Point(payload.getInt("x"), payload.getInt("y"));
    }

    public synchronized void setWindowSize(String sessionId, org.openqa.selenium.Dimension size) {
        sendCommand("setWindowSize", new JSONObject()
                .put("sessionId", sessionId)
                .put("width", size.getWidth())
                .put("height", size.getHeight()));
    }

    public synchronized void maximizeWindow(String sessionId) {
        sendCommand("maximizeWindow", new JSONObject().put("sessionId", sessionId));
    }

    public synchronized void setNavigationTimeout(String sessionId, Duration timeout) {
        sendCommand("setNavigationTimeout", new JSONObject()
                .put("sessionId", sessionId)
                .put("timeoutMs", timeout.toMillis()));
    }

    public synchronized void addCookie(String sessionId, org.openqa.selenium.Cookie cookie, String currentUrl) {
        JSONObject payload = new JSONObject()
                .put("sessionId", sessionId)
                .put("cookie", toJson(cookie, currentUrl));
        sendCommand("addCookie", payload);
    }

    public synchronized void deleteCookieNamed(String sessionId, String name) {
        sendCommand("deleteCookieNamed", new JSONObject()
                .put("sessionId", sessionId)
                .put("name", name));
    }

    public synchronized void deleteAllCookies(String sessionId) {
        sendCommand("deleteAllCookies", new JSONObject().put("sessionId", sessionId));
    }

    public synchronized List<JSONObject> getCookies(String sessionId) {
        JSONArray cookies = sendCommand("getCookies", new JSONObject().put("sessionId", sessionId))
                .payload().getJSONArray("cookies");
        java.util.ArrayList<JSONObject> values = new java.util.ArrayList<>();
        for (int index = 0; index < cookies.length(); index++) {
            values.add(cookies.getJSONObject(index));
        }
        return values;
    }

    public synchronized JSONObject getAlert(String sessionId) {
        return sendCommand("getAlert", new JSONObject().put("sessionId", sessionId)).payload();
    }

    public synchronized void acceptAlert(String sessionId, String text) {
        JSONObject payload = new JSONObject().put("sessionId", sessionId);
        if (null != text) {
            payload.put("text", text);
        }
        sendCommand("acceptAlert", payload);
    }

    public synchronized void dismissAlert(String sessionId) {
        sendCommand("dismissAlert", new JSONObject().put("sessionId", sessionId));
    }

    public synchronized String captureScreenshot(String sessionId) {
        return sendCommand("screenshot", new JSONObject().put("sessionId", sessionId))
                .payload().getString("base64");
    }

    public synchronized List<JSONObject> getConsoleLogs(String sessionId) {
        JSONArray logs = sendCommand("getConsoleLogs", new JSONObject().put("sessionId", sessionId))
                .payload().getJSONArray("entries");
        List<JSONObject> values = new ArrayList<>();
        for (int index = 0; index < logs.length(); index++) {
            values.add(logs.getJSONObject(index));
        }
        return values;
    }

    public synchronized int countElements(String sessionId, PlaywrightLocatorReference locatorReference) {
        return countElements(sessionId, locatorReference, Duration.ZERO);
    }

    public synchronized int countElements(String sessionId, PlaywrightLocatorReference locatorReference, Duration timeout) {
        return sendCommand("countElements", new JSONObject().put("sessionId", sessionId)
                .put("locator", locatorReference.toJson())
                .put("timeoutMs", Math.max(0L, timeout.toMillis()))).payload().getInt("count");
    }

    public synchronized void click(String sessionId, PlaywrightLocatorReference locatorReference) {
        sendElementCommand(sessionId, locatorReference, "click", null);
    }

    public synchronized void type(String sessionId, PlaywrightLocatorReference locatorReference, String value) {
        sendElementCommand(sessionId, locatorReference, "type", value);
    }

    public synchronized void clear(String sessionId, PlaywrightLocatorReference locatorReference) {
        sendElementCommand(sessionId, locatorReference, "clear", null);
    }

    public synchronized String getText(String sessionId, PlaywrightLocatorReference locatorReference) {
        return sendElementCommand(sessionId, locatorReference, "getText", null).payload().optString("value");
    }

    public synchronized String getTagName(String sessionId, PlaywrightLocatorReference locatorReference) {
        return sendElementCommand(sessionId, locatorReference, "getTagName", null).payload().optString("value");
    }

    public synchronized String getAttribute(String sessionId, PlaywrightLocatorReference locatorReference,
            String attributeName) {
        return sendElementCommand(sessionId, locatorReference, "getAttribute", attributeName)
                .payload().optString("value", null);
    }

    public synchronized boolean isVisible(String sessionId, PlaywrightLocatorReference locatorReference) {
        return sendElementCommand(sessionId, locatorReference, "isVisible", null).payload().getBoolean("value");
    }

    public synchronized boolean isEnabled(String sessionId, PlaywrightLocatorReference locatorReference) {
        return sendElementCommand(sessionId, locatorReference, "isEnabled", null).payload().getBoolean("value");
    }

    public synchronized boolean isSelected(String sessionId, PlaywrightLocatorReference locatorReference) {
        return sendElementCommand(sessionId, locatorReference, "isSelected", null).payload().getBoolean("value");
    }

    public synchronized String getCssValue(String sessionId, PlaywrightLocatorReference locatorReference,
            String propertyName) {
        return sendElementCommand(sessionId, locatorReference, "getCssValue", propertyName)
                .payload().optString("value", "");
    }

    public synchronized org.openqa.selenium.Rectangle getElementRect(String sessionId,
            PlaywrightLocatorReference locatorReference) {
        JSONObject payload = sendElementCommand(sessionId, locatorReference, "getBoundingBox", null)
                .payload().getJSONObject("value");
        return new org.openqa.selenium.Rectangle(
                payload.getInt("x"),
                payload.getInt("y"),
                payload.getInt("width"),
                payload.getInt("height"));
    }

    public synchronized Object executeScript(String sessionId, String script) {
        return executeScript(sessionId, script, new JSONArray());
    }

    public synchronized Object executeScript(String sessionId, String script, JSONArray args) {
        JSONObject payload = sendCommand("executeScript", new JSONObject().put("sessionId", sessionId)
                .put("script", script)
                .put("args", args)).payload();
        return payload.has("value") ? payload.get("value") : null;
    }

    public synchronized void closeSession(String sessionId) {
        sendCommand("closeSession", new JSONObject().put("sessionId", sessionId));
    }

    public synchronized void goBack(String sessionId) {
        sendCommand("goBack", new JSONObject().put("sessionId", sessionId));
    }

    public synchronized void goForward(String sessionId) {
        sendCommand("goForward", new JSONObject().put("sessionId", sessionId));
    }

    public synchronized void refresh(String sessionId) {
        sendCommand("refresh", new JSONObject().put("sessionId", sessionId));
    }

    public synchronized PlaywrightWorkerResponse shutdown() {
        if (!isRunning()) {
            return new PlaywrightWorkerResponse("shutdown-skipped", "shutdown", true,
                    new JSONObject().put("status", "not-running"));
        }
        PlaywrightWorkerResponse response = sendCommand("shutdown", new JSONObject());
        closeResources();
        return response;
    }

    private PlaywrightWorkerResponse sendCommand(String action, JSONObject payload) {
        if (!isRunning()) {
            throw new InvalidTestDataException("Playwright worker is not running");
        }

        String requestId = UUID.randomUUID().toString();
        JSONObject request = new JSONObject()
                .put("requestId", requestId)
                .put("action", action)
                .put("payload", payload);
        try {
            requestWriter.write(request.toString());
            requestWriter.newLine();
            requestWriter.flush();

            String line = responseReader.readLine();
            if (null == line) {
                throw new InvalidTestDataException(
                        String.format("Playwright worker closed unexpectedly while handling '%s'", action));
            }
            JSONObject responseJson = new JSONObject(line);
            PlaywrightWorkerResponse response = new PlaywrightWorkerResponse(
                    responseJson.getString("requestId"),
                    responseJson.getString("action"),
                    responseJson.getBoolean("ok"),
                    responseJson.getJSONObject("payload"));
            if (!response.ok()) {
                throw new InvalidTestDataException(String.format("Playwright worker action '%s' failed: %s",
                        action, response.payload().optString("message")));
            }
            return response;
        } catch (IOException e) {
            throw new CommandLineExecutorException(
                    String.format("Unable to communicate with Playwright worker for action '%s'", action), e);
        }
    }

    private JSONObject toJson(org.openqa.selenium.Cookie cookie, String currentUrl) {
        JSONObject json = new JSONObject()
                .put("name", cookie.getName())
                .put("value", cookie.getValue())
                .put("secure", cookie.isSecure())
                .put("httpOnly", cookie.isHttpOnly());
        if (null != currentUrl && !currentUrl.isBlank()) {
            json.put("url", currentUrl);
        } else {
            json.put("path", null == cookie.getPath() ? "/" : cookie.getPath());
            if (null != cookie.getDomain() && !cookie.getDomain().isBlank()) {
                json.put("domain", cookie.getDomain());
            }
        }
        if (null != cookie.getExpiry()) {
            json.put("expires", cookie.getExpiry().toInstant().atOffset(ZoneOffset.UTC).toEpochSecond());
        }
        if (null != cookie.getSameSite() && !cookie.getSameSite().isBlank()) {
            json.put("sameSite", cookie.getSameSite());
        }
        return json;
    }

    private PlaywrightWorkerResponse sendElementCommand(String sessionId, PlaywrightLocatorReference locatorReference,
            String action, String value) {
        JSONObject payload = new JSONObject()
                .put("sessionId", sessionId)
                .put("locator", locatorReference.toJson())
                .put("elementAction", action);
        if (null != value) {
            payload.put("value", value);
        }
        return sendCommand("elementAction", payload);
    }

    @Override
    public synchronized void close() {
        if (isRunning()) {
            try {
                shutdown();
                return;
            } catch (RuntimeException e) {
                LOGGER.warn("Unable to shutdown Playwright worker cleanly: {}", e.getMessage());
            }
        }
        closeResources();
    }

    private void closeResources() {
        try {
            if (null != requestWriter) {
                requestWriter.close();
            }
        } catch (IOException ignored) {
        }
        try {
            if (null != responseReader) {
                responseReader.close();
            }
        } catch (IOException ignored) {
        }
        if (null != process && process.isAlive()) {
            process.destroy();
        }
        process = null;
        requestWriter = null;
        responseReader = null;
    }
}
