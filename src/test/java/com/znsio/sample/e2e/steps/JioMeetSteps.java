package com.znsio.sample.e2e.steps;

import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.znsio.e2e.entities.Platform;
import com.znsio.e2e.exceptions.InvalidTestDataException;
import com.znsio.e2e.runner.Runner;
import com.znsio.e2e.tools.Drivers;
import com.znsio.sample.e2e.businessLayer.jiomeet.AuthBL;
import com.znsio.sample.e2e.businessLayer.jiomeet.InAMeetingBL;
import com.znsio.sample.e2e.businessLayer.jiomeet.JoinAMeetingBL;
import com.znsio.sample.e2e.businessLayer.jiomeet.LandingBL;
import com.znsio.sample.e2e.entities.SAMPLE_TEST_CONTEXT;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.log4j.Logger;

import java.util.Locale;
import java.util.Map;

public class JioMeetSteps {
    private static final Logger LOGGER = Logger.getLogger(JioMeetSteps.class.getName());
    private final TestExecutionContext context;
    private final Drivers allDrivers;

    public JioMeetSteps() {
        context = SessionContext.getTestExecutionContext(Thread.currentThread()
                                                               .getId());
        LOGGER.info("context: " + context.getTestName());
        allDrivers = (Drivers) context.getTestState(SAMPLE_TEST_CONTEXT.ALL_DRIVERS);
        LOGGER.info("allDrivers: " + (null == allDrivers));
    }

    @Given("I sign in as a registered {string}")
    public void iSignInAsARegistered(String userSuffix) {
        Map userDetails = Runner.getTestDataAsMap(userSuffix);
        LOGGER.info(System.out.printf("iSignInAsARegistered - Persona:'%s', User details: '%s', Platform: '%s'", SAMPLE_TEST_CONTEXT.ME, userDetails, Runner.platform));
        allDrivers.createDriverFor(SAMPLE_TEST_CONTEXT.ME, Runner.platform, context);
        context.addTestState(SAMPLE_TEST_CONTEXT.ME, String.valueOf(userDetails.get("username")));
        new AuthBL(SAMPLE_TEST_CONTEXT.ME, Runner.platform).signIn(userDetails);
    }

    @And("I start an instant meeting")
    public void iStartAnInstantMeeting() {
        new LandingBL().startInstantMeeting();
    }

    @When("I Unmute myself")
    public void iUnmuteMyself() {
        new InAMeetingBL().unmuteMyself();
    }

    @Then("I should be able to Mute myself")
    public void iShouldBeAbleToMuteMyself() {
        new InAMeetingBL().muteMyself();
    }

    @Given("{string} logs-in and starts an instant meeting on {string}")
    public void logsInAndStartsAnInstantMeetingOn(String userPersona, String fromPlatform) {
        Platform currentPlatform = Platform.valueOf(fromPlatform);
        allDrivers.createDriverFor(userPersona, currentPlatform, context);
        new AuthBL(userPersona, currentPlatform).signInAndStartMeeting(Runner.getTestDataAsMap(userPersona));
    }

    @And("{string} joins the meeting from {string}")
    public void joinsTheMeetingFrom(String userPersona, String fromPlatform) {
        Platform currentPlatform = Platform.valueOf(fromPlatform);
        allDrivers.createDriverFor(userPersona, currentPlatform, context);
        String meetingId = context.getTestStateAsString(SAMPLE_TEST_CONTEXT.MEETING_ID);
        String meetingPassword = context.getTestStateAsString(SAMPLE_TEST_CONTEXT.MEETING_PASSWORD);
        new JoinAMeetingBL(userPersona, currentPlatform).joinMeeting(meetingId, meetingPassword);
    }

    @Given("{string} logs-in and starts an instant meeting in {string} on {string}")
    public void logsInAndStartsAnInstantMeetingInOn(String userPersona, String appName, String platform) {
        appName = appName.toLowerCase(Locale.ROOT);
        Platform onPlatform = Platform.valueOf(platform);
        LOGGER.info(System.out.printf("startOn - Persona:'%s', AppName: '%s', Platform: '%s'", userPersona, appName, onPlatform.name()));
        context.addTestState(userPersona, userPersona);
        allDrivers.createDriverFor(userPersona, appName, onPlatform, context);
        new AuthBL(userPersona, onPlatform).signInAndStartMeeting(Runner.getTestDataAsMap(userPersona));
    }

    @And("{string} joins the meeting from {string} on {string}")
    public void joinsTheMeetingFromOn(String userPersona, String appName, String platform) {
        appName = appName.toLowerCase(Locale.ROOT);
        Platform onPlatform = Platform.valueOf(platform);
        LOGGER.info(System.out.printf("startOn - Persona:'%s', AppName: '%s', Platform: '%s'", userPersona, appName, onPlatform.name()));
        context.addTestState(userPersona, userPersona);
        switch(onPlatform) {
            case android:
            case iOS:
            case windows:
                allDrivers.createDriverFor(userPersona, appName, onPlatform, context);
                break;
            case web:
                String[] parts = appName.toLowerCase(Locale.ROOT)
                                        .split("-");
                String app = parts[0];
                String browserName = parts[1];
                allDrivers.createDriverFor(userPersona, app, browserName, onPlatform, context);
                break;
            default:
                throw new InvalidTestDataException("Unexpected value for platform: " + onPlatform);
        }

        String meetingId = context.getTestStateAsString(SAMPLE_TEST_CONTEXT.MEETING_ID);
        String meetingPassword = context.getTestStateAsString(SAMPLE_TEST_CONTEXT.MEETING_PASSWORD);
        new JoinAMeetingBL(userPersona, onPlatform).joinMeeting(meetingId, meetingPassword);
    }
}
