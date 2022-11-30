package com.znsio.sample.e2e.businessLayer.indigo;

import com.context.TestExecutionContext;
import com.znsio.e2e.entities.Platform;
import com.znsio.e2e.runner.Runner;
import com.znsio.sample.e2e.businessLayer.jiomeet.AuthBL;
import com.znsio.sample.e2e.entities.SAMPLE_TEST_CONTEXT;
import com.znsio.sample.e2e.screen.web.indigo.GiftVoucherScreen;
import com.znsio.sample.e2e.screen.web.indigo.IndigoHomeScreen;
import com.znsio.sample.e2e.screen.web.indigo.PaymentScreen;
import org.apache.log4j.Logger;
import org.assertj.core.api.SoftAssertions;
import org.testng.Assert;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class GiftVoucherBL {
    private static final Logger LOGGER = Logger.getLogger(AuthBL.class.getName());
    private final TestExecutionContext context;
    private final SoftAssertions softly;
    private final String currentUserPersona;
    private final Platform currentPlatform;


    public GiftVoucherBL(String userPersona, Platform forPlatform) {
        long threadId = Thread.currentThread()
                .getId();
        this.context = Runner.getTestExecutionContext(threadId);
        softly = Runner.getSoftAssertion(threadId);
        this.currentUserPersona = userPersona;
        this.currentPlatform = forPlatform;
        Runner.setCurrentDriverForUser(userPersona, forPlatform, context);
    }

    public GiftVoucherBL() {
        long threadId = Thread.currentThread()
                .getId();
        this.context = Runner.getTestExecutionContext(threadId);
        softly = Runner.getSoftAssertion(threadId);
        this.currentUserPersona = SAMPLE_TEST_CONTEXT.ME;
        this.currentPlatform = Runner.platform;
    }

    public GiftVoucherBL personaliseVoucher(String quantity, String denomination) {
        Map data = (Map) context.getTestState(SAMPLE_TEST_CONTEXT.USER_DETAILS);
        String voucherPageTitle = IndigoHomeScreen.get().gotoVoucherPage();
        Assert.assertEquals(voucherPageTitle,"Buy Gift Vouchers online | IndiGo");
        LOGGER.info(String.format("'%s' Gift vouchers of '%s' are selected", quantity, denomination));
        GiftVoucherScreen.get().personaliseVoucher(quantity, denomination);
        LOGGER.info("Get Voucher Details");
        softly.assertThat(GiftVoucherScreen.get().getPreviewVoucherDetails()).as("Receipent or message is not matched")
                .isEqualTo(data.get("personName").toString() + ", " + data.get("message").toString());
        return this;
    }

    public GiftVoucherBL enterPromocode() {
        String errorMssg = GiftVoucherScreen.get().enterInvalidPromocode();
//        Assert.assertEquals(errorMssg, "Invalid Promo Code.");
        softly.assertThat(errorMssg).as("Promocode not applied").isEqualTo("Invalid Promo Code.");
        String amount = (String) context.getTestState(SAMPLE_TEST_CONTEXT.VOUCHER_PRICE);
        Assert.assertEquals(GiftVoucherScreen.get().getAmountAfterApplyingPromocode(),amount);
        GiftVoucherScreen.get().fillDeliveryDetails();
        return this;
    }

    public GiftVoucherBL buyVoucher() {
        String paymentPageTitle = PaymentScreen.get().getPaymentPageTitle();
        Assert.assertEquals(paymentPageTitle, "CCAvenue: Billing Shipping");
        String amount = (String) context.getTestState(SAMPLE_TEST_CONTEXT.VOUCHER_PRICE);
        LOGGER.info(String.format("Payment of %s amount is to be done ",amount));
        softly.assertThat(PaymentScreen.get().getPaymentAmount()).as("Incorrect amount").contains(amount);
        return this;
    }
}
