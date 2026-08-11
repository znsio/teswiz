package com.znsio.teswiz.businessLayer.cryptoAPI;

import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.services.RestAssuredService;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CryptoAPIBL {
    private static final Logger LOGGER = LogManager.getLogger(CryptoAPIBL.class.getName());
    private final Map<String, Object> testData = Runner.getTestDataAsMap("Crypto_API");
    private final String base_URL = testData.get("url").toString();

    public Response getDataUsingCryptoSymbol(String symbol) {
        LOGGER.info("Getting crypto currency data for last 24-Hrs");
        Response jsonResponse= RestAssuredService.getHttpResponseWithQueryParameter(base_URL, "symbol", symbol);
        assertThat(jsonResponse.getStatusCode()).as("API status code incorrect!")
                .isEqualTo(200);
        return jsonResponse;
    }

    public CryptoAPIBL verifypriceChange(Response jsonResponse, int maxPriceChange) {
        LOGGER.info("Verifying price change is less than "+maxPriceChange);
        double priceChange = Double.parseDouble(jsonResponse.jsonPath().getString("priceChange"));
        assertThat(priceChange)
                .as("Price change value more than expected maximum value!")
                .isLessThan(maxPriceChange);
        return this;
    }

    public CryptoAPIBL verifyPriceChangePercent(Response jsonResponse, int maxPriceChangePercent) {
        LOGGER.info("Verifying price change percent is less than "+maxPriceChangePercent);
        double priceChangePercent = Double.parseDouble(jsonResponse.jsonPath().getString("priceChangePercent"));
        assertThat(priceChangePercent)
                .as("Price change percent value more than expected maximum value!")
                .isLessThan(maxPriceChangePercent);
        return this;
    }
}
