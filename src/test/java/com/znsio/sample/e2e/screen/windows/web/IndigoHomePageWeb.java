package com.znsio.sample.e2e.screen.windows.web;

import com.znsio.e2e.tools.Driver;
import com.znsio.e2e.tools.Visual;
import com.znsio.sample.e2e.screen.web.indigo.IndigoHomeScreen;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;

public class IndigoHomePageWeb extends IndigoHomeScreen {
    private static final org.apache.log4j.Logger LOGGER = Logger.getLogger(IndigoHomePageWeb.class.getName());
    private final Driver driver;
    private final Visual visually;
    private final String SCREEN_NAME = IndigoHomePageWeb.class.getSimpleName();
    private final By bybookMenuXpath = By.xpath("//a[@title = 'Book']");
    private final By byGiftVoucherXpath = By.xpath("//a[@class='text-decoration-none']//div[text()='Gift Voucher']");

    public IndigoHomePageWeb(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public String gotoVoucherPage() {
        visually.checkWindow(SCREEN_NAME, " Voucher Home Page ");
        driver.findElement(bybookMenuXpath).click();
        driver.waitForClickabilityOf(byGiftVoucherXpath);
        driver.findElement(byGiftVoucherXpath).click();
        LOGGER.info("Voucher Page is opened");
        visually.checkWindow(SCREEN_NAME, "Gift Voucher Page is opened");
        String voucherPageTitle = driver.getInnerDriver().getTitle();
        return voucherPageTitle;
    }

}
