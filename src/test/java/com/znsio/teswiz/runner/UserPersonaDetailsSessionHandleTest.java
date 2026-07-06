package com.znsio.teswiz.runner;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.znsio.teswiz.entities.Platform;

class UserPersonaDetailsSessionHandleTest {
    @Test
    void shouldStoreAndRetrieveSessionHandleForPersona() {
        UserPersonaDetails userPersonaDetails = new UserPersonaDetails();
        SessionHandle sessionHandle = SessionHandle.create("buyer", Platform.web, WebEngine.SELENIUM.getConfigValue(),
                "target/scenario", Map.of("browserName", "chrome"));

        userPersonaDetails.addSessionHandle("buyer", sessionHandle);

        assertThat(userPersonaDetails.isSessionHandleAssignedForUser("buyer")).isTrue();
        assertThat(userPersonaDetails.getSessionHandleAssignedForUser("buyer")).isEqualTo(sessionHandle);
    }

    @Test
    void shouldReassignSessionHandleWhenPersonaChanges() {
        UserPersonaDetails userPersonaDetails = new UserPersonaDetails();
        SessionHandle sessionHandle = SessionHandle.create("buyer", Platform.web, WebEngine.SELENIUM.getConfigValue(),
                "target/scenario", Map.of("browserName", "chrome"));
        userPersonaDetails.addCapabilities("buyer", new org.openqa.selenium.remote.DesiredCapabilities());
        userPersonaDetails.addAppName("buyer", "default");
        userPersonaDetails.addPlatform("buyer", Platform.web);
        userPersonaDetails.addSessionHandle("buyer", sessionHandle);

        userPersonaDetails.assignNewPersonaForUser("buyer", "seller");

        assertThat(userPersonaDetails.isSessionHandleAssignedForUser("buyer")).isFalse();
        assertThat(userPersonaDetails.isSessionHandleAssignedForUser("seller")).isTrue();
        assertThat(userPersonaDetails.getSessionHandleAssignedForUser("seller").userPersona()).isEqualTo("seller");
        assertThat(userPersonaDetails.getSessionHandleAssignedForUser("seller").engine())
                .isEqualTo(WebEngine.SELENIUM.getConfigValue());
    }
}
