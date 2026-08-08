package com.znsio.teswiz.web.playwright;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

public final class PlaywrightBy {
    private PlaywrightBy() {
    }

    public static By text(String text) {
        return new CustomPlaywrightBy("text", text, "PlaywrightBy.text: " + text);
    }

    public static By role(String role, String accessibleName) {
        return new CustomPlaywrightBy("role", role + "|" + accessibleName,
                "PlaywrightBy.role: " + role + "|" + accessibleName);
    }

    public static By testId(String testId) {
        return new CustomPlaywrightBy("testId", testId, "PlaywrightBy.testId: " + testId);
    }

    private static final class CustomPlaywrightBy extends By {
        private final String strategy;
        private final String value;
        private final String description;

        private CustomPlaywrightBy(String strategy, String value, String description) {
            this.strategy = strategy;
            this.value = value;
            this.description = description;
        }

        @Override
        public List<WebElement> findElements(SearchContext context) {
            throw new UnsupportedOperationException(
                    "PlaywrightBy locators are resolved by the Playwright web engine and should not be executed directly");
        }

        String strategy() {
            return strategy;
        }

        String value() {
            return value;
        }

        @Override
        public String toString() {
            return description;
        }
    }

    static boolean isCustom(By by) {
        return by instanceof CustomPlaywrightBy;
    }

    static String getStrategy(By by) {
        return ((CustomPlaywrightBy) by).strategy();
    }

    static String getValue(By by) {
        return ((CustomPlaywrightBy) by).value();
    }
}
