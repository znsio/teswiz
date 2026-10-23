package com.znsio.teswiz.web.provider.selenium;

import com.browserstack.local.Local;
import com.znsio.teswiz.exceptions.EnvironmentSetupException;
import com.znsio.teswiz.tools.JsonPrettyPrinter;
import com.znsio.teswiz.tools.Randomizer;
import com.znsio.teswiz.tools.SensitiveDataMasker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.MutableCapabilities;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.openqa.selenium.remote.CapabilityType.ACCEPT_INSECURE_CERTS;

public final class BrowserStackWebCapabilitySetup {
    private static final Logger LOGGER = LogManager.getLogger(BrowserStackWebCapabilitySetup.class.getName());
    private static final String BSTACK_OPTIONS_CAPABILITY = "bstack:options";
    private static final String BROWSERSTACK_OPTIONS_CAPABILITY = "browserstackOptions";
    private static Local bsLocal;
    private static final String BROWSERSTACK_LOCAL_IDENTIFIER = Randomizer.randomize(10);

    private BrowserStackWebCapabilitySetup() {
    }

    public static MutableCapabilities updateBrowserStackCapabilities(MutableCapabilities capabilities,
            Map<String, Object> loadedPlatformCapability, String projectName, String launchName,
            String logDir, String sessionName, boolean useLocalTesting, boolean useProxy,
            String proxyUrl, String authenticationKey) {
        String subsetOfLogDir = logDir.replace("/", "").replace("\\", "");
        capabilities.setCapability("browserName", loadedPlatformCapability.get("browserName"));

        Map<String, Object> browserstackOptions = getBrowserStackOptionsForWeb(loadedPlatformCapability);
        browserstackOptions.put("projectName", projectName);
        browserstackOptions.put("buildName", launchName + "-" + subsetOfLogDir);

        browserstackOptions.put("sessionName", sessionName);
        if (useLocalTesting) {
            LOGGER.info(String.format(
                    "CLOUD_USE_LOCAL_TESTING=true. Setting up BrowserStackLocal testing using identified: '%s'",
                    BROWSERSTACK_LOCAL_IDENTIFIER));
            startBrowserStackLocal(authenticationKey, BROWSERSTACK_LOCAL_IDENTIFIER, useProxy, proxyUrl);
            browserstackOptions.put(ACCEPT_INSECURE_CERTS, "true");
            browserstackOptions.put("local", "true");
            browserstackOptions.put("localIdentifier", BROWSERSTACK_LOCAL_IDENTIFIER);
        }
        capabilities.setCapability(BSTACK_OPTIONS_CAPABILITY, browserstackOptions);

        return capabilities;
    }

    private static Map<String, Object> getBrowserStackOptionsForWeb(
            Map<String, Object> loadedPlatformCapability) {
        Object browserstackOptionsRaw = loadedPlatformCapability.get(BROWSERSTACK_OPTIONS_CAPABILITY);
        if (browserstackOptionsRaw instanceof Map) {
            return (Map<String, Object>) browserstackOptionsRaw;
        }
        Object bstackOptionsRaw = loadedPlatformCapability.get(BSTACK_OPTIONS_CAPABILITY);
        if (bstackOptionsRaw instanceof Map) {
            return (Map<String, Object>) bstackOptionsRaw;
        }
        return new HashMap<>();
    }

    private static void startBrowserStackLocal(String authenticationKey, String id, boolean useProxy, String proxyUrl) {
        bsLocal = new Local();

        HashMap<String, String> bsLocalArgs = new HashMap<>();
        bsLocalArgs.put("key", authenticationKey);
        bsLocalArgs.put("v", "true");
        bsLocalArgs.put("localIdentifier", id);
        bsLocalArgs.put("forcelocal", "true");
        bsLocalArgs.put("verbose", "3");
        bsLocalArgs.put("force", "true");
        try {
            LOGGER.info("Is BrowserStackLocal running? - " + bsLocal.isRunning());
            if (useProxy) {
                URL url = new URL(proxyUrl);
                String host = url.getHost();
                int port = url.getPort() == -1 ? url.getDefaultPort() : url.getPort();
                LOGGER.info(String.format("Using proxyHost: %s", host));
                LOGGER.info(String.format("Using proxyPort: %d", port));
                bsLocalArgs.put("proxyHost", host);
                bsLocalArgs.put("proxyPort", String.valueOf(port));
            }

            LOGGER.info(String.format("Start BrowserStackLocal using: %s",
                    SensitiveDataMasker.mask(JsonPrettyPrinter.prettyPrint(bsLocalArgs))));
            bsLocal.start(bsLocalArgs);
            LOGGER.info(String.format("Is BrowserStackLocal started? - %s", bsLocal.isRunning()));
        } catch (Exception e) {
            throw new EnvironmentSetupException("Error starting BrowserStackLocal", e);
        }
    }
}
