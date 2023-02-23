package com.znsio.sample.e2e.steps;

import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.znsio.e2e.entities.Platform;
import com.znsio.e2e.runner.Runner;
import com.znsio.e2e.runner.Drivers;
import com.znsio.sample.e2e.businessLayer.theapp.AppBL;
import com.znsio.sample.e2e.businessLayer.theapp.ClipboardBL;
import com.znsio.sample.e2e.businessLayer.theapp.EchoBL;
import com.znsio.sample.e2e.businessLayer.theapp.FileUploadBL;
import com.znsio.sample.e2e.entities.SAMPLE_TEST_CONTEXT;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.log4j.Logger;

public class TheAppSteps {
    private static final Logger LOGGER = Logger.getLogger(TheAppSteps.class.getName());
    private final TestExecutionContext context;

    public TheAppSteps() {
        context = SessionContext.getTestExecutionContext(Thread.currentThread().getId());
        LOGGER.info("context: " + context.getTestName());
    }

    @When("I login with invalid credentials - {string}, {string}")
    public void iLoginWithInvalidCredentials(String username, String password) {
        LOGGER.info(System.out.printf(
                "iLoginWithInvalidCredentials - Persona:'%s', Username: '%s', Password:'%s', " +
                "Platform: '%s'",
                SAMPLE_TEST_CONTEXT.ME, username, password, Runner.getPlatform()));
        Drivers.createDriverFor(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform(), context);
        context.addTestState(SAMPLE_TEST_CONTEXT.ME, username);
        new AppBL(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform()).provideInvalidDetailsForSignup(username,
                                                                                          password);
    }

    @When("I go back")
    public void iGoBack() {
        new AppBL().goBack();
    }

    @Given("{string} login with invalid credentials - {string}, {string} on {string}")
    public void loginWithInvalidCredentialsOn(String userPersona, String username, String password,
                                              String onPlatform) {
        LOGGER.info(System.out.printf(
                "LoginWithInvalidCredentials - Persona:'%s', Username: '%s', Password:'%s', " +
                "Platform: '%s'",
                userPersona, username, password, onPlatform));
        context.addTestState(userPersona, username);
        Drivers.createDriverFor(userPersona, Platform.valueOf(onPlatform), context);
        new AppBL(userPersona, Platform.valueOf(onPlatform)).provideInvalidDetailsForSignup(
                username, password);
    }

    @Then("I try to login again with invalid credentials - {string}, {string}")
    public void iTryToLoginAgainWithInvalidCredentials(String username, String password) {
        LOGGER.info(System.out.printf(
                "iTryToLoginAgainWithInvalidCredentials - Username: '%s', Password:'%s'", username,
                password));
        new AppBL().loginAgain(username, password);
    }

    @When("{string} login with invalid credentials - {string}, {string}")
    public void loginWithInvalidCredentials(String userPersona, String username, String password) {
        Platform onPlatform = Drivers.getPlatformForUser(userPersona);
        LOGGER.info(System.out.printf(
                "LoginWithInvalidCredentials - Persona:'%s', Username: '%s', Password:'%s', " +
                "Platform: '%s'",
                SAMPLE_TEST_CONTEXT.ME, username, password, onPlatform.name()));
        new AppBL(userPersona, onPlatform).provideInvalidDetailsForSignup(username, password);
    }

    @When("{string} login again with invalid credentials - {string}, {string}")
    public void loginAgainWithInvalidCredentials(String userPersona, String username,
                                                 String password) {
        Platform onPlatform = Drivers.getPlatformForUser(userPersona);
        LOGGER.info(System.out.printf(
                "LoginWithInvalidCredentials - Persona:'%s', Username: '%s', Password:'%s', " +
                "Platform: '%s'",
                SAMPLE_TEST_CONTEXT.ME, username, password, onPlatform.name()));
        new AppBL(userPersona, onPlatform).loginAgain(username, password);
    }

    @Then("I can echo {string} in the message box")
    public void iCanEchoInTheMessageBox(String message) {
        new EchoBL().echoMessage(message);
    }

    @Given("I start the app")
    public void iStartTheApp() {
        LOGGER.info(System.out.printf("iStartTheApp - Persona:'%s'", SAMPLE_TEST_CONTEXT.ME));
        Drivers.createDriverFor(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform(), context);
        new AppBL(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform());
    }

    @When("I set {string} in the clipboard")
    public void iSetInTheClipboard(String content) {
        new ClipboardBL().setContentInClipboard(content);
    }

    @Then("I can see the content saved in the clipboard")
    public void iCanSeeTheContentSavedInTheClipboard() {
        String contentExpectedInClipboard = context.getTestStateAsString("contentInClipboard");
        new ClipboardBL().verifyContentIsSaved(contentExpectedInClipboard);
    }

    @Given("I save {string} in the clipboard")
    public void iSaveInTheClipboard(String content) {
        LOGGER.info(System.out.printf("iStartTheApp - Persona:'%s'", SAMPLE_TEST_CONTEXT.ME));
        Drivers.createDriverFor(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform(), context);
        new ClipboardBL(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform()).saveContentInClipboard(content);
    }

    @Given("I am on file upload page")
    public void iAmOnFileUploadPage() {
        LOGGER.info(System.out.printf("iStartTheApp - Persona:'%s'", SAMPLE_TEST_CONTEXT.ME));
        Drivers.createDriverFor(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform(), context);
        new FileUploadBL(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform()).navigationToUploadScreen();
    }

    @When("I upload the {string} file")
    public void iUploadTheFile(String file) {
        new FileUploadBL().uploadFile(Runner.getTestDataAsMap(file));
    }

    @Then("File is uploaded successfully")
    public void fileIsUploadedSuccessfully() {
        new FileUploadBL().verifyFileUpload();
    }
}
