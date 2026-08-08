package com.znsio.teswiz.web.playwright;

import java.time.Duration;
import java.util.List;
import java.util.stream.IntStream;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

final class PlaywrightShadowRootSearchContext implements SearchContext {
    private final PlaywrightWorkerClient workerClient;
    private final PlaywrightWorkerSession session;
    private final PlaywrightLocatorReference hostLocatorReference;
    private final Duration implicitWaitTimeout;

    PlaywrightShadowRootSearchContext(PlaywrightWorkerClient workerClient, PlaywrightWorkerSession session,
            PlaywrightLocatorReference hostLocatorReference, Duration implicitWaitTimeout) {
        this.workerClient = workerClient;
        this.session = session;
        this.hostLocatorReference = hostLocatorReference;
        this.implicitWaitTimeout = implicitWaitTimeout;
    }

    @Override
    public List<WebElement> findElements(By by) {
        PlaywrightLocatorReference childLocator = hostLocatorReference.child(by, 0);
        int count = workerClient.countElements(session.sessionId(), childLocator, implicitWaitTimeout);
        return IntStream.range(0, count)
                .mapToObj(index -> new PlaywrightWebElement(workerClient, session,
                        hostLocatorReference.child(by, index), implicitWaitTimeout))
                .map(WebElement.class::cast)
                .toList();
    }

    @Override
    public WebElement findElement(By by) {
        PlaywrightLocatorReference childLocator = hostLocatorReference.child(by, 0);
        int count = workerClient.countElements(session.sessionId(), childLocator, implicitWaitTimeout);
        if (count <= 0) {
            throw new NoSuchElementException("Unable to locate element: " + by);
        }
        return new PlaywrightWebElement(workerClient, session, childLocator, implicitWaitTimeout);
    }
}
