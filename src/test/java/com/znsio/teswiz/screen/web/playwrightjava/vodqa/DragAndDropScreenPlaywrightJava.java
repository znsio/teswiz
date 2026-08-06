package com.znsio.teswiz.screen.web.playwrightjava.vodqa;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.vodqa.DragAndDropScreen;

public class DragAndDropScreenPlaywrightJava extends DragAndDropScreen {
    private static final String ENGINE_NAME = "playwright-java";

    public DragAndDropScreenPlaywrightJava(Driver driver, Visual visually) {
    }

    @Override
    public boolean isMessageVisible() {
        throw unsupported("message visibility");
    }

    @Override
    public DragAndDropScreen dragAndDropCircleObject() {
        throw unsupported("drag and drop");
    }

    private UnsupportedOperationException unsupported(String action) {
        return new UnsupportedOperationException(String.format(
                "Vodqa drag and drop %s is not supported on web for WEB_ENGINE=%s.",
                action, ENGINE_NAME));
    }
}
