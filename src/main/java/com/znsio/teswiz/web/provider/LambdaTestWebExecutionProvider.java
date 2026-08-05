package com.znsio.teswiz.web.provider;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.znsio.teswiz.session.SessionHandle;

import java.util.List;
import java.util.Optional;

public final class LambdaTestWebExecutionProvider extends AbstractWebExecutionProvider {
    private static final Logger LOGGER = LogManager.getLogger(LambdaTestWebExecutionProvider.class.getName());

    @Override
    public String name() {
        return "lambdatest";
    }

    @Override
    protected Logger log() {
        return LOGGER;
    }

    @Override
    protected String displayName() {
        return "LambdaTest";
    }

    @Override
    protected List<String> buildSessionNameCommands(String sessionName) {
        return List.of(String.format("lambda-name=%s", sessionName));
    }

    @Override
    protected List<String> buildSessionStatusCommands(String scenarioStatus, String scenarioFailureReasons) {
        String sanitizedFailureReason = scenarioFailureReasons.replace("\n", " ").replace("\r", " ");
        return List.of(
                String.format("lambda-status=%s", scenarioStatus),
                String.format("lambda-comment=%s", sanitizedFailureReason));
    }

    @Override
    public Optional<String> buildReportMessage(SessionHandle sessionHandle) {
        String reportUrl = sessionHandle.metadata().get("providerReportUrl");
        if (null == reportUrl || reportUrl.isBlank()) {
            String providerSessionId = sessionHandle.metadata().get("providerSessionId");
            if (null != providerSessionId && !providerSessionId.isBlank()) {
                reportUrl = "https://automation.lambdatest.com/logs/?sessionID=" + providerSessionId;
            }
        }
        if (null == reportUrl || reportUrl.isBlank()) {
            return Optional.empty();
        }
        return Optional.of("LambdaTest Report link available here: " + reportUrl);
    }
}
