package com.znsio.teswiz.testng.fixtures;

import org.testng.annotations.Test;

public class AlwaysFailingButExcludableTestNgTest {
    @Test(groups = {"fixture", "excludeme"})
    public void alwaysFailsButShouldBeExcludable() {
        org.testng.Assert.fail("Should never actually run when 'excludeme' group is excluded");
    }
}
