package com.znsio.e2e.screen.web;

import com.znsio.e2e.screen.*;
import com.znsio.e2e.tools.*;
import org.apache.log4j.*;

public class EchoScreenWeb extends EchoScreen {
    private static final String NOT_YET_IMPLEMENTED = "NOT_YET_IMPLEMENTED";
    private static final Logger LOGGER = Logger.getLogger(EchoScreenWeb.class.getName());
    private final Driver driver;
    private final Visual visually;
    private final String SCREEN_NAME = EchoScreenWeb.class.getSimpleName();

    public EchoScreenWeb(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public EchoScreen echoMessage(String message) {
        LOGGER.info("Skipping this step for Web");
        return this;
    }
}
