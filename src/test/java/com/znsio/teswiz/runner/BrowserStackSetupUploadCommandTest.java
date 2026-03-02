package com.znsio.teswiz.runner;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BrowserStackSetupUploadCommandTest {

    @Test
    void shouldAddIosKeychainSupportFlagForIpaUploads() {
        String[] command = BrowserStackSetup.buildUploadAppCurlCommand(
                "user:key",
                "/tmp/TheApp.ipa",
                "https://api-cloud.browserstack.com/app-automate/",
                "");

        assertThat(command).contains("-F \"ios_keychain_support=true\"");
    }

    @Test
    void shouldNotAddIosKeychainSupportFlagForNonIpaUploads() {
        String[] command = BrowserStackSetup.buildUploadAppCurlCommand(
                "user:key",
                "/tmp/TheApp-release.apk",
                "https://api-cloud.browserstack.com/app-automate/",
                "");

        assertThat(command).doesNotContain("-F \"ios_keychain_support=true\"");
    }

    @Test
    void shouldAddIosKeychainSupportFlagForZipUploads() {
        String[] command = BrowserStackSetup.buildUploadAppCurlCommand(
                "user:key",
                "/tmp/TheApp.zip",
                "https://api-cloud.browserstack.com/app-automate/",
                "");

        assertThat(command).contains("-F \"ios_keychain_support=true\"");
    }
}
