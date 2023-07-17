package com.znsio.teswiz.steps;

import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.znsio.teswiz.businessLayer.restUser.RestUserBL;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import kong.unirest.json.JSONObject;
import org.apache.log4j.Logger;

public class RestUserSteps {
    private static final Logger LOGGER = Logger.getLogger(WeatherAPISteps.class.getName());
    private final TestExecutionContext context;
    private JSONObject jsonObject;

    public RestUserSteps() {
        context = SessionContext.getTestExecutionContext(Thread.currentThread().getId());
        LOGGER.info("context: " + context.getTestName());
    }

    @Given("I create a post")
    public void iCreateAPost() {
        jsonObject = new RestUserBL().createPost();
    }

    @Then("verify post created")
    public void verifyPostCreated() {
        new RestUserBL().verifyPostCreatedSuccessfully(jsonObject);
    }

    @When("I update title of the post")
    public void iUpdateTitleOfThePost() {
        jsonObject = new RestUserBL().updatePost();
    }

    @Then("verify title updated")
    public void verifyTitleUpdated() {
        new RestUserBL().verifyPostUpdatedSuccessfully(jsonObject);

    }

    @Then("I delete the post")
    public void iDeleteThePost() {
        new RestUserBL().deletePost();
    }
}
