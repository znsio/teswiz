package com.znsio.teswiz.testng.fixtures;

import org.testng.annotations.Test;

public class AlwaysPassingTestNgTest {
    @Test(groups = "fixture")
    public void alwaysPasses() {
        org.testng.Assert.assertTrue(true);
    }
}
