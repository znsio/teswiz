package com.znsio.teswiz.web.provider;

import org.openqa.selenium.JavascriptExecutor;

public final class HeadSpinWebExecutionProvider implements WebExecutionProvider {
    @Override
    public String name() {
        return "headspin";
    }

    @Override
    public void updateSessionName(JavascriptExecutor executor, String sessionName) {
    }

    @Override
    public void updateSessionStatus(JavascriptExecutor executor, String scenarioStatus, String scenarioFailureReasons) {
    }
}
