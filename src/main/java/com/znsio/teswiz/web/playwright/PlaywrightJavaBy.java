package com.znsio.teswiz.web.playwright;

import org.openqa.selenium.By;

final class PlaywrightJavaBy {
    private PlaywrightJavaBy() {
    }

    static String toSelector(By by) {
        String value = by.toString();
        if (value.startsWith("By.id: ")) {
            return "#" + cssEscape(value.substring("By.id: ".length()));
        }
        if (value.startsWith("By.name: ")) {
            return "[name=\"" + escapeQuotes(value.substring("By.name: ".length())) + "\"]";
        }
        if (value.startsWith("By.className: ")) {
            return "." + cssEscape(value.substring("By.className: ".length()));
        }
        if (value.startsWith("By.cssSelector: ")) {
            return value.substring("By.cssSelector: ".length());
        }
        if (value.startsWith("By.tagName: ")) {
            return value.substring("By.tagName: ".length());
        }
        if (value.startsWith("By.linkText: ")) {
            return "a:has-text(\"" + escapeQuotes(value.substring("By.linkText: ".length())) + "\")";
        }
        if (value.startsWith("By.xpath: ")) {
            return "xpath=" + value.substring("By.xpath: ".length());
        }
        throw new UnsupportedOperationException("Unsupported locator for Playwright Java: " + value);
    }

    private static String cssEscape(String value) {
        return value.replace(".", "\\.");
    }

    private static String escapeQuotes(String value) {
        return value.replace("\"", "\\\"");
    }
}
