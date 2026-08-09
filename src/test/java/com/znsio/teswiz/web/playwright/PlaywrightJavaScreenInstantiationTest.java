package com.znsio.teswiz.web.playwright;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.znsio.teswiz.config.browser.PlaywrightBrowserConfig;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.ScreenInstanceFactory;
import com.znsio.teswiz.web.playwright.screen.PlaywrightJavaScreenContext;

class PlaywrightJavaScreenInstantiationTest {
    @Test
    void shouldInstantiatePlaywrightJavaScreensUsingNativeScreenContext() {
        Page page = mock(Page.class);
        BrowserContext browserContext = mock(BrowserContext.class);
        PlaywrightJavaWebDriver webDriver = new PlaywrightJavaWebDriver(session(page, browserContext));
        Driver driver = mock(Driver.class);
        Visual visual = mock(Visual.class);
        when(driver.getInnerDriver()).thenReturn(webDriver);

        NativePlaywrightJavaScreen screen = ScreenInstanceFactory.create(NativePlaywrightJavaScreen.class, driver,
                visual);

        assertThat(screen.page()).isSameAs(page);
        assertThat(screen.browserContext()).isSameAs(browserContext);
        assertThat(screen.visual()).isSameAs(visual);
        assertThat(screen.driver()).isSameAs(driver);
    }

    @Test
    void shouldFailFastWhenNativePlaywrightJavaScreenIsUsedWithoutAPlaywrightJavaDriver() {
        Driver driver = mock(Driver.class);
        when(driver.getInnerDriver()).thenReturn(mock(WebDriver.class));

        assertThatThrownBy(() -> ScreenInstanceFactory.create(NativePlaywrightJavaScreen.class, driver, mock(Visual.class)))
                .hasMessageContaining("requires a Playwright Java screen context");
    }

    @Test
    void shouldContinueInstantiatingLegacyDriverBasedScreens() {
        Driver driver = mock(Driver.class);
        Visual visual = mock(Visual.class);

        LegacyScreen screen = ScreenInstanceFactory.create(LegacyScreen.class, driver, visual);

        assertThat(screen.driver()).isSameAs(driver);
        assertThat(screen.visual()).isSameAs(visual);
    }

    private static PlaywrightJavaSession session(Page page, BrowserContext browserContext) {
        return new PlaywrightJavaSession(
                "session-1",
                "buyer",
                new PlaywrightJavaRuntime(mock(com.microsoft.playwright.Playwright.class), mock(Browser.class)),
                new PlaywrightBrowserConfig("chrome", true, List.of("--headless=new"), null, null, Map.of(), Map.of()),
                browserContext,
                page,
                Path.of("trace.zip"),
                Path.of("network.har"),
                Path.of("console.log"),
                List.of());
    }

    public static final class NativePlaywrightJavaScreen {
        private final PlaywrightJavaScreenContext context;

        public NativePlaywrightJavaScreen(PlaywrightJavaScreenContext context) {
            this.context = context;
        }

        Page page() {
            return context.page();
        }

        BrowserContext browserContext() {
            return context.browserContext();
        }

        Visual visual() {
            return context.visual();
        }

        Driver driver() {
            return context.driver();
        }
    }

    public static final class LegacyScreen {
        private final Driver driver;
        private final Visual visual;

        public LegacyScreen(Driver driver, Visual visual) {
            this.driver = driver;
            this.visual = visual;
        }

        Driver driver() {
            return driver;
        }

        Visual visual() {
            return visual;
        }
    }
}
