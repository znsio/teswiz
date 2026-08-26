package com.znsio.teswiz.testng;

import org.testng.annotations.Test;

import com.znsio.teswiz.businessLayer.theapp.AppBL;
import com.znsio.teswiz.context.TestExecutionContext;
import com.znsio.teswiz.entities.SAMPLE_TEST_CONTEXT;
import com.znsio.teswiz.entities.TEST_CONTEXT;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.runner.Runner;

public class TheAppInvalidLoginTestNgTest {
    private static final String USERNAME = "znsio1";
    private static final String PASSWORD = "invalid password";

    @Test(groups = { "web", "theapp" })
    public void verifyErrorMessageOnInvalidLogin() {
        createDriverForInvalidLoginAttempt();

        new AppBL(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform())
                .provideInvalidDetailsForSignup(USERNAME, PASSWORD);
    }

    @Test(groups = { "web", "theapp" })
    public void verifyErrorMessageOnInvalidLogin1() {
        createDriverForInvalidLoginAttempt();

        new AppBL(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform()).goToLogin();
    }

    @Test(groups = { "web", "theapp" })
    public void verifyErrorMessageOnInvalidLogin3() {
        createDriverForInvalidLoginAttempt();

        new AppBL(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform()).goToLogin().goToLogin_fail();
    }

    @Test(groups = { "web", "theapp" })
    public void verifyErrorMessageOnInvalidLogin2() {
        createDriverForInvalidLoginAttempt();

        new AppBL(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform()).goToLogin_fail();
    }

    private void createDriverForInvalidLoginAttempt() {
        TestExecutionContext context = Runner.getTestExecutionContext(Thread.currentThread().getId());
        context.addTestState(TEST_CONTEXT.UPDATED_BROWSER_CONFIG_FILE_FOR_THIS_TEST, "./configs/browser_config.json");
        context.addTestState(TEST_CONTEXT.UPDATED_BASE_URL_FOR_WEB, "BASE_URL");
        Drivers.createDriverFor(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform(), context);
        context.addTestState(SAMPLE_TEST_CONTEXT.ME, USERNAME);
    }
}
