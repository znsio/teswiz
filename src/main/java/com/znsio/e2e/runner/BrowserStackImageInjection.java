package com.znsio.e2e.runner;

import com.znsio.e2e.tools.cmd.CommandLineExecutor;
import com.znsio.e2e.tools.cmd.CommandLineResponse;
import io.appium.java_client.AppiumDriver;
import org.apache.commons.lang3.NotImplementedException;
import org.json.JSONObject;

import java.io.File;
import java.util.Arrays;

import org.apache.log4j.Logger;

import static com.znsio.e2e.runner.Runner.configs;
import static com.znsio.e2e.runner.Setup.*;
import static com.znsio.e2e.tools.Wait.waitFor;

public class BrowserStackImageInjection {

   private static final Logger LOGGER = Logger.getLogger(BrowserStackImageInjection.class.getName());


    public static String uploadToCloud(String uploadFilePath) {
        uploadFilePath = new File(uploadFilePath).getAbsolutePath();
        String fileName = new File(uploadFilePath).getName();
        String cloudUser = configs.get(CLOUD_USER);
        String cloudKey = configs.get(CLOUD_KEY);
        String cloudName = configs.get(CLOUD_NAME);
        String mediaUrl = Runner.NOT_SET;

        if(cloudName.equalsIgnoreCase("browserstack")) {
            String[] curlCommand = new String[]{
                    "curl --insecure -u \"" + cloudUser + ":" + cloudKey + "\"",
                    "-X POST \"https://api-cloud.browserstack.com/app-automate/upload-media\"",
                    "-F \"file=@" + uploadFilePath + "\"",
                    "-F \"custom_id=" + fileName + "\""
            };
            LOGGER.info(String.format("Uploading file: '%s' to '%s' using command: '%s'", uploadFilePath, cloudName, Arrays.toString(curlCommand)));
            CommandLineResponse uploadQRToCloud = CommandLineExecutor.execCommand(curlCommand);
            String stdOut = uploadQRToCloud.getStdOut();
            LOGGER.info(String.format("Response of upload command: '%s'", stdOut));
            mediaUrl = new JSONObject(stdOut).getString("media_url");
            LOGGER.info(String.format("Uploaded file: '%s' to '%s'. Media URL: '%s'", uploadFilePath, cloudName, mediaUrl));
            waitFor(5);
        } else {
            throw new NotImplementedException("uploadToCloud is not implemented for device in: " + cloudName);
        }
        return mediaUrl;
    }

    public static void injectMediaToDriver(String mediaUrl, AppiumDriver driver) {
        LOGGER.info(String.format("Inject media url in driver: '%s'", mediaUrl));
        driver.executeScript("browserstack_executor: {\"action\":\"cameraImageInjection\", \"arguments\": {" +
                "    \"imageUrl\" : \"" + mediaUrl + "\"}}");
        waitFor(5);
    }
}
