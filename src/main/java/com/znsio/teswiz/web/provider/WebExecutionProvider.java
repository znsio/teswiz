package com.znsio.teswiz.web.provider;

import org.openqa.selenium.JavascriptExecutor;

public interface WebExecutionProvider {
    String name();

    void updateSessionName(JavascriptExecutor executor, String sessionName);

    void updateSessionStatus(JavascriptExecutor executor, String scenarioStatus, String scenarioFailureReasons);
}
