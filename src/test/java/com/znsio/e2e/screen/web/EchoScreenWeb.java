package com.znsio.e2e.screen.web;

import com.znsio.e2e.screen.EchoScreen;
import com.znsio.e2e.tools.Driver;
import com.znsio.e2e.tools.Visual;
import org.apache.commons.lang3.NotImplementedException;

public class EchoScreenWeb extends EchoScreen {
    private static final String NOT_YET_IMPLEMENTED = "NOT_YET_IMPLEMENTED";
    private final Driver driver;
    private final Visual visually;
    private final String SCREEN_NAME = EchoScreenWeb.class.getSimpleName();

    public EchoScreenWeb(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public EchoScreen echoMessage(String message) {
        throw new NotImplementedException(SCREEN_NAME + ":"
                + new Throwable().getStackTrace()[0].getMethodName()
                + NOT_YET_IMPLEMENTED);
    }
}
