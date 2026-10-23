package com.znsio.teswiz.screen.web.playwrightjava;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.web.playwrightjava.autoscroll.AutoScrollScreenPlaywrightJava;
import com.znsio.teswiz.screen.web.playwrightjava.calculator.CalculatorScreenPlaywrightJava;
import com.znsio.teswiz.screen.web.playwrightjava.calculator.NewCalculatorScreenPlaywrightJava;
import com.znsio.teswiz.screen.web.playwrightjava.duckduckgo.DuckDuckGoScreenPlaywrightJava;
import com.znsio.teswiz.screen.web.playwrightjava.helloWorld.HelloWorldScreenPlaywrightJava;
import com.znsio.teswiz.screen.web.playwrightjava.jiocinema.JioCinemaScreenPlaywrightJava;
import com.znsio.teswiz.screen.web.playwrightjava.notepad.NotepadScreenPlaywrightJava;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class PlaywrightJavaUnsupportedWebContractsTest {
    private final Driver driver = mock(Driver.class);
    private final Visual visual = mock(Visual.class);

    @Test
    void shouldFailFastForHelloWorld() {
        assertThatThrownBy(() -> new HelloWorldScreenPlaywrightJava(driver, visual).generateRandomNumber(1))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("HelloWorld")
                .hasMessageContaining("playwright-java");
    }

    @Test
    void shouldFailFastForNotepad() {
        assertThatThrownBy(() -> new NotepadScreenPlaywrightJava(driver, visual).typeMessage("hello"))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("Notepad")
                .hasMessageContaining("playwright-java");
    }

    @Test
    void shouldFailFastForDuckDuckGo() {
        assertThatThrownBy(() -> new DuckDuckGoScreenPlaywrightJava(driver, visual).launchBrowser())
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("DuckDuckGo")
                .hasMessageContaining("playwright-java");
    }

    @Test
    void shouldFailFastForAutoScroll() {
        assertThatThrownBy(() -> new AutoScrollScreenPlaywrightJava(driver, visual).goToDropdownWindow())
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("AutoScroll")
                .hasMessageContaining("playwright-java");
    }

    @Test
    void shouldFailFastForCalculator() {
        assertThatThrownBy(() -> new CalculatorScreenPlaywrightJava(driver, visual).handlePopupIfPresent())
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("Calculator")
                .hasMessageContaining("playwright-java");
    }

    @Test
    void shouldFailFastForNewCalculator() {
        assertThatThrownBy(() -> new NewCalculatorScreenPlaywrightJava(driver, visual).launch())
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("NewCalculator")
                .hasMessageContaining("playwright-java");
    }

    @Test
    void shouldFailFastForJioCinema() {
        assertThatThrownBy(() -> new JioCinemaScreenPlaywrightJava(driver, visual).swipeRight())
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("JioCinema")
                .hasMessageContaining("playwright-java");
    }
}
