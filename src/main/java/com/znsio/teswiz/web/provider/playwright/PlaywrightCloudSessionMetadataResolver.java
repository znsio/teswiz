package com.znsio.teswiz.web.provider.playwright;

import java.util.Map;

import org.openqa.selenium.JavascriptExecutor;

import com.znsio.teswiz.web.provider.WebCloudSessionMetadataResolver;

public final class PlaywrightCloudSessionMetadataResolver {
    private final WebCloudSessionMetadataResolver delegate = new WebCloudSessionMetadataResolver();

    public Map<String, String> resolve(JavascriptExecutor executor, String providerName) {
        return delegate.resolve(executor, providerName);
    }
}
