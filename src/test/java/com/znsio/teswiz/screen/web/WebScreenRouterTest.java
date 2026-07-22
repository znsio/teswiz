package com.znsio.teswiz.screen.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.znsio.teswiz.web.WebEngine;

class WebScreenRouterTest {
    @Test
    void shouldRouteToSeleniumSupplierForSeleniumEngine() {
        String selected = WebScreenRouter.forEngine(WebEngine.SELENIUM, "SampleScreen",
                () -> "selenium", () -> "playwright-ts");

        assertThat(selected).isEqualTo("selenium");
    }

    @Test
    void shouldRouteToPlaywrightTsSupplierForPlaywrightTsEngine() {
        String selected = WebScreenRouter.forEngine(WebEngine.PLAYWRIGHT_TS, "SampleScreen",
                () -> "selenium", () -> "playwright-ts");

        assertThat(selected).isEqualTo("playwright-ts");
    }
}
