package com.znsio.sample.e2e.businessLayer.jiomeet;

import com.context.TestExecutionContext;
import com.znsio.e2e.entities.Platform;
import com.znsio.e2e.runner.Runner;
import com.znsio.sample.e2e.entities.SAMPLE_TEST_CONTEXT;
import com.znsio.sample.e2e.screen.jiomeet.SignInScreen;
import org.apache.log4j.Logger;
import org.assertj.core.api.SoftAssertions;

import static org.assertj.core.api.Assertions.assertThat;

public class JoinAMeetingBL {
    private static final Logger LOGGER = Logger.getLogger(JoinAMeetingBL.class.getName());
    private final TestExecutionContext context;
    private final SoftAssertions softly;
    private final String currentUserPersona;
    private final Platform currentPlatform;

    public JoinAMeetingBL(String userPersona, Platform forPlatform) {
        long threadId = Thread.currentThread()
                              .getId();
        this.context = Runner.getTestExecutionContext(threadId);
        softly = Runner.getSoftAssertion(threadId);
        this.currentUserPersona = userPersona;
        this.currentPlatform = forPlatform;
        Runner.setCurrentDriverForUser(userPersona, forPlatform, context);
    }

    public JoinAMeetingBL() {
        long threadId = Thread.currentThread()
                              .getId();
        this.context = Runner.getTestExecutionContext(threadId);
        softly = Runner.getSoftAssertion(threadId);
        this.currentUserPersona = SAMPLE_TEST_CONTEXT.ME;
        this.currentPlatform = Runner.platform;
    }

    public InAMeetingBL joinMeeting(String meetingId, String meetingPassword) {
        boolean hasMeetingStarted = SignInScreen.get()
                                                .joinAMeeting(meetingId, meetingPassword, currentUserPersona)
                                                .isMeetingStarted();
        assertThat(hasMeetingStarted).as(currentUserPersona + " not yet joined the meeting")
                                     .isTrue();
        return new InAMeetingBL();
    }
}
