package com.znsio.teswiz.web.playwright;

public record PlaywrightWorkerSession(
        String sessionId,
        String userPersona,
        String browserName,
        String contextId,
        String pageId) {
}
