package com.znsio.teswiz.mobile.provider;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.znsio.teswiz.exceptions.InvalidTestDataException;
import com.znsio.teswiz.tools.JsonFile;

public final class LambdaTestMobileAppUpload {
    private LambdaTestMobileAppUpload() {
    }

    public static String[] buildUploadCurlCommand(String authenticationUser,
            String authenticationKey,
            String appPath,
            String apiUrl,
            String curlProxyCommand) {
        return new String[] {
                "curl --insecure " + curlProxyCommand + " -u \"" + authenticationUser + ":"
                        + authenticationKey + "\"",
                "-X POST \"" + normalizeUploadUrl(apiUrl) + "\"",
                "-F \"appFile=@" + appPath + "\""
        };
    }

    public static String parseUploadedAppUrl(String appPath, String stdOut) {
        try {
            JsonObject uploadResult = JsonFile.convertToMap(stdOut).getAsJsonObject();
            if (uploadResult.has("app_url")) {
                return uploadResult.get("app_url").getAsString();
            }
        } catch (IllegalStateException | NullPointerException | JsonSyntaxException e) {
            throw new InvalidTestDataException("Unable to parse LambdaTest app upload response", e);
        }
        throw new InvalidTestDataException(String.format("Unable to upload app '%s' to LambdaTest. Response: %s",
                appPath, stdOut));
    }

    private static String normalizeUploadUrl(String apiUrl) {
        return apiUrl.endsWith("/")
                ? apiUrl + "app/upload/realDevice"
                : apiUrl + "/app/upload/realDevice";
    }
}
