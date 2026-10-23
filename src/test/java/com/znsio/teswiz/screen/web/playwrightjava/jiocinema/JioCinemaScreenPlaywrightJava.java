package com.znsio.teswiz.screen.web.playwrightjava.jiocinema;

import com.znsio.teswiz.entities.Direction;
import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.jiocinema.JioCinemaScreen;

public class JioCinemaScreenPlaywrightJava extends JioCinemaScreen {
    private static final String ENGINE_NAME = "playwright-java";

    public JioCinemaScreenPlaywrightJava(Driver driver, Visual visually) {
    }

    @Override
    public JioCinemaScreen swipeRight() {
        throw unsupported();
    }

    @Override
    public JioCinemaScreen swipeLeft() {
        throw unsupported();
    }

    @Override
    public JioCinemaScreen scrollTillTrendingInIndiaSection() {
        throw unsupported();
    }

    @Override
    public boolean isMovieNumberVisibleOnScreen(int movieNumberOnScreen) {
        throw unsupported();
    }

    @Override
    public JioCinemaScreen swipeTrendingItem(Direction direction, int movieNumberOnScreen) {
        throw unsupported();
    }

    private UnsupportedOperationException unsupported() {
        return new UnsupportedOperationException(String.format(
                "JioCinema is not supported on web for WEB_ENGINE=%s.",
                ENGINE_NAME));
    }
}
