package com.znsio.sample.e2e.screen.android.ajio;

import com.znsio.e2e.tools.Driver;
import com.znsio.e2e.tools.Visual;
import com.znsio.sample.e2e.screen.ajio.HomeScreen;
import com.znsio.sample.e2e.screen.ajio.SearchScreen;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;

import java.util.Map;

public class HomeScreenAndroid extends HomeScreen {
    private final Driver driver;
    private final Visual visually;
    private static final String SCREEN_NAME = HomeScreenAndroid.class.getSimpleName();
    private static final Logger LOGGER = Logger.getLogger(SCREEN_NAME);

    private static final By byStartSearchBoxId = By.id("com.ril.ajio:id/llpsTvSearch");
    private static final By byUploadPhotoButtonId = By.id("com.ril.ajio:id/layout_select_photo");
    private static final By byImageDirectoryXpath = By.xpath("//android.widget.TextView[contains(@text, 'Images')]");
    private static final By byImageXpath = By.xpath("//android.view.ViewGroup[contains(@content-desc,'Photo taken')][1]");
    private static final By byDismissButtonId = By.id("com.ril.ajio:id/footer_button_2");
    private static final By bySystemPermissionMessageId = By.id("com.android.permissioncontroller:id/permission_message");
    private static final By byAllowButtonId = By.id("com.android.permissioncontroller:id/permission_allow_button");

    public HomeScreenAndroid(Driver driver, Visual visually) {
        this.driver = driver;
        this.visually = visually;
    }

    @Override
    public SearchScreen searchByImage() {
        driver.waitTillElementIsPresent(byImageDirectoryXpath).click();
        driver.waitTillElementIsPresent(byImageXpath).click();
        return SearchScreen.get();
    }

    @Override
    public HomeScreen attachFileToDevice(Map imageData){
        String sourceFileLocation = System.getProperty("user.dir") + imageData.get("IMAGE_FILE_LOCATION");
        String destinationFileLocation = (String) imageData.get("UPLOAD_IMAGE_LOCATION");
        LOGGER.info("searchByImage");

        if(driver.isElementPresent(byDismissButtonId))
            driver.findElement(byDismissButtonId).click();

        driver.waitTillElementIsPresent(byStartSearchBoxId)
                .click();
        visually.checkWindow(SCREEN_NAME, "Upload a Photo");
        driver.waitTillElementIsPresent(byUploadPhotoButtonId)
                .click();
        if(driver.isElementPresent(bySystemPermissionMessageId))
            driver.waitTillElementIsPresent(byAllowButtonId).click();

        driver.pushFileToDevice(sourceFileLocation, destinationFileLocation);
        LOGGER.info("Image Pushed to Device path" + destinationFileLocation);
        return this;
    }

}
