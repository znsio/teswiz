package com.znsio.teswiz.businessLayer.jiomeet;

import com.context.TestExecutionContext;
import com.znsio.teswiz.entities.Platform;
import com.znsio.teswiz.exceptions.InvalidTestDataException;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.entities.SAMPLE_TEST_CONTEXT;
import com.znsio.teswiz.screen.jiomeet.InAMeetingScreen;
import org.apache.log4j.Logger;
import org.assertj.core.api.AssertionsForClassTypes;
import org.assertj.core.api.SoftAssertions;
import static org.assertj.core.api.Assertions.assertThat;


public class InAMeetingBL {
    private static final Logger LOGGER = Logger.getLogger(InAMeetingBL.class.getName());
    private final TestExecutionContext context;
    private final SoftAssertions softly;
    private final String currentUserPersona;
    private final Platform currentPlatform;

    public InAMeetingBL(String userPersona, Platform forPlatform) {
        long threadId = Thread.currentThread().getId();
        this.context = Runner.getTestExecutionContext(threadId);
        softly = Runner.getSoftAssertion(threadId);
        this.currentUserPersona = userPersona;
        this.currentPlatform = forPlatform;
        Runner.setCurrentDriverForUser(userPersona, forPlatform, context);
    }

    public InAMeetingBL() {
        long threadId = Thread.currentThread().getId();
        this.context = Runner.getTestExecutionContext(threadId);
        softly = Runner.getSoftAssertion(threadId);
        this.currentUserPersona = SAMPLE_TEST_CONTEXT.ME;
        this.currentPlatform = Runner.getPlatform();
    }

    public InAMeetingBL unmuteMyself() {
        InAMeetingScreen.get().unmute();
        return this;
    }


    public InAMeetingBL muteMyself() {
        InAMeetingScreen.get().mute();
        return this;
    }

    public InAMeetingBL openNotificationFromNotificationBar() {
        InAMeetingScreen.get().openJioMeetNotification();
        return this;
    }

    public InAMeetingBL verifyMeetingOpenedInJioMeetApplication() {
        assertThat(InAMeetingScreen.get().isMeetingStarted())
                .as("Meeting is not opened in Jio Meet Application")
                .isTrue();
        return this;
    }

    public InAMeetingBL userClicksOnChatWindow() {
        InAMeetingScreen.get().userClicksOnChatWindow();
        return this;
    }

    public InAMeetingBL userSendsChatMessageInAMeeting(String chatMessage) {
        InAMeetingScreen inAMeetingScreen = InAMeetingScreen.get();
        int currentNumberOfMessages = inAMeetingScreen.getNumberOfMessages();

        int updatedNumberOfMessages = inAMeetingScreen
                .sendsChatMessage(chatMessage)
                .getNumberOfMessages();

        AssertionsForClassTypes.assertThat(updatedNumberOfMessages)
                .as("Number of Messages did not update on sending the message")
                .isEqualTo(currentNumberOfMessages + 1);

        return this;
    }

    public InAMeetingBL userChecksTheReceivedChatMessage() {
        return this
                .userValidatesRedBubbleChatNotification()
                .userValidatesChatWindowForTheReceivedChatMessage();
    }

    private InAMeetingBL userValidatesChatWindowForTheReceivedChatMessage() {
        String chatMessage = "Hey";

        InAMeetingScreen inAMeetingScreen = InAMeetingScreen.get()
                .userTapsOnChatIcon();

        boolean isChatMessageReceived = inAMeetingScreen.isChatMessageReceived(chatMessage);

        AssertionsForClassTypes.assertThat(isChatMessageReceived)
                .as(String.format("Chat Message not received on %s's screen", this.currentUserPersona))
                .isTrue();

        return this;
    }

    private InAMeetingBL userValidatesRedBubbleChatNotification() {
        if (Platform.web.equals(this.currentPlatform)) {
            boolean isChatNotificationRedBubbleVisible =
                    InAMeetingScreen.get()
                            .isChatNotificationRedBubbleVisible();

            softly.assertThat(isChatNotificationRedBubbleVisible)
                    .as("Red bubble didn't appear on receiving chat message")
                    .isTrue();
        } else {
            LOGGER.info("userValidatesRedBubbleChatNotification: NA for Android since red bubble is not a separate element in android app");
        }
        return this;
    }

    public InAMeetingBL userPersonaLeavesTheMeeting(String userPersona) {
        switch (userPersona.toLowerCase()) {
            case "participant":
            case "new participant":
                InAMeetingScreen.get()
                        .userSelectsToLeaveMeeting()
                        .loggedInUserClosesMeetingFeedback();
                return this;

            default:
                throw new InvalidTestDataException(String.format("'%s' is an invalid userPersona", userPersona));
        }

    }

    public InAMeetingBL userShouldBeAbleToViewTheMessageSentByItselfInChatsTab() {
        String chatMessage = "Hey1";
        boolean isChatMessageReceived = InAMeetingScreen.get()
                .userNavigatesToChatsTab()
                .userSelectsChatSection()
                .isChatMessageReceivedInChatsTab(chatMessage);

        AssertionsForClassTypes.assertThat(isChatMessageReceived)
                .as(String.format("Chat Message '%s' is not found in Chats tab", chatMessage))
                .isTrue();
        return this;
    }
}
