package com.znsio.teswiz.runner;

import org.json.JSONObject;
import org.openqa.selenium.By;

record PlaywrightLocatorReference(PlaywrightLocator locator, int index, PlaywrightLocatorReference parent) {
    static PlaywrightLocatorReference root(By by) {
        return new PlaywrightLocatorReference(PlaywrightLocator.from(by), 0, null);
    }

    PlaywrightLocatorReference child(By by, int childIndex) {
        return new PlaywrightLocatorReference(PlaywrightLocator.from(by), childIndex, this);
    }

    JSONObject toJson() {
        JSONObject json = new JSONObject()
                .put("strategy", locator.strategy())
                .put("value", locator.value())
                .put("index", index);
        if (null != parent) {
            json.put("parent", parent.toJson());
        }
        return json;
    }
}
