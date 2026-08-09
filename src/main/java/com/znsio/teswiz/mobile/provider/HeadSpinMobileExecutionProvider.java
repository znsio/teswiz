package com.znsio.teswiz.mobile.provider;

import java.util.Optional;
import java.util.function.Supplier;

public final class HeadSpinMobileExecutionProvider implements MobileExecutionProvider {
    @Override
    public String name() {
        return "headspin";
    }

    @Override
    public Optional<String> buildReportMessage(String sessionId, Supplier<Optional<String>> providerLinkSupplier) {
        return Optional.of("Headspin Report link available here: https://ui-dev.headspin.io/sessions/" + sessionId
                + "/waterfall");
    }
}
