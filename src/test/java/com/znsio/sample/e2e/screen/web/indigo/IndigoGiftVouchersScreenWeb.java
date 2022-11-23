package com.znsio.sample.e2e.screen.web.indigo;

import com.context.TestExecutionContext;
import com.znsio.e2e.runner.Runner;
import com.znsio.e2e.tools.Driver;
import com.znsio.e2e.tools.Visual;
import com.znsio.sample.e2e.screen.indigo.IndigoGiftVouchersScreen;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import static com.znsio.e2e.tools.Wait.waitFor;

public class IndigoGiftVouchersScreenWeb
        extends IndigoGiftVouchersScreen {
    private final Driver driver;
    private final Visual visually;
    private final WebDriver innerDriver;
    private static final String SCREEN_NAME = IndigoGiftVouchersScreenWeb.class.getSimpleName();
    private static final Logger LOGGER = Logger.getLogger(SCREEN_NAME);
    private static final String NOT_YET_IMPLEMENTED = " not yet implemented";
    private final TestExecutionContext context;

    public IndigoGiftVouchersScreenWeb(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
        this.innerDriver = this.driver.getInnerDriver();
        long threadId = Thread.currentThread()
                              .getId();
        context = Runner.getTestExecutionContext(threadId);
        waitFor(2);
    }

    @Override
    public IndigoGiftVouchersScreen select(String numberOfGiftVouchersToPurchase, String denomination) {
        Select selectDenomination = new Select(driver.findElement(By.id("SelectedVoucherValue")));
        selectDenomination.selectByValue(denomination);
        Select selectQuantity = new Select(driver.findElement(By.id("SelectedVoucherQuantity")));
        selectQuantity.selectByValue(numberOfGiftVouchersToPurchase);
        visually.checkWindow(SCREEN_NAME, "Selected denomination and quality");
        return this;
    }

    @Override
    public int getTotalPrice() {
        String total = driver.findElement(By.id("lblTotal"))
                             .getText();
        int totalAmount = Integer.parseInt(total.split(" ")[1]);
        return totalAmount;
    }

    @Override
    public IndigoGiftVouchersScreen select(String numberOfGiftVouchersToPurchase, String denomination, String forWhom, String customMessage) {
        select(numberOfGiftVouchersToPurchase, denomination);
        driver.findElement(By.id("chkPersonal"))
              .click();
        WebElement forWhomElement = driver.findElement(By.id("Per_Fname"));
        forWhomElement.clear();
        forWhomElement.sendKeys(forWhom);

        WebElement customMessageElement = driver.findElement(By.id("Message"));
        customMessageElement.clear();
        customMessageElement.sendKeys(customMessage);
        visually.checkWindow(SCREEN_NAME, "Personalised Gift Voucher");
        return this;
    }

    @Override
    public IndigoGiftVouchersScreen preview() {
        driver.waitForClickabilityOf(By.xpath("//input[@class='preview-btn']")).click();
        driver.waitTillElementIsVisible(By.xpath("//div[@class='heading']/h2[contains(text(),'Preview Your Voucher')]"));
        visually.checkWindow(SCREEN_NAME, "Preview Gift Voucher");
        return this;
    }
}
