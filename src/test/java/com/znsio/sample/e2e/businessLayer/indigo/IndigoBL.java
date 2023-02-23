package com.znsio.sample.e2e.businessLayer.indigo;

import com.context.TestExecutionContext;
import com.znsio.e2e.entities.Platform;
import com.znsio.e2e.runner.Runner;
import com.znsio.sample.e2e.entities.SAMPLE_TEST_CONTEXT;
import com.znsio.sample.e2e.screen.indigo.IndigoHomeScreen;
import org.apache.log4j.Logger;
import org.assertj.core.api.SoftAssertions;

public class IndigoBL {
    private static final Logger LOGGER = Logger.getLogger(IndigoBL.class.getName());
    private final TestExecutionContext context;
    private final SoftAssertions softly;
    private final String currentUserPersona;
    private final Platform currentPlatform;

    public IndigoBL(String userPersona, Platform forPlatform) {
        long threadId = Thread.currentThread().getId();
        this.context = Runner.getTestExecutionContext(threadId);
        softly = Runner.getSoftAssertion(threadId);
        this.currentUserPersona = userPersona;
        this.currentPlatform = forPlatform;
        Runner.setCurrentDriverForUser(userPersona, forPlatform, context);
    }

    public IndigoBL() {
        long threadId = Thread.currentThread().getId();
        this.context = Runner.getTestExecutionContext(threadId);
        softly = Runner.getSoftAssertion(threadId);
        this.currentUserPersona = SAMPLE_TEST_CONTEXT.ME;
        this.currentPlatform = Runner.getPlatform();
    }

    public FlightResultsBL searchForTicket(String journeyType, String from, String destination,
                                           String numberOfAdults) {
        IndigoHomeScreen.get().selectJourneyType(journeyType)
                        .selectNumberOfAdultPassengers(Integer.parseInt(numberOfAdults))
                        .selectFrom(from).selectTo(destination).searchFlightOptions();
        return new FlightResultsBL();
    }
}
