package com.znsio.teswiz.mobile.provider;

import com.znsio.teswiz.runner.Runner;

public class MobileExecutionProviderResolver {
    public MobileExecutionProvider resolve() {
        return resolve(Runner.getCloudName());
    }

    public MobileExecutionProvider resolve(String providerName) {
        if (null == providerName || providerName.isBlank() || Runner.NOT_SET.equalsIgnoreCase(providerName)) {
            return new LocalMobileExecutionProvider();
        }
        switch (providerName.toLowerCase()) {
            case "browserstack":
                return new BrowserStackMobileExecutionProvider();
            case "lambdatest":
                return new LambdaTestMobileExecutionProvider();
            case "headspin":
                return new HeadSpinMobileExecutionProvider();
            case "pcloudy":
                return new PCloudyMobileExecutionProvider();
            default:
                return new LocalMobileExecutionProvider();
        }
    }
}
