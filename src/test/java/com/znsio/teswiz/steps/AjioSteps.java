package com.znsio.teswiz.steps;

import com.context.SessionContext;
import com.context.TestExecutionContext;
import com.znsio.teswiz.businessLayer.ajio.HomeBL;
import com.znsio.teswiz.businessLayer.ajio.ProductBL;
import com.znsio.teswiz.businessLayer.ajio.SearchBL;
import com.znsio.teswiz.runner.Runner;
import com.znsio.teswiz.runner.Drivers;
import com.znsio.teswiz.entities.SAMPLE_TEST_CONTEXT;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.log4j.Logger;

public class AjioSteps {
    private static final Logger LOGGER = Logger.getLogger(AjioSteps.class.getName());
    private final TestExecutionContext context;

    public AjioSteps() {
        context = SessionContext.getTestExecutionContext(Thread.currentThread().getId());
        LOGGER.info("context: " + context.getTestName());
    }

    @Given("I search for products using {string}")
    public void iSearchForProductsUsing(String searchtype) {
        LOGGER.info(System.out.printf("iSearchForProductsUsing:'%s' - Persona:'%s'", searchtype,
                                      SAMPLE_TEST_CONTEXT.ME));
        Drivers.createDriverFor(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform(), context);
        new SearchBL().searchProduct(Runner.getTestDataAsMap(searchtype));
    }

    @When("I add the product to the cart")
    public void iAddTheProductToTheCart() {
        LOGGER.info(System.out.printf("iAddTheProductToTheCart:- Persona:'%s'",
                                      SAMPLE_TEST_CONTEXT.ME));
        new SearchBL().prepareCart();
    }

    @Then("I should see the product in the cart")
    public void iShouldSeeTheProductInTheCart() {
        LOGGER.info(System.out.printf("iShouldSeeTheProductInTheCart:- Persona:'%s'",
                                      SAMPLE_TEST_CONTEXT.ME));
        new SearchBL().verifyCart();
    }

    @Given("I open {string} from {string} section for {string}")
    public void iOpebShirtsSectionForMen(String product, String category, String gender) {
        Drivers.createDriverFor(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform(), context);
        new HomeBL().openProduct(product,category, gender);
    }


    @When("I select the first result")
    public void iSelectTheFirstResult() {
        new ProductBL().selectTheFirstResultFromList();
    }

    @Then("I should be able to perform flick and view images")
    public void iShouldBeAbleToPerformFlickAndViewImages() {
        new ProductBL().flickAndViewImages();
    }
}
