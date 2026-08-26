package com.znsio.teswiz.testng;

import com.znsio.teswiz.businessLayer.jiomeet.AuthBL;
import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.SAMPLE_TEST_CONTEXT;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;
import org.testng.annotations.Test;

import java.util.Map;

public class JioMeetMicSettingsTestNgTest {
    @Test(groups = {"jiomeet", "single-user"})
    public void userShouldBeAbleToChangeTheMicSettings() {
        Map userDetails = createDriverForRegisteredHost();

        new AuthBL(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform())
                .signIn(userDetails)
                .startInstantMeeting()
                .unmuteMyself()
                .muteMyself();
    }

    private Map createDriverForRegisteredHost() {
        TestExecutionContext context = Runner.getTestExecutionContext(Thread.currentThread().getId());
        Map userDetails = Runner.getTestDataAsMap("Host");
        Drivers.createDriverFor(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform(), context);
        context.addTestState(SAMPLE_TEST_CONTEXT.ME, String.valueOf(userDetails.get("username")));
        return userDetails;
    }
}
