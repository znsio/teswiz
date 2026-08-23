package com.znsio.teswiz.testng;

import com.znsio.teswiz.businessLayer.interactiveCalculatorCLI.InteractiveCalculatorCLIBL;
import org.testng.annotations.Test;

public class InteractiveCalculatorCliTestNgTest {
    @Test(groups = {"cli", "calculator"})
    public void addAndSubtractUsingInteractiveCli() {
        InteractiveCalculatorCLIBL calculator = new InteractiveCalculatorCLIBL();
        calculator.launchInteractiveCLIForCalculator();
        calculator.add(24, 43);
        calculator.subtract(43, 24);
        calculator.seeInvalidMessages();
        calculator.closeCalculatorCLI();
    }
}
