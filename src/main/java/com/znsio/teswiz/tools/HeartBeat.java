package com.znsio.teswiz.tools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.HttpURLConnection;
import java.net.URL;

public class HeartBeat implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger(HeartBeat.class.getName());
    private final String threadName;
    private final String apiUrl;
    private final int pollingDuration;
    private int heartBeatCounter = 0;
    private volatile boolean running = true; // Flag to control the loop
    private Thread currentThread;

    public HeartBeat(String userPersona, String url, int pollingDurationInSec) {
        this.threadName = userPersona;
        this.apiUrl = url;
        this.pollingDuration = pollingDurationInSec;
    }

    @Override
    public void run() {
        currentThread = Thread.currentThread();
        currentThread.setName(threadName);
        while(running) { // Continue running as long as the flag is true
            try {
                String message = "HeartBeat: '%s', API is '%s'. Current count: '%d'";
                String status = "up";
                if(!checkHeartBeat()) {
                    status = "down";
                }
                heartBeatCounter++;
                message = String.format(message, this.threadName, status, heartBeatCounter);
                ReportPortalLogger.logInfoMessage(message);
                Thread.sleep(this.pollingDuration * 1000); // Sleep for x seconds
            } catch(InterruptedException e) {
                // do nothing
            }
        }
    }

    private boolean checkHeartBeat() {
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            int responseCode = connection.getResponseCode();
            return responseCode == HttpURLConnection.HTTP_OK;
        } catch(Exception e) {
            return false;
        }
    }

    // Method to fetch the number of heartbeat calls done
    public int getHeartBeatCounter() {
        return heartBeatCounter;
    }

    // Method to stop the heartbeat
    public void stopHeartBeat() {
        String message = String.format("HeartBeat: Stopping heartbeat for user: '%s' with url: '%s' after '%d' counts", this.threadName, this.apiUrl, heartBeatCounter);
        ReportPortalLogger.logInfoMessage(message);
        running = false;
        if (this.currentThread != null) {
            currentThread.interrupt(); // Interrupt the thread if it's sleeping
        }
    }
}
