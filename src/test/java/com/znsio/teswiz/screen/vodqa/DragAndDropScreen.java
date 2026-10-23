package com.znsio.teswiz.screen.vodqa;


public abstract class DragAndDropScreen {

    public static DragAndDropScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(DragAndDropScreen.class);
    }

    public abstract boolean isMessageVisible();
    public abstract DragAndDropScreen dragAndDropCircleObject();
}
