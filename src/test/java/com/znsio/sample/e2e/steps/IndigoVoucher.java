package com.znsio.sample.e2e.steps;

import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.znsio.e2e.runner.Runner;
import com.znsio.e2e.tools.Drivers;
import com.znsio.sample.e2e.businessLayer.indigo.GiftVoucherBL;
import com.znsio.sample.e2e.entities.SAMPLE_TEST_CONTEXT;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.log4j.Logger;

public class IndigoVoucher {
    private static final Logger LOGGER = Logger.getLogger(WindowsSteps.class.getName());
    private final TestExecutionContext context;
    private final Drivers allDrivers;

    public IndigoVoucher() {
        context = SessionContext.getTestExecutionContext(Thread.currentThread()
                .getId());
        LOGGER.info("context: " + context.getTestName());
        allDrivers = (Drivers) context.getTestState(SAMPLE_TEST_CONTEXT.ALL_DRIVERS);
        LOGGER.info("allDrivers: " + (null == allDrivers));
    }

    @Given("I personalise {string} Gift voucher of {string}")
    public void iPersonaliseGiftVoucherOf(String quantity, String denomination) {
        allDrivers.createDriverFor(SAMPLE_TEST_CONTEXT.ME, Runner.platform, context);
        LOGGER.info(System.out.printf("user Personalises Gift Voucher - Persona:'%s', Platform: '%s'", SAMPLE_TEST_CONTEXT.ME, Runner.platform));
        new GiftVoucherBL(SAMPLE_TEST_CONTEXT.ME, Runner.platform).personaliseVoucher(quantity, denomination);
    }

    @When("I provide a invalid promocode")
    public void iProvideAInvalidPromocode() {
        LOGGER.info("User is entering invalid Promocode");
        new GiftVoucherBL(SAMPLE_TEST_CONTEXT.ME, Runner.platform).enterPromocode();
    }

    @Then("I can purchase the gift voucher at origional price")
    public void iCanPurchaseTheGiftVoucherAtOrigionalPrice() {
        LOGGER.info("User is navigated to Payment page");
        new GiftVoucherBL(SAMPLE_TEST_CONTEXT.ME, Runner.platform).buyVoucher();
    }

}
