package com.znsio.sample.e2e.businessLayer.indigo;

import com.context.TestExecutionContext;
import com.znsio.e2e.entities.Platform;
import com.znsio.e2e.runner.Runner;
import com.znsio.sample.e2e.entities.SAMPLE_TEST_CONTEXT;
import com.znsio.sample.e2e.screen.indigo.IndigoGiftVouchersScreen;
import com.znsio.sample.e2e.screen.indigo.IndigoHomeScreen;
import org.apache.log4j.Logger;
import org.assertj.core.api.SoftAssertions;

import static org.assertj.core.api.Assertions.assertThat;

public class GiftVoucherBL {
    private static final Logger LOGGER = Logger.getLogger(GiftVoucherBL.class.getName());
    private final TestExecutionContext context;
    private final SoftAssertions softly;
    private final String currentUserPersona;
    private final Platform currentPlatform;

    public GiftVoucherBL(String userPersona, Platform forPlatform) {
        long threadId = Thread.currentThread().getId();
        this.context = Runner.getTestExecutionContext(threadId);
        softly = Runner.getSoftAssertion(threadId);
        this.currentUserPersona = userPersona;
        this.currentPlatform = forPlatform;
        Runner.setCurrentDriverForUser(userPersona, forPlatform, context);
    }

    public GiftVoucherBL() {
        long threadId = Thread.currentThread().getId();
        this.context = Runner.getTestExecutionContext(threadId);
        softly = Runner.getSoftAssertion(threadId);
        this.currentUserPersona = SAMPLE_TEST_CONTEXT.ME;
        this.currentPlatform = Runner.platform;
    }

    private GiftVoucherBL selectWithoutPersonalise(String numberOfGiftVouchersToPurchase,
                                                   String denomination) {
        IndigoGiftVouchersScreen indigoGiftVouchersScreen = IndigoGiftVouchersScreen.get()
                                                                                    .select(numberOfGiftVouchersToPurchase,
                                                                                            denomination)
                                                                                    .preview();
        int totalPrice = indigoGiftVouchersScreen.getTotalPrice();
        assertThat(totalPrice).as("Computed amount is incorrect").isEqualTo(
                Integer.parseInt(denomination) * Integer.parseInt(numberOfGiftVouchersToPurchase));
        return this;
    }

    public GiftVoucherBL selectGiftVoucher(String numberOfGiftVouchersToPurchase,
                                           String denomination) {
        IndigoGiftVouchersScreen indigoGiftVouchersScreen = IndigoHomeScreen.get()
                                                                            .selectGiftVouchers()
                                                                            .select(numberOfGiftVouchersToPurchase,
                                                                                    denomination);
        int totalPrice = indigoGiftVouchersScreen.getTotalPrice();
        assertThat(totalPrice).as("Computed amount is incorrect").isEqualTo(
                Integer.parseInt(denomination) * Integer.parseInt(numberOfGiftVouchersToPurchase));
        indigoGiftVouchersScreen.preview();
        return this;
    }

    public GiftVoucherBL selectGiftVoucherAndPersonalise(String numberOfGiftVouchersToPurchase,
                                                         String denomination, String forWhom,
                                                         String customMessage) {
        IndigoGiftVouchersScreen indigoGiftVouchersScreen = IndigoHomeScreen.get()
                                                                            .selectGiftVouchers()
                                                                            .select(numberOfGiftVouchersToPurchase,
                                                                                    denomination,
                                                                                    forWhom,
                                                                                    customMessage);
        int totalPrice = indigoGiftVouchersScreen.getTotalPrice();
        assertThat(totalPrice).as("Computed amount is incorrect").isEqualTo(
                Integer.parseInt(denomination) * Integer.parseInt(numberOfGiftVouchersToPurchase));
        indigoGiftVouchersScreen.preview();
        return this;
    }
}
