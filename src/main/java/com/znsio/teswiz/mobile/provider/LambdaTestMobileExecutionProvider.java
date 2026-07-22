package com.znsio.teswiz.mobile.provider;

import java.util.Optional;
import java.util.function.Supplier;

public final class LambdaTestMobileExecutionProvider implements MobileExecutionProvider {
    @Override
    public String name() {
        return "lambdatest";
    }

    @Override
    public Optional<String> buildReportMessage(String sessionId, Supplier<Optional<String>> providerLinkSupplier) {
        return Optional.of("LambdaTest Report link available here: https://automation.lambdatest.com/logs/?sessionID="
                + sessionId);
    }
}
