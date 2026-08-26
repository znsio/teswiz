package com.znsio.teswiz.testng;

import com.znsio.teswiz.businessLayer.search.SearchBL;
import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;
import org.testng.annotations.Test;

public class MultiUserWebSearchTestNgTest {
    private static final String PERSONA_ONE = "someone";
    private static final String PERSONA_TWO = "someone-else";

    @Test(groups = {"web", "multiuser"})
    public void twoPersonasOrchestrateDifferentWebsites() {
        createDriversForBothPersonas();

        new SearchBL(PERSONA_ONE, Runner.getPlatformForUser(PERSONA_ONE))
                .searchFor("bear");
        new SearchBL(PERSONA_TWO, Runner.getPlatformForUser(PERSONA_TWO))
                .searchFor("tiger");
    }

    private void createDriversForBothPersonas() {
        TestExecutionContext context = Runner.getTestExecutionContext(Thread.currentThread().getId());
        Drivers.createDriverFor(PERSONA_ONE, "images", "chrome", Platform.web, context);
        Drivers.createDriverFor(PERSONA_TWO, "bing", "firefox", Platform.web, context);
    }
}
