package com.znsio.teswiz.tools;

import com.context.TestExecutionContext;
import com.epam.reportportal.service.ReportPortal;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Heartbeat implements Runnable{
    private static final Logger LOGGER = LogManager.getLogger(Heartbeat.class.getName());
    private Thread localThread;
    private static TestExecutionContext mainThreadContext = null;
    private static final String DEBUG = "DEBUG";
    private String participantName;


    @Override
    public void run() {
        while (!this.localThread.isInterrupted()) {
            try {
                LOGGER.info(String.format("Thread alert for participant '%s'", this.participantName));
                Thread.sleep(50000);
                LOGGER.info(String.format("Thread slept, now alert for participant '%s'", this.participantName));
            } catch (Exception e) {
                String exceptionMessage = String.format("Exception while hitting roomConnectionStatus API for participant '%s':\n%s", this.participantName, e.getLocalizedMessage());
                LOGGER.info(exceptionMessage);
                ReportPortal.emitLog(exceptionMessage, DEBUG, new Date());
                this.localThread.interrupt();
                removeHeartbeatFor(this.participantName);
            }
        }
        String threadKilledMessage = String.format("overridden run: thread killed for participant '%s'", this.participantName);
        LOGGER.info(threadKilledMessage);
    }

    public void startHeartbeat (String participantName) {
        this.participantName = participantName;
        this.localThread = new Thread(this);
        this.localThread.start();
        String logMessage = String.format("startHeartbeat: thread '%d' started for participant '%s'", this.localThread.getId(), participantName);
        LOGGER.info(logMessage);
        ReportPortal.emitLog(logMessage, DEBUG, new Date());
    }

    public void stopHeartbeat () {
        if (null == this.localThread) {
            return;
        }

        if (!this.localThread.isAlive()) {
            String logMessage = String.format("stopHeartbeat: thread '%d' for participant '%s' not alive", this.localThread.getId(), this.participantName);
            LOGGER.info(logMessage);
            ReportPortal.emitLog(logMessage, DEBUG, new Date());
            return;
        }

        String logMessage = String.format("stopHeartbeat: killing the thread '%d' for participant '%s'", this.localThread.getId(), this.participantName);
        LOGGER.info(logMessage);
        ReportPortal.emitLog(logMessage, DEBUG, new Date());
        this.localThread.interrupt();
    }

    private static void removeHeartbeatFor (String participant) {
        String logMessage;
        if (null == mainThreadContext) {
            logMessage = "removeHeartbeatFor: main thread context found null";
            LOGGER.info(logMessage);
            ReportPortal.emitLog(logMessage, DEBUG, new Date());
            return;
        }

        Map<String, Heartbeat> heartbeatMap;
        if (null != mainThreadContext.getTestState(TEST_CONTEXT.HEARTBEAT_MAP)) {
            heartbeatMap = (Map<String, Heartbeat>) mainThreadContext.getTestState(TEST_CONTEXT.HEARTBEAT_MAP);

            heartbeatMap.remove(participant);
            mainThreadContext.addTestState(TEST_CONTEXT.HEARTBEAT_MAP, heartbeatMap);
            logMessage = String.format("removeHeartbeatFor: '%s' removed from Heartbeat map", participant);

        } else {
            logMessage = "removeHeartbeatFor: Heartbeat map in main thread context found null";
        }
        LOGGER.info(logMessage);
        ReportPortal.emitLog(logMessage, DEBUG, new Date());
    }

    public static void initializeHeartbeatFor (String participantName, @NotNull TestExecutionContext context) {
        Map<String, Heartbeat> heartbeatMap = new HashMap<>();
        if (null != context.getTestState(TEST_CONTEXT.HEARTBEAT_MAP)) {
            heartbeatMap = (Map<String, Heartbeat>) context.getTestState(TEST_CONTEXT.HEARTBEAT_MAP);
        }

        heartbeatMap.put(participantName, new Heartbeat());
        context.addTestState(TEST_CONTEXT.HEARTBEAT_MAP, heartbeatMap);

        mainThreadContext = context;
    }
}
