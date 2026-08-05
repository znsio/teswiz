package com.znsio.teswiz.web.provider.selenium;

import static com.znsio.teswiz.runner.Setup.CAPS;

import java.util.ArrayList;
import java.util.Map;

import org.openqa.selenium.MutableCapabilities;

import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.tools.JsonFile;

public final class SeleniumRemoteWebDriverRequestResolver {
    private static final String WEB_DRIVER_HUB_SUFFIX = "/wd/hub";

    public SeleniumRemoteWebDriverRequest resolve(MutableCapabilities capabilities) {
        String cloudName = Runner.getCloudName();
        if ("headspin".equalsIgnoreCase(cloudName)) {
            return new SeleniumRemoteWebDriverRequest(resolveHeadSpinRemoteUrl(), capabilities);
        }
        if ("browserstack".equalsIgnoreCase(cloudName)) {
            return new SeleniumRemoteWebDriverRequest(resolveBrowserStackRemoteUrl(),
                    BrowserStackWebSetup.updateCapabilities(capabilities));
        }
        if ("lambdatest".equalsIgnoreCase(cloudName)) {
            return new SeleniumRemoteWebDriverRequest(resolveLambdaTestRemoteUrl(),
                    LambdaTestWebSetup.updateCapabilities(capabilities));
        }
        return new SeleniumRemoteWebDriverRequest(resolveDefaultRemoteUrl(), capabilities);
    }

    private String resolveDefaultRemoteUrl() {
        return "http://" + Runner.getRemoteDriverGridHostName() + ":" + Runner.getRemoteDriverGridPort()
                + WEB_DRIVER_HUB_SUFFIX;
    }

    private String resolveHeadSpinRemoteUrl() {
        String authenticationKey = Runner.getCloudKey();
        String capabilityFile = System.getProperty(CAPS);
        Map<String, Map> loadedCapabilityFile = JsonFile.loadJsonFile(capabilityFile);
        ArrayList hostMachinesList = (ArrayList) loadedCapabilityFile.get("hostMachines");
        Map hostMachines = (Map) hostMachinesList.get(0);
        String remoteServerURL = String.valueOf(hostMachines.get("machineIP"));
        String remoteUrl = remoteServerURL.endsWith("/")
                ? remoteServerURL + authenticationKey + WEB_DRIVER_HUB_SUFFIX
                : remoteServerURL + "/" + authenticationKey + WEB_DRIVER_HUB_SUFFIX;
        return remoteUrl.startsWith("https") ? remoteUrl : "https://" + remoteUrl;
    }

    private String resolveBrowserStackRemoteUrl() {
        return "https://" + Runner.getCloudUser() + ":" + Runner.getCloudKey() + "@hub-cloud.browserstack.com/wd/hub";
    }

    private String resolveLambdaTestRemoteUrl() {
        return "https://" + Runner.getCloudUser() + ":" + Runner.getCloudKey() + "@hub.lambdatest.com/wd/hub";
    }
}
