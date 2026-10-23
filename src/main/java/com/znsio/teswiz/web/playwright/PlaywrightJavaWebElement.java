package com.znsio.teswiz.web.playwright;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.BoundingBox;

final class PlaywrightJavaWebElement implements WebElement {
    private final Locator locator;
    private final Duration implicitWaitTimeout;

    PlaywrightJavaWebElement(Locator locator, Duration implicitWaitTimeout) {
        this.locator = locator;
        this.implicitWaitTimeout = implicitWaitTimeout;
    }

    @Override
    public void click() {
        locator.click();
    }

    @Override
    public void submit() {
        click();
    }

    @Override
    public void sendKeys(CharSequence... keysToSend) {
        String value = java.util.Arrays.stream(keysToSend)
                .map(String::valueOf)
                .collect(Collectors.joining());
        if (isFileInput(value)) {
            locator.setInputFiles(Path.of(value));
            return;
        }
        locator.pressSequentially(value);
    }

    @Override
    public void clear() {
        locator.clear();
    }

    @Override
    public String getTagName() {
        Object tagName = locator.evaluate("element => element.tagName.toLowerCase()");
        return String.valueOf(tagName);
    }

    @Override
    public String getAttribute(String name) {
        return locator.getAttribute(name);
    }

    @Override
    public String getDomAttribute(String name) {
        return getAttribute(name);
    }

    @Override
    public String getDomProperty(String name) {
        Object value = locator.evaluate("(args) => args[0][args[1]]", List.of(elementHandle(), name));
        return null == value ? null : String.valueOf(value);
    }

    @Override
    public String getText() {
        String text = locator.innerText();
        if (null == text || text.isBlank()) {
            text = locator.textContent();
        }
        return null == text ? "" : text.trim();
    }

    @Override
    public List<WebElement> findElements(By by) {
        Locator childLocator = locator.locator(PlaywrightJavaBy.toSelector(by));
        int count = PlaywrightJavaWait.untilCountAtLeast(childLocator, implicitWaitTimeout, 0);
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(index -> new PlaywrightJavaWebElement(childLocator.nth(index), implicitWaitTimeout))
                .map(WebElement.class::cast)
                .toList();
    }

    @Override
    public WebElement findElement(By by) {
        Locator childLocator = locator.locator(PlaywrightJavaBy.toSelector(by));
        if (PlaywrightJavaWait.untilCountAtLeast(childLocator, implicitWaitTimeout, 1) <= 0) {
            throw new NoSuchElementException("Unable to locate element: " + by);
        }
        return new PlaywrightJavaWebElement(childLocator.first(), implicitWaitTimeout);
    }

    @Override
    public SearchContext getShadowRoot() {
        throw new UnsupportedOperationException("Shadow DOM is not implemented for Playwright Java yet");
    }

    @Override
    public boolean isSelected() {
        try {
            return locator.isChecked();
        } catch (RuntimeException ignored) {
            Object selected = locator.evaluate("element => element.matches(':checked')");
            return Boolean.TRUE.equals(selected);
        }
    }

    @Override
    public boolean isEnabled() {
        return locator.isEnabled();
    }

    @Override
    public boolean isDisplayed() {
        return locator.isVisible();
    }

    @Override
    public Point getLocation() {
        Rectangle rectangle = getRect();
        return new Point(rectangle.getX(), rectangle.getY());
    }

    @Override
    public Dimension getSize() {
        Rectangle rectangle = getRect();
        return new Dimension(rectangle.getWidth(), rectangle.getHeight());
    }

    @Override
    public Rectangle getRect() {
        BoundingBox boundingBox = locator.boundingBox();
        if (null == boundingBox) {
            return new Rectangle(0, 0, 0, 0);
        }
        return new Rectangle((int) Math.round(boundingBox.x), (int) Math.round(boundingBox.y),
                (int) Math.round(boundingBox.width), (int) Math.round(boundingBox.height));
    }

    @Override
    public String getCssValue(String propertyName) {
        Object value = locator.evaluate("(args) => getComputedStyle(args[0]).getPropertyValue(args[1])",
                List.of(elementHandle(), propertyName));
        return null == value ? "" : String.valueOf(value);
    }

    @Override
    public <X> X getScreenshotAs(OutputType<X> target) {
        return target.convertFromPngBytes(locator.screenshot());
    }

    ElementHandle elementHandle() {
        return locator.elementHandle();
    }

    private boolean isFileInput(String value) {
        if (!"input".equalsIgnoreCase(getTagName())) {
            return false;
        }
        if (!"file".equalsIgnoreCase(getAttribute("type"))) {
            return false;
        }
        return Files.exists(Path.of(value));
    }
}
