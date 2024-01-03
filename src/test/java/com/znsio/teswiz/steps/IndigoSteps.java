package com.znsio.teswiz.steps;

import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.znsio.teswiz.businessLayer.indigo.GiftVoucherBL;
import com.znsio.teswiz.businessLayer.indigo.IndigoBL;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.entities.SAMPLE_TEST_CONTEXT;
import io.cucumber.java.en.Given;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class IndigoSteps {
    private static final Logger LOGGER = LogManager.getLogger(IndigoSteps.class.getName());
    private final TestExecutionContext context;

    public IndigoSteps() {
        context = SessionContext.getTestExecutionContext(Thread.currentThread().getId());
        LOGGER.info("context: " + context.getTestName());
    }

    @Given("I search for a {string} ticket from {string} to {string} for {string} adult passenger")
    public void iSearchForATicketFromToForPassenger(String journeyType, String from,
                                                    String destination, String numberOfAdults) {
        LOGGER.info(System.out.printf("iSearchForATicketFromToForPassenger - Persona:'%s'",
                                      SAMPLE_TEST_CONTEXT.ME));
        Drivers.createDriverFor(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform(), context);
        new IndigoBL(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform()).searchForTicket(journeyType, from,
                                                                                   destination,
                                                                                   numberOfAdults);
    }

    @Given("I want to purchase {string} gift voucher of INR {string}")
    public void iWantToPurchaseGiftVoucherOfINR(String numberOfGiftVouchersToPurchase,
                                                String denomination) {
        LOGGER.info(System.out.printf("iWantToPurchaseGiftVoucherOfINR - Persona:'%s'",
                                      SAMPLE_TEST_CONTEXT.ME));
        Drivers.createDriverFor(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform(), context);
        new GiftVoucherBL(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform()).selectGiftVoucher(
                numberOfGiftVouchersToPurchase, denomination);
    }

    @Given("I want to personalize {string} gift voucher of INR {string} for {string} with message" +
           " {string}")
    public void iWantToPersonalizeGiftVoucherOfINRForWithMessage(
            String numberOfGiftVouchersToPurchase, String denomination, String forWhom,
            String customMessage) {
        LOGGER.info(System.out.printf("iWantToPurchaseGiftVoucherOfINR - Persona:'%s'",
                                      SAMPLE_TEST_CONTEXT.ME));
        Drivers.createDriverFor(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform(), context);
        new GiftVoucherBL(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform()).selectGiftVoucherAndPersonalise(
                numberOfGiftVouchersToPurchase, denomination, forWhom, customMessage);
    }
}
