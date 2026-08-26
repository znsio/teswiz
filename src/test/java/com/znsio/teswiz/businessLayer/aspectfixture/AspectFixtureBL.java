package com.znsio.teswiz.businessLayer.aspectfixture;

import com.znsio.teswiz.exceptions.InvalidTestDataException;

public class AspectFixtureBL {
    public String doSomething() {
        return "done";
    }

    public String doSomethingThatFails() {
        throw new InvalidTestDataException("deliberate failure for aspect testing");
    }
}
