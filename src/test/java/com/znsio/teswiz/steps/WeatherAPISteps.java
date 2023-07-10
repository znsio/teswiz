package com.znsio.teswiz.steps;

import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.znsio.teswiz.businessLayer.weatherAPI.WeatherAPIBL;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.json.JSONObject;
import org.apache.log4j.Logger;

public class WeatherAPISteps {
    private HttpResponse<JsonNode> jsonResponse;
    private JSONObject jsonObject;
    private static final Logger LOGGER = Logger.getLogger(WeatherAPISteps.class.getName());
    private final TestExecutionContext context;

    public WeatherAPISteps() {
        context = SessionContext.getTestExecutionContext(Thread.currentThread().getId());
        LOGGER.info("context: " + context.getTestName());
    }

    @Given("I send GET request with valid location coordinates")
    public void getRequestIsSentToTheWeatherAPIWithValidLatitudeAndLongitude() {
        jsonResponse = new WeatherAPIBL().getCurrentWeatherJSON();
    }

    @When("I query key {string} in response")
    public void iQueryKeyWeatherInResponse(String key) {
        jsonObject = new WeatherAPIBL().getValueForKey(jsonResponse, key);
    }

    @Then("I verify temperature of that location in range {int} and {int} C")
    public void weVerifyWeatherOfThatLocationInResponse(int lowerLimit, int upperLimit) {
        new WeatherAPIBL().verifyCurrentTemperature(jsonObject, lowerLimit, upperLimit);
    }

    @Then("I verify {string} is {int}")
    public void iVerifyIs(String key, int value) {
        new WeatherAPIBL().verifyKeyValueInResponse(jsonObject, key, value);
    }
}
