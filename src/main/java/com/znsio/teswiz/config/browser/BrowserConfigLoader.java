package com.znsio.teswiz.config.browser;

import org.json.JSONObject;

import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.tools.JsonSchemaValidator;

public final class BrowserConfigLoader {
    private static final String BROWSER_CONFIG_SCHEMA_FILE = "BrowserConfigSchema.json";

    private BrowserConfigLoader() {
    }

    public static JSONObject load(TestExecutionContext context) {
        String browserConfigFile = Runner.getBrowserConfigFile();
        String updatedBrowserConfigFileForThisTest = context
                .getTestStateAsString(TEST_CONTEXT.UPDATED_BROWSER_CONFIG_FILE_FOR_THIS_TEST);
        if (null != updatedBrowserConfigFileForThisTest) {
            browserConfigFile = updatedBrowserConfigFileForThisTest;
        }
        JSONObject browserConfig = Runner.getBrowserConfigFileContents(browserConfigFile);
        return JsonSchemaValidator.validateJsonFileAgainstSchema(browserConfigFile, browserConfig.toString(),
                BROWSER_CONFIG_SCHEMA_FILE);
    }
}
