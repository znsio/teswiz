package com.znsio.teswiz.web.playwright;

import java.nio.file.Path;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.MutableCapabilities;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;
import com.microsoft.playwright.options.Proxy;
import com.znsio.teswiz.config.browser.PlaywrightBrowserConfig;
import com.znsio.teswiz.config.browser.PlaywrightBrowserConfigResolver;
import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import com.znsio.teswiz.exceptions.InvalidTestDataException;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.session.SessionHandle;
import com.znsio.teswiz.web.WebEngine;
import com.znsio.teswiz.web.browser.WebDriverSessionResult;
import com.znsio.teswiz.web.provider.playwright.PlaywrightExecutionProviderConfig;
import com.znsio.teswiz.web.provider.playwright.PlaywrightExecutionProviderConfigResolver;
import com.znsio.teswiz.web.provider.playwright.PlaywrightRemoteConnectionResolver;
import com.znsio.teswiz.web.selenium.WebBaseUrlResolver;

public final class PlaywrightJavaDriverManager {
    private final BiFunction<String, TestExecutionContext, PlaywrightBrowserConfig> browserConfigLookup;
    private final Supplier<PlaywrightExecutionProviderConfig> providerConfigSupplier;
    private final PlaywrightJavaRuntimeFactory runtimeFactory;

    public PlaywrightJavaDriverManager() {
        this(new PlaywrightBrowserConfigResolver()::resolve,
                new PlaywrightExecutionProviderConfigResolver()::resolve,
                new DefaultPlaywrightJavaRuntimeFactory());
    }

    PlaywrightJavaDriverManager(BiFunction<String, TestExecutionContext, PlaywrightBrowserConfig> browserConfigLookup,
            Supplier<PlaywrightExecutionProviderConfig> providerConfigSupplier, PlaywrightJavaRuntimeFactory runtimeFactory) {
        this.browserConfigLookup = browserConfigLookup;
        this.providerConfigSupplier = providerConfigSupplier;
        this.runtimeFactory = runtimeFactory;
    }

    public WebDriverSessionResult createWebSessionForUser(String userPersona, String browserName, Platform forPlatform,
            TestExecutionContext context) {
        PlaywrightBrowserConfig browserConfig = browserConfigLookup.apply(browserName, context);
        PlaywrightExecutionProviderConfig providerConfig = providerConfigSupplier.get();
        PlaywrightJavaSession session = createSession(userPersona, browserConfig, providerConfig, context);
        PlaywrightJavaWebDriver webDriver = new PlaywrightJavaWebDriver(session);
        String baseUrl = WebBaseUrlResolver.resolve(Drivers.getAppNamefor(userPersona));
        webDriver.get(baseUrl);

        SessionHandle sessionHandle = buildSessionHandle(userPersona, forPlatform, context, session, browserConfig,
                providerConfig);
        return new WebDriverSessionResult(webDriver, browserConfig.headless(),
                createCapabilities(browserConfig, sessionHandle), sessionHandle);
    }

    public void closeWebDriver(String userPersona, Driver driver) {
        if (null != driver.getInnerDriver()) {
            driver.getInnerDriver().quit();
        }
    }

    private SessionHandle buildSessionHandle(String userPersona, Platform forPlatform, TestExecutionContext context,
            PlaywrightJavaSession session, PlaywrightBrowserConfig browserConfig,
            PlaywrightExecutionProviderConfig providerConfig) {
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("browserName", browserConfig.browserName());
        metadata.put("browserVersion", session.runtime().browser().version());
        metadata.put("provider", providerConfig.providerName());
        metadata.put("playwrightSessionId", session.sessionId());
        metadata.put("traceFile", session.traceFile().getFileName().toString());
        metadata.put("harFile", session.harFile().getFileName().toString());
        metadata.put("consoleFile", session.consoleFile().getFileName().toString());
        addProviderMetadata(providerConfig, metadata);
        return new SessionHandle(userPersona, forPlatform, WebEngine.PLAYWRIGHT_JAVA.getConfigValue(),
                session.sessionId(), context.getTestStateAsString(TEST_CONTEXT.SCENARIO_LOG_DIRECTORY), metadata);
    }

    private void addProviderMetadata(PlaywrightExecutionProviderConfig providerConfig, Map<String, String> metadata) {
        if (providerConfig.isRemote()) {
            metadata.put("remoteUrl", providerConfig.remoteUrl());
        }
        if (null != providerConfig.apiUrl() && !providerConfig.apiUrl().isBlank()) {
            metadata.put("apiUrl", providerConfig.apiUrl());
        }
    }

    private Capabilities createCapabilities(PlaywrightBrowserConfig browserConfig, SessionHandle sessionHandle) {
        MutableCapabilities capabilities = new MutableCapabilities();
        capabilities.setCapability("browserName", browserConfig.browserName());
        capabilities.setCapability("engine", WebEngine.PLAYWRIGHT_JAVA.getConfigValue());
        capabilities.setCapability("playwrightSessionId", sessionHandle.sessionId());
        return capabilities;
    }

    interface PlaywrightJavaRuntimeFactory {
        PlaywrightJavaRuntime create(PlaywrightBrowserConfig browserConfig, Path artifactDirectory,
                PlaywrightExecutionProviderConfig providerConfig, String userPersona);
    }

