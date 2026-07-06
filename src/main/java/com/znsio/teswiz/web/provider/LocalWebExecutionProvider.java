package com.znsio.teswiz.web.provider;

import org.openqa.selenium.JavascriptExecutor;

public final class LocalWebExecutionProvider implements WebExecutionProvider {
    @Override
    public String name() {
        return "local";
    }

    @Override
    public void updateSessionName(JavascriptExecutor executor, String sessionName) {
    }

    @Override
    public void updateSessionStatus(JavascriptExecutor executor, String scenarioStatus, String scenarioFailureReasons) {
    }
}
