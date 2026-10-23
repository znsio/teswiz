# Android Test Implementation Example

This guide provides a concrete example of implementing an Android mobile test case using Appium in Teswiz.

---

## 1. Feature File (`mic-settings.feature`)
```gherkin
@android
Feature: Mic Settings Configuration

  Scenario: User should be able to mute/unmute mic in a meeting
    Given I sign in as a registered "Host"
    And I start an instant meeting
    When I Unmute myself
    Then I should be able to Mute myself
```

---

## 2. Step Definition (`MeetingSteps.java`)
```java
package com.znsio.sample.e2e.steps;

import com.znsio.teswiz.businessLayer.MeetingBL;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

public class MeetingSteps {
    @When("I Unmute myself")
    public void unmuteMyself() {
        new MeetingBL().unmute();
    }

    @Then("I should be able to Mute myself")
    public void muteMyself() {
        new MeetingBL().mute();
    }
}
```

---

## 3. Business Layer (`MeetingBL.java`)
```java
package com.znsio.teswiz.businessLayer;

import com.znsio.teswiz.screen.InAMeetingScreen;
import static org.assertj.core.api.Assertions.assertThat;

public class MeetingBL {
    public MeetingBL unmute() {
        InAMeetingScreen.get().unmuteMic();
        return this;
    }

    public MeetingBL mute() {
        InAMeetingScreen.get().muteMic();
        return this;
    }
}
```

---

## 4. Shared Contract (`InAMeetingScreen.java`)
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

## 5. Android Implementation (`InAMeetingScreenAndroid.java`)
```java
package com.znsio.teswiz.screen.android;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.InAMeetingScreen;
import org.openqa.selenium.By;

public class InAMeetingScreenAndroid extends InAMeetingScreen {
    private final Driver driver;
    private final Visual visually;
    private static final By MIC_BUTTON = By.id("com.jiomeet.org:id/mic_button");

    public InAMeetingScreenAndroid(Driver driver, Visual visually) {
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

## 6. Capabilities Configuration (`android_caps.json`)
```json
{
  "platformName": "Android",
  "automationName": "UiAutomator2",
  "appPackage": "com.jiomeet.org",
  "appActivity": "com.jiomeet.org.MainActivity",
  "noReset": true
}
```
