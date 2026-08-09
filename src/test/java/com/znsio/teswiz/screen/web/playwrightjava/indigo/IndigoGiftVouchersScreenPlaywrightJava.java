package com.znsio.teswiz.screen.web.playwrightjava.indigo;

import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.indigo.IndigoGiftVouchersScreen;
import com.znsio.teswiz.web.playwright.screen.PlaywrightJavaScreenContext;

public class IndigoGiftVouchersScreenPlaywrightJava extends IndigoGiftVouchersScreen {
    private static final String SCREEN_NAME = IndigoGiftVouchersScreenPlaywrightJava.class.getSimpleName();

    private static final String VOUCHER_VALUE_DROPDOWN = "#SelectedVoucherValue";
    private static final String VOUCHER_QUANTITY_DROPDOWN = "#SelectedVoucherQuantity";
    private static final String TOTAL_AMOUNT = "#lblTotal";
    private static final String PERSONALIZATION_CHECKBOX = "#chkPersonal";
    private static final String RECIPIENT_NAME = "#Per_Fname";
    private static final String CUSTOM_MESSAGE = "#Message";
    private static final String PREVIEW_BUTTON = "input.preview-btn";
    private static final String PREVIEW_HEADING = "div.heading h2:has-text(\"Preview Your Voucher\")";

    private final PlaywrightJavaScreenContext context;
    private final Visual visually;

    public IndigoGiftVouchersScreenPlaywrightJava(PlaywrightJavaScreenContext context) {
        this.context = context;
        this.visually = context.visual();
    }

    @Override
    public IndigoGiftVouchersScreen select(String numberOfGiftVouchersToPurchase, String denomination) {
        selectDropdownValue(VOUCHER_VALUE_DROPDOWN, denomination);
        selectDropdownValue(VOUCHER_QUANTITY_DROPDOWN, numberOfGiftVouchersToPurchase);
        visually.checkWindow(SCREEN_NAME, "Selected denomination and quality");
        return this;
    }

    @Override
    public int getTotalPrice() {
        String total = context.page().locator(TOTAL_AMOUNT).innerText();
        return Integer.parseInt(total.split(" ")[1]);
    }

    @Override
    public IndigoGiftVouchersScreen select(String numberOfGiftVouchersToPurchase,
                                           String denomination,
                                           String forWhom,
                                           String customMessage) {
        select(numberOfGiftVouchersToPurchase, denomination);
        context.page().locator(PERSONALIZATION_CHECKBOX).click();
        replaceText(RECIPIENT_NAME, forWhom);
        replaceText(CUSTOM_MESSAGE, customMessage);
        visually.checkWindow(SCREEN_NAME, "Personalised Gift Voucher");
        return this;
    }

    @Override
    public IndigoGiftVouchersScreen preview() {
        context.page().locator(PREVIEW_BUTTON).click();
        context.page().locator(PREVIEW_HEADING).waitFor();
        visually.checkWindow(SCREEN_NAME, "Preview Gift Voucher");
        return this;
    }

    private void selectDropdownValue(String selector, String value) {
        context.page().locator(selector).selectOption(value);
    }

    private void replaceText(String selector, String value) {
        context.page().locator(selector).fill(value);
    }
}
