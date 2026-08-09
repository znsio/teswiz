package com.znsio.teswiz.mobile.provider;

import java.util.Optional;
import java.util.function.Supplier;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.tools.cmd.CommandLineExecutor;
import com.znsio.teswiz.tools.cmd.CommandLineResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class LocalMobileExecutionProvider implements MobileExecutionProvider {
    private static final Logger LOGGER = LogManager.getLogger(LocalMobileExecutionProvider.class.getName());

    @Override
    public String name() {
        return "local";
    }

    @Override
    public Optional<String> buildReportMessage(String sessionId, Supplier<Optional<String>> providerLinkSupplier) {
        return Optional.empty();
    }

    @Override
    public void disableNotifications(Driver driver, String udid) {
        String[] disableNotificationsCommand = new String[] { "adb", "-s", udid, "shell", "settings", "put", "global",
                "heads_up_notifications_enabled", "0" };
        CommandLineResponse disableNotificationsCommandResponse = CommandLineExecutor
                .execCommand(disableNotificationsCommand);
        LOGGER.info(String.format("disableNotificationsCommandResponse: %s", disableNotificationsCommandResponse));
    }
}
