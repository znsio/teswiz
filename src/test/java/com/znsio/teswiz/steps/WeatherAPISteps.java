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

    @Given("I send GET request with valid location coordinates and invalid forecast days")
    public void iSendGETRequestWithValidLocationCoordinatesAndForecastDays() {
        jsonObject = new WeatherAPIBL().getForecastForInvalidDays();
    }

    @Then("I verify error reason {string}")
    public void iVerifyErrorReason(String errorMessage) {
        new WeatherAPIBL().verifyErrorForInvalidForecastDays(jsonObject, errorMessage);
    }

}
