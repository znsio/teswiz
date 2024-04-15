## Getting Started, or how can you use teswiz?
It is very easy to use teswiz in your framework.
Follow these steps:
1. Setup the prerequisites mentioned below [https://github.com/znsio/teswiz#prerequisites]
1. Using your favorite IDE (I use IntelliJ Idea Community Edition), create a new Java-Gradle project
1. Copy build.gradle.sample file to your newly created project's root folder and rename it to build.gradle
2. Validate that build.gradle has the right version of teswiz
1. For `android app` automation
   * Get APP_PACKAGE_NAME - example: `aapt dump badging temp/sampleApps/theapp.apk | grep package`
   * Get APP_ACTIVITY - example: `aapt dump badging temp/ajio-8-3-4.apk | grep activity`
1. For `web` automation
   * Add `<>_BASE_URL` in environments.json - example: `THEAPP_BASE_URL=http://the-internet.herokuapp.com`
   * Update `BASE_URL` with the above in config.properties - example: `BASE_URL=THEAPP_BASE_URL`
1. For `electron` automation
   * Add `binary` path in browser_config.json - example: `"binary": "C:\\path\\to\\chrome.exe"`
   * Add `browserVersion` in browser_config.json - example: `"browserVersion": "latest"`
1. Create config.properties in some folder - ex: `./configs` and provide default values - refer to src/test/resources/com/znsio/e2e/features/android/configs/theapp_local_config.properties
1. Create capabilities.json in some folder - ex: `./caps` - refer to src/test/resources/com/znsio/e2e/features/android/caps/theapp_local_capabilities.json
1. Update `reportportal.properties` file present under 'src/test/resources' with the neccessary details for reporting
1. Validate the presence of **package.json ** at the root directory and execute **npm install**
1. **Implement the test**
   1. Define your scenario in a feature file (`src/test/resources/<package_name>/<feature_dir>`)
   2. Create your step definitions (`src/test/java/<package_name>/steps`)
   3. Implement your business layer classes/methods (`src/test/java/<package_name>/businessLayer`)
   3. Implement your screen classes/methods (`src/test/java/<package_name>/screen`) in the corresponding OS (Android/iOS) folders
   4. [Setup Applitools Visual AI Testing](RunningVisualTests-README.md) 
2. Update the **run** task in build.gradle with appropriate values for config.properties, pathToStepDef, pathToFeaturesDir, pathToLogProperties
1. Refer to the **[Running the tests](SampleTests-README.md)** section