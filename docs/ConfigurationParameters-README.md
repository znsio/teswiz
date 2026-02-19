# Configuration parameters

The config.properties file has the following properties. Highlighting the defaults, and options for each here.

These can be overridden by providing the same either as environment variables or system properties.

    # ATD properties
    RUNNER=distribute -> ATD property. We will always use distributed
    FRAMEWORK=cucumber -> ATD property. We will always use cucumber
    RUNNER_LEVEL=methods -> ATD property. We will always use methods
    CAPS=./caps/capabilities.json -> Path to capabilties.json file

    # teswiz configuration properties. Can be overridden using environment variables or system properties

    APP_NAME=teswiz -> Name of your application
    APP_PACKAGE_NAME=io.cloudgrey.the_app -> android app package name
    APP_PATH=./temp/abc.apk -> path to android / windows app name
    APPLITOOLS_CONFIGURATION=./configs/applitools_config.json -> Applitools configuration 
    BASE_URL_FOR_WEB=BASE_URL -> Key name of the property in TEST_DATA_FILE for environment specific base url
    BRANCH_NAME -> Key name of environment variable which should be used to get the current Branch name. 
                   IF this is not specified, then teswiz will try to get the BRANCH_NAME using this command: `git rev-parse --abbrev-ref HEAD`
    BROWSER=chrome -> Which browser to use for Web execution? Supported: chrome || firefox
                      Browsers should to be installed. Corresponding WebDriver for the browser will be downloaded automatically
    BUILD_ID=BUILDID -> The key name of the environment variable that has the corresponding build id of the test execution
    CLEANUP_DEVICE_BEFORE_STARTING_EXECUTION=true -> Uninstall app from local Android devices before starting test execution
    CLOUD_KEY=<auth / api key> for pCloudy / Headspin
    CLOUD_USERNAME=<username / email> for pCloudy -> Not required for Headspin
    CLOUD_UPLOAD_APP=false -> Upload the app to pCloudy / headspin before running the tests
    CLOUD_USE_PROXY=true -> If we need proxy for connecting to the cloud device farm using the curl command. Default: false
    CLOUD_USE_LOCAL_TESTING=false -> If we want to enable local testing (currently only in BrowserStack) -  
    ENVIRONMENT_CONFIG_FILE=./src/test/resources/environments.json -> Environment specific configuration file
    IS_FAILING_TEST=false -> Do not run failing tests. Refer to [Hard Gate](HardGate.md) for more information
    IS_VISUAL=false -> Should enable Applitools Visual Testing? If yes, set to true
    FAIL_TEST_ON_VISUAL_DIFFERENCE=true -> 
        If visual testing is enabled, and this is set to true, then the test will fail if there are any visual differences found
        If this is set to false, then a message will be logged about the visual differences, and the test will not fail for this reason 
    HEADLESS=false -> If set, run web/electron app tests in HEADLESS mode (overrides the headless value in browser_config.json)
    LOG_DIR=target -> Where should logs be created?
    LOG_PROPERTIES_FILE=./src/test/resources/log4j2.properties -> log4j configuration file
    MAX_NUMBER_OF_APPIUM_DRIVERS -> The max number of drivers on cloud to create for multiuser android tests, default value is 5
    MAX_NUMBER_OF_WEB_DRIVERS -> The max number of web drivers on cloud to create for multiuser web tests, default value is 5
    PLATFORM=android -> Run tests against? Supported: android | iOS | windows | web | api
    PARALLEL=1 -> How many tests should be run in parallel?
    PROXY_KEY=HTTP_PROXY -> If proxy should be set, what is the environment variable specifying the proxy?
    PROXY_URL=<proxy_url> -> What is the proxy url to be used if PROXY_KEY is set
    REMOTE_WEBDRIVER_GRID_PORT=<environment variable name which holds the port to be used for RemoteWebDriver>
    REMOTE_WEBDRIVER_GRID_HOST_NAME=<environment variable name which holds the host name/ip for RemoteWebDriver>
    REPORT_PORTAL_FILE=./src/test/resources/reportportal.properties -> ReportPortal.io configuration
    RP_DESCRIPTION=<description of the test execution to be shown in reportportal's launch description. Default: End-2-End scenarios>
    RUN_IN_CI=false -> Are tests running in CI?
    SET_HARD_GATE=true -> Enables Hard Gate for test execution. See [Hard Gate](HardGate.md) for more information 
    TARGET_ENVIRONMENT=prod -> Which environment are the tests running against? Should map to envrionments specified in ENVIRONMENT_CONFIG_FILE
    TEST_DATA_FILE=./src/test/resources/testData.json -> Environment specific static test data
    BROWSER_CONFIG_FILE=./src/test/resources/com/znsio/teswiz/features/configs/browser_config.json -> json containing browser configurations

# Overriding the BASE_URL_FOR_WEB and BROWSER_CONFIG_FILE for Web execution
The BASE_URL_FOR_WEB and BROWSER_CONFIG_FILE once set, cannot be changed for the test execution.
However, there may be reasons when you need to use a different BASE_URL_FOR_WEB or a different BROWSER_CONFIG_FILE for specific tests.

To allow for that, **before the driver is created** for a test, you can add the following data to the TestExecutionContext.  

Example:

    @When("I login with invalid credentials - {string}, {string}")
    public void iLoginWithInvalidCredentials(String username, String password) {
        LOGGER.info(System.out.printf(
                "iLoginWithInvalidCredentials - Persona:'%s', Username: '%s', Password:'%s', " +
                "Platform: '%s'",
                SAMPLE_TEST_CONTEXT.ME, username, password, Runner.getPlatform()));
        context.addTestState(TEST_CONTEXT.UPDATED_BROWSER_CONFIG_FILE_FOR_THIS_TEST, "./configs/browser_headless_config.json");
        context.addTestState(TEST_CONTEXT.UPDATED_BASE_URL_FOR_WEB, "BASE_URL");
        Drivers.createDriverFor(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform(), context);
        context.addTestState(SAMPLE_TEST_CONTEXT.ME, username);
        new AppBL(SAMPLE_TEST_CONTEXT.ME, Runner.getPlatform()).provideInvalidDetailsForSignup(username,
                                                                                               password);
    }