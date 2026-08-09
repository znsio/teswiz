package com.znsio.teswiz.screen;

import org.apache.commons.lang3.NotImplementedException;
import org.openqa.selenium.WebDriver;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.web.playwright.PlaywrightJavaWebDriver;
import com.znsio.teswiz.web.playwright.screen.PlaywrightJavaScreenContext;

public final class ScreenInstanceFactory {
    private ScreenInstanceFactory() {
    }

    public static <T> T create(Class<? extends T> implementationClass, Driver driver, Visual visually) {
        T playwrightJavaScreen = tryCreatePlaywrightJavaScreen(implementationClass, driver, visually);
        if (null != playwrightJavaScreen) {
            return playwrightJavaScreen;
        }
        return createLegacyScreen(implementationClass, driver, visually);
    }

    private static <T> T createLegacyScreen(Class<? extends T> implementationClass, Driver driver, Visual visually) {
        try {
            return implementationClass.getConstructor(Driver.class, Visual.class).newInstance(driver, visually);
        } catch (NoSuchMethodException exception) {
            throw new NotImplementedException(
                    "Unable to instantiate screen: " + implementationClass.getName(),
                    exception);
        } catch (ReflectiveOperationException exception) {
            throw new NotImplementedException(
                    "Unable to instantiate screen: " + implementationClass.getName(),
                    exception);
        }
    }

    private static <T> T tryCreatePlaywrightJavaScreen(Class<? extends T> implementationClass, Driver driver,
            Visual visually) {
        try {
            return implementationClass.getConstructor(PlaywrightJavaScreenContext.class)
                    .newInstance(resolvePlaywrightJavaScreenContext(implementationClass, driver, visually));
        } catch (NoSuchMethodException exception) {
            return null;
        } catch (ReflectiveOperationException exception) {
            throw new NotImplementedException(
                    "Unable to instantiate screen: " + implementationClass.getName(),
                    exception);
        }
    }

    private static <T> PlaywrightJavaScreenContext resolvePlaywrightJavaScreenContext(
            Class<? extends T> implementationClass, Driver driver, Visual visually) {
        WebDriver innerDriver = driver.getInnerDriver();
        if (innerDriver instanceof PlaywrightJavaWebDriver playwrightJavaWebDriver) {
            return playwrightJavaWebDriver.createScreenContext(driver, visually);
        }
        throw new NotImplementedException(String.format(
                "Screen %s requires a Playwright Java screen context, but the active driver is %s",
                implementationClass.getName(),
                null == innerDriver ? "null" : innerDriver.getClass().getName()));
    }
}
