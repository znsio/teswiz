package com.znsio.teswiz.runner;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import com.znsio.teswiz.entities.Platform;

public record SessionHandle(
        String userPersona,
        Platform platform,
        String engine,
        String sessionId,
        String artifactPath,
        Map<String, String> metadata) {

    public SessionHandle {
        metadata = null == metadata ? Collections.emptyMap() : Collections.unmodifiableMap(metadata);
    }

    static SessionHandle create(String userPersona, Platform platform, String engine, String artifactPath,
            Map<String, String> metadata) {
        return new SessionHandle(userPersona, platform, engine, UUID.randomUUID().toString(), artifactPath, metadata);
    }
}
