package com.znsio.e2e.tools;

public class Wait {
    public synchronized static void waitFor (int seconds) {
        System.out.printf("Wait for '%d' seconds%n", seconds);
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
        }
    }
}
