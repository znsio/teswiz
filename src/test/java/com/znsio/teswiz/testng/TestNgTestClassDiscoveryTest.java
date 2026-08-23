package com.znsio.teswiz.testng;

import com.znsio.teswiz.testng.fixtures.AlwaysFailingTestNgTest;
import com.znsio.teswiz.testng.fixtures.AlwaysPassingTestNgTest;
import com.znsio.teswiz.testng.fixtures.ThreadRecordingTestNgTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TestNgTestClassDiscoveryTest {

    @Test
    void shouldDiscoverAllTestNgAnnotatedClassesInThePackage() {
        List<String> discovered = TestNgTestClassDiscovery.discoverTestClassesIn(
                "com.znsio.teswiz.testng.fixtures");

        assertThat(discovered).containsExactlyInAnyOrder(
                AlwaysPassingTestNgTest.class.getName(),
                AlwaysFailingTestNgTest.class.getName(),
                ThreadRecordingTestNgTest.class.getName());
    }

    @Test
    void shouldReturnEmptyListWhenPackageHasNoTestNgAnnotatedClasses() {
        List<String> discovered = TestNgTestClassDiscovery.discoverTestClassesIn(
                "com.znsio.teswiz.entities");

        assertThat(discovered).isEmpty();
    }
}
