package com.znsio.teswiz.mobile.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.znsio.teswiz.exceptions.InvalidTestDataException;

class LambdaTestMobileAppUploadTest {
    @Test
    void shouldBuildUploadCommandWithNormalizedUrl() {
        String[] command = LambdaTestMobileAppUpload.buildUploadCurlCommand(
                "user",
                "key",
                "/tmp/TheApp.apk",
                "https://manual-api.lambdatest.com",
                "--proxy http://proxy:8080");

        assertThat(command).containsExactly(
                "curl --insecure --proxy http://proxy:8080 -u \"user:key\"",
                "-X POST \"https://manual-api.lambdatest.com/app/upload/realDevice\"",
                "-F \"appFile=@/tmp/TheApp.apk\"");
    }

    @Test
    void shouldAcceptApiUrlWithTrailingSlash() {
        String[] command = LambdaTestMobileAppUpload.buildUploadCurlCommand(
                "user",
                "key",
                "/tmp/TheApp.apk",
                "https://manual-api.lambdatest.com/",
                "");

        assertThat(command[1]).isEqualTo("-X POST \"https://manual-api.lambdatest.com/app/upload/realDevice\"");
    }

    @Test
    void shouldParseUploadedAppUrlFromResponse() {
        assertThat(LambdaTestMobileAppUpload.parseUploadedAppUrl(
                "/tmp/TheApp.apk",
                "{\"app_url\":\"lt://APP123\"}"))
                .isEqualTo("lt://APP123");
    }

    @Test
    void shouldFailWhenUploadResponseIsInvalid() {
        assertThatThrownBy(() -> LambdaTestMobileAppUpload.parseUploadedAppUrl(
                "/tmp/TheApp.apk",
                "{\"message\":\"failed\"}"))
                .isInstanceOf(InvalidTestDataException.class)
                .hasMessageContaining("Unable to upload app '/tmp/TheApp.apk' to LambdaTest");
    }

    @Test
    void shouldFailWhenUploadResponseCannotBeParsed() {
        assertThatThrownBy(() -> LambdaTestMobileAppUpload.parseUploadedAppUrl(
                "/tmp/TheApp.apk",
                "not-json"))
                .isInstanceOf(InvalidTestDataException.class)
                .hasMessageContaining("Unable to parse LambdaTest app upload response");
    }
}
