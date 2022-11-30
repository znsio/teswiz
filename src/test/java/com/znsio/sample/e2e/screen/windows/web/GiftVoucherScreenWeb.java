package com.znsio.sample.e2e.screen.windows.web;

import Utils.ReusableMethods;
import com.context.TestExecutionContext;
import com.znsio.e2e.runner.Runner;
import com.znsio.e2e.tools.Driver;
import com.znsio.e2e.tools.Visual;
import com.znsio.sample.e2e.entities.SAMPLE_TEST_CONTEXT;
import com.znsio.sample.e2e.screen.web.indigo.GiftVoucherScreen;
import com.znsio.sample.e2e.screen.windows.notepad.NotepadScreenWindows;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Select;


import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class GiftVoucherScreenWeb extends GiftVoucherScreen {
    private static final org.apache.log4j.Logger LOGGER = Logger.getLogger(NotepadScreenWindows.class.getName());
    private final Driver driver;

    private final TestExecutionContext context;
    private final Visual visually;
    private final String SCREEN_NAME = GiftVoucherScreenWeb.class.getSimpleName();
    private final By byselectVoucherValueXpath = By.xpath("//select[@id = 'SelectedVoucherValue']");
    private final By byquantityId = By.id("SelectedVoucherQuantity");
    private final By byselectPersonalId = By.id("chkPersonal");
    private final By personNameId = By.id("Per_Fname");
    private final By bymessageId = By.id("Message");
    private final By byPreviewClassName = By.className("preview-btn");
    private final By byProceedButtonXpath = By.xpath("//input[@value = 'Proceed']");
    private final By byReceiverFNameId = By.id("Rec_Fname");
    private final By byReceiverLNameId = By.id("Rec_Lname");
    private final By byReceiverEmailId = By.id("Rec_EmailID");
    private final By byReceiverPhoneId = By.id("Rec_Phone");
    private final By bySenderFNameId = By.id("Per_Fname");
    private final By bySenderLNameId = By.id("Per_Lname");
    private final By bySenderEmailId = By.id("Per_EmailID");
    private final By bySenderPhoneId = By.id("Per_Phone");
    private final By byPromocodeId = By.id("PromoCode");
    private final By byPayNowXpath = By.xpath("//input[@value = 'Pay Now']");
    private final By byTnCId = By.id("chkTnC");
    private final By byActualPriceXpath = By.xpath("//label[contains(text(), 'Total =')]//following-sibling::span[@id = 'lblTotal']");
    private final By byNewPriceXpath = By.xpath("//label[contains(text(), 'Payment Amount')]//following-sibling::span[@id = 'lblTotal']");
    private final By byApplyPromocodeButtonId = By.id("btnApplyPromoCode");
    private final By byErrorMssgXpath = By.xpath("//div[contains(text(), 'Invalid Promo Code.')]");
    private final By byreceipentXpath = By.xpath("//div[@class='title-name']");
    private final By byMessageXpath = By.xpath("//div[@class='doted-line']/preceding-sibling::p");

    private ReusableMethods utils;

    public GiftVoucherScreenWeb(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
        long threadId = Thread.currentThread()
                .getId();
        context = Runner.getTestExecutionContext(threadId);
    }

    @Override
    public GiftVoucherScreenWeb personaliseVoucher(String quantity, String denomination) {
        int voucherAmount = Integer.parseInt(quantity) * Integer.parseInt(denomination);
        Select selectDenomination = new Select(driver.findElement(byselectVoucherValueXpath));
        selectDenomination.selectByValue(denomination);
        Select selectQuantity = new Select(driver.findElement(byquantityId));
        selectQuantity.selectByVisibleText(quantity);
        LOGGER.info("quantity" + "Gift Voucher of " + denomination + "is selected");
        context.addTestState(SAMPLE_TEST_CONTEXT.VOUCHER_PRICE, String.valueOf(voucherAmount));
        driver.findElement(byselectPersonalId).click();
        Map data = (Map) context.getTestState(SAMPLE_TEST_CONTEXT.USER_DETAILS);
        driver.findElement(personNameId).sendKeys(data.get("personName").toString());
        driver.findElement(bymessageId).sendKeys(data.get("message").toString());
        LOGGER.info("Voucher is personalised");
        visually.takeScreenshot(SCREEN_NAME, "Voucher is personalised");
        driver.scrollToBottom();
        driver.waitTillElementIsPresent(byPreviewClassName);
        driver.findElement(byPreviewClassName).click();
//        driver.findElement(byProceedButtonXpath).click();
        return this;
    }

    public String getPreviewVoucherDetails() {
        String receipent = driver.findElement(byreceipentXpath).getText().trim();
        String message = driver.findElement(byMessageXpath).getText().trim();
        driver.findElement(byProceedButtonXpath).click();
        LOGGER.info("Proceed Button clicked on Preview page");
        return receipent + " " + message;
    }

    @Override
    public GiftVoucherScreen fillDeliveryDetails() {
        driver.waitTillElementIsPresent(byReceiverFNameId);
        driver.findElement(byReceiverFNameId).sendKeys(utils.randomString());
        driver.findElement(byReceiverLNameId).sendKeys(utils.randomString());
        driver.findElement(byReceiverEmailId).sendKeys(utils.randomString() + "@gmail.com");
        driver.findElement(byReceiverPhoneId).sendKeys(utils.randomNumber());
        driver.findElement(bySenderFNameId).sendKeys(utils.randomString());
        driver.findElement(bySenderLNameId).sendKeys(utils.randomString());
        driver.findElement(bySenderEmailId).sendKeys(utils.randomString() + "@gmail.com");
        driver.findElement(bySenderPhoneId).sendKeys(utils.randomNumber());
        driver.findElement(byTnCId).click();
        driver.findElement(byPayNowXpath).click();
        return this;
    }

    @Override
    public String enterInvalidPromocode() {
        LOGGER.info("Entering invalid promocode");
        driver.waitTillElementIsVisible(byPromocodeId, 3);
        driver.findElement(byPromocodeId).sendKeys(ReusableMethods.randomString());
        driver.findElement(byApplyPromocodeButtonId).click();
        String errMsg = driver.findElement(byErrorMssgXpath).getText();
        visually.checkWindow(SCREEN_NAME, "Screen with invalid promocode");
        return errMsg;
    }

    @Override
    public String getAmountAfterApplyingPromocode() { //improvement
        String amountAfterPromocode = driver.findElement(byNewPriceXpath).getText();
        return utils.fetchPrice(amountAfterPromocode);
    }

}
