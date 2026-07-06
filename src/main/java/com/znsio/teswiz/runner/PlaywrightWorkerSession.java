package com.znsio.teswiz.runner;

public record PlaywrightWorkerSession(
        String sessionId,
        String userPersona,
        String browserName,
        String contextId,
        String pageId) {
}
