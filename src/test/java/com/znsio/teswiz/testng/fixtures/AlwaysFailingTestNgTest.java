package com.znsio.teswiz.testng.fixtures;

import org.testng.annotations.Test;

public class AlwaysFailingTestNgTest {
    @Test(groups = "fixture")
    public void alwaysFails() {
        org.testng.Assert.fail("Deliberately failing fixture for TestNgRunnerTest");
    }
}
