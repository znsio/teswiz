package com.znsio.teswiz.runner;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.Assumptions;

final class SeleniumContractConditions {
    static final String ENABLE_PROPERTY = "teswiz.selenium.contract.enabled";
    static final String CHROME_BINARY_PROPERTY = "teswiz.selenium.contract.chromeBinary";
    private static final Path DEFAULT_MAC_CHROME_BINARY = Path.of(
            "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome");

    private SeleniumContractConditions() {
    }

    static void assumeEnabled() {
        Assumptions.assumeTrue(isEnabled(),
                () -> String.format("Set -D%s=true to enable Selenium shared web-driver contract tests.",
                        ENABLE_PROPERTY));
    }

    static boolean isEnabled() {
        return Boolean.parseBoolean(System.getProperty(ENABLE_PROPERTY, "false"));
    }

    static Optional<String> resolveChromeBinary() {
        String configuredBinary = System.getProperty(CHROME_BINARY_PROPERTY);
        if (null != configuredBinary && !configuredBinary.isBlank()) {
            return Optional.of(configuredBinary);
        }
        if (Files.isExecutable(DEFAULT_MAC_CHROME_BINARY)) {
            return Optional.of(DEFAULT_MAC_CHROME_BINARY.toString());
        }
        return Optional.empty();
    }
}
