package com.znsio.teswiz.runner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    PlaywrightWorkerClient(Path workerScriptPath) {
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
        JSONObject payload = new JSONObject()
                .put("userPersona", userPersona)
                .put("browserName", browserName);
        PlaywrightWorkerResponse response = sendCommand("createSession", payload);
        JSONObject sessionPayload = response.payload();
        return new PlaywrightWorkerSession(
                sessionPayload.getString("sessionId"),
                sessionPayload.getString("userPersona"),
                sessionPayload.getString("browserName"),
                sessionPayload.getString("contextId"),
                sessionPayload.getString("pageId"));
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
