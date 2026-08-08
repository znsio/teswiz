package com.znsio.teswiz.web.provider.selenium;

import static com.znsio.teswiz.runner.Setup.CAPS;

import java.util.ArrayList;
import java.util.Map;

import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.tools.JsonFile;

final class SeleniumRemoteUrlResolver {
    private static final String WEB_DRIVER_HUB_SUFFIX = "/wd/hub";

    String resolveDefaultRemoteUrl() {
        return "http://" + Runner.getRemoteDriverGridHostName() + ":" + Runner.getRemoteDriverGridPort()
                + WEB_DRIVER_HUB_SUFFIX;
    }

    String resolveHeadSpinRemoteUrl() {
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

    String resolveBrowserStackRemoteUrl() {
        return "https://" + Runner.getCloudUser() + ":" + Runner.getCloudKey() + "@hub-cloud.browserstack.com/wd/hub";
    }

    String resolveLambdaTestRemoteUrl() {
        return "https://" + Runner.getCloudUser() + ":" + Runner.getCloudKey() + "@hub.lambdatest.com/wd/hub";
    }
}
