package com.znsio.teswiz.testng;

import com.znsio.teswiz.businessLayer.interactiveCalculatorCLI.InteractiveCalculatorCLIBL;
import org.testng.annotations.Test;

public class InteractiveCalculatorCliTestNgTest {
    @Test(groups = {"cli", "calculator"})
    public void addAndSubtractUsingInteractiveCli() {
        new InteractiveCalculatorCLIBL()
                .launchInteractiveCLIForCalculator()
                .add(24, 43)
                .subtract(43, 24)
                .seeInvalidMessages()
                .closeCalculatorCLI();
    }
}
