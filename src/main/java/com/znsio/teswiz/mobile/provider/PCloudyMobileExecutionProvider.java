package com.znsio.teswiz.mobile.provider;

import java.util.Optional;
import java.util.function.Supplier;
import io.appium.java_client.AppiumDriver;
import com.znsio.teswiz.runner.Driver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class PCloudyMobileExecutionProvider implements MobileExecutionProvider {
    private static final Logger LOGGER = LogManager.getLogger(PCloudyMobileExecutionProvider.class.getName());

    @Override
    public String name() {
        return "pCloudy";
    }

    @Override
    public Optional<String> buildReportMessage(String sessionId, Supplier<Optional<String>> providerLinkSupplier) {
        return providerLinkSupplier.get().map(link -> "pCloudy Report link available here: " + link);
    }

    @Override
    public Optional<String> getProviderReportLink(String sessionId, AppiumDriver driver) {
        return Optional.ofNullable((String) driver.executeScript("pCloudy_getReportLink"));
    }

    @Override
    public void disableNotifications(Driver driver, String udid) {
        java.util.Map<String, Object> adbCommand = new java.util.HashMap<>();
        adbCommand.put("command", "settings put global heads_up_notifications_enabled 0");
        Object result = ((AppiumDriver) driver.getInnerDriver()).executeScript(
                "pCloudy_executeAdbCommand", adbCommand);
        LOGGER.info("@disableNotificationsCommandResponse: " + result);
    }
}
