package com.znsio.teswiz.mobile.provider;

import java.util.Optional;
import java.util.function.Supplier;

public final class BrowserStackMobileExecutionProvider implements MobileExecutionProvider {
    @Override
    public String name() {
        return "browserstack";
    }

    @Override
    public Optional<String> buildReportMessage(String sessionId, Supplier<Optional<String>> providerLinkSupplier) {
        return providerLinkSupplier.get().map(link -> "BrowserStack Report link available here: " + link);
    }
}
