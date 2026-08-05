package com.znsio.teswiz.web.provider;

import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;

import com.znsio.teswiz.session.SessionHandle;

abstract class AbstractWebExecutionProvider implements WebExecutionProvider {
    @Override
    public final void updateSessionName(JavascriptExecutor executor, String sessionName) {
        log().info("updateSessionName for {}: '{}'", displayName(), sessionName);
        executeCommands(executor, buildSessionNameCommands(sessionName), "session name");
    }

    @Override
    public final void updateSessionStatus(JavascriptExecutor executor, String scenarioStatus,
            String scenarioFailureReasons) {
        log().info("updateSessionStatus for {}: '{}'", displayName(), scenarioStatus);
        executeCommands(executor, buildSessionStatusCommands(scenarioStatus, scenarioFailureReasons), "session status");
    }

    @Override
    public abstract Optional<String> buildReportMessage(SessionHandle sessionHandle);

    protected abstract Logger log();

    protected abstract String displayName();

    protected abstract List<String> buildSessionNameCommands(String sessionName);

    protected abstract List<String> buildSessionStatusCommands(String scenarioStatus, String scenarioFailureReasons);

    private void executeCommands(JavascriptExecutor executor, List<String> commands, String actionName) {
        for (String command : commands) {
            try {
                executor.executeScript(command);
            } catch (RuntimeException e) {
                log().warn("Unable to set {} using {} executor command: {}", actionName, displayName(),
                        e.getMessage());
            }
        }
    }
}
