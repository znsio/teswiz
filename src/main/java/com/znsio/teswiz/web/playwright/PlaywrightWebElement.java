package com.znsio.teswiz.web.playwright;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

final class PlaywrightWebElement implements WebElement {
    private final PlaywrightWorkerClient workerClient;
    private final PlaywrightWorkerSession session;
    private final PlaywrightLocatorReference locatorReference;

    PlaywrightWebElement(PlaywrightWorkerClient workerClient, PlaywrightWorkerSession session,
            PlaywrightLocatorReference locatorReference) {
        this.workerClient = workerClient;
        this.session = session;
        this.locatorReference = locatorReference;
    }

    @Override
    public void click() {
        workerClient.click(session.sessionId(), locatorReference);
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
        workerClient.type(session.sessionId(), locatorReference, value);
    }

    @Override
    public void clear() {
        workerClient.clear(session.sessionId(), locatorReference);
    }

    @Override
    public String getTagName() {
        return workerClient.getTagName(session.sessionId(), locatorReference);
    }

    @Override
    public String getAttribute(String name) {
        return workerClient.getAttribute(session.sessionId(), locatorReference, name);
    }

    @Override
    public String getDomAttribute(String name) {
        return getAttribute(name);
    }

    @Override
    public String getDomProperty(String name) {
        return getAttribute(name);
    }

    @Override
    public String getText() {
        return workerClient.getText(session.sessionId(), locatorReference);
    }

    @Override
    public List<WebElement> findElements(By by) {
        int count = workerClient.countElements(session.sessionId(), locatorReference.child(by, 0));
        return IntStream.range(0, count)
                .mapToObj(index -> new PlaywrightWebElement(workerClient, session, locatorReference.child(by, index)))
                .map(WebElement.class::cast)
                .toList();
    }

    @Override
    public WebElement findElement(By by) {
        return new PlaywrightWebElement(workerClient, session, locatorReference.child(by, 0));
    }

    @Override
    public SearchContext getShadowRoot() {
        throw new UnsupportedOperationException("Shadow DOM is not implemented for Playwright TS yet");
    }

    @Override
    public boolean isSelected() {
        return workerClient.isSelected(session.sessionId(), locatorReference);
    }

    @Override
    public boolean isEnabled() {
        return workerClient.isEnabled(session.sessionId(), locatorReference);
    }

    @Override
    public boolean isDisplayed() {
        return workerClient.isVisible(session.sessionId(), locatorReference);
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
        return workerClient.getElementRect(session.sessionId(), locatorReference);
    }

    @Override
    public String getCssValue(String propertyName) {
        return workerClient.getCssValue(session.sessionId(), locatorReference, propertyName);
    }

    @Override
    public <X> X getScreenshotAs(OutputType<X> target) {
        return target.convertFromBase64Png(workerClient.captureScreenshot(session.sessionId()));
    }
}
