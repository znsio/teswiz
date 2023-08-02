package com.znsio.teswiz.steps;

import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.znsio.teswiz.businessLayer.cryptoAPI.CryptoAPIBL;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import org.apache.log4j.Logger;

public class CryptoAPISteps {

    private HttpResponse<JsonNode> jsonResponse;
    private static final Logger LOGGER = Logger.getLogger(CryptoAPISteps.class.getName());
    private final TestExecutionContext context;

    public CryptoAPISteps() {
        context = SessionContext.getTestExecutionContext(Thread.currentThread().getId());
        LOGGER.info("context: " + context.getTestName());
    }

    @Given("I send GET request for crypto {string}")
    public void iSendGETRequestWithValidCryptoSymbol(String symbol) {
        jsonResponse = new CryptoAPIBL().getDataUsingCryptoSymbol(symbol);
    }

    @Then("price change should be less than {int}")
    public void iVerifyPriceChangeIsLessThan(int maxPriceChange) {
        new CryptoAPIBL().verifypriceChange(jsonResponse, maxPriceChange);
    }

    @Then("price change percentage should be less than {int}")
    public void iVerifyPriceChangePercentageIsLessThan(int maxPriceChangePercent) {
        new CryptoAPIBL().verifyPriceChangePercent(jsonResponse, maxPriceChangePercent);
    }
}
