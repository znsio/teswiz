package com.znsio.teswiz.screen.web.playwrightjava.indigo;

import com.znsio.teswiz.runner.Driver;
import com.znsio.teswiz.runner.Visual;
import com.znsio.teswiz.screen.indigo.IndigoGiftVouchersScreen;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

public class IndigoGiftVouchersScreenPlaywrightJava extends IndigoGiftVouchersScreen {
    private static final String SCREEN_NAME = IndigoGiftVouchersScreenPlaywrightJava.class.getSimpleName();

    private static final By VOUCHER_VALUE_DROPDOWN = By.id("SelectedVoucherValue");
    private static final By VOUCHER_QUANTITY_DROPDOWN = By.id("SelectedVoucherQuantity");
    private static final By TOTAL_AMOUNT = By.id("lblTotal");
    private static final By PERSONALIZATION_CHECKBOX = By.id("chkPersonal");
    private static final By RECIPIENT_NAME = By.id("Per_Fname");
    private static final By CUSTOM_MESSAGE = By.id("Message");
    private static final By PREVIEW_BUTTON = By.xpath("//input[@class='preview-btn']");
    private static final By PREVIEW_HEADING = By.xpath("//div[@class='heading']/h2[contains(text(),'Preview Your Voucher')]");

    private final Driver driver;
    private final Visual visually;

    public IndigoGiftVouchersScreenPlaywrightJava(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
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
        String total = driver.findElement(TOTAL_AMOUNT).getText();
        return Integer.parseInt(total.split(" ")[1]);
    }

    @Override
    public IndigoGiftVouchersScreen select(String numberOfGiftVouchersToPurchase,
                                           String denomination,
                                           String forWhom,
                                           String customMessage) {
        select(numberOfGiftVouchersToPurchase, denomination);
        driver.findElement(PERSONALIZATION_CHECKBOX).click();
        replaceText(RECIPIENT_NAME, forWhom);
        replaceText(CUSTOM_MESSAGE, customMessage);
        visually.checkWindow(SCREEN_NAME, "Personalised Gift Voucher");
        return this;
    }

    @Override
    public IndigoGiftVouchersScreen preview() {
        driver.waitForClickabilityOf(PREVIEW_BUTTON).click();
        driver.waitTillElementIsVisible(PREVIEW_HEADING);
        visually.checkWindow(SCREEN_NAME, "Preview Gift Voucher");
        return this;
    }

    private void selectDropdownValue(By locator, String value) {
        new Select(driver.findElement(locator)).selectByValue(value);
    }

    private void replaceText(By locator, String value) {
        WebElement element = driver.findElement(locator);
        element.clear();
        element.sendKeys(value);
    }
}
