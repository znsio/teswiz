# Web Playwright-Java Test Implementation Example

This guide provides a concrete example of implementing a Playwright-Java screen using native Playwright Java APIs while keeping the teswiz screen-contract pattern.

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

import com.microsoft.playwright.Locator;
import com.znsio.teswiz.screen.ajio.HomeScreen;
import com.znsio.teswiz.screen.ajio.SearchScreen;
import com.znsio.teswiz.web.playwright.screen.PlaywrightJavaScreenContext;

public class HomeScreenPlaywrightJava extends HomeScreen {
    private final Locator searchInput;

    public HomeScreenPlaywrightJava(PlaywrightJavaScreenContext context) {
        this.searchInput = context.page().locator("input[name='searchVal']");
    }

    @Override
    public SearchScreen searchForTheProduct(String productName) {
        searchInput.fill(productName);
        searchInput.press("Enter");
        return SearchScreen.get();
    }
}
```

Use this model for `playwright-java` screens:

- author screens with Playwright Java `Page`, `Locator`, and Playwright-native actions
- keep the shared teswiz screen contract unchanged
- do not author new `playwright-java` screens with Selenium `By` locators
