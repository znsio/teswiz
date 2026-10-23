package com.znsio.teswiz.web.provider;

import kong.unirest.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.znsio.teswiz.session.SessionHandle;

import java.util.List;
import java.util.Optional;

public final class BrowserStackWebExecutionProvider extends AbstractWebExecutionProvider {
    private static final Logger LOGGER = LogManager.getLogger(BrowserStackWebExecutionProvider.class.getName());

    @Override
    public String name() {
        return "browserstack";
    }

    @Override
    protected Logger log() {
        return LOGGER;
    }

    @Override
    protected String displayName() {
        return "BrowserStack";
    }

    @Override
    protected List<String> buildSessionNameCommands(String sessionName) {
        JSONObject executorObject = new JSONObject();
        JSONObject argumentsObject = new JSONObject();
        argumentsObject.put("name", sessionName);
        executorObject.put("action", "setSessionName");
        executorObject.put("arguments", argumentsObject);
        return List.of(String.format("browserstack_executor: %s", executorObject));
    }

    @Override
    protected List<String> buildSessionStatusCommands(String scenarioStatus, String scenarioFailureReasons) {
        JSONObject executorObject = new JSONObject();
        JSONObject argumentsObject = new JSONObject();
        argumentsObject.put("status", scenarioStatus);
        argumentsObject.put("reason", scenarioFailureReasons);
        executorObject.put("action", "setSessionStatus");
        executorObject.put("arguments", argumentsObject);
        return List.of(String.format("browserstack_executor: %s", executorObject));
    }

    @Override
    public Optional<String> buildReportMessage(SessionHandle sessionHandle) {
        String reportUrl = sessionHandle.metadata().get("providerReportUrl");
        if (null == reportUrl || reportUrl.isBlank()) {
            return Optional.empty();
        }
        return Optional.of("BrowserStack Report link available here: " + reportUrl);
    }
}
