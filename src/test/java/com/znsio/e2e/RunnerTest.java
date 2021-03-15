package com.znsio.e2e;

import com.znsio.e2e.entities.Platform;
import com.znsio.e2e.runner.Runner;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class RunnerTest {
    private String stepDefDir = "com/znsio/e2e/steps";
    private String logDir = "target";

    @Test
    void mainLocalDefault () {
        String featuresDir = "./src/test/resources";
        System.setProperty("TARGET_ENVIRONMENT", "prod");
        System.setProperty("environmentConfig", "src/test/resources/environments.json");
        System.setProperty("testDataFile", "src/test/resources/testData.json");
        System.setProperty("IS_VISUAL", "true");
        Runner runner = new Runner("./src/test/resources/com/znsio/e2e/features/android/configs/config.properties", stepDefDir, featuresDir, logDir);
        String baseUrl = Runner.getFromEnvironmentConfiguration("BASE_URL");
        assertThat(baseUrl)
                .as("environment config is incorrect")
                .isEqualTo("https://github.com/znsio/unified-e2e/");

        String actualTestData = Runner.getTestData("GMAIL_USER_1_EMAIL");
        assertThat(actualTestData)
                .as("environment config is incorrect")
                .isEqualTo("mytestemail@gmail.com");
    }

    @Test
    void mainLocalAndroid () {
        String featuresDir = "./src/test/resources/com/znsio/e2e/features/android";
        System.setProperty("Platform", Platform.android.name());
        Runner runner = new Runner("./src/test/resources/com/znsio/e2e/features/android/configs/config.properties", stepDefDir, featuresDir, logDir);
//        runner.printProcessedConfiguration();
    }

    @Test
    void mainLocalWeb () {
        String featuresDir = "./src/test/resources/com/znsio/e2e/features/web";
        System.setProperty("Platform", Platform.web.name());
        Runner runner = new Runner("./src/test/resources/com/znsio/e2e/features/android/configs/config.properties", stepDefDir, featuresDir, logDir);
//        runner.printProcessedConfiguration();
    }

    @Test
    void mainAndroidCloud () {
        String featuresDir = "./src/test/resources/com/znsio/e2e/features/android";
        System.setProperty("runOnCloud", "true");
        Runner runner = new Runner("./src/test/resources/com/znsio/e2e/features/android/configs/mobilab_config.properties", stepDefDir, featuresDir, logDir);
//        runner.printProcessedConfiguration();
    }
}