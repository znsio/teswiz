package com.znsio.teswiz.web.provider;

import org.openqa.selenium.JavascriptExecutor;

import com.znsio.teswiz.session.SessionHandle;

import java.util.Optional;

public interface WebExecutionProvider {
    String name();

    void updateSessionName(JavascriptExecutor executor, String sessionName);

    void updateSessionStatus(JavascriptExecutor executor, String scenarioStatus, String scenarioFailureReasons);

    Optional<String> buildReportMessage(SessionHandle sessionHandle);
}
