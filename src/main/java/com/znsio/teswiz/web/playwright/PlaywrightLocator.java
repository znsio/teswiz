package com.znsio.teswiz.web.playwright;

import org.openqa.selenium.By;

record PlaywrightLocator(String strategy, String value) {
    static PlaywrightLocator from(By by) {
        String locator = by.toString();
        if (locator.startsWith("By.id: ")) {
            return new PlaywrightLocator("id", locator.substring("By.id: ".length()));
        }
        if (locator.startsWith("By.cssSelector: ")) {
            return new PlaywrightLocator("css", locator.substring("By.cssSelector: ".length()));
        }
        if (locator.startsWith("By.xpath: ")) {
            return new PlaywrightLocator("xpath", locator.substring("By.xpath: ".length()));
        }
        if (locator.startsWith("By.className: ")) {
            return new PlaywrightLocator("className", locator.substring("By.className: ".length()));
        }
        if (locator.startsWith("By.name: ")) {
            return new PlaywrightLocator("name", locator.substring("By.name: ".length()));
        }
        if (locator.startsWith("By.tagName: ")) {
            return new PlaywrightLocator("tagName", locator.substring("By.tagName: ".length()));
        }
        if (locator.startsWith("By.linkText: ")) {
            return new PlaywrightLocator("linkText", locator.substring("By.linkText: ".length()));
        }
        if (locator.startsWith("By.partialLinkText: ")) {
            return new PlaywrightLocator("partialLinkText",
                    locator.substring("By.partialLinkText: ".length()));
        }
        throw new UnsupportedOperationException("Unsupported Playwright locator: " + locator);
    }
}
