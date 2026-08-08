package com.znsio.teswiz.runner;

class PlaywrightWebDriverTest extends AbstractSharedWebDriverContractTest {
    @Override
    protected SharedWebDriverFixture createFixture() {
        return new PlaywrightWebDriverFixture();
    }
}
