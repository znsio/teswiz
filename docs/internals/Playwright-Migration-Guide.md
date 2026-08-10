# Playwright Migration & Implementation Guide

This guide describes how to implement web screens in teswiz using Selenium, Playwright-Java, and Playwright-TS while keeping the shared screen-contract pattern.

---

## 1. Selecting the Web Engine

You can configure the active web execution engine on a per-suite basis by setting the `WEB_ENGINE` property in your configuration properties file (e.g., `ajio_local_web_config.properties`):

```properties
# Valid options: selenium | playwright-java | playwright-ts
WEB_ENGINE=playwright-ts
```

If not specified, Teswiz defaults to `selenium`.

---

## 2. Defining the Screen Contract

All platform and engine implementations must implement a single shared Java contract. `ScreenRegistry`
resolves implementation classes (and Playwright-TS modules) by convention from the **contract class's own
package** — it does not require that package to be `com.znsio.teswiz.screen`. The only rule is that the
contract's package must contain a segment literally named `screen`; everything before that segment is your
project's own namespace, and everything after it (if anything) is treated as the "domain" (e.g. `ajio`) that
gets reused for every platform/engine implementation package.

For example, in your own project the contract might live under
`src/test/java/com/acme/tests/screen/ajio/`:

```java
package com.acme.tests.screen.ajio;

public abstract class HomeScreen {
    public static HomeScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(HomeScreen.class);
    }

    public abstract SearchScreen searchForTheProduct(String productName);
}
```

(`com.znsio.teswiz.screen.ScreenRegistry` itself is always the framework class from the teswiz JAR — only the
*contract's* package is up to you.)

---

## 3. Implementing for Selenium Web

The Selenium Web implementation resides under `<contract-root-package>/web/<app_name>/` — i.e. sibling to the
contract's own package, under a `web` segment. The class name must append `Web` to the contract class name:

```java
package com.acme.tests.screen.web.ajio;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.acme.tests.screen.ajio.HomeScreen;
import com.acme.tests.screen.ajio.SearchScreen;
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

---

## 4. Implementing for Playwright-Java Web

The Playwright-Java Web implementation resides under `<contract-root-package>/web/playwrightjava/<app_name>/`.
The class name must append `PlaywrightJava` to the contract class name:

```java
package com.acme.tests.screen.web.playwrightjava.ajio;

import com.microsoft.playwright.Locator;
import com.acme.tests.screen.ajio.HomeScreen;
import com.acme.tests.screen.ajio.SearchScreen;
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

Important:

- `playwright-java` screens should be authored with native Playwright Java APIs
- use `Page`, `Locator`, and Playwright-native actions/waits inside the screen implementation
- do not treat Selenium `By` locators as the intended long-term authoring model for `playwright-java`

---

## 5. Implementing for Playwright-TS Web (Zero-Java Proxying)

When using `playwright-ts`, **you do not write any Java implementation class**. The framework uses ByteBuddy at runtime to dynamically subclass your Java contract (`HomeScreen.java`) and intercept all calls, proxying them over IPC to a node worker running your TypeScript screen module.

The TypeScript module resides under `src/test/resources/playwright/screens/<app_name>/` (this path is fixed,
regardless of your contract's Java package) and the file name must be `kebab-case.screen.ts` (matching the
`CamelCase` contract name, e.g. `home.screen.ts`):

```typescript
import type { ScreenContext } from "../screen-context.ts";

const LOCATORS = {
  searchInput: 'input[name="searchVal"]',
};

export async function searchForTheProduct(screen: ScreenContext, productName: string): Promise<void> {
  await screen.page.locator(LOCATORS.searchInput).fill(productName);
  await screen.page.locator(LOCATORS.searchInput).press("Enter");
}
```

---

## 6. Verifying Screen Parity and Compliance

Teswiz's own build defines a `verifyScreenContracts` Gradle task (see `build.gradle`) that checks teswiz's own
screen contracts under `src/test/java/com/znsio/teswiz/screen` for missing classes, missing TypeScript modules,
or method mismatches. It is not a task the teswiz JAR publishes for consumers to use automatically.

To get the same check in your own project, register an equivalent task pointing at your own screen source root
(`com.znsio.teswiz.screen.ScreenContractSanityChecker` takes the source root as its first argument):

```groovy
tasks.register('verifyScreenContracts', JavaExec) {
    group = "verification"
    classpath = sourceSets.test.runtimeClasspath
    mainClass = "com.znsio.teswiz.screen.ScreenContractSanityChecker"
    args "src/test/java/com/acme/tests/screen"
}
```
