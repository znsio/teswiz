package com.znsio.e2e.businessLayer;

import com.context.*;
import com.znsio.e2e.entities.*;
import com.znsio.e2e.runner.*;
import com.znsio.e2e.screen.*;
import org.apache.log4j.*;
import org.assertj.core.api.*;

public class CalculatorBL {
    private static final Logger LOGGER = Logger.getLogger(CalculatorBL.class.getName());
    private final TestExecutionContext context;
    private final SoftAssertions softly;
    private final String currentUserPersona;
    private final Platform currentPlatform;

    public CalculatorBL(String userPersona, Platform forPlatform) {
        long threadId = Thread.currentThread().getId();
        this.context = Runner.getTestExecutionContext(threadId);
        softly = Runner.getSoftAssertion(threadId);
        this.currentUserPersona = userPersona;
        this.currentPlatform = forPlatform;
        Runner.setCurrentDriverForUser(userPersona, forPlatform, context);
    }

    public CalculatorBL() {
        long threadId = Thread.currentThread().getId();
        this.context = Runner.getTestExecutionContext(threadId);
        softly = Runner.getSoftAssertion(threadId);
        this.currentUserPersona = SAMPLE_TEST_CONTEXT.ME;
        this.currentPlatform = Runner.platform;
    }

    public CalculatorBL startCalculator() {
        CalculatorScreen.get().handlePopupIfPresent();
        return this;
    }

    public CalculatorBL selectNumber(String number) {
        CalculatorScreen.get().selectNumber(number);
        return this;
    }

    public CalculatorBL pressOperation(String operation) {
        CalculatorScreen.get().pressOperation(operation);
        return this;
    }
}
