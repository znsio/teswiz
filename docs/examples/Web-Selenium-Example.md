# Web Selenium Test Implementation Example

This guide provides a concrete example of implementing a Selenium Web test case in Teswiz.

---

## 1. Config Setup (`config.properties`)
```properties
PLATFORM=web
WEB_ENGINE=selenium
BROWSER=chrome
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

## 3. Web Selenium Implementation (`HomeScreenWeb.java`)
```java
package com.znsio.teswiz.screen.web.ajio;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.ajio.HomeScreen;
import com.znsio.teswiz.screen.ajio.SearchScreen;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;

public class HomeScreenWeb extends HomeScreen {
    private final Driver driver;
    private final Visual visually;
    private static final By SEARCH_INPUT = By.name("searchVal");

    public HomeScreenWeb(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public SearchScreen searchForTheProduct(String productName) {
        driver.waitTillElementIsPresent(SEARCH_INPUT);
        driver.findElement(SEARCH_INPUT).sendKeys(productName, Keys.ENTER);
        return SearchScreen.get();
    }
}
```
