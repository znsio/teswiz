package com.znsio.teswiz.testng.fixtures;

import org.testng.annotations.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ThreadRecordingTestNgTest {
    public static final List<Long> observedThreadIds = new CopyOnWriteArrayList<>();

    @Test(groups = "fixture")
    public void firstMethodRecordsItsThread() throws InterruptedException {
        recordCurrentThreadAndPause();
    }

    @Test(groups = "fixture")
    public void secondMethodRecordsItsThread() throws InterruptedException {
        recordCurrentThreadAndPause();
    }

    private void recordCurrentThreadAndPause() throws InterruptedException {
        observedThreadIds.add(Thread.currentThread().getId());
        Thread.sleep(200);
    }
}
