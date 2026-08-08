package com.znsio.teswiz.web.playwright;

import org.json.JSONArray;
import org.openqa.selenium.WebDriver;

import com.znsio.teswiz.exceptions.InvalidTestDataException;
import com.znsio.teswiz.runner.Driver;

public class PlaywrightTsScreenActionExecutor {
    private final Driver driver;

    public PlaywrightTsScreenActionExecutor(Driver driver) {
        this.driver = driver;
    }

    public void run(String screenModule, String actionName, Object... arguments) {
        playwrightDriver().runScreenAction(screenModule, actionName, toJsonArray(arguments));
    }

    public String runForString(String screenModule, String actionName, Object... arguments) {
        Object value = playwrightDriver().runScreenAction(screenModule, actionName, toJsonArray(arguments));
        return null == value ? "" : String.valueOf(value);
    }

    private PlaywrightWebDriver playwrightDriver() {
        WebDriver innerDriver = driver.getInnerDriver();
        if (!(innerDriver instanceof PlaywrightWebDriver playwrightWebDriver)) {
            throw new InvalidTestDataException("Current driver is not backed by the playwright-ts web engine");
        }
        return playwrightWebDriver;
    }

    private JSONArray toJsonArray(Object... arguments) {
        JSONArray values = new JSONArray();
        if (null == arguments) {
            return values;
        }
        for (Object argument : arguments) {
            values.put(argument);
        }
        return values;
    }
}
