package com.znsio.teswiz.mobile.provider;

import java.util.Optional;
import java.util.function.Supplier;

public final class LocalMobileExecutionProvider implements MobileExecutionProvider {
    @Override
    public String name() {
        return "local";
    }

    @Override
    public Optional<String> buildReportMessage(String sessionId, Supplier<Optional<String>> providerLinkSupplier) {
        return Optional.empty();
    }
}
