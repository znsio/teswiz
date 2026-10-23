package com.znsio.teswiz.web.playwright;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.znsio.teswiz.config.browser.PlaywrightBrowserConfig;

class PlaywrightJavaCloudCommandRoutingTest {
    @Test
    void shouldRouteBrowserStackExecutorCommandsThroughPageEvaluate() {
        Page page = mock(Page.class);
        when(page.evaluate(anyString(), anyString())).thenReturn(null);
        PlaywrightJavaWebDriver webDriver = new PlaywrightJavaWebDriver(session(page));

        webDriver.executeScript(
                "browserstack_executor: {\"action\":\"setSessionStatus\",\"arguments\":{\"status\":\"passed\",\"reason\":\"ok\"}}");

        verify(page).evaluate(eq("_ => {}"),
                eq("browserstack_executor: {\"action\":\"setSessionStatus\",\"arguments\":{\"status\":\"passed\",\"reason\":\"ok\"}}"));
    }

    @Test
    void shouldTranslateLegacyLambdaTestNameCommandToPlaywrightAction() {
        Page page = mock(Page.class);
        when(page.evaluate(anyString(), anyString())).thenReturn(null);
        PlaywrightJavaWebDriver webDriver = new PlaywrightJavaWebDriver(session(page));

        webDriver.executeScript("lambda-name=buyer-test");

        ArgumentCaptor<String> commandCaptor = ArgumentCaptor.forClass(String.class);
        verify(page).evaluate(eq("_ => {}"), commandCaptor.capture());
        assertThat(commandCaptor.getValue())
                .contains("lambdatest_action:")
                .contains("\"action\":\"setTestName\"")
                .contains("\"name\":\"buyer-test\"");
    }

    @Test
    void shouldTranslateLegacyLambdaTestStatusAndCommentCommandsIntoSinglePlaywrightAction() {
        Page page = mock(Page.class);
        when(page.evaluate(anyString(), anyString())).thenReturn(null);
        PlaywrightJavaWebDriver webDriver = new PlaywrightJavaWebDriver(session(page));

        Object status = webDriver.executeScript("lambda-status=failed");
        webDriver.executeScript("lambda-comment=Assertion failure");

        assertThat(status).isEqualTo("failed");
        verify(page, never()).evaluate(eq("_ => {}"),
                eq("lambda-status=failed"));

        ArgumentCaptor<String> commandCaptor = ArgumentCaptor.forClass(String.class);
        verify(page).evaluate(eq("_ => {}"), commandCaptor.capture());
        assertThat(commandCaptor.getValue())
                .contains("lambdatest_action:")
                .contains("\"action\":\"setTestStatus\"")
                .contains("\"status\":\"failed\"")
                .contains("\"remark\":\"Assertion failure\"");
    }

    private static PlaywrightJavaSession session(Page page) {
        return new PlaywrightJavaSession(
                "session-1",
                "buyer",
                new PlaywrightJavaRuntime(mock(com.microsoft.playwright.Playwright.class), mock(Browser.class)),
                new PlaywrightBrowserConfig("chrome", true, List.of("--headless=new"), null, null, Map.of(), Map.of()),
                mock(BrowserContext.class),
                page,
                Path.of("trace.zip"),
                Path.of("network.har"),
                Path.of("console.log"),
                List.of());
    }
}
