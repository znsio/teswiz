package com.znsio.teswiz.tools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Wait {
    private static final Logger LOGGER = LogManager.getLogger(Wait.class.getName());

    public synchronized static void waitFor(int seconds) {
        LOGGER.info("Wait for " + seconds + " seconds");
        try {
            Thread.sleep(seconds * 1000L);
        } catch(InterruptedException e) {
        }
    }
}
