# Web Playwright-TS Test Implementation Example

This guide provides a concrete example of implementing a Playwright-TS (TypeScript) test screen using zero-Java dynamic subclassing and proxying.

---

## 1. Config Setup (`config.properties`)
```properties
PLATFORM=web
WEB_ENGINE=playwright-ts
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

## 3. TypeScript Implementation (`home.screen.ts`)
Create your file under `src/test/resources/playwright/screens/<app_name>/home.screen.ts`. There is **no Java class required**. The framework dynamically resolves the TypeScript module at runtime.

```typescript
import type { ScreenContext } from "../screen-context.ts";

const LOCATORS = {
  searchInput: 'input[name="searchVal"]',
};

export async function searchForTheProduct(screen: ScreenContext, productName: string): Promise<void> {
  // Wait for the input locator and type search text, pressing enter
  await screen.page.locator(LOCATORS.searchInput).fill(productName);
  await screen.page.locator(LOCATORS.searchInput).press("Enter");
}
```
