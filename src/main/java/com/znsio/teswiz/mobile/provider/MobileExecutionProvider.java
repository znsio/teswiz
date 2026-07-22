package com.znsio.teswiz.mobile.provider;

import java.util.Optional;
import java.util.function.Supplier;

public interface MobileExecutionProvider {
    String name();

    Optional<String> buildReportMessage(String sessionId, Supplier<Optional<String>> providerLinkSupplier);
}
