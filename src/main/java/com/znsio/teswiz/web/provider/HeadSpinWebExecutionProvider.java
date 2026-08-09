package com.znsio.teswiz.web.provider;

import com.znsio.teswiz.session.SessionHandle;

import java.util.List;
import java.util.Optional;

public final class HeadSpinWebExecutionProvider extends AbstractWebExecutionProvider {
    @Override
    public String name() {
        return "headspin";
    }

    @Override
    protected org.apache.logging.log4j.Logger log() {
        return org.apache.logging.log4j.LogManager.getLogger(HeadSpinWebExecutionProvider.class.getName());
    }

    @Override
    protected String displayName() {
        return "HeadSpin";
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
