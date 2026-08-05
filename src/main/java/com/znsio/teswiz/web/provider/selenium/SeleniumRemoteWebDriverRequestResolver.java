package com.znsio.teswiz.web.provider.selenium;

import org.openqa.selenium.MutableCapabilities;

import com.znsio.teswiz.runner.Runner;

public final class SeleniumRemoteWebDriverRequestResolver {
    private final SeleniumRemoteUrlResolver remoteUrlResolver;

    public SeleniumRemoteWebDriverRequestResolver() {
        this(new SeleniumRemoteUrlResolver());
    }

    SeleniumRemoteWebDriverRequestResolver(SeleniumRemoteUrlResolver remoteUrlResolver) {
        this.remoteUrlResolver = remoteUrlResolver;
    }

    public SeleniumRemoteWebDriverRequest resolve(MutableCapabilities capabilities) {
        String cloudName = Runner.getCloudName();
        if ("headspin".equalsIgnoreCase(cloudName)) {
            return new SeleniumRemoteWebDriverRequest(remoteUrlResolver.resolveHeadSpinRemoteUrl(), capabilities);
        }
        if ("browserstack".equalsIgnoreCase(cloudName)) {
            return new SeleniumRemoteWebDriverRequest(remoteUrlResolver.resolveBrowserStackRemoteUrl(),
                    BrowserStackWebSetup.updateCapabilities(capabilities));
        }
        if ("lambdatest".equalsIgnoreCase(cloudName)) {
            return new SeleniumRemoteWebDriverRequest(remoteUrlResolver.resolveLambdaTestRemoteUrl(),
                    LambdaTestWebSetup.updateCapabilities(capabilities));
        }
        return new SeleniumRemoteWebDriverRequest(remoteUrlResolver.resolveDefaultRemoteUrl(), capabilities);
    }
}
