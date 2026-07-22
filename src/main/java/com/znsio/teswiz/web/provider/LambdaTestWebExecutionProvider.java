package com.znsio.teswiz.web.provider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;

public final class LambdaTestWebExecutionProvider implements WebExecutionProvider {
    private static final Logger LOGGER = LogManager.getLogger(LambdaTestWebExecutionProvider.class.getName());

    @Override
    public String name() {
        return "lambdatest";
    }

    @Override
    public void updateSessionName(JavascriptExecutor executor, String sessionName) {
        LOGGER.info("updateSessionName for LambdaTest: '{}'", sessionName);
        try {
            executor.executeScript(String.format("lambda-name=%s", sessionName));
        } catch (RuntimeException e) {
            LOGGER.warn("Unable to set LambdaTest session name using executor command: {}", e.getMessage());
        }
    }

    @Override
    public void updateSessionStatus(JavascriptExecutor executor, String scenarioStatus, String scenarioFailureReasons) {
        LOGGER.info("updateSessionStatus for LambdaTest: '{}'", scenarioStatus);
        String sanitizedFailureReason = scenarioFailureReasons.replace("\n", " ").replace("\r", " ");
        try {
            executor.executeScript(String.format("lambda-status=%s", scenarioStatus));
            executor.executeScript(String.format("lambda-comment=%s", sanitizedFailureReason));
        } catch (RuntimeException e) {
            LOGGER.warn("Unable to set LambdaTest session status using executor command: {}", e.getMessage());
        }
    }
}
