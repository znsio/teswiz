package com.znsio.teswiz.screen.jiocinema;

import com.znsio.teswiz.entities.Direction;

public abstract class JioCinemaScreen {

    public static JioCinemaScreen get() {
        return com.znsio.teswiz.screen.ScreenRegistry.getScreen(JioCinemaScreen.class);
    }

    public abstract JioCinemaScreen swipeRight();

    public abstract JioCinemaScreen swipeLeft();

    public abstract JioCinemaScreen scrollTillTrendingInIndiaSection();

    public abstract boolean isMovieNumberVisibleOnScreen(int movieNumberOnScreen);

    public abstract JioCinemaScreen swipeTrendingItem(Direction direction, int movieNumberOnScreen);

}
