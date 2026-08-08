package com.znsio.teswiz.web.provider.selenium;

import org.openqa.selenium.MutableCapabilities;

public record SeleniumRemoteWebDriverRequest(String remoteUrl, MutableCapabilities capabilities) {
}
