package com.znsio.teswiz.web.provider;

import kong.unirest.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;

public final class BrowserStackWebExecutionProvider implements WebExecutionProvider {
    private static final Logger LOGGER = LogManager.getLogger(BrowserStackWebExecutionProvider.class.getName());

    @Override
    public String name() {
        return "browserstack";
    }

    @Override
    public void updateSessionName(JavascriptExecutor executor, String sessionName) {
        LOGGER.info("updateSessionName for BrowserStack: '{}'", sessionName);
        JSONObject executorObject = new JSONObject();
        JSONObject argumentsObject = new JSONObject();
        argumentsObject.put("name", sessionName);
        executorObject.put("action", "setSessionName");
        executorObject.put("arguments", argumentsObject);
        try {
            executor.executeScript(String.format("browserstack_executor: %s", executorObject));
        } catch (RuntimeException e) {
            LOGGER.warn("Unable to set BrowserStack session name using executor command: {}", e.getMessage());
        }
    }

    @Override
    public void updateSessionStatus(JavascriptExecutor executor, String scenarioStatus, String scenarioFailureReasons) {
        LOGGER.info("updateSessionStatus for BrowserStack: '{}'", scenarioStatus);
        JSONObject executorObject = new JSONObject();
        JSONObject argumentsObject = new JSONObject();
        argumentsObject.put("status", scenarioStatus);
        argumentsObject.put("reason", scenarioFailureReasons);
        executorObject.put("action", "setSessionStatus");
        executorObject.put("arguments", argumentsObject);
        try {
            executor.executeScript(String.format("browserstack_executor: %s", executorObject));
        } catch (RuntimeException e) {
            LOGGER.warn("Unable to set BrowserStack session status using executor command: {}", e.getMessage());
        }
    }
}
