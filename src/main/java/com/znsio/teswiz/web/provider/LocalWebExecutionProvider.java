package com.znsio.teswiz.web.provider;

import com.znsio.teswiz.session.SessionHandle;

import java.util.List;
import java.util.Optional;

public final class LocalWebExecutionProvider extends AbstractWebExecutionProvider {
    @Override
    public String name() {
        return "local";
    }

    @Override
    protected org.apache.logging.log4j.Logger log() {
        return org.apache.logging.log4j.LogManager.getLogger(LocalWebExecutionProvider.class.getName());
    }

    @Override
    protected String displayName() {
        return "Local";
    }

    @Override
    protected List<String> buildSessionNameCommands(String sessionName) {
        return List.of();
    }

    @Override
    protected List<String> buildSessionStatusCommands(String scenarioStatus, String scenarioFailureReasons) {
        return List.of();
    }

    @Override
    public Optional<String> buildReportMessage(SessionHandle sessionHandle) {
        return Optional.empty();
    }
}
