package com.znsio.teswiz.testng;

import com.znsio.teswiz.businessLayer.googlesearch.GoogleSearchBL;
import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.entities.SAMPLE_TEST_CONTEXT;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;
import org.testng.annotations.Test;

public class GoogleSearchWebTestNgTest {
    @Test(groups = {"web", "googlesearch"})
    public void searchForIndiaOnChromeWeb() {
        TestExecutionContext context = Runner.getTestExecutionContext(Thread.currentThread().getId());
        Drivers.createDriverFor(SAMPLE_TEST_CONTEXT.ME, "chrome-web", "chrome", Platform.web, context);
        new GoogleSearchBL(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform()).searchFor("india");
    }
}