    private PlaywrightJavaSession createSession(String userPersona, PlaywrightBrowserConfig browserConfig,
            PlaywrightExecutionProviderConfig providerConfig, TestExecutionContext context) {
        Path artifactDirectory = Path.of(context.getTestStateAsString(TEST_CONTEXT.SCENARIO_LOG_DIRECTORY));
        PlaywrightJavaRuntime runtime = new PlaywrightJavaRuntimePool(context, runtimeFactory)
                .getOrCreate(browserConfig, artifactDirectory, providerConfig, userPersona);
        runtime.incrementSessions();
        String sessionId = UUID.randomUUID().toString();
        Path traceFile = artifactDirectory.resolve(userPersona + "-" + sessionId + "-trace.zip");
        Path harFile = artifactDirectory.resolve(userPersona + "-" + sessionId + "-network.har");
        Path consoleFile = artifactDirectory.resolve(userPersona + "-" + sessionId + "-console.log");
        BrowserContext browserContext = runtime.browser().newContext(
                DefaultPlaywrightJavaRuntimeFactory.buildContextOptions(browserConfig, harFile));
        browserContext.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true)
                .setSources(true));
        List<String> consoleMessages = new CopyOnWriteArrayList<>();
        com.microsoft.playwright.Page page = browserContext.newPage();
        page.onConsoleMessage(message -> consoleMessages.add("[%s] %s".formatted(message.type(), message.text())));
        return new PlaywrightJavaSession(sessionId, userPersona, runtime, browserConfig, browserContext, page, traceFile,
                harFile, consoleFile, consoleMessages);
    }

    private static final class DefaultPlaywrightJavaRuntimeFactory implements PlaywrightJavaRuntimeFactory {
        private final PlaywrightRemoteConnectionResolver remoteConnectionResolver = new PlaywrightRemoteConnectionResolver();

        @Override
        public PlaywrightJavaRuntime create(PlaywrightBrowserConfig browserConfig, Path artifactDirectory,
                PlaywrightExecutionProviderConfig providerConfig, String userPersona) {
            Playwright playwright = Playwright.create();
            BrowserType browserType = resolveBrowserType(playwright, browserConfig.browserName());
            Browser browser = remoteConnectionResolver
                    .resolve(browserConfig.browserName(), browserConfig, providerConfig, userPersona)
                    .map(remoteConnection -> browserType.connect(remoteConnection.wsEndpoint()))
                    .orElseGet(() -> browserType.launch(buildLaunchOptions(browserConfig)));
            return new PlaywrightJavaRuntime(playwright, browser);
        }

        private static BrowserType resolveBrowserType(Playwright playwright, String browserName) {
            return switch (browserName.toLowerCase()) {
                case "chrome", "chromium", "msedge", "edge" -> playwright.chromium();
                case "firefox" -> playwright.firefox();
                case "safari", "webkit" -> playwright.webkit();
                default -> throw new InvalidTestDataException(
                        String.format("Browser: '%s' is NOT supported for Playwright Java", browserName));
            };
        }

        private static BrowserType.LaunchOptions buildLaunchOptions(PlaywrightBrowserConfig browserConfig) {
            BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
                    .setHeadless(browserConfig.headless())
                    .setArgs(browserConfig.launchArgs());

            if (null != browserConfig.channel() && !browserConfig.channel().isBlank()) {
                options.setChannel(browserConfig.channel());
            } else {
                maybeSetDefaultChannel(browserConfig.browserName(), options);
            }

            if (null != browserConfig.executablePath() && !browserConfig.executablePath().isBlank()) {
                options.setExecutablePath(Path.of(browserConfig.executablePath()));
            }

            Map<String, Object> launchOptions = browserConfig.launchOptions();
            if (launchOptions.containsKey("proxy") && launchOptions.get("proxy") instanceof Map<?, ?> proxyMap) {
                options.setProxy(toProxy(proxyMap));
            }
            return options;
        }

        private static void maybeSetDefaultChannel(String browserName, BrowserType.LaunchOptions options) {
            if ("chrome".equalsIgnoreCase(browserName)) {
                options.setChannel("chrome");
            } else if ("edge".equalsIgnoreCase(browserName) || "msedge".equalsIgnoreCase(browserName)) {
                options.setChannel("msedge");
            }
        }

        private static Browser.NewContextOptions buildContextOptions(PlaywrightBrowserConfig browserConfig, Path harFile) {
            Browser.NewContextOptions options = new Browser.NewContextOptions();
            Map<String, Object> contextOptions = browserConfig.contextOptions();
            if (contextOptions.containsKey("ignoreHTTPSErrors")) {
                options.setIgnoreHTTPSErrors(Boolean.TRUE.equals(contextOptions.get("ignoreHTTPSErrors")));
            }
            if (contextOptions.containsKey("baseURL") && contextOptions.get("baseURL") instanceof String baseUrl
                    && !baseUrl.isBlank()) {
                options.setBaseURL(baseUrl);
            }
            options.setRecordHarPath(harFile);
            return options;
        }

        private static Proxy toProxy(Map<?, ?> proxyMap) {
            Object server = proxyMap.get("server");
            if (null == server) {
                throw new InvalidTestDataException("Playwright Java proxy requires a server value");
            }
            Proxy proxy = new Proxy(String.valueOf(server));
            Object bypass = proxyMap.get("bypass");
            if (null != bypass) {
                proxy.setBypass(String.valueOf(bypass));
            }
            Object username = proxyMap.get("username");
            if (null != username) {
                proxy.setUsername(String.valueOf(username));
            }
            Object password = proxyMap.get("password");
            if (null != password) {
                proxy.setPassword(String.valueOf(password));
            }
            return proxy;
        }
    }
}
