package com.znsio.teswiz.steps;
import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.znsio.teswiz.businessLayer.weatherAPI.WeatherAPIBL;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import kong.unirest.json.JSONObject;
import org.apache.log4j.Logger;

public class ApiTestSteps {
    private JSONObject jsonResponse;
    private static final Logger LOGGER = Logger.getLogger(ApiTestSteps.class.getName());
    private final TestExecutionContext context;
    public ApiTestSteps() {
        context = SessionContext.getTestExecutionContext(Thread.currentThread().getId());
        LOGGER.info("context: " + context.getTestName());
    }

    @Given("GET request is sent to the weather API with valid latitude {string} and longitude {string}")
    public void getRequestIsSentToTheWeatherAPIWithValidLatitudeAndLongitude(String latitude, String longitude) {
        jsonResponse = new WeatherAPIBL().getCurrentWeatherJSON(latitude, longitude);
    }
    @Then("we verify weather of that location in response")
    public void weVerifyWeatherOfThatLocationInResponse() {
        new WeatherAPIBL().verifyCurrentWeather(jsonResponse);
    }
}
