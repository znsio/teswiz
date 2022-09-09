# Running the sample tests

### Android tests
  Example:

![ClearIntent-annotated.png](ClearIntent-annotated.png)

    CONFIG=./src/test/resources/configs/jiomeet_config.properties TAG="@jiomeet and @single-user" PLATFORM=android ./gradlew run 

  With Visual Testing enabled:

    CONFIG=./src/test/resources/configs/jiomeet_config.properties TAG="@jiomeet and @single-user" PLATFORM=android IS_VISUAL=true ./gradlew run

### Web tests
Example:

![ClearIntent-annotated.png](ClearIntent-annotated.png)

    CONFIG=./src/test/resources/configs/jiomeet_config.properties TAG="@jiomeet and @single-user" PLATFORM=web ./gradlew run 

  With Visual Testing enabled:

    CONFIG=./src/test/resources/configs/jiomeet_config.properties TAG="@jiomeet and @single-user" PLATFORM=web IS_VISUAL=true ./gradlew run

### Multiuser Android tests
  Example:

![Multiuser-android-annotated.png](Multiuser-android-annotated.png)

    CONFIG=./src/test/resources/configs/jiomeet_config.properties TAG="@jiomeet and @multiuser-android and @single-app" ./gradlew run

  With Visual Testing enabled:

    CONFIG=./src/test/resources/configs/jiomeet_config.properties TAG="@jiomeet and @multiuser-android and @single-app" IS_VISUAL=true ./gradlew run

### Multiuser Android-web tests
  Example:

![Multiuser-android-web-annotated.png](Multiuser-android-web-annotated.png)

    CONFIG=./src/test/resources/configs/jiomeet_config.properties TAG="@jiomeet and @multiuser-android-web and @single-app" ./gradlew run

  With Visual Testing enabled:

    CONFIG=./src/test/resources/configs/jiomeet_config.properties TAG="@jiomeet and @multiuser-android-web and @single-app" IS_VISUAL=true ./gradlew run

### Multiuser-Multiapp Android-web tests
Example:

![Multiapp-Multiuser-android-web-annotated.png](Multiapp-Multiuser-android-web-annotated.png)

    CONFIG=./src/test/resources/configs/jiomeet_config.properties TAG="@jiomeet and @multiuser-android-web and @multi-app" ./gradlew run

With Visual Testing enabled:

    CONFIG=./src/test/resources/configs/jiomeet_config.properties TAG="@jiomeet and @multiuser-android-web and @multi-app" IS_VISUAL=true ./gradlew run

### Multiuser-Multiapp Android tests
    CONFIG=./src/test/resources/configs/jiomeet_config.properties TAG="@jiomeet and @multiuser-android and @multi-app" ./gradlew run

  With Visual Testing enabled:

    CONFIG=./src/test/resources/configs/jiomeet_config.properties TAG="@jiomeet and @multiuser-android and @multi-app" IS_VISUAL=true ./gradlew run


# Visual Test Automation
To enable Visual test automation using Applitools Visual AI, follow the steps below:
* In build.gradle, provide your APPLITOOLS_API_KEY:

    > environment "APPLITOOLS_API_KEY", System.getenv("teswiz_APPLITOOLS_API_KEY")

* Enable visual validation by setting `IS_VISUAL=true` in either of:
  * the config file, or
  * from the command line - ex: `CONFIG=./src/test/resources/configs/jiomeet_config.properties IS_VISUAL=true ./gradlew run`, or
  * as an environment variable

# Configuration options
Test execution using teswiz is highly configurable. This enables you to control what type of tests you want to execute, and where (environment, local/cloud), etc. 

Refer here for all the configuration options: https://github.com/znsio/teswiz#configuration-parameters

# Rich Reports using cucumber-reporting
teswiz creates rich reports for offline consuption using cucumber-reporting (https://github.com/damianszczepanik/cucumber-reporting)

These reports will be available in the following directory:

`LOG_DIR/mm-dd-yyyy/hh-mm-ss/reports/richReports/cucumber-html-reports/overview-features.html`

Example:

`~/teswiz/target/08-17-2022/17-36-02/reports/richReports/cucumber-html-reports/overview-features.html`

## Feature coverage
cucumber-reports has the ability to show tag statistics. This is very helpful to understand feature coverage from your automated tests.

Read more about this feature here: https://github.com/damianszczepanik/cucumber-reporting/blob/master/.README/tag-overview.png

You can find that report here:

`~/teswiz/target/08-17-2022/17-36-02/reports/richReports/cucumber-html-reports/overview-tags.html`

To exclude any tag(s) to be added in the rich reports, add the following line in your RunTestCukes.java

`System.setProperty(TEST_CONTEXT.TAGS_TO_EXCLUDE_FROM_CUCUMBER_REPORT, "@web,@android");`