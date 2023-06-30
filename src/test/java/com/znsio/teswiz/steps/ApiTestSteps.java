package com.znsio.teswiz.steps;

import com.znsio.teswiz.businessLayer.weatherAPI.WeatherAPIBL;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import kong.unirest.json.JSONObject;

public class ApiTestSteps {
    private JSONObject jsonResponse;

    @Given("GET request is sent to the weather API with valid latitude {string} and longitude {string}")
    public void getRequestIsSentToTheWeatherAPIWithValidLatitudeAndLongitude(String latitude, String longitude) {
        jsonResponse = new WeatherAPIBL().getCurrentWeatherJSON(latitude, longitude);
    }
    @Then("we verify weather of that location in response")
    public void weVerifyWeatherOfThatLocationInResponse() {
        new WeatherAPIBL().verifyCurrentWeather(jsonResponse);
    }
}
