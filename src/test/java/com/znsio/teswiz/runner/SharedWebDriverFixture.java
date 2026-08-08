package com.znsio.teswiz.runner;

import org.openqa.selenium.WebDriver;

interface SharedWebDriverFixture extends AutoCloseable {
    WebDriver createDriver(String userPersona);

    @Override
    void close();
}
