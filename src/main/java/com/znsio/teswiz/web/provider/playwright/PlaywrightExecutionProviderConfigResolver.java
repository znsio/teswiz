package com.znsio.teswiz.web.provider.playwright;

import com.znsio.teswiz.web.provider.WebExecutionProviderConfig;
import com.znsio.teswiz.web.provider.WebExecutionProviderConfigResolver;

public final class PlaywrightExecutionProviderConfigResolver {
    private final WebExecutionProviderConfigResolver delegate;

    public PlaywrightExecutionProviderConfigResolver() {
        this(new WebExecutionProviderConfigResolver());
    }

    PlaywrightExecutionProviderConfigResolver(WebExecutionProviderConfigResolver delegate) {
        this.delegate = delegate;
    }

    public PlaywrightExecutionProviderConfig resolve() {
        WebExecutionProviderConfig config = delegate.resolve();
        if ("local".equalsIgnoreCase(config.providerName())) {
            return PlaywrightExecutionProviderConfig.local();
        }
        return new PlaywrightExecutionProviderConfig(config.providerName(), config.remoteUrl(), config.apiUrl(),
                config.username(), config.accessKey(), config.webCapabilities());
    }
}
