package com.znsio.teswiz.steps;

import com.znsio.teswiz.businessLayer.failing.FailingBL;
import io.cucumber.java.en.Given;

public class FailingSteps {
    @Given("I softly fail {string}")
    public void iSoftlyFail(String randomString) {
        new FailingBL().softlyFail(randomString);
    }

    @Given("I fail hard {string}")
    public void iFailHard(String randomString) {
        new FailingBL().hardFail(randomString);
    }

    @Given("I pass {string}")
    public void iPass(String randomString) {
        new FailingBL().pass(randomString);
    }
}
