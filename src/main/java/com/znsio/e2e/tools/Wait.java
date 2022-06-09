package com.znsio.e2e.tools;

import org.apache.log4j.Logger;

public class Wait {
    private static final Logger LOGGER = Logger.getLogger(Wait.class.getName());

    public synchronized static void waitFor(int seconds) {
        LOGGER.info("Wait for " + seconds + " seconds");
        try {
            Thread.sleep(seconds * 1000L);
        } catch(InterruptedException e) {
        }
    }
}
