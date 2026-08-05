package com.znsio.teswiz.screen.web.theapp;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.web.playwrightjava.theapp.AppLaunchScreenPlaywrightJava;

class TheAppUnsupportedWebActionsTest {
    @Test
    void shouldFailFastForClipboardDemoOnSeleniumWeb() {
        AppLaunchScreenWeb screen = new AppLaunchScreenWeb(mock(Driver.class), mock(Visual.class));

        assertThatThrownBy(screen::goToClipboardDemo)
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("Clipboard Demo")
                .hasMessageContaining("web");
    }

    @Test
    void shouldFailFastForEchoOnSeleniumWeb() {
        AppLaunchScreenWeb screen = new AppLaunchScreenWeb(mock(Driver.class), mock(Visual.class));

        assertThatThrownBy(screen::selectEcho)
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("Echo")
                .hasMessageContaining("web");
    }

    @Test
    void shouldFailFastForClipboardDemoOnPlaywrightJavaWeb() {
        AppLaunchScreenPlaywrightJava screen =
                new AppLaunchScreenPlaywrightJava(mock(Driver.class), mock(Visual.class));

        assertThatThrownBy(screen::goToClipboardDemo)
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("Clipboard Demo")
                .hasMessageContaining("playwright-java");
    }

    @Test
    void shouldFailFastForEchoOnPlaywrightJavaWeb() {
        AppLaunchScreenPlaywrightJava screen =
                new AppLaunchScreenPlaywrightJava(mock(Driver.class), mock(Visual.class));

        assertThatThrownBy(screen::selectEcho)
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("Echo")
                .hasMessageContaining("playwright-java");
    }
}
