package com.znsio.teswiz.testng;

import com.znsio.teswiz.businessLayer.cryptoAPI.CryptoAPIBL;
import io.restassured.response.Response;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CryptoApiPriceChangeDataDrivenTestNgTest {

    @DataProvider(name = "cryptoSymbolsAndMaxPriceChange", parallel = true)
    public Object[][] cryptoSymbolsAndMaxPriceChange() {
        return new Object[][]{
                {"LTCUSDT", 75},
                {"ETHUSDT", 200},
                {"BNBUSDT", 55},
                {"XRPUSDT", 80},
        };
    }

    @Test(dataProvider = "cryptoSymbolsAndMaxPriceChange", groups = {"api", "cryptoAPI", "priceChange"})
    public void validatePriceChangeInLast24Hrs(String symbol, int maxPriceChange) {
        CryptoAPIBL cryptoApi = new CryptoAPIBL();
        Response jsonResponse = cryptoApi.getDataUsingCryptoSymbol(symbol);
        cryptoApi.verifypriceChange(jsonResponse, maxPriceChange);
    }
}
