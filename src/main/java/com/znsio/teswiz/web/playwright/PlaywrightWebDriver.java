package com.znsio.teswiz.web.playwright;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.logging.Logs;

import com.znsio.teswiz.exceptions.InvalidTestDataException;

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
                workerClient.switchToFrame(session.sessionId(), index);
                return PlaywrightWebDriver.this;
            }

            @Override
            public WebDriver frame(String nameOrId) {
                workerClient.switchToFrame(session.sessionId(), nameOrId);
                return PlaywrightWebDriver.this;
            }

            @Override
            public WebDriver frame(WebElement frameElement) {
                throw new UnsupportedOperationException("switchTo().frame(WebElement) is not implemented for Playwright TS yet");
            }

            @Override
            public WebDriver parentFrame() {
                workerClient.switchToParentFrame(session.sessionId());
                return PlaywrightWebDriver.this;
            }

            @Override
            public WebDriver window(String nameOrHandle) {
                workerClient.switchToWindow(session.sessionId(), nameOrHandle);
                return PlaywrightWebDriver.this;
            }

            @Override
            public WebDriver defaultContent() {
                workerClient.switchToDefaultContent(session.sessionId());
                return PlaywrightWebDriver.this;
            }

            @Override
            public WebElement activeElement() {
                return PlaywrightWebDriver.this.findElement(By.cssSelector(":focus"));
            }

            @Override
            public org.openqa.selenium.Alert alert() {
                org.openqa.selenium.Alert alert = new org.openqa.selenium.Alert() {
                    @Override
                    public void dismiss() {
                        ensureAlertPresent();
                        workerClient.dismissAlert(session.sessionId());
                    }

                    @Override
                    public void accept() {
                        ensureAlertPresent();
                        workerClient.acceptAlert(session.sessionId(), null);
                    }

                    @Override
                    public String getText() {
                        return ensureAlertPresent().getString("message");
                    }

                    @Override
                    public void sendKeys(String keysToSend) {
                        JSONObject alert = ensureAlertPresent();
                        String alertType = alert.optString("type", "alert");
                        if (!"prompt".equalsIgnoreCase(alertType)) {
                            throw new UnhandledAlertException("Only prompt dialogs accept text input");
                        }
                        workerClient.acceptAlert(session.sessionId(), keysToSend);
                    }

                    private JSONObject ensureAlertPresent() {
                        try {
                            return workerClient.getAlert(session.sessionId());
                        } catch (InvalidTestDataException | WebDriverException exception) {
                            throw new NoAlertPresentException(exception.getMessage(), exception);
                        }
                    }
                };
                alert.getText();
                return alert;
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
        return new Options() {
            @Override
            public void addCookie(org.openqa.selenium.Cookie cookie) {
                throw new UnsupportedOperationException("manage().addCookie() is not implemented for Playwright TS yet");
            }

            @Override
            public void deleteCookieNamed(String name) {
                throw new UnsupportedOperationException("manage().deleteCookieNamed() is not implemented for Playwright TS yet");
            }

            @Override
            public void deleteCookie(org.openqa.selenium.Cookie cookie) {
                throw new UnsupportedOperationException("manage().deleteCookie() is not implemented for Playwright TS yet");
            }

            @Override
            public void deleteAllCookies() {
                throw new UnsupportedOperationException("manage().deleteAllCookies() is not implemented for Playwright TS yet");
            }

            @Override
            public Set<org.openqa.selenium.Cookie> getCookies() {
                throw new UnsupportedOperationException("manage().getCookies() is not implemented for Playwright TS yet");
            }

            @Override
            public org.openqa.selenium.Cookie getCookieNamed(String name) {
                throw new UnsupportedOperationException("manage().getCookieNamed() is not implemented for Playwright TS yet");
            }

            @Override
            public Timeouts timeouts() {
                throw new UnsupportedOperationException("manage().timeouts() is not implemented for Playwright TS yet");
            }

            @Override
            public Window window() {
                return new Window() {
                    @Override
                    public void setSize(Dimension targetSize) {
                        workerClient.setWindowSize(session.sessionId(), targetSize);
                    }

                    @Override
                    public void setPosition(Point targetPosition) {
                        throw new UnsupportedOperationException("manage().window().setPosition() is not implemented for Playwright TS yet");
                    }

                    @Override
                    public Dimension getSize() {
                        return workerClient.getWindowSize(session.sessionId());
                    }

                    @Override
                    public Point getPosition() {
                        return workerClient.getWindowPosition(session.sessionId());
                    }

                    @Override
                    public void maximize() {
                        workerClient.maximizeWindow(session.sessionId());
                    }

                    @Override
                    public void minimize() {
                        throw new UnsupportedOperationException("manage().window().minimize() is not implemented for Playwright TS yet");
                    }

                    @Override
                    public void fullscreen() {
                        throw new UnsupportedOperationException("manage().window().fullscreen() is not implemented for Playwright TS yet");
                    }
                };
            }

            @Override
            public Logs logs() {
                throw new UnsupportedOperationException("manage().logs() is not implemented for Playwright TS yet");
            }
        };
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
