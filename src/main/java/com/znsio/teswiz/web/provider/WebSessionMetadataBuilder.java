package com.znsio.teswiz.web.provider;

import java.util.LinkedHashMap;
import java.util.Map;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

public final class WebSessionMetadataBuilder {
    private final WebExecutionProviderResolver providerResolver;
    private final WebCloudSessionMetadataResolver cloudSessionMetadataResolver;

    public WebSessionMetadataBuilder() {
        this(new WebExecutionProviderResolver(), new WebCloudSessionMetadataResolver());
    }

    WebSessionMetadataBuilder(WebExecutionProviderResolver providerResolver,
            WebCloudSessionMetadataResolver cloudSessionMetadataResolver) {
        this.providerResolver = providerResolver;
        this.cloudSessionMetadataResolver = cloudSessionMetadataResolver;
    }

    public Map<String, String> build(String browserName, WebDriver webDriver) {
        WebExecutionProvider provider = providerResolver.resolve();
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("browserName", browserName);
        metadata.put("provider", provider.name());
        metadata.put("driverClass", webDriver.getClass().getSimpleName());
        if (!(webDriver instanceof RemoteWebDriver remoteWebDriver)) {
            return metadata;
        }
        metadata.put("remoteSessionId", remoteWebDriver.getSessionId().toString());
        if (webDriver instanceof org.openqa.selenium.JavascriptExecutor executor) {
            metadata.putAll(cloudSessionMetadataResolver.resolve(executor, provider.name()));
        }
        return metadata;
    }
}
