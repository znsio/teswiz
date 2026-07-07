package com.znsio.teswiz.web.playwright;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WindowType;

public final class PlaywrightWebDriver implements WebDriver, JavascriptExecutor, TakesScreenshot {
    private final PlaywrightWorkerClient workerClient;
    private final PlaywrightWorkerSession session;

    public PlaywrightWebDriver(PlaywrightWorkerClient workerClient, PlaywrightWorkerSession session) {
        this.workerClient = workerClient;
        this.session = session;
    }

    @Override
    public void get(String url) {
        workerClient.navigateTo(session.sessionId(), url);
    }

    @Override
    public String getCurrentUrl() {
        return workerClient.getCurrentUrl(session.sessionId());
    }

    @Override
    public String getTitle() {
        return workerClient.getTitle(session.sessionId());
    }

    @Override
    public List<WebElement> findElements(By by) {
        PlaywrightLocatorReference locatorReference = PlaywrightLocatorReference.root(by);
        int count = workerClient.countElements(session.sessionId(), locatorReference);
        return IntStream.range(0, count)
                .mapToObj(index -> new PlaywrightWebElement(workerClient, session,
                        new PlaywrightLocatorReference(locatorReference.locator(), index, locatorReference.parent())))
                .map(WebElement.class::cast)
                .toList();
    }

    @Override
    public WebElement findElement(By by) {
        return new PlaywrightWebElement(workerClient, session, PlaywrightLocatorReference.root(by));
    }

    @Override
    public String getPageSource() {
        return workerClient.getPageSource(session.sessionId());
    }

    @Override
    public void close() {
        workerClient.closeSession(session.sessionId());
    }

    @Override
    public void quit() {
        close();
    }

    @Override
    public Set<String> getWindowHandles() {
        return workerClient.getWindowHandles(session.sessionId());
    }

    @Override
    public String getWindowHandle() {
        return workerClient.getWindowHandle(session.sessionId());
    }

    @Override
    public TargetLocator switchTo() {
        return new TargetLocator() {
            @Override
            public WebDriver frame(int index) {
                throw new UnsupportedOperationException("switchTo().frame(int) is not implemented for Playwright TS yet");
            }

            @Override
            public WebDriver frame(String nameOrId) {
                throw new UnsupportedOperationException("switchTo().frame(String) is not implemented for Playwright TS yet");
            }

            @Override
            public WebDriver frame(WebElement frameElement) {
                throw new UnsupportedOperationException("switchTo().frame(WebElement) is not implemented for Playwright TS yet");
            }

            @Override
            public WebDriver parentFrame() {
                throw new UnsupportedOperationException("switchTo().parentFrame() is not implemented for Playwright TS yet");
            }

            @Override
            public WebDriver window(String nameOrHandle) {
                workerClient.switchToWindow(session.sessionId(), nameOrHandle);
                return PlaywrightWebDriver.this;
            }

            @Override
            public WebDriver defaultContent() {
                return PlaywrightWebDriver.this;
            }

            @Override
            public WebElement activeElement() {
                throw new UnsupportedOperationException("switchTo().activeElement() is not implemented for Playwright TS yet");
            }

            @Override
            public org.openqa.selenium.Alert alert() {
                throw new UnsupportedOperationException("switchTo().alert() is not implemented for Playwright TS yet");
            }

            @Override
            public WebDriver newWindow(WindowType typeHint) {
                WindowType resolvedType = null == typeHint ? WindowType.TAB : typeHint;
                workerClient.openNewWindow(session.sessionId(), resolvedType.name());
                return PlaywrightWebDriver.this;
            }
        };
    }

    @Override
    public Navigation navigate() {
        return new Navigation() {
            @Override
            public void back() {
                workerClient.goBack(session.sessionId());
            }

            @Override
            public void forward() {
                workerClient.goForward(session.sessionId());
            }

            @Override
            public void to(String url) {
                get(url);
            }

            @Override
            public void to(java.net.URL url) {
                get(url.toString());
            }

            @Override
            public void refresh() {
                workerClient.refresh(session.sessionId());
            }
        };
    }

    @Override
    public Options manage() {
        throw new UnsupportedOperationException("manage is not implemented for Playwright TS yet");
    }

    @Override
    public Object executeScript(String script, Object... args) {
        return workerClient.executeScript(session.sessionId(), script, serializeScriptArguments(args));
    }

    @Override
    public Object executeAsyncScript(String script, Object... args) {
        return executeScript(script, args);
    }

    @Override
    public <X> X getScreenshotAs(OutputType<X> target) throws WebDriverException {
        String screenshotBase64 = workerClient.captureScreenshot(session.sessionId());
        if (OutputType.BASE64.equals(target)) {
            return target.convertFromBase64Png(screenshotBase64);
        }
        if (OutputType.BYTES.equals(target)) {
            return target.convertFromBase64Png(screenshotBase64);
        }
        if (OutputType.FILE.equals(target)) {
            return target.convertFromBase64Png(screenshotBase64);
        }
        throw new WebDriverException("Unsupported screenshot output type: " + target);
    }

    private JSONArray serializeScriptArguments(Object... args) {
        JSONArray serializedArgs = new JSONArray();
        for (Object arg : args) {
            serializedArgs.put(serializeScriptArgument(arg));
        }
        return serializedArgs;
    }

    private Object serializeScriptArgument(Object arg) {
        if (null == arg) {
            return JSONObject.NULL;
        }
        if (arg instanceof PlaywrightWebElement element) {
            return new JSONObject()
                    .put("type", "element")
                    .put("locator", element.locatorReference().toJson());
        }
        if (arg instanceof String || arg instanceof Number || arg instanceof Boolean) {
            return arg;
        }
        throw new JavascriptException("Unsupported Playwright TS executeScript argument type: "
                + arg.getClass().getName());
    }
}
