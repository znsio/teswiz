# iOS Test Implementation Example

This guide provides a concrete example of implementing an iOS mobile test case using Appium in Teswiz.

---

## 1. Shared Contract (`InAMeetingScreen.java`)
```java
package com.znsio.teswiz.screen;

import com.znsio.teswiz.screen.ScreenRegistry;

public abstract class InAMeetingScreen {
    public static InAMeetingScreen get() {
        return ScreenRegistry.getScreen(InAMeetingScreen.class);
    }

    public abstract InAMeetingScreen unmuteMic();
    public abstract InAMeetingScreen muteMic();
}
```

---

## 2. iOS Implementation (`InAMeetingScreenIOS.java`)
```java
package com.znsio.teswiz.screen.ios;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.InAMeetingScreen;
import org.openqa.selenium.By;

public class InAMeetingScreenIOS extends InAMeetingScreen {
    private final Driver driver;
    private final Visual visually;
    private static final By MIC_BUTTON = By.xpath("//XCUIElementTypeButton[@name='Mic']");

    public InAMeetingScreenIOS(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public InAMeetingScreen unmuteMic() {
        driver.waitTillElementIsPresent(MIC_BUTTON).click();
        return this;
    }

    @Override
    public InAMeetingScreen muteMic() {
        driver.waitTillElementIsPresent(MIC_BUTTON).click();
        return this;
    }
}
```

---

## 3. Capabilities Configuration (`ios_caps.json`)
```json
{
  "platformName": "iOS",
  "automationName": "XCUITest",
  "bundleId": "com.jiomeet.org",
  "deviceName": "iPhone 15",
  "platformVersion": "17.0"
}
```
