package com.znsio.teswiz.mobile.provider;

import java.util.Optional;
import java.util.function.Supplier;
import io.appium.java_client.AppiumDriver;
import com.znsio.teswiz.tools.cmd.CommandLineExecutor;
import com.znsio.teswiz.tools.cmd.CommandLineResponse;
import com.znsio.teswiz.tools.JsonPrettyPrinter;
import com.znsio.teswiz.tools.SensitiveDataMasker;
import org.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.commons.lang3.exception.ExceptionUtils;
import static com.znsio.teswiz.tools.OverriddenVariable.getOverriddenStringValue;

public final class BrowserStackMobileExecutionProvider implements MobileExecutionProvider {
    private static final Logger LOGGER = LogManager.getLogger(BrowserStackMobileExecutionProvider.class.getName());

    @Override
    public String name() {
        return "browserstack";
    }

    @Override
    public Optional<String> buildReportMessage(String sessionId, Supplier<Optional<String>> providerLinkSupplier) {
        return providerLinkSupplier.get().map(link -> "BrowserStack Report link available here: " + link);
    }

    @Override
    public Optional<String> getProviderReportLink(String sessionId, AppiumDriver driver) {
        String browserStackTestResultUrl = "";
        String cloudUser = getOverriddenStringValue("CLOUD_USERNAME");
        String cloudPassword = getOverriddenStringValue("CLOUD_KEY");
        try {
            String[] curlCommand = new String[] { "curl --in" + "secure " + getCurlProxyCommand() + " -u \"" + cloudUser
                    + ":" + cloudPassword + "\" -X GET \"https://api-cloud.browserstack.com/app-automate/sessions/"
                    + sessionId + ".json\"" };
            CommandLineResponse commandLineResponse = CommandLineExecutor.execCommand(curlCommand);
            LOGGER.info("Response from BrowserStack - '{}'",
                    JsonPrettyPrinter.prettyPrint(SensitiveDataMasker.maskSecret(commandLineResponse.getStdOut())));
            JSONObject pr = new JSONObject(commandLineResponse.getStdOut());
            JSONObject automation_session = pr.getJSONObject("automation_session");
            browserStackTestResultUrl = automation_session.getString("browser_url");
            LOGGER.info("BrowserStack execution link: {}", browserStackTestResultUrl);
        } catch (Exception e) {
            LOGGER.info("Unable to get test execution link from BrowserStack: {}", e.getMessage());
            ExceptionUtils.getStackTrace(e);
        }
        return Optional.ofNullable(browserStackTestResultUrl.isEmpty() ? null : browserStackTestResultUrl);
    }

    private static String getCurlProxyCommand() {
        String curlProxyCommand = "";
        if (null != getOverriddenStringValue("PROXY_URL")) {
            curlProxyCommand = " --proxy " + System.getProperty("PROXY_URL");
        }
        return curlProxyCommand;
    }
}
