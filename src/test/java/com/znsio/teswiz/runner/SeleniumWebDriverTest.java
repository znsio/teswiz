package com.znsio.teswiz.runner;

class SeleniumWebDriverTest extends AbstractSharedWebDriverContractTest {
    @Override
    protected SharedWebDriverFixture createFixture() {
        SeleniumContractConditions.assumeEnabled();
        return new SeleniumWebDriverFixture();
    }
}
