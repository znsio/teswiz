package com.znsio.teswiz.web.playwright;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WindowType;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.Logs;
import org.json.JSONObject;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Page.NavigateOptions;
import com.microsoft.playwright.Tracing;
import com.microsoft.playwright.options.WaitUntilState;
import com.znsio.teswiz.visual.PlaywrightVisualDriver;
import com.znsio.teswiz.visual.PlaywrightVisualSessionRequest;
import com.znsio.teswiz.visual.PlaywrightVisualResults;

public final class PlaywrightJavaWebDriver implements WebDriver, org.openqa.selenium.JavascriptExecutor,
        org.openqa.selenium.TakesScreenshot, PlaywrightVisualDriver {
    private static final String BROWSERSTACK_EXECUTOR_PREFIX = "browserstack_executor:";
    private static final String LAMBDATEST_ACTION_PREFIX = "lambdatest_action:";
    private static final String LEGACY_LAMBDATEST_NAME_PREFIX = "lambda-name=";
    private static final String LEGACY_LAMBDATEST_STATUS_PREFIX = "lambda-status=";
    private static final String LEGACY_LAMBDATEST_COMMENT_PREFIX = "lambda-comment=";
    private final PlaywrightJavaSession session;
    private Duration implicitWaitTimeout = Duration.ZERO;
    private Duration pageLoadTimeout = Duration.ofSeconds(30);
    private Duration scriptTimeout = Duration.ofSeconds(30);
    private String pendingLambdaTestStatus;
    private PlaywrightJavaVisualSession visualSession;

    public PlaywrightJavaWebDriver(PlaywrightJavaSession session) {
        this.session = session;
    }

    @Override
    public void get(String url) {
        session.page().navigate(url, new NavigateOptions()
                .setTimeout((double) pageLoadTimeout.toMillis())
                .setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
    }

    @Override
    public String getCurrentUrl() {
        return session.page().url();
    }

    @Override
    public String getTitle() {
        return session.page().title();
    }

    @Override
    public List<WebElement> findElements(By by) {
        com.microsoft.playwright.Locator locator = session.page().locator(PlaywrightJavaBy.toSelector(by));
        int count = PlaywrightJavaWait.untilCountAtLeast(locator, implicitWaitTimeout, 0);
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(index -> new PlaywrightJavaWebElement(locator.nth(index), implicitWaitTimeout))
                .map(WebElement.class::cast)
                .toList();
    }

    @Override
    public WebElement findElement(By by) {
        com.microsoft.playwright.Locator locator = session.page().locator(PlaywrightJavaBy.toSelector(by));
        if (PlaywrightJavaWait.untilCountAtLeast(locator, implicitWaitTimeout, 1) <= 0) {
            throw new NoSuchElementException("Unable to locate element: " + by);
        }
        return new PlaywrightJavaWebElement(locator.first(), implicitWaitTimeout);
    }

    @Override
    public String getPageSource() {
        return session.page().content();
    }

    @Override
    public void close() {
        closeSession();
    }

    @Override
    public void quit() {
        closeSession();
    }

    @Override
    public Set<String> getWindowHandles() {
        return new LinkedHashSet<>(Collections.singletonList(getWindowHandle()));
    }

    @Override
    public String getWindowHandle() {
        return session.sessionId();
    }

    @Override
    public TargetLocator switchTo() {
        return new TargetLocator() {
            @Override
            public WebDriver frame(int index) {
                throw unsupported("frame(int)");
            }

            @Override
            public WebDriver frame(String nameOrId) {
                throw unsupported("frame(String)");
            }

            @Override
            public WebDriver frame(WebElement frameElement) {
                throw unsupported("frame(WebElement)");
            }

            @Override
            public WebDriver parentFrame() {
                throw unsupported("parentFrame()");
            }

            @Override
            public WebDriver window(String nameOrHandle) {
                if (!getWindowHandle().equals(nameOrHandle)) {
                    throw unsupported("window(String)");
                }
                return PlaywrightJavaWebDriver.this;
            }

            @Override
            public WebDriver defaultContent() {
                return PlaywrightJavaWebDriver.this;
            }

            @Override
            public WebElement activeElement() {
                return new PlaywrightJavaWebElement(session.page().locator(":focus"), implicitWaitTimeout);
            }

            @Override
            public org.openqa.selenium.Alert alert() {
                throw new org.openqa.selenium.NoAlertPresentException("Alerts are not implemented for Playwright Java");
            }

            @Override
            public WebDriver newWindow(WindowType typeHint) {
                throw unsupported("newWindow(WindowType)");
            }
        };
    }

    @Override
    public Navigation navigate() {
        return new Navigation() {
            @Override
            public void back() {
                session.page().goBack();
            }

            @Override
            public void forward() {
                session.page().goForward();
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
                session.page().reload();
            }
        };
    }

    @Override
    public Options manage() {
        return new Options() {
            @Override
            public void addCookie(Cookie cookie) {
                throw unsupported("manage().addCookie");
            }

            @Override
            public void deleteCookieNamed(String name) {
                throw unsupported("manage().deleteCookieNamed");
            }

            @Override
            public void deleteCookie(Cookie cookie) {
                throw unsupported("manage().deleteCookie");
            }

            @Override
            public void deleteAllCookies() {
                throw unsupported("manage().deleteAllCookies");
            }

            @Override
            public Set<Cookie> getCookies() {
                return Collections.emptySet();
            }

            @Override
            public Cookie getCookieNamed(String name) {
                return null;
            }

            @Override
            public Timeouts timeouts() {
                return new Timeouts() {
                    @Override
                    public Timeouts implicitlyWait(Duration duration) {
                        implicitWaitTimeout = duration;
                        return this;
                    }

                    @Override
                    public Timeouts scriptTimeout(Duration duration) {
                        scriptTimeout = duration;
                        return this;
                    }

                    @Override
                    public Timeouts pageLoadTimeout(Duration duration) {
                        pageLoadTimeout = duration;
                        return this;
                    }

                    @Override
                    public Duration getImplicitWaitTimeout() {
                        return implicitWaitTimeout;
                    }

                    @Override
                    public Duration getScriptTimeout() {
                        return scriptTimeout;
                    }

                    @Override
                    public Duration getPageLoadTimeout() {
                        return pageLoadTimeout;
                    }
                };
            }

            @Override
            public Window window() {
                return new Window() {
                    @Override
                    public void setSize(Dimension targetSize) {
                        throw unsupported("manage().window().setSize");
                    }

                    @Override
                    public void setPosition(Point targetPosition) {
                        throw unsupported("manage().window().setPosition");
                    }

                    @Override
                    public Dimension getSize() {
                        return new Dimension(1280, 720);
                    }

                    @Override
                    public Point getPosition() {
                        return new Point(0, 0);
                    }

                    @Override
                    public void maximize() {
                    }

                    @Override
                    public void minimize() {
                    }

                    @Override
                    public void fullscreen() {
                    }
                };
            }

            @Override
            public Logs logs() {
                return new Logs() {
                    @Override
                    public LogEntries get(String logType) {
                        return new LogEntries(Collections.emptyList());
                    }

                    @Override
                    public Set<String> getAvailableLogTypes() {
                        return Collections.singleton(LogType.BROWSER);
                    }
                };
            }
        };
    }

    @Override
    public Object executeScript(String script, Object... args) {
        if (isCloudControlScript(script)) {
            return executeCloudControlScript(script);
        }
        return evaluateScript(session.page(), script, args);
    }

    @Override
    public Object executeAsyncScript(String script, Object... args) {
        throw unsupported("executeAsyncScript");
    }

    @Override
    public <X> X getScreenshotAs(OutputType<X> target) {
        return target.convertFromPngBytes(session.page().screenshot());
    }

    @Override
    public void openVisualSession(PlaywrightVisualSessionRequest request) {
        visualSession = new PlaywrightJavaVisualSession(session.page());
        visualSession.open(request);
    }

    @Override
    public void checkWindow(String tag) {
        visualSession.checkWindow(tag);
    }

    @Override
    public void check(String tag, com.applitools.eyes.selenium.fluent.SeleniumCheckSettings checkSettings) {
        visualSession.check(tag, checkSettings);
    }

    @Override
    public void checkWindow(String tag, com.applitools.eyes.MatchLevel matchLevel) {
        visualSession.checkWindow(tag, matchLevel);
    }

    @Override
    public PlaywrightVisualResults closeVisualSession() {
        return null == visualSession ? null : visualSession.close();
    }

    @Override
    public boolean isVisualSessionDisabled() {
        return null == visualSession || visualSession.isDisabled();
    }

    private Object evaluateScript(Page page, String script, Object[] args) {
        String expression = "(args) => { " + PlaywrightJavaScript.adapt(script) + " }";
        try {
            return page.evaluate(expression, PlaywrightJavaScript.adaptArguments(args));
        } catch (RuntimeException exception) {
            throw new WebDriverException("Unable to execute Playwright Java script: " + script, exception);
        }
    }

    private boolean isCloudControlScript(String script) {
        return script.startsWith(BROWSERSTACK_EXECUTOR_PREFIX)
                || script.startsWith(LAMBDATEST_ACTION_PREFIX)
                || script.startsWith(LEGACY_LAMBDATEST_NAME_PREFIX)
                || script.startsWith(LEGACY_LAMBDATEST_STATUS_PREFIX)
                || script.startsWith(LEGACY_LAMBDATEST_COMMENT_PREFIX);
    }

    private Object executeCloudControlScript(String script) {
        if (script.startsWith(BROWSERSTACK_EXECUTOR_PREFIX) || script.startsWith(LAMBDATEST_ACTION_PREFIX)) {
            return executeProviderAction(script);
        }
        if (script.startsWith(LEGACY_LAMBDATEST_NAME_PREFIX)) {
            return executeProviderAction(buildLambdaTestNameCommand(script.substring(LEGACY_LAMBDATEST_NAME_PREFIX.length())));
        }
        if (script.startsWith(LEGACY_LAMBDATEST_STATUS_PREFIX)) {
            pendingLambdaTestStatus = script.substring(LEGACY_LAMBDATEST_STATUS_PREFIX.length());
            return pendingLambdaTestStatus;
        }
        if (script.startsWith(LEGACY_LAMBDATEST_COMMENT_PREFIX)) {
            String status = null == pendingLambdaTestStatus || pendingLambdaTestStatus.isBlank()
                    ? "passed"
                    : pendingLambdaTestStatus;
            pendingLambdaTestStatus = null;
            return executeProviderAction(buildLambdaTestStatusCommand(status,
                    script.substring(LEGACY_LAMBDATEST_COMMENT_PREFIX.length())));
        }
        return null;
    }

    private Object executeProviderAction(String command) {
        try {
            return session.page().evaluate("_ => {}", command);
        } catch (RuntimeException exception) {
            throw new WebDriverException("Unable to execute Playwright Java cloud command: " + command, exception);
        }
    }

    private String buildLambdaTestNameCommand(String testName) {
        JSONObject action = new JSONObject()
                .put("action", "setTestName")
                .put("arguments", new JSONObject().put("name", testName));
        return LAMBDATEST_ACTION_PREFIX + " " + action;
    }

    private String buildLambdaTestStatusCommand(String status, String remark) {
        JSONObject action = new JSONObject()
                .put("action", "setTestStatus")
                .put("arguments", new JSONObject()
                        .put("status", status)
                        .put("remark", remark));
        return LAMBDATEST_ACTION_PREFIX + " " + action;
    }

    private void closeSession() {
        writeConsoleLog();
        stopTracing();
        try {
            session.page().close();
        } catch (RuntimeException ignored) {
        }
        try {
            session.browserContext().close();
        } catch (RuntimeException ignored) {
        }
        PlaywrightJavaRuntime runtime = session.runtime();
        if (runtime.decrementSessions() != 0) {
            return;
        }
        try {
            runtime.browser().close();
        } catch (RuntimeException ignored) {
        }
        runtime.playwright().close();
    }

    private void writeConsoleLog() {
        try {
            Files.write(session.consoleFile(), session.consoleMessages(), StandardCharsets.UTF_8);
        } catch (Exception ignored) {
        }
    }

    private void stopTracing() {
        try {
            session.browserContext().tracing().stop(new Tracing.StopOptions().setPath(session.traceFile()));
        } catch (RuntimeException ignored) {
        }
    }

    private UnsupportedOperationException unsupported(String operation) {
        return new UnsupportedOperationException(
                "Playwright Java WebDriver does not support " + operation + " yet");
    }
}
