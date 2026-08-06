package com.znsio.teswiz.mobile.provider;

import java.util.Optional;
import java.util.function.Supplier;
import io.appium.java_client.AppiumDriver;
import com.znsio.teswiz.runner.Driver;

public interface MobileExecutionProvider {
    String name();

    Optional<String> buildReportMessage(String sessionId, Supplier<Optional<String>> providerLinkSupplier);

    default Optional<String> getProviderReportLink(String sessionId, AppiumDriver driver) {
        return Optional.empty();
    }

    default void disableNotifications(Driver driver, String udid) {
        // Default no-op
    }
}
