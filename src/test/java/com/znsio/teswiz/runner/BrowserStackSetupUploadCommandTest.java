package com.znsio.teswiz.runner;

import com.znsio.teswiz.mobile.provider.BrowserStackMobileSetup;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BrowserStackSetupUploadCommandTest {

    @Test
    void shouldAddIosKeychainSupportFlagForIpaUploads() {
        String[] command = BrowserStackMobileSetup.buildUploadAppCurlCommand(
                "user:key",
                "/tmp/TheApp.ipa",
                "https://api-cloud.browserstack.com/app-automate/",
                "");

        assertThat(command).contains("-F \"ios_keychain_support=true\"");
    }

    @Test
    void shouldNotAddIosKeychainSupportFlagForNonIpaUploads() {
        String[] command = BrowserStackMobileSetup.buildUploadAppCurlCommand(
                "user:key",
                "/tmp/TheApp-release.apk",
                "https://api-cloud.browserstack.com/app-automate/",
                "");

        assertThat(command).doesNotContain("-F \"ios_keychain_support=true\"");
    }

    @Test
    void shouldAddIosKeychainSupportFlagForZipUploads() {
        String[] command = BrowserStackMobileSetup.buildUploadAppCurlCommand(
                "user:key",
                "/tmp/TheApp.zip",
                "https://api-cloud.browserstack.com/app-automate/",
                "");

        assertThat(command).contains("-F \"ios_keychain_support=true\"");
    }
}
