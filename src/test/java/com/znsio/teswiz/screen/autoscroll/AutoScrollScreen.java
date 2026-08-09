package com.znsio.teswiz.screen.autoscroll;

import com.znsio.teswiz.entities.Direction;

public abstract class AutoScrollScreen {

    public static AutoScrollScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(AutoScrollScreen.class);
    }

    public abstract AutoScrollScreen goToDropdownWindow();

    public abstract AutoScrollScreen scrollInDynamicLayer(Direction direction);

    public abstract boolean isScrollSuccessful();
}
