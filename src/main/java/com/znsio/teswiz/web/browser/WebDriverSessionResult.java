package com.znsio.teswiz.web.browser;

import com.znsio.teswiz.session.SessionHandle;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;

public record WebDriverSessionResult(
        WebDriver webDriver,
        boolean headless,
        Capabilities capabilities,
        SessionHandle sessionHandle) {
}
