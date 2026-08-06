# Web Playwright-Java Test Implementation Example

This guide provides a concrete example of implementing a Playwright-Java test screen.

---

## 1. Config Setup (`config.properties`)
```properties
PLATFORM=web
WEB_ENGINE=playwright-java
```

---

## 2. Shared Contract (`HomeScreen.java`)
```java
package com.znsio.teswiz.screen.ajio;

import com.znsio.teswiz.screen.ScreenRegistry;

public abstract class HomeScreen {
    public static HomeScreen get() {
        return ScreenRegistry.getScreen(HomeScreen.class);
    }

    public abstract SearchScreen searchForTheProduct(String productName);
}
```

---

## 3. Playwright-Java Implementation (`HomeScreenPlaywrightJava.java`)
Resides under `src/test/java/com/znsio/teswiz/screen/web/playwrightjava/<app_name>/HomeScreenPlaywrightJava.java`:

```java
package com.znsio.teswiz.screen.web.playwrightjava.ajio;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.ajio.HomeScreen;
import com.znsio.teswiz.screen.ajio.SearchScreen;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

public class HomeScreenPlaywrightJava extends HomeScreen {
    private final Driver driver;
    private final Visual visually;
    private static final By SEARCH_INPUT = By.name("searchVal");

    public HomeScreenPlaywrightJava(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public SearchScreen searchForTheProduct(String productName) {
        // Leverages the custom Driver compatibility adapter class
        driver.waitTillElementIsPresent(SEARCH_INPUT);
        driver.findElement(SEARCH_INPUT).sendKeys(productName, Keys.ENTER);
        return SearchScreen.get();
    }
}
```
