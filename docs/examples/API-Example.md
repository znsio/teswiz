# API Test Implementation Example

This guide provides a concrete example of implementing an API-level test in Teswiz using the integrated `RestAssuredService`.

---

## 1. Feature File (`weather-api.feature`)
```gherkin
@api
Feature: Weather Forecast Service

  Scenario: Get weather forecast for location coordinates
    Given I send GET request with location coordinates
    Then temperature of that location should be in range 10 and 40 C
```

---

## 2. Step Definition (`WeatherAPISteps.java`)
```java
package com.znsio.teswiz.steps;

import com.znsio.teswiz.businessLayer.weatherAPI.WeatherAPIBL;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.json.JSONObject;

public class WeatherAPISteps {
    private JSONObject jsonObject;

    @Given("I send GET request with location coordinates")
    public void sendGetRequest() {
        jsonObject = new WeatherAPIBL().getCurrentWeatherJSON();
    }

    @Then("temperature of that location should be in range {int} and {int} C")
    public void verifyTemperature(int lowerLimit, int upperLimit) {
        new WeatherAPIBL().verifyCurrentTemperature(jsonObject, lowerLimit, upperLimit);
    }
}
```

---

## 3. Business Layer (`WeatherAPIBL.java`)
```java
package com.znsio.teswiz.businessLayer.weatherAPI;

import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.services.RestAssuredService;
import io.restassured.response.Response;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

public class WeatherAPIBL {
    private final Map<String, Object> testData = Runner.getTestDataAsMap("Weather_API");
    private final String baseUrl = testData.get("url").toString();

    public JSONObject getCurrentWeatherJSON() {
        HashMap<String, Object> queryParams = new HashMap<>(){{
            put("latitude", testData.get("latitude").toString());
            put("longitude", testData.get("longitude").toString());
            put("current_weather", true);
        }};
        
        // Execute HTTP GET using the built-in RestAssuredService wrapper
        Response jsonResponse = RestAssuredService.getHttpResponseWithQueryMap(baseUrl, queryParams);
        
        assertThat(jsonResponse.getStatusCode())
                .as("Failed weather check API status code")
                .isEqualTo(200);
                
        return new JSONObject(jsonResponse.getBody().asString()).getJSONObject("current_weather");
    }

    public void verifyCurrentTemperature(JSONObject response, int minTemp, int maxTemp) {
        double currentTemp = response.getDouble("temperature");
        assertThat(currentTemp)
                .as("Current temperature value check")
                .isBetween((double) minTemp, (double) maxTemp);
    }
}
```
