package com.znsio.teswiz.web.provider;

import com.znsio.teswiz.runner.Runner;

public final class WebExecutionProviderResolver {
    public WebExecutionProvider resolve() {
        return resolve(Runner.getCloudName());
    }

    public WebExecutionProvider resolve(String providerName) {
        if (null == providerName || providerName.isBlank() || Runner.NOT_SET.equalsIgnoreCase(providerName)) {
            return new LocalWebExecutionProvider();
        }
        switch (providerName.toLowerCase()) {
            case "browserstack":
                return new BrowserStackWebExecutionProvider();
            case "lambdatest":
                return new LambdaTestWebExecutionProvider();
            case "headspin":
                return new HeadSpinWebExecutionProvider();
            default:
                return new LocalWebExecutionProvider();
        }
    }
}
