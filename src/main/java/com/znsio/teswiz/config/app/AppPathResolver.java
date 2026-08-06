package com.znsio.teswiz.config.app;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.znsio.teswiz.exceptions.InvalidTestDataException;
import com.znsio.teswiz.tools.SensitiveDataMasker;

public final class AppPathResolver {
    private static final Logger LOGGER = LogManager.getLogger(AppPathResolver.class.getName());
    private static final String LAMBDATEST_APP_PREFIX = "lt://";
    private static final String BROWSERSTACK_APP_PREFIX = "bs://";

    private AppPathResolver() {
    }

    public static String resolveAppPath(String appPath, String saveToLocalDirectory) {
        if (isCloudHostedAppReference(appPath)) {
            LOGGER.info(String.format("Cloud hosted app reference '%s' provided. Skipping local file validation.",
                    SensitiveDataMasker.mask(appPath)));
            return appPath;
        }
        String fileName = new File(appPath).getName();
        String localFilePath = saveToLocalDirectory + File.separator + fileName;
        if (isAppPathAUrl(appPath)) {
            LOGGER.info(String.format("App url '%s' is provided in capabilities. Download it, if " +
                            "not already available at '%s'",
                    SensitiveDataMasker.mask(appPath), SensitiveDataMasker.mask(localFilePath)));
            downloadFileIfDoesNotExist(appPath, localFilePath, saveToLocalDirectory);
            LOGGER.info("Changing value of appPath from URL to file path");
            LOGGER.info(String.format("Before change, appPath value: %s", SensitiveDataMasker.mask(appPath)));
            appPath = localFilePath;
            LOGGER.info(String.format("After change, appPath value: %s", SensitiveDataMasker.mask(localFilePath)));
        } else {
            LOGGER.info(String.format("App file path '%s' is provided in capabilities.",
                    SensitiveDataMasker.mask(appPath)));
            if (!(new File(appPath).exists())) {
                throw new InvalidTestDataException(
                        String.format("App file path '%s' provided in capabilities is incorrect", appPath));
            }
        }
        LOGGER.info(String.format("App file path '%s' is provided in capabilities.",
                SensitiveDataMasker.mask(appPath)));
        LOGGER.info(String.format("File available at App file path '%s'", SensitiveDataMasker.mask(appPath)));
        return appPath;
    }

    public static boolean isCloudHostedAppReference(String appPath) {
        if (null == appPath) {
            return false;
        }
        return appPath.startsWith(LAMBDATEST_APP_PREFIX) || appPath.startsWith(BROWSERSTACK_APP_PREFIX);
    }

    private static void downloadFile(String url, String filePath, String saveToDirectory) {
        LOGGER.info(String.format("Downloading App from url: '%s'", url));
        try {
            URL fileUrl = new URL(url);
            HttpURLConnection connection = getHttpURLConnection(fileUrl);
            downloadFileFromHttpUrl(filePath, saveToDirectory, connection);
            String formattedSize = getDownloadedAppSize(Path.of(filePath));
            LOGGER.info(String.format("App downloaded at path: '%s', having size: '%s MB'", filePath, formattedSize));
        } catch (IOException e) {
            throw new InvalidTestDataException(
                    "An error occurred while opening the URL/downloading file: " + e.getMessage());
        }
    }

    private static String getDownloadedAppSize(Path filePath) {
        try {
            long fileSizeBytes = Files.size(filePath);
            double fileSizeMB = (double) fileSizeBytes / (1024 * 1024);
            return new DecimalFormat("#.##").format(fileSizeMB);
        } catch (IOException e) {
            throw new InvalidTestDataException("Unable to get downloaded app file size. Download " +
                    "may be corrupt. Check and fix before rerunning the test.", e);
        }
    }

    private static void downloadFileFromHttpUrl(String filePath, String saveToDirectory, HttpURLConnection connection) {
        try (InputStream inputStream = connection.getInputStream()) {
            createDirectoryIfNotExists(saveToDirectory);
            Files.copy(inputStream, Path.of(filePath), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new InvalidTestDataException(
                    String.format("Unable to download file '%s'", connection.getURL().toString()), e);
        }
    }

    private static HttpURLConnection getHttpURLConnection(URL fileUrl) {
        try {
            HttpURLConnection connection = (HttpURLConnection) fileUrl.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new InvalidTestDataException(
                        String.format("Unable to connect to url: '%s'. Got connection error '%d'", fileUrl,
                                responseCode));
            }
            return connection;
        } catch (IOException e) {
            throw new InvalidTestDataException(String.format("Unable to connect to url: '%s'.", fileUrl));
        }
    }

    private static void createDirectoryIfNotExists(String directory) throws IOException {
        Path directoryPath = Path.of(directory);
        if (!Files.exists(directoryPath)) {
            Files.createDirectories(directoryPath);
        }
    }

    private static void downloadFileIfDoesNotExist(String appPath, String filePath, String saveToDirectory) {
        if (!(new File(filePath).exists())) {
            LOGGER.info(String.format("App is not available at path: '%s'. Download it.", appPath));
            downloadFile(appPath, filePath, saveToDirectory);
        } else {
            LOGGER.info(String.format("App is already available at path: '%s'. No need to download it.", appPath));
        }
    }

    private static boolean isAppPathAUrl(String appPathUrl) {
        try {
            new URL(appPathUrl);
            LOGGER.info(String.format("'%s' is a URL.", appPathUrl));
            validateAppUrl(appPathUrl);
            return true;
        } catch (MalformedURLException e) {
            LOGGER.info(String.format("'%s' is not a URL.", appPathUrl));
            return false;
        }
    }

    private static void validateAppUrl(String appPathUrl) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(appPathUrl).openConnection();
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();
            connection.disconnect();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                LOGGER.info(String.format("'%s' is an invalid URL.", appPathUrl));
                throw new InvalidTestDataException("URL is not accessible: " + appPathUrl);
            }
            LOGGER.info(String.format("'%s' is a valid URL.", appPathUrl));
        } catch (IOException e) {
            throw new InvalidTestDataException(
                    String.format("Failed to make a connection using url: '%s'", appPathUrl) + e);
        }
    }
}
