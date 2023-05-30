package com.znsio.teswiz.runner;

import com.znsio.teswiz.tools.cmd.CommandLineExecutor;
import com.znsio.teswiz.tools.cmd.CommandLineResponse;
import io.appium.java_client.AppiumDriver;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.File;
import java.util.Arrays;

import static com.znsio.teswiz.tools.Wait.waitFor;

class BrowserStackImageInjection {
    private static final Logger LOGGER = Logger.getLogger(
            BrowserStackImageInjection.class.getName());

    private static String uploadToCloud(String uploadFilePath, String cloudUser, String cloudKey) {
        uploadFilePath = new File(uploadFilePath).getAbsolutePath();
        String fileName = new File(uploadFilePath).getName();
        String mediaUrl = Runner.NOT_SET;
        String cloudName = System.getenv("CLOUD_NAME");

        String[] curlCommand = new String[]{
                "curl --insecure -u \"" + cloudUser + ":" + cloudKey + "\"",
                "-X POST \"https://api-cloud.browserstack.com/app-automate/upload-media\"",
                "-F \"file=@" + uploadFilePath + "\"", "-F \"custom_id=" + fileName + "\""};
        LOGGER.info(
                String.format("Uploading file: '%s' to '%s' using command: '%s'", uploadFilePath,
                              cloudName, Arrays.toString(curlCommand)));
        CommandLineResponse uploadFileResponse = CommandLineExecutor.execCommand(curlCommand);
        String stdOut = uploadFileResponse.getStdOut();
        LOGGER.info(String.format("Response of upload command: '%s'", stdOut));
        mediaUrl = new JSONObject(stdOut).getString("media_url");
        LOGGER.info(String.format("Uploaded file: '%s' to '%s'. Media URL: '%s'", uploadFilePath,
                                  cloudName, mediaUrl));
        waitFor(5);
        return mediaUrl;
    }

    static String injectMediaToDriver(String uploadFilePath, AppiumDriver driver, String cloudUser,
                                      String cloudKey) {
        String mediaUrl = BrowserStackImageInjection.uploadToCloud(uploadFilePath, cloudUser,
                                                                   cloudKey);
        LOGGER.info(String.format("Inject media url in driver: '%s'", mediaUrl));
        driver.executeScript(
                "browserstack_executor: {\"action\":\"cameraImageInjection\"," + " \"arguments" +
                "\":" + " {\"imageUrl\" : \"" + mediaUrl + "\"}}");
        waitFor(5);
        return mediaUrl;
    }
}
