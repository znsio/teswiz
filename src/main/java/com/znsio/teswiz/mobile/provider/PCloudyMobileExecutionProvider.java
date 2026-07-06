package com.znsio.teswiz.mobile.provider;

import java.util.Optional;
import java.util.function.Supplier;

public final class PCloudyMobileExecutionProvider implements MobileExecutionProvider {
    @Override
    public String name() {
        return "pCloudy";
    }

    @Override
    public Optional<String> buildReportMessage(String sessionId, Supplier<Optional<String>> providerLinkSupplier) {
        return providerLinkSupplier.get().map(link -> "pCloudy Report link available here: " + link);
    }
}
